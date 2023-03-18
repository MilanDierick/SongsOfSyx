package settlement.stats;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.boostable.*;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.stats.Init.*;
import settlement.stats.SETT_STATISTICS.SettStatistics;
import settlement.stats.law.LAW;
import settlement.stats.standing.STANDINGS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sets.ArrayInt.ArrayInt2D;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE.DoubleImp;
import util.data.INT_O.INT_OE;
import util.dic.DicRes;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.RCollection;
import util.statistics.HistoryInt;

public final class StatsMultipliers {
	

	private final LIST<LIST<StatMultiplier>> classes;
	private final LIST<StatMultiplier> all;

	public final StatMultiplier BONUS;
	public final StatMultiplier KILLER;
	public final StatMultiplierAction PROSECUTION;
	public final StatMultiplierAction EMANCIPATE;
	public final StatMultiplierAction DRINK;
	public final StatMultiplierAction HANDOUT;
	public final StatMultiplierAction DAY_OFF;
	public final StatMultiplierWork OVERTIME;
	
	public final RCollection<StatMultiplier> MAP;
	private static CharSequence ¤¤bonusDesc = "¤Boosts from different aspects such as technologies.";

	static {
		D.ts(StatsMultipliers.class);
	}
	
	StatsMultipliers(Init init){

		init.init("MULTIPLIERS");
		
		LinkedList<StatMultiplier> all = new LinkedList<>();

		BONUS = new Bonus(init, all);
		KILLER = new Killer(init, all);
		PROSECUTION = new Prosecution(init, all);
		EMANCIPATE = new Emancipate(init, all);
		
		DRINK = new StatMultiplierActionImp("DRINKS", init, all, SETT.ROOMS().TAVERNS.get(0).iconBig().nomal, HCLASS.CITIZEN);
		HANDOUT = new Handout("HANDOUT", init, all, SPRITES.icons().s.money, HCLASS.CITIZEN);
		DAY_OFF = new StatMultiplierActionImp("DAY_OFF", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.anti), HCLASS.SLAVE, HCLASS.CITIZEN);
		OVERTIME = new StatMultiplierWork("OVERTIME", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.arrow_up), HCLASS.SLAVE, HCLASS.CITIZEN);
		
		
		this.all = new ArrayList<>(all);
		
		MAP = new RCollection<StatsMultipliers.StatMultiplier>("MULTIPLIERS") {
			{
				for (StatMultiplier m : StatsMultipliers.this.all) {
					if (m.key != null && !map.containsKey(m.key))
						map.put(m.key, m);
				}
			}

			@Override
			public StatMultiplier getAt(int index) {
				return StatsMultipliers.this.all.get(index);
			}

			@Override
			public LIST<StatMultiplier> all() {
				return StatsMultipliers.this.all;
			}
			
		};
		
		ArrayList<LIST<StatMultiplier>> classes = new ArrayList<>(HCLASS.ALL.size());
		
		for (HCLASS cl : HCLASS.ALL) {
			all = new LinkedList<>();
			for (StatMultiplier m : this.all) {
				if (m.classes[cl.index()])
					all.add(m);
			}
			LIST<StatMultiplier> res = new ArrayList<>(all);
			classes.add(res);
		}
		
		this.classes = classes;

		init.upers.add(new Updatable2() {
			
			@Override
			public void update(double ds) {
				for (StatMultiplier m : StatsMultipliers.this.all)
					m.update(ds);
			}
		});
		
	}
	
	public LIST<StatMultiplier> get(HCLASS cl){
		return classes.get(cl.index());
	}
	
	public LIST<StatMultiplier> all(){
		return all;
	}
	
	public static abstract class StatMultiplier extends INFO implements INDEXED{
		
		private final int index;
		public final String key;
		public final CharSequence verb;
		private boolean[] classes = new boolean[HCLASS.ALL.size()];
		public final double defValue;
		
		private StatMultiplier(C cc, LISTE<StatMultiplier> all, HCLASS... cl) {
			super(cc.name, cc.desc);
			this.verb = cc.verb;
			this.index = all.add(this);
			this.key = cc.key;
			
			defValue = cc.def;
			for (HCLASS c : cl) {
				classes[c.index()] = true;
			}
		}
		
		@Override
		public int index() {
			return index;
		}
		
		public boolean available(HCLASS cl) {
			return classes[cl.index()];
		}

		public abstract double multiplier(Humanoid h);

		public final double multiplier(HCLASS cl, Race race, int daysBack) {
			if (race != null)
				return mul(cl, race, daysBack);
			double vv = 0;
			double pop = 0;
			for (Race r : RACES.all()) {
				double p = STATS.POP().POP.data(cl).get(r);
				vv +=  mul(cl, r, daysBack)*p;
				pop += p;
			}
			if (pop == 0) {
				vv = 0;
				for (Race r : RACES.all()) {
					vv +=  mul(cl, r, daysBack);
				}
				return vv/RACES.all().size();
			}

			return vv/pop;
		}
		
		public double min(HCLASS cl, Race race) {
			if (race == null) {
				double pop = 0;
				double v = 0;
				for (Race r : RACES.all()) {
					double p = STATS.POP().POP.data(cl).get(r);
					pop += p;
					v += Math.min(1, r.stats().multiplier(this))*p;
				}
				if (pop == 0)
					return v/= RACES.all().size();
				return v/pop;
			}
			return Math.min(1, race.stats().multiplier(this));
			
		}
		public double max(HCLASS cl, Race race) {
			if (race == null) {
				double pop = 0;
				double v = 0;
				for (Race r : RACES.all()) {
					double p = STATS.POP().POP.data(cl).get(r);
					pop += p;
					v += Math.max(1, r.stats().multiplier(this))*p;
				}
				if (pop == 0)
					return v/= RACES.all().size();
				return v/pop;
			}
			return Math.max(1, race.stats().multiplier(this));
		}
		protected abstract double mul(HCLASS cl, Race race, int daysBack);
		
		protected abstract void update(double ds);
		
	}
	
	private static class Bonus extends StatMultiplier {
		
		private final Data data = new Data();
		private static final double di = 1.0/1000;
		
		private Bonus(Init init, LISTE<StatMultiplier> all) {
			super(new C(BOOSTABLES.INFO().name, ¤¤bonusDesc, ""), all, HCLASS.CITIZEN, HCLASS.SLAVE);
			init.savables.add(data);
		}
		
		@Override
		protected double mul(HCLASS cl, Race race, int daysBack) {
			return data.get(cl, race).get(daysBack)*di;
		}
		
		private BOOSTABLE boost(HCLASS cl) {
			if (cl == HCLASS.SLAVE)
				return BOOSTABLES.BEHAVIOUR().SUBMISSION;
			return BOOSTABLES.BEHAVIOUR().HAPPINESS;
		}

		@Override
		protected void update(double ds) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				data.get(HCLASS.CITIZEN, r).set((int) (1000*boost(HCLASS.CITIZEN).get(HCLASS.CITIZEN, r)));
				data.get(HCLASS.SLAVE, r).set((int) (1000*boost(HCLASS.SLAVE).get(HCLASS.SLAVE, r)));
			} 
			
		}

		@Override
		public double min(HCLASS cl, Race race) {
			return 0;
		}

		@Override
		public double max(HCLASS cl, Race race) {
			return BOOSTABLES.player().max(boost(cl));
		}

		@Override
		public double multiplier(Humanoid h) {
			return boost(h.indu().clas()).get(h);
		}

		
	}
	
	private static class Killer extends StatMultiplier {
		
		private final Data data = new Data();
		private static final double di = 1.0/1000;
		
		private Killer(Init init, LISTE<StatMultiplier> all) {
			super(new C("SERIAL_KILLER", init), all, HCLASS.CITIZEN, HCLASS.SLAVE);
			init.savables.add(data);
		}
		
		@Override
		protected double mul(HCLASS cl, Race race, int daysBack) {
			double d = data.get(cl, race).get(daysBack)*di;
			return 1 + (race.stats().multiplier(this)-1)*d;
		}

		@Override
		protected void update(double ds) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				double ra = GAME.events().killer.victimRace() == r ? GAME.events().killer.rate() : 0;
				data.get(HCLASS.CITIZEN, r).set((int) (1000*ra));
				data.get(HCLASS.SLAVE, r).set((int) (1000*ra));
			} 
			
		}

		@Override
		public double multiplier(Humanoid h) {
			return mul(h.indu().clas(), h.indu().race(), 0);
		}

		
	}
	
	public static abstract class StatMultiplierAction extends StatMultiplier {
		
		public final SPRITE icon;
		
		private StatMultiplierAction(String key, Init init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(new C(key, init), all, cl);
			this.icon = icon;
		}
		
		public abstract boolean canUnmark();
		public abstract int unmarkable(HCLASS cl, Race race);
		public abstract void unmark(HCLASS cl, Race race);
		public boolean markIs(Humanoid a) {
			return markIs(a.indu());
		}
		public abstract boolean markIs(Induvidual a);
		public boolean canBeMarked(Induvidual a) {
			return maxAmount(a.clas(), a.race()) > 0;
		}
		public abstract void mark(HCLASS cl, Race race, int amount);
		public abstract void mark(Humanoid a, boolean set);
		public abstract void consume(Humanoid a);
		public final boolean consumeIs(Humanoid a) {
			return consumeIs(a.indu());
		}
		public abstract boolean consumeIs(Induvidual a);
		public abstract int maxAmount(HCLASS cl, Race race);
		public void info(GBox box, int amount) {
			
		}
		
		public abstract double standingIncrease(HCLASS cl, Race race, int amount);
		
	}

	
	private static class Prosecution extends StatMultiplierAction {
		
		private final INT_OE<Induvidual> in;
		protected final ArrayInt count;
		
		private Prosecution(Init init, LISTE<StatMultiplier> all) {
			super("PROSECUTION", init, all, SPRITES.icons().m.slave, HCLASS.CITIZEN);
			count = new ArrayInt(RACES.all().size());
			init.savables.add(count);
			in = init.count.new DataBit();
			init.addable.add(new Addable() {
				
				@Override
				public void removeH(Induvidual i) {
					count.inc(i.race(), -in.get(i));
				}
				
				@Override
				public void addH(Induvidual i) {
					count.inc(i.race(), in.get(i));
				}
			});
		}

		@Override
		protected double mul(HCLASS cl, Race race, int daysBack) {
			double d = CLAMP.d(LAW.process().prosecute.rate(race).getD()*40, 0, 1);
			return 1 + (race.stats().multiplier(this)-1)*d;
		}

		@Override
		protected void update(double ds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean markIs(Induvidual a) {
			return in.get(a) == 1;
		}

		@Override
		public void mark(Humanoid a, boolean set) {
			count.inc(a.race(), -in.get(a.indu()));
			in.set(a.indu(), set ? 1 : 0);
			count.inc(a.race(), in.get(a.indu()));
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race)-count.get(race);
		}

		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {

				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.race() == race && h.indu().clas() == HCLASS.CITIZEN && !markIs(h.indu())) {
						mark(h, true);
						amount--;
						if (amount <= 0)
							return true;
					}
					return false;
				}
				
			};
		}
		
		@Override
		public boolean canUnmark() {
			return true;
		}
		
		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return count.get(race);
		}
		
		@Override
		public void unmark(HCLASS cl, Race race) {
			new Iter(cl, race, 1) {

				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.race() == race && h.indu().clas() == HCLASS.CITIZEN && markIs(h.indu())) {
						mark(h, false);
					}
					return false;
				}
				
			};
		}

		@Override
		public double standingIncrease(HCLASS cl, Race race, int amount) {
			double d = CLAMP.d(40*amount/(1.0+STATS.POP().POP.data(cl).get(race)), 0, 1);
			return (race.stats().multiplier(this)-1)*d;
		}
		
	

		@Override
		public void consume(Humanoid a) {
			
		}

		@Override
		public double multiplier(Humanoid h) {
			return 1;
		}

		@Override
		public boolean consumeIs(Induvidual a) {
			return false;
		}
		
		@Override
		public void info(GBox box, int amount) {
			if (SETT.ROOMS().GUARD.employment().employed() == 0) {
				box.add(box.text().add(SETT.ROOMS().GUARD.employment().verb).add(':').s().add('0'));
			}
		}
	}
	
	private static class Emancipate extends StatMultiplierAction {
		
		private final HistoryInt data = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		private final DoubleImp timer = new DoubleImp();
		private final double rate = 16.0/(TIME.secondsPerDay*8);
		
		private Emancipate(Init init, LISTE<StatMultiplier> all) {
			super("SLAVES_FREED", init, all, SPRITES.icons().m.chainsFree, HCLASS.SLAVE);
			init.savables.add(data);
			init.savables.add(timer);;
			init.upers.add(new Updatable2() {
				
				@Override
				public void update(double ds) {
					
					
				}
			});
		}
		
		@Override
		public double multiplier(Humanoid h) {
			return 1;
		}

		@Override
		public boolean canUnmark() {
			return false;
		}
		
		@Override
		public boolean markIs(Induvidual a) {
			return false;
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race);
		}

		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			
			new Ite(amount, race);
		}
		
		private  class Ite extends EntityIterator.Humans {

			private int amount;
			private final Race race;
			
			Ite(int amount, Race race){
				this.amount = amount;
				this.race = race;
				iterate();
			}
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (h.race() == race && h.indu().clas() == HCLASS.SLAVE) {
					mark(h, true);
					amount--;
					if (amount <= 0)
						return true;
				}
				return false;
			}
			
		}

		@Override
		public double standingIncrease(HCLASS cl, Race race, int amount) {
			STANDINGS.SLAVE().update();
			double now = mul(cl, race, 0);
			data.inc(16*amount);
			double next =  mul(cl, race, 0);
			data.inc(-16*amount);
			
			double old = STANDINGS.SLAVE().target();
			double nn = old;
			nn /= STANDINGS.SLAVE().numbersMul(STATS.POP().POP.data(HCLASS.SLAVE).get(null), STATS.POP().POP.data(HCLASS.CITIZEN).get(null));
			nn /= now;
			nn *= STANDINGS.SLAVE().numbersMul(STATS.POP().POP.data(HCLASS.SLAVE).get(null)-amount, STATS.POP().POP.data(HCLASS.CITIZEN).get(null)+amount);
			nn *= next;
			old = CLAMP.d(old, 0, 1);
			nn = CLAMP.d(nn, 0, 1);
			return CLAMP.d(nn-old, 0, 1);
		}


		@Override
		protected double mul(HCLASS cl, Race race, int daysBack) {
			double d = data.get(daysBack)/(1.0+STATS.POP().POP.data(cl).get(null));
			return 1 + (race.stats().multiplier(this)-1)*d;
		}


		@Override
		protected void update(double ds) {
			timer.incD(-rate*ds);
			if (timer.getD() > 0)
				return;
			timer.incD(1);
			int tot = data.get();
			int pop = STATS.POP().POP.data(HCLASS.SLAVE).get(null);
			
			tot -= pop;
			tot = CLAMP.i(tot, 0, pop*16);
			data.set(tot);
			
		}

		@Override
		public void mark(Humanoid h, boolean set) {
			data.inc(16);
			h.HTypeSet(HTYPE.SUBJECT, null, CAUSE_ARRIVE.EMANCIPATED);
			STATS.POP().TYPE.FORMER_SLAVE.set(h.indu());
			
		}

		@Override
		public void consume(Humanoid a) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean consumeIs(Induvidual a) {
			return false;
		}

		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return 0;
		}

		@Override
		public void unmark(HCLASS cl, Race race) {
			// TODO Auto-generated method stub
			
		}

		
	}
	
	
	private static class StatMultiplierActionImp extends StatMultiplierAction implements Addable, Updatable{
		
		protected final SettStatistics active;
		protected final ArrayInt2D selected;
		protected final INT_OE<Induvidual> iActive;
		protected final INT_OE<Induvidual> iActiveCount;
		protected final INT_OE<Induvidual> iSelected;
		
		
		private StatMultiplierActionImp(String key, Init init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(key, init, all, icon, cl);
			active = new SettStatistics(init, null);
			selected = new ArrayInt2D(HCLASS.ALL.size(), RACES.all().size());
			init.savables.add(selected);
			iActive = init.count.new DataBit();
			iActiveCount = init.count.new DataCrumb();
			iSelected = init.count.new DataBit();
			
			init.addable.add(this);
			init.updatable.add(this);
		}
		
		@Override
		public void removeH(Induvidual i) {
			selected.get(i.clas()).inc(i.race(), -iSelected.get(i));
			active.inc(i, -iActive.get(i));
		}
		
		@Override
		public void addH(Induvidual i) {
			selected.get(i.clas()).inc(i.race(), iSelected.get(i));
			active.inc(i, iActive.get(i));
		}
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int updateI) {
			if ((updateI & 0xF) == 0 &&  iActive.get(h.indu()) > 0) {
				Induvidual i = h.indu();
				if (iActiveCount.get(i) == 0) {
					removeH(i);
					iActive.inc(i, -1);
					addH(i);
				}else {
					iActiveCount.inc(h.indu(), -1);
				}
			}
		}

		@Override
		public boolean markIs(Induvidual a) {
			return iSelected.get(a) != 0;
		}

		@Override
		public void consume(Humanoid a) {
			removeH(a.indu());
			iActive.setD(a.indu(), 1.0);
			iActiveCount.set(a.indu(), 2 + RND.rInt(1));
			iSelected.set(a.indu(), 0);
			addH(a.indu());
		}
		
		@Override
		public boolean consumeIs(Induvidual a) {
			return iActive.get(a) != 0;
		}

		@Override
		public void mark(Humanoid a, boolean set) {
			removeH(a.indu());
			iActive.setD(a.indu(), 0);
			iSelected.set(a.indu(), set ? 1 : 0);
			addH(a.indu());
		}


		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && !markIs(h.indu()) && iActive.get(h.indu()) == 0) {
						mark(h, true);
						amount --;
						return amount <= 0;
					}
					return false;
				}
			};
			
		}

		@Override
		public double standingIncrease(HCLASS cl, Race race, int amount) {
			double d = CLAMP.d(amount/(1.0+STATS.POP().POP.data(cl).get(race)), 0, 1);
			return (race.stats().multiplier(this)-1)*d;
		}


		@Override
		public double multiplier(Humanoid h) {
			return 1 + (h.race().stats().multiplier(this)-1)*iActive.get(h.indu());
		}


		@Override
		protected double mul(HCLASS cl, Race race, int daysBack) {
			double d = active.data(cl).get(race, daysBack)/(1.0+STATS.POP().POP.data(cl).get(race));
			return 1 + (race.stats().multiplier(this)-1)*d;
		}


		@Override
		protected void update(double ds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race) - selected.get(cl).get(race)-active.data(cl).get(race);
		}

		@Override
		public boolean canUnmark() {
			return true;
		}

		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return selected.get(cl).get(race);
		}

		@Override
		public void unmark(HCLASS cl, Race race) {
			new Iter(cl, race, 1) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && markIs(h.indu())) {
						mark(h, false);
					}
					return false;
				}
			};
			
		}
		
		
	}
	
	public static class StatMultiplierWork extends StatMultiplierActionImp implements Addable, Updatable{

		public final LIST<RoomBlueprintImp> ROOMS;
		private final boolean[] roomsB = new boolean[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
		private int[][] available = new int[HCLASS.ALL.size()][RACES.all().size()+1];
		private int ai = -121;
		
		private StatMultiplierWork(String key, Init init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(key, init, all, icon, cl);
			
			LinkedList<RoomBlueprintImp> rooms = new LinkedList<>();
			
			
			for (BOOSTABLERoom b : BOOSTABLES.ROOMS().rooms()) {
				if (!roomsB[b.room.index()]) {
					roomsB[b.room.index()] = true;
					rooms.add(b.room);
					new Booster(init, this, new BBoost(b, 1, false));
				}
			}
			
			new Booster(init, this, new BBoost(BOOSTABLES.CIVICS().ACCIDENT, 0.5, true));
			
			ROOMS = new ArrayList<RoomBlueprintImp>(rooms);
		}
		
		private final EntityIterator.Humans iter = new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (canBeMarked(h.indu())) {
					available[h.indu().clas().index()][h.race().index] ++;
					available[h.indu().clas().index()][RACES.all().size()] ++;
				}
				return false;
			}
		};
		
		@Override
		public int maxAmount(HCLASS cl, Race race) {
			
			if (Math.abs(ai-GAME.updateI()) > 120) {
				for (int[] i : available)
				Arrays.fill(i, 0);
				iter.iterate();
			}
			
			int ri = race == null ? RACES.all().size() : race.index();
			return available[cl.index()][ri];
			
		}
		
		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && !markIs(h.indu()) && iActive.get(h.indu()) == 0) {
						if (canBeMarked(h.indu())) {
							mark(h, true);
							amount --;
							return amount <= 0;
						}
					}
					return false;
				}
			};
		}
		
		public boolean canMark(RoomBlueprint b) {
			return roomsB[b.index()];
		}
		
		@Override
		public boolean canBeMarked(Induvidual a) {
			if (a.player() && !markIs(a) && !consumeIs(a)) {
				RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
				if (ins != null && roomsB[ins.blueprint().index()]) {
					return true;
				}
			}
			return false;
		}
		
		private static final class Booster extends BBooster.BBoosterImp {

			private final StatMultiplierWork stat;

			Booster(Init init, StatMultiplierWork stat, BBoost boost) {
				super(stat.name, boost, true, false, false);
				this.stat = stat;
			}

			@Override
			public double pvalue(Induvidual v) {
				return CLAMP.d(stat.iActive.get(v), 0, 1);
			}

			@Override
			public double pvalue(HCLASS c, Race r) {
				return stat.active.data(c).getD(r);
			}

			@Override
			public double pvalue(Div v) {
				return 0;
			}


		}
		
	}
	
	private static class Handout extends StatMultiplierActionImp {
		
		private final int amount = 400;
		
		Handout(String key, Init init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl){
			super(key, init, all, icon, cl);
		}
		
		@Override
		public int maxAmount(HCLASS cl, Race race) {
			int creds = (int) FACTIONS.player().credits().credits();
			creds /= amount;
			int am = super.maxAmount(cl, race);
			return Math.min(creds, am);
			
		}
		
		@Override
		public void mark(Humanoid a, boolean set) {
			super.mark(a, set);
			FACTIONS.player().credits().handouts.OUT.inc(amount);
			if (set) {
				super.consume(a);
			}
		}
		
		@Override
		public void info(GBox box, int amount) {
			box.textL(DicRes.¤¤Curr);
			box.tab(5);
			box.add(GFORMAT.iIncr(box.text(), -amount*this.amount));
		}
		
		@Override
		public boolean canUnmark() {
			return false;
		}
		
		
		
	}
	
	

	

	
	
	private static class Data implements SAVABLE {
		
		private final HistoryInt[][] iii = new HistoryInt[HCLASS.ALL.size()][RACES.all().size()]; 
		
		Data(){
			for (int ci = 0; ci < HCLASS.ALL.size(); ci++) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					iii[ci][ri] = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
				}
			}
		}
		
		public HistoryInt get(HCLASS cl, Race r) {
			return iii[cl.index()][r.index()];
		}

		@Override
		public void save(FilePutter file) {
			for (HistoryInt[] ii : iii)
				for (HistoryInt i : ii)
					i.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (HistoryInt[] ii : iii)
				for (HistoryInt i : ii)
					i.load(file);
		}

		@Override
		public void clear() {
			for (HistoryInt[] ii : iii)
				for (HistoryInt i : ii)
					i.clear();
		}
		
	}
	
	private static abstract class Iter extends EntityIterator.Humans {

		int amount;
		final Race race;
		final HCLASS cl;
		
		Iter(HCLASS cl, Race race, int amount){
			this.amount = amount;
			this.race = race;
			this.cl = cl;
			iterate();
		}
		
	}
	
	private static class C extends INFO{
		
		private CharSequence verb;
		private double def = 1.0;
		private String key;
		
		C(String key, Init init){
			super(init.jText.json(key));
			verb = init.jText.json(key).text("VERB");
			def = init.jData.d(key);
			this.key = key;
		}
		
		C(CharSequence name, CharSequence desc, CharSequence verb){
			super(name, desc);
			this.verb = verb;
		}
		
	}
	

}
