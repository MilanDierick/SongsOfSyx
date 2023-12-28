package settlement.stats.muls;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.npc.NPCBonus;
import game.time.TIME;
import init.race.*;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.*;
import settlement.stats.StatsInit.*;
import settlement.stats.colls.StatsService;
import settlement.stats.law.LAW;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import settlement.stats.util.CAUSE_ARRIVE;
import settlement.stats.util.StatBooster;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
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
	
	private final static String pre = "EVENT_";

	private final LIST<LIST<StatMultiplier>> classes;
	private final LIST<StatMultiplier> all;

	public final StatMultiplier KILLER;
	public final StatMultiplierAction PROSECUTION;
	public final StatMultiplierAction EMANCIPATE;
	public final StatMultiplierAction HANDOUT;
	public final StatMultiplierAction DAY_OFF;
	public final StatMultiplierWork OVERTIME;
	
	public final RCollection<StatMultiplier> MAP;
	
	public StatsMultipliers(StatsInit init, StatsService service){
		
		LinkedList<StatMultiplier> all = new LinkedList<>();

		KILLER = new Killer(init, all);
		PROSECUTION = new Prosecution(init, all);
		EMANCIPATE = new Emancipate(init, all);
		
		HANDOUT = new Handout("HANDOUT", init, all, SPRITES.icons().s.money, HCLASS.CITIZEN);
		DAY_OFF = new StatMultiplierActionImp("DAY_OFF", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.anti), HCLASS.SLAVE, HCLASS.CITIZEN);
		OVERTIME = new StatMultiplierWork("OVERTIME", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.arrow_up), HCLASS.SLAVE, HCLASS.CITIZEN);
		
		this.all = new ArrayList<>(all);
		
		
		
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

		init.upers.add(new StatUpdatable() {
			
			@Override
			public void update(double ds) {
				for (StatMultiplier m : StatsMultipliers.this.all)
					m.update(ds);
			}
		});
		

		
		
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
	}
	
	public void setBoost(StatMultiplier m, Json json, String key) {
		m.boosters.push(key, json, new StatBooster() {

			@Override
			public double vGet(Induvidual indu) {
				return m.value(indu);
			}

			@Override
			public double vGet(POP_CL reg, int daysBack) {
				return m.value(reg.cl, reg.race, daysBack);
			}

			@Override
			public double vGet(Div div) {
				return 0;
			}

			@Override
			public double vGet(NPCBonus bonus) {
				return 0;
			}
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> b) {
				
				return b != NPCBonus.class && StatBooster.super.has(b);
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
		public final BoostSpecs boosters;
		
		private StatMultiplier(C cc, LISTE<StatMultiplier> all, HCLASS... cl) {
			super(cc.name, cc.desc);
			this.verb = cc.verb;
			this.index = all.add(this);
			this.key = cc.key;
			
			boosters = new BoostSpecs(cc.name, UI.icons().s.crown, true);
			
			for (HCLASS c : cl) {
				classes[c.index()] = true;
			}
		}
		
		@Override
		public int index() {
			return index;
		}
		
		public boolean available(HCLASS cl, Race race) {
			return classes[cl.index()];
		}
		
		public boolean available(Induvidual i) {
			return available(i.clas(), i.race());
		}

		public abstract double value(Induvidual h);
		
		public abstract double value(HCLASS cl, Race race, int daysBack);
		
		protected abstract void update(double ds);
		
	}
	
	private static class Killer extends StatMultiplier {
		
		private final Data data = new Data();
		private static final double di = 1.0/1000;
		
		private Killer(StatsInit init, LISTE<StatMultiplier> all) {
			super(new C("SERIAL_KILLER", init), all, HCLASS.CITIZEN, HCLASS.SLAVE);
			init.savables.add(data);
		}
		
		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			if (cl == null) {
				double v = 0;
				double am = 0;
				for (int ci = 0; ci < HCLASS.ALL.size(); ci++) {
					HCLASS cll = HCLASS.ALL.get(ci);
					if (cll.player) {
						double p = STATS.POP().POP.data(cll).get(race);
						v += value(cll, race, daysBack)*p;
						am += p;
					}
				}
				if (am == 0)
					return 0;
				return di*v/am;
			}
			
			if (race == null) {
				double v = 0;
				double am = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					double p = STATS.POP().POP.data(cl).get(r);
					v += data.get(cl, r).get(daysBack)*p;
					am += p;
				}
				if (am == 0)
					return 0;
				return di*v/am;
			}
			return data.get(cl, race).get(daysBack)*di;
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
		public double value(Induvidual h) {
			return value(h.clas(), h.race(), 0);
		}

		
	}
	
	public static abstract class StatMultiplierAction extends StatMultiplier {
		
		public final SPRITE icon;
		
		private StatMultiplierAction(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(new C(key, init), all, cl);
			this.icon = icon;
		}
		
		private StatMultiplierAction(C c, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(c, all, cl);
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
		
	}

	
	private static class Prosecution extends StatMultiplierAction {
		
		private final INT_OE<Induvidual> in;
		protected final ArrayInt count;
		
		private Prosecution(StatsInit init, LISTE<StatMultiplier> all) {
			super("PROSECUTION", init, all, SPRITES.icons().m.slave, HCLASS.CITIZEN);
			count = new ArrayInt(RACES.all().size());
			init.savables.add(count);
			in = init.count.new DataBit();
			init.addable.add(new Addable() {
				
				@Override
				public void removePrivate(Induvidual i) {
					if (i.player())
						count.inc(i.race(), -in.get(i));
				}
				
				@Override
				public void addPrivate(Induvidual i) {
					if (i.player())
						count.inc(i.race(), in.get(i));
				}
			});
		}

		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			return CLAMP.d(LAW.process().prosecute.rate(race).getD()*40, 0, 1);
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
		public void consume(Humanoid a) {
			
		}

		@Override
		public double value(Induvidual h) {
			return in.get(h) == 1 ? 1 :0;
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
		
		private Emancipate(StatsInit init, LISTE<StatMultiplier> all) {
			super("SLAVES_FREED", init, all, SPRITES.icons().m.chainsFree, HCLASS.SLAVE);
			init.savables.add(data);
			init.savables.add(timer);;
		}
		
		@Override
		public double value(Induvidual h) {
			return STATS.POP().TYPE.get(h) == STATS.POP().TYPE.FORMER_SLAVE ? 1 : 0;
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
		public double value(HCLASS cl, Race race, int daysBack) {
			return data.get(daysBack)/(1.0+STATS.POP().POP.data(cl).get(null));
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

	
	private static class StatMultiplierActionImp extends StatMultiplierAction implements Addable, StatUpdatableI{
		
		protected final SettStatistics active;
		protected final ArrayInt2D selected;
		protected final INT_OE<Induvidual> iActive;
		protected final INT_OE<Induvidual> iActiveCount;
		protected final INT_OE<Induvidual> iSelected;
		
		
		private StatMultiplierActionImp(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
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
		
		private StatMultiplierActionImp(C c, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(c, all, icon, cl);
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
		public void removePrivate(Induvidual i) {
			if (i.player()) {
				selected.get(i.clas()).inc(i.race(), -iSelected.get(i));
				active.inc(i, -iActive.get(i));
			}
			
		}
		
		@Override
		public void addPrivate(Induvidual i) {
			if (i.player()) {
				selected.get(i.clas()).inc(i.race(), iSelected.get(i));
				active.inc(i, iActive.get(i));
			}
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
		public double value(Induvidual h) {
			return iActive.get(h);
		}


		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			return active.data(cl).get(race, daysBack)/(1.0+STATS.POP().POP.data(cl).get(race));
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
	
	public static class StatMultiplierWork extends StatMultiplierActionImp implements Addable, StatUpdatableI{

		public final LIST<RoomBlueprintImp> ROOMS;
		private final boolean[] roomsB = new boolean[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
		private int[][] available = new int[HCLASS.ALL.size()][RACES.all().size()+1];
		private int ai = -121;
		
		private StatMultiplierWork(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(key, init, all, icon, cl);
			
			LinkedList<RoomBlueprintImp> rooms = new LinkedList<>();
			
			ROOMS = new ArrayList<RoomBlueprintImp>(rooms);
			
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					for (BoostSpec s : boosters.all()) {
						
						for (RoomEmploymentSimple b : SETT.ROOMS().employment.ALLS()) {
							if (b.blueprint().bonus() == s.boostable)
								roomsB[b.blueprint().index()] = true;
							
							
						}
						
					}
					
				}
			});
			
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
		
	}
	
	private static class Handout extends StatMultiplierActionImp {
		
		private final int amount = 400;
		
		Handout(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl){
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
			FACTIONS.player().credits().inc(-amount, CTYPE.HANDOUT);
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
		
		private final CharSequence verb;
		private final String key;
		
		C(String key, StatsInit init){
			super(init.dText.json(pre + key));
			key = pre + key;
			verb = init.dText.json(key).text("VERB");
			this.key = key;
		}
		
		C(String key, CharSequence name, CharSequence desc, CharSequence verb){
			super(name, desc);
			this.verb = verb;
			this.key = key;
		}
		
	}
	

}
