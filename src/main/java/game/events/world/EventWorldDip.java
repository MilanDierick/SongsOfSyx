package game.events.world;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.*;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.time.TIME;
import init.D;
import init.race.KingMessage;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.dic.DicGeo;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import view.ui.diplomacy.*;
import view.ui.message.MessageText;
import world.army.AD;
import world.regions.data.RD;

public class EventWorldDip extends EventResource{
	
	private static CharSequence ¤¤TradeCancelled = "¤Trade Cancelled.";
	private static CharSequence ¤¤TradeCancelledD = "¤Since the faction of {0} is no longer reachable to us, either directly or through another trading partner. Trade is impossible and our trade agreement is forfeit.";
	private static CharSequence ¤¤TradeCancelledA = "¤This faction has resigned their trade agreement with you.";
	
	private static CharSequence ¤¤Gift = "Gift";
	private static CharSequence ¤¤Request = "Request";
	private static CharSequence ¤¤Demand = "Demand";
	private static CharSequence ¤¤Welcome = "Welcome";
	
	private static CharSequence ¤¤noTrade = "Stop trade with {0} upon the demand of {1}";
	private static CharSequence ¤¤gang = "Join {0} in a war against {1}";


	static {
		D.ts(EventWorldDip.class);
	}
	
	private final Bitsmap1D tradeI = new Bitsmap1D(0, 2, FACTIONS.MAX);
	private final Bitmap1D welcomeI = new Bitmap1D(FACTIONS.MAX, false);
	private final int[] fri = new int[FACTIONS.MAX];
	{
		for (int i = 0; i < fri.length; i++) {
			fri[i] = i;
		}
		for (int i = 0; i < fri.length; i++) {
			int k = RND.rInt(fri.length);
			int o = fri[i];
			fri[i] = fri[k];
			fri[k] = o;
		}
	}
	
	private final LinkedList<Inter> inters = new LinkedList<>();
	
	EventWorldDip(){
		IDebugPanel.add("Test diplomacy events", new ACTION() {
			
			@Override
			public void exe() {
				FactionNPC f = FACTIONS.pRel().neighs().rnd();
				if (f == null)
					return;
				
				for (Inter i : inters)
					i.execute(f, true);
			}
		});
	}
	
	@Override
	protected void save(FilePutter file) {
		welcomeI.save(file);
		uper.save(file);
		tradeI.save(file);
		file.is(fri);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		welcomeI.load(file);
		uper.load(file);
		tradeI.load(file);
		file.is(fri);
	}

	@Override
	protected void clear() {
		welcomeI.clear();
		uper.clear();
		tradeI.clear();
	}
	
	private IUpdater uper = new IUpdater(FACTIONS.MAX, TIME.secondsPerDay*4) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			Faction f = FACTIONS.getByIndex(fri[i]);
			if (f.isActive() && f instanceof FactionNPC) {
				FactionNPC ff = (FactionNPC) f;
				if (!ff.request.has()) {
					if (tradeI.get(f.index()) > 0)
						tradeI.inc(f.index(), -1);
					for (Inter ii : inters) {
						if (ii.execute(ff, false))
							return;
					}
					
				}
			}
		}
	};

	@Override
	protected void update(double ds) {
		uper.update(ds);
	}



	public final Inter PEACE = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			if (!debugForce && !FACTIONS.DIP().war.is(f))
				return false;
			KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
			Deal d = Deal.TMP();
			d.setFactionAndClear(f);
			d.peace.set(true);
			CharSequence desc = d.valueCredits() < 0 ? m.tPeaceA.get(f, null) : m.tPeaceB.get(f, null);
			DealDrawfter.draft(d, d.valueCredits());
			new UIDipMessDeal(DicGeo.¤¤peace, desc, d, 0.1, -0.25).send();
			return true;
		}
	};
	
	public final Inter WELCOME = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			if (!RD.DIST().factionBordersPlayer(f))
				return false;
			if (welcomeI.get(f.index()))
				return false;
			
			if (debugForce || STATS.POP().POP.data(HCLASS.CITIZEN).get(null) > 40) {
				if (ROpinions.current(f) > 0) {
					KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
					Deal d = Deal.TMP();
					d.setFactionAndClear(f);
					DealDrawfter.draft(d, DealValues.netValue(d, FACTIONS.player(), f)*0.05);
					if (d.hasDeal()) {
						welcomeI.set(f.index(), true);
						new UIDipMessDeal(¤¤Welcome, m.tGreeting.get(f, null), d, -0.25, 0.25).send();
					}
				}else {
					welcomeI.set(f.index(), true);
				}
			}
			
			return true;
		}
	};
	
	public final Inter TRADE = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			if (FACTIONS.DIP().trades(f)) {
				if (!FACTIONS.pRel().tradersPotential().contains(f)) {
					FACTIONS.DIP().trade(f, FACTIONS.player(), false);
					new MessageText(¤¤TradeCancelled, Str.TMP.clear().add(¤¤TradeCancelledD).insert(0, f.name)).send();

					return true;
				}else if (ROpinions.current(f) < 0) {
					KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
					FACTIONS.DIP().trade(f, FACTIONS.player(), false);
					new UIDipMess(¤¤TradeCancelled, m.tTradeCancel.get(f, null), ¤¤TradeCancelledA, f).send();
					
					return true;
				}
				return false;
			}
			
			if (!RD.DIST().factionBordersPlayer(f))
				return false;
			if (!debugForce && !SETT.ROOMS().IMPORT.reqs.passes(FACTIONS.player()))
				return false;
			if (!debugForce && !RND.oneIn((1+FACTIONS.pRel().neighs().size()))) {
				tradeI.set(f.index(), 3);
				return false;
			}
			if (tradeI.get(f.index()) > 0 ||  ROpinions.current(f) < 0.25) {
				return false;
			}
			Deal d = Deal.TMP();
			d.setFactionAndClear(f);
			d.trade.set(true);
			double v = -d.valueCredits();
			double b = v*0.5+(0.5+RND.rFloat());
			DealDrawfter.draft(d, b);
			if (-d.valueCredits() <= (b)*1.1) {
				KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
				new UIDipMessDeal(DicGeo.¤¤Trade, m.tTrade.get(f, null), d, 0, -0.25).send();
				tradeI.set(f.index(), 3);
				return true;
			}
			
			return false;
		}
	};
	
	public final Inter GIFT = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			
			if (!RD.DIST().factionBordersPlayer(f))
				return false;
			
			if (!debugForce && !RND.oneIn(16*(1+FACTIONS.pRel().neighs().size())))
				return false;
			double rep = ROpinions.current(f);
			KingMessage m = f.court().king().roy().induvidual.race().kingMessage();
			if (rep < 0) {
				if (FACTIONS.DIP().war.is(f))
					return false;
				rep = -rep;
				if (RND.rBoolean()) {
					Deal d = Deal.TMP();
					d.setFactionAndClear(f);
					DealDrawfter.draft(d, -DealValues.netValue(d, FACTIONS.player(), f)*0.25);
					if (d.hasDeal()) {
						new UIDipMessDeal(¤¤Demand, m.tDemand.get(f, null), d, 4, -0.5).send();
					}
					return true;
				}
				return true;
			}

			boolean gift = rep > RND.rFloat()*8;
			
			if (gift) {
				Deal d = Deal.TMP();
				d.setFactionAndClear(f);
				DealDrawfter.draft(d, DealValues.netValue(d, FACTIONS.player(), f)*0.1);
				if (d.hasDeal()) {
					new UIDipMessDeal(¤¤Gift, m.tGift.get(f, null), d, -0.5, 1).send();
					return true;
				}
			}else {
				Deal d = Deal.TMP();
				d.setFactionAndClear(f);
				DealDrawfter.draft(d, -DealValues.netValue(d, FACTIONS.player(), f)*0.1);
				if (d.hasDeal()) {
					new UIDipMessDeal(¤¤Request, m.tRequest.get(f, null), d, 1, -1).send();
					return true;
				}
			}
			
			return false;
		}
	};
	
	public final Inter STOP_TRADE = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			
			if (!RD.DIST().factionBordersPlayer(f))
				return false;
			if (!FACTIONS.DIP().trades(f))
				return false;
			
			if (!debugForce && !RND.oneIn(48*(1+FACTIONS.pRel().neighs().size())))
				return false;
			
			for (FactionNPC o : FACTIONS.pRel().traders()) {
				if (o != f) {
					new MessNoTrade(f, o).send();
					return true;
				}
			}
			
			return false;
		}
	};
	
	public final Inter GANBANG = new Inter(inters) {
		
		@Override
		public boolean execute(FactionNPC f, boolean debugForce) {
			
			if (!RD.DIST().factionBordersPlayer(f))
				return false;
			
			if (!debugForce && !RND.oneIn(16*(1+FACTIONS.pRel().neighs().size())))
				return false;
			
			if (AD.men(null).total().get(FACTIONS.player()) <= 0 || RD.MILITARY().garrison.get(FACTIONS.player().capitolRegion()) <= 0)
				return false;
			
			if (FACTIONS.DIP().war.is(f))
				return false;
			
			for (FactionNPC o : FACTIONS.pRel().neighs()) {
				if (o != f && !FACTIONS.DIP().war.is(o)) {
					new MessWar(f, o).send();
					return true;
				}
			}
			return false;
		}
	};
	
	private static class MessNoTrade extends UIDipMessAction {

		private static final long serialVersionUID = 1L;

		public MessNoTrade(FactionNPC f, FactionNPC o) {
			super(¤¤Request, f.court().king().roy().induvidual.race().kingMessage().tTradeStop.get(f, o), Str.TMP.clear().add(¤¤noTrade).insert(0, o.name).insert(1, f.name), f, o, 0.25, -0.25);
		}

		@Override
		protected void accept(FactionNPC f, FactionNPC o) {
			FACTIONS.DIP().trade(FACTIONS.player(), o, false);
		}

		@Override
		protected boolean valid(FactionNPC f, FactionNPC o) {
			return FACTIONS.DIP().trades(o);
		}
		
	}
	
	private static class MessWar extends UIDipMessAction {

		private static final long serialVersionUID = 1L;

		public MessWar(FactionNPC f, FactionNPC o) {
			super(¤¤Request, f.court().king().roy().induvidual.race().kingMessage().tGangbang.get(f, o), Str.TMP.clear().add(¤¤gang).insert(0, o.name).insert(1, f.name), f, o, 1.0, -0.25);
		}

		@Override
		protected void accept(FactionNPC f, FactionNPC o) {
			FACTIONS.DIP().war.set(f, o, true);
			FACTIONS.DIP().war.set(FACTIONS.player(), o, true);
		}
		
		@Override
		protected boolean valid(FactionNPC f, FactionNPC o) {
			return !FACTIONS.DIP().war.is(f) && FACTIONS.DIP().war.is(o);
		}
		
	}
	
	public static abstract class Inter {
		
		Inter(LISTE<Inter> all){
			all.add(this);
		}
		
		public abstract boolean execute(FactionNPC f, boolean debugForce);
		
	}
	


	
}
