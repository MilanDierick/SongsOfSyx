package game.faction.npc.ruler;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DWar;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.D;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.entity.ENTETIES;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.dic.*;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public class ROpinions {

	private static ROpinions self;
	private final ArrayListGrower<OpinionFactorE> allD = new ArrayListGrower<>();
	public final Boostable boostable = BOOSTING.push("OPINION", 0, DicGeo.¤¤Opinion, DicGeo.¤¤OpinionD, UI.icons().s.soso, BoostableCat.WORLD);

	
	{
		self = this;
		
		D.gInit(ROpinions.class);
	}
	
	public static void init() {
		new ROpinions();
	}
	
	private final OpinionFactorE warDeclare = new OpinionFactorE(allD, D.g("WarD", "Declarations of war"), UI.icons().s.sword, 0, -2) {
		
		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			value -= ds/(TIME.secondsPerDay*16.0);
			value = CLAMP.d(value, 0, 1);
			return value;
		}

		@Override
		public double getP(double value, Royalty t) {
			return CLAMP.d(value, 0, 1);
		}

	};
	
	private final OpinionFactorE favours = new OpinionFactorE(allD, DicMisc.¤¤Favours, UI.icons().s.crown, -10, 10) {

		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			double d = ds/(TIME.secondsPerDay*16*64.0);
			if (value < 0) {
				value += d;
				if (value > 0)
					value = 0;
			}
			else if (value > 0){
				value -= d;
				if (value < 0)
					value = 0;
			}
			value = CLAMP.d(value, -1, 3);
			return value;
		}
		@Override
		public double getP(double value, Royalty t) {
			return 0.5 + value*0.5;
		}
	};
	
	private final OpinionFactorE peace = new OpinionFactorE(allD, DicArmy.¤¤Peace, UI.icons().s.sprout, 0, 10) {
		
		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			value -= ds/(TIME.secondsPerDay*128.0);
			value = CLAMP.d(value, 0, 100);
			return value;
		}

		@Override
		public double getP(double value, Royalty t) {
			return value;
		}

	};
	
	private final OpinionFactorE diplomacy = new OpinionFactorE(allD, DicMisc.¤¤Diplomacy, UI.icons().s.flags, -10, 10) {
		
		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			double d = ds/(TIME.secondsPerDay*16*64.0);
			if (value < 0) {
				value += d;
				if (value > 0)
					value = 0;
			}
			else if (value > 0){
				value -= d;
				if (value < 0)
					value = 0;
			}
			value = CLAMP.d(value, -1, 3);
			return value;
		}

		@Override
		public double getP(double value, Royalty t) {
			return 0.5+0.5*value;
		}

	};
	
	private final OpinionFactorE flattery = new OpinionFactorE(allD, D.g("Flattery"), UI.icons().s.heart,  0, 4) {

		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			double d = ds/(TIME.secondsPerDay*16.0);
			value -= d;
			value = CLAMP.d(value, 0, 1);
			return value;
		}

		@Override
		public double getP(double value, Royalty t) {
			return value;
		}
		
	};
	
	private final OpinionFactorE assasination = new OpinionFactorE(allD, D.g("Assassination"), UI.icons().s.alert,  0, -10) {

		@Override
		protected double pUpdate(Royalty roy, double value, double ds) {
			double d = ds/(TIME.secondsPerDay*16.0);
			value -= d;
			value = CLAMP.d(value, 0, 1);
			return value;
		}

		@Override
		public double getP(double value, Royalty t) {
			return value;
		}
		
	};
	
	private final OpinionFactorE trade;
	private final OpinionFactorE liberation;
	
	private ROpinions() {
		
		
		
		new BoosterImp(new BSourceInfo(D.g("Kinship"), UI.icons().s.human), 0.5, 1.5, true) {

			
			@Override
			public double vGet(Royalty roy) {
				

				
				double d = -1 + 2.0*roy.induvidual.race().pref().race(FACTIONS.player().race());
				d *=(1-roy.trait(RTraits.get().tolerance));
				d = (d+1)/2;
				return CLAMP.d(d, 0, 1);
			}

			@Override
			public double vGet(Faction f) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> o) {
				return o == Royalty.class;
			};
			
		}.add(boostable);
		
		BoostSpec threat = new BoosterImp(new BSourceInfo(D.g("Threat"), UI.icons().s.muster),  1, -10, false) {

			@Override
			public double vGet(Royalty roy) {
				double res = 0;
				res += 0.7*(FACTIONS.player().realm().all().size()-1)/32.0;
				res += 0.3*(STATS.POP().POP.data().get(null)) / ENTETIES.MAX;
				return res;
			}
			
			@Override
			public double vGet(Faction f) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> o) {
				return o == Royalty.class;
			};
			
		}.add(boostable);
		
		BoostSpec wealth = new BoosterImp(new BSourceInfo(D.g("Wealth"), UI.icons().s.money),  0, -5, false) {

			@Override
			public double vGet(Royalty roy) {
				double res = FACTIONS.player().credits().credits()/(140000.0*RESOURCES.ALL().size());
				return Math.min(res, 1);
			}
			
			@Override
			public double vGet(Faction f) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> o) {
				return o == Royalty.class;
			};
			
		}.add(boostable);
		
		new BoosterImp(new BSourceInfo(D.g("Cruelty"), UI.icons().s.death),  0, -2, false) {

			@Override
			public double vGet(Royalty roy) {
				RDRace r = RD.RACE(roy.induvidual.race());
				double d = r.massacre.realm.getD(FACTIONS.player()) + r.exile.realm.getD(FACTIONS.player())*0.5 + r.sanction.realm.getD(FACTIONS.player())*0.25;
				d *= roy.trait(RTraits.get().mercy);
				return d;
			}
			
			@Override
			public double vGet(Faction f) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> o) {
				return o == Royalty.class;
			};
			
		}.add(boostable);
		
		trade = new OpinionFactorE(allD, DicGeo.¤¤TradePartner, UI.icons().s.wheel,  0, 2, false) {

			@Override
			protected double pUpdate(Royalty roy, double value, double ds) {
//				value -= ds/(TIME.secondsPerDay*16.0*4.0);
//				value = CLAMP.d(value, 0, 1);
//				return value;
//				
				if (FACTIONS.DIP().trades(FACTIONS.player(), roy.court.faction)) {
					double d = roy.trait(RTraits.get().competence);
					value += d*ds/(TIME.secondsPerDay*16.0);
					
				}else {
					value -= ds/(TIME.secondsPerDay*16.0*4.0);
				}
				value = CLAMP.d(value, 0, 1);
				return value;
			}
			@Override
			public double getP(double value, Royalty t) {
				return value;
			}
		};
		
		new OpinionFactorE(allD, DicArmy.¤¤War, UI.icons().s.fist, 0, -5) {

			@Override
			protected double pUpdate(Royalty roy, double value, double ds) {
				if (roy.isKing() && FACTIONS.DIP().war.is(FACTIONS.player(), roy.court.faction)) {
					value += ds/(TIME.secondsPerDay*16.0);
				}else {
					value -= ds/(TIME.secondsPerDay*16.0*4.0);
				}
				value = CLAMP.d(value, 0, 1);
				return value;
			}
			@Override
			public double getP(double value, Royalty t) {
				return value;
			}
		};
		
		liberation = new OpinionFactorE(allD, DicArmy.¤¤Vassal, UI.icons().s.happy, 0, 100) {

			@Override
			protected double pUpdate(Royalty roy, double value, double ds) {
				value -= value*ds/(TIME.secondsPerDay*16.0*8.0);
				value = CLAMP.d(value, 0, 1);
				return value;
			}
			@Override
			public double getP(double value, Royalty t) {
				double d = wealth.get(t) + threat.get(t);
				d = -d;
				if (d <= 0)
					return 0;
				double v = d*value*2.0;
				v = CLAMP.d(v, 0, d+1);
				return v/100.0;
			}
		};
		

	}
	
	static OpinionData createData() {
		return new OpinionData(self.allD.size());
	}
	
	public static double current(FactionNPC f) {
		return current(f.court().king().roy());
	}
	
	public static double tradeCost(FactionNPC f) {
		if (FACTIONS.DIP().war.is(f, FACTIONS.player()))
			return 0.95;
		
		double tt = 1.0-self.trade.getP(0, f.court().king().roy());
		
		tt = 0.5 + 0.5*tt;
		
		double op = current(f.court().king().roy());
		if (op < 0)
			op = 0.5;
		else
			op = 0.5/(1+ op*2);
		
		return op*tt;
//		
//		tt /= (1 + CLAMP.d(current(f.court().king().roy())/2.0, 0, Double.MAX_VALUE));
//		
//		return 1.0 / (1 + CLAMP.d(current(f.court().king().roy())/2.0, 0, Double.MAX_VALUE));
	}
	
//	public static double goodWill(FactionNPC f) {
//		if (FACTIONS.DIP().war.is(f, FACTIONS.player()))
//			return 0;
//		double op = current(f);
//		if (op > 0) {
//			op /= self.diplomacy.max();
//			return op;
//		}
//		return 0;
//	}
	
	public static Boostable GET(){
		return self.boostable;
	}
	
	public static double current(Royalty roy) {
		return self.boostable.get(roy);
	}
	
	public static double attackValue(FactionNPC f) {
		double d = current(f.court().king().roy());
		d *= 1.0 + f.court().king().roy().trait(RTraits.get().war);
		if (d < 0)
			return -d/10.0;
		return 0;
	}
	
	public static void update(Royalty roy, double ds) {
		for (int i = 0; i < self.allD.size(); i++)
			self.allD.get(i).update(roy, ds);
	}
	
	public static void flatter(Royalty roy, double am) {
		self.flattery.inc(roy, am*(1+roy.trait(RTraits.get().pride)));
	}
	
	public static double flattery(Royalty roy) {
		return self.flattery.vGet(roy)/self.flattery.max();
	}
	
	public static void assasinate(Royalty roy) {
		double ch = 0.5;
		ch = 1.0-Math.sqrt(self.assasination.data(roy));
		ch *=0.5;
		if (ch > RND.rFloat()) {
			roy.kill(false);
			GAME.count().ROYALTIES_KILLED.inc(1);
			
		}else {
			self.assasination.inc(roy, 0.25);
		}
		
	}
	
	public static void liberate(FactionNPC f) {
		for (Royalty r : f.court().all()) {
			self.liberation.set(r, r.isKing() ? 1.0 : 0.5);
		}
	}
	
	public static void makeDeal(FactionNPC f, double generousity) {
		for (Royalty r : f.court().all()) {
			makeDeal(r, r.isKing() ? generousity : generousity*0.25);
		}
	}
	
	
	
	public static void makeDeal(Royalty roy, double generousity) {
		double d = 0.5 + 0.5*roy.trait(RTraits.get().honesty);
		self.diplomacy.inc(roy, generousity*d);
	}
	
	public static void makeDealRaw(Royalty roy, double generousity) {
		self.diplomacy.inc(roy, generousity/self.diplomacy.max());
	}
	
	public static void favour(FactionNPC f, double generousity) {
		generousity /= self.favours.max();
		for (Royalty r : f.court().all()) {
			self.favours.inc(r, r.isKing() ? generousity : generousity*0.25);
		}
	}
	
	public static double dealIncrease(FactionNPC f, double v) {
		double d = 0.5 + 0.5*f.court().king().roy().trait(RTraits.get().honesty);
		return self.diplomacy.max()*v*d;
	}
	
	public static void declareWar(FactionNPC f) {
		for (Royalty r : f.court().all()) {
			self.warDeclare.inc(r, r.isKing() ? 1.0 : 0.25);
			self.peace.set(r, 0);
		}
		
	}
	
	public static void makePeace(FactionNPC f) {
		double d = current(f);
		
		if (d < 0) {
			double p = DWar.peaceValue(f);
			double tar = -d;
			tar = -d + 0.5 + p;
			tar /= self.peace.max();
			
			self.peace.inc(f.court().king().roy(), tar);
		}
	}
	
	public static void trade(FactionNPC s, int price) {
		double d = price/(15000.0*RESOURCES.ALL().size());
		for (Royalty r : s.court().all()) {
			self.trade.inc(r, r.isKing() ? d : 0.25*d);
			
		}
	}

	
	public static abstract class OpinionFactorE extends BoosterImp{

		private final int index;
		
		OpinionFactorE(LISTE<OpinionFactorE> allD, CharSequence name, SPRITE icon, double min, double max, boolean isMul){
			super(new BSourceInfo(name, icon), min, max, isMul);
			this.index = allD.add(this);
			add(self.boostable);
		}
		
		OpinionFactorE(LISTE<OpinionFactorE> allD, CharSequence name, SPRITE icon, double min, double max){
			this(allD, name, icon, min, max, false);
		}
		
		public abstract double getP(double value, Royalty t);

		public void inc(Royalty roy, double doo) {
			roy.data.data[index] += doo;
		}
		
		public void set(Royalty roy, double doo) {
			roy.data.data[index] = doo;
		}
		
		void update(Royalty roy, double ds) {
			roy.data.data[index] = pUpdate(roy, roy.data.data[index], ds);
		}
		
		double data(Royalty roy) {
			return roy.data.data[index];
		}
		
		protected abstract double pUpdate(Royalty roy, double value, double ds);

		@Override
		public double vGet(Royalty t) {
			return getP(t.data.data[index], t);
		}
		
		@Override
		public double vGet(Faction f) {
			return 0;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> o) {
			return o == Royalty.class;
		}

		
		
	}

	
	
}
