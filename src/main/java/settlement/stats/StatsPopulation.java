package settlement.stats;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.D;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.Init.*;
import settlement.stats.SETT_STATISTICS.SettStatistics;
import settlement.stats.STAT.StatInfo;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.GETTER_TRANS;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import util.info.INFO;
import util.statistics.*;
import util.statistics.HISTORY.HISTORY_OBJECT;

public class StatsPopulation extends StatCollection{
	
	public final SettStatistics POP;
	private final DataStat[] pops = new DataStat[HTYPE.ALL().size()];
	public final STAT NOBLES;
	public final STAT AGE;
	public final PopType TYPE;
	public final STAT TRAPPED;
	public final STAT EMMIGRATING;
	public final STAT MAJORITY;
	
	public final STAT SLAVES_SELF;
	public final STAT SLAVES_OTHER;
	public final STAT WRONGFUL;
	public final StatsDeath COUNT;
	
	private final HistoryInt popYearly = new HistoryInt(STATS.DAYS_SAVED, TIME.years(), false);
	
	private final Demography demo;
	public final INT_OE<Induvidual> NAKED;
	private final LIST<STAT> all;
	final int dy = (int) TIME.years().bitConversion(TIME.days());
	public final GETTER_TRANSE<Induvidual, ENTITY> FRIEND;
	
	StatsPopulation(Init init){
		super(init, "POPULATION");
		D.gInit(this);
		demo = new Demography(init);
		NAKED = init.count.new DataBit();
		
		POP = new SettStatistics(init, new StatInfo(DicMisc.¤¤Population, "")) {
			
			@Override
			public int popDivider(HCLASS c, Race r, int daysback) {
				return 1;
			};
		};
		
		WRONGFUL = new STAT.STATImp("WRONGFUL_DEATHS", init) {
			
			@Override
			int getDD(HCLASS s, Race r) {
				double am = (int) COUNT.wrongful[s.index()][r.index]*50.0;
				if (s == HCLASS.CITIZEN) {
					am += COUNT.wrongful[HCLASS.CHILD.index()][r.index]*50.0;
				}
				return CLAMP.i((int)am, 0, STATS.POP().POP.data(s).get(r));
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return super.pdivider(c, r, daysback);
			}
			
			@Override
			public int dataDivider() {
				return 1;
			}
			
		};
		WRONGFUL.info().setMatters(true, false);

		
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return t.clas() == HCLASS.NOBLE ? 1 : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {
				
			}
			
		};
		
		NOBLES = new STAT.STATFacade("NOBLES", init, indu) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double n = POP.data(HCLASS.NOBLE).get(r, daysBack);
				double p = POP.data(HCLASS.NOBLE).get(null, daysBack);
				if (p == 0)
					return n > 0 ? 1 : 0;
				return n/p;
			}
		};
		
		NOBLES.info().setInt();
		NOBLES.info().setMatters(true, false);
		
		TYPE = new PopType(init);

		TRAPPED = new STAT.STATData(null, init, init.count.new DataBit(), new StatInfo(D.g("Trapped"), D.g("TrappedD", "People who are trapped and cut off from the throne")), null);
		TRAPPED.info().setInt();
		
		EMMIGRATING = new STAT.STATData(null, init, init.count.new DataBit(), new StatInfo(D.g("Emigrating"), D.g("EmigratingD", "People who are upset and leaving your city.")), null);
		EMMIGRATING.info().setInt();
		
		AGE = new STAT.STATData("AGE", init, init.count.new DataInt() {
			
			@Override
			public void set(Induvidual i, int v) {
				demo.removeH(i);
				super.set(i, v);
				demo.addH(i);
				
			}
			
			@Override
			public double getD(Induvidual t) {
				return super.get(t)/(1+BOOSTABLES.PHYSICS().DEATH_AGE.get(t)*dy);
			}
			
		});
		AGE.info().setInt();
		
		MAJORITY = new STAT.STATFacade("MAJORITY", init, 1.0) {

			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double pop = POP.data(s).get(null, daysBack);
				if (pop == 0)
					return 0;
				return POP.data(s).get(r, daysBack)/pop;
			}
		};
		MAJORITY.info().setMatters(true, false);
		
		SLAVES_SELF = new STAT.STATFacade("SLAVES_SELF", init) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double p = STATS.POP().POP.data(HCLASS.CITIZEN).get(r, daysBack);
				if (p == 0)
					return 0;
				return STATS.POP().POP.data(HCLASS.SLAVE).get(r, daysBack)/p;
			}

		};
		SLAVES_SELF.info().setMatters(true, false);
		
		SLAVES_OTHER = new STAT.STATFacade("SLAVES_OTHER", init) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				double slaves = (STATS.POP().POP.data(HCLASS.SLAVE).get(null, daysBack));
				double p = STATS.POP().POP.data(null).get(null, daysBack);
				if (p == 0)
					return 0;
				return (slaves)/p;
			}

		};
		SLAVES_OTHER.info().setMatters(true, false);
		
		init.updatable.add(updater);
		
		COUNT = new StatsDeath(init);
		
		FRIEND = new Friend(init);
		
		all = makeStats(init);
		
		
		for (HTYPE t : HTYPE.ALL()) {
			pops[t.index()] = new DataStat(init) {
				
				@Override
				public double getD(Race t, int fromZero) {
					double d = get(t, fromZero);
					double p = POP.data().get(t);
					if (p == 0)
						return CLAMP.d(d, 0, 1);
					return CLAMP.d(d/p, 0, 1);
				}
				
				@Override
				public int min(Race t) {
					return 0;
				}
				
				@Override
				public int max(Race t) {
					return POP.data().get(t);
				}
			};
		}
	
		
		init.addable.add(new Addable() {
			
			@Override
			public void removeH(Induvidual i) {
				POP.inc(i, -1);
				pops[i.hType().index()].incrFull(i, -1);
				if (i.hType().player)
					popYearly.inc(-1);
			}
			
			@Override
			public void addH(Induvidual i) {
				POP.inc(i, 1);
				pops[i.hType().index()].incrFull(i, 1);
				if (i.hType().player)
					popYearly.inc(1);
			}
		});
		
	}
	
	public HISTORY_INT popYearly() {
		return popYearly;
	}
	
	private final Updatable updater = new Updatable() {
		
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {

			
			Induvidual i = h.indu();
			
			if ((updateI&0x0F) == (i.randomness&0x0F)) {
				
				AGE.indu().inc(i, 1);				
				if (shouldDieOfOldAge(AGE.indu().get(i), BOOSTABLES.PHYSICS().DEATH_AGE.get(i)))
					HumanoidResource.dead = CAUSE_LEAVE.AGE;
				
				
			}
			
		}
		
		@Override
		public void init(Induvidual i) {
			{
				int min = i.race().physics.adultAt;
				int max = (int) (BOOSTABLES.PHYSICS().DEATH_AGE.get(i)*dy*0.5);
				AGE.indu().set(i, min + RND.rInt(max));
			}
		};
		

	};
	
	public boolean shouldDieOfOldAge(double ageDays, double lifeYears) {
		double death = (lifeYears*dy);
		double now = ageDays;
		double delta = now/death;
		double expo = Math.pow(death, 0.5);
		
		delta = Math.pow(delta, expo);
		
		if (delta > RND.rFloat()) {
			return true;
		}
		
		
		return false;
	}

	public boolean shoudRetire(Induvidual i) {
		int oa = (int) (BOOSTABLES.PHYSICS().DEATH_AGE.get(i)*dy);
		int oldAge = (int) (oa - oa*0.5*STATS.WORK().RETIREMENT_AGE.decree().get(i.clas()).getD(i.race()));
		return AGE.indu().get(i) >= oldAge;
	}
	
	public boolean shoudRetire(HCLASS cl, Race race, double lifeYears, double ageDays) {
		int oa = (int) (lifeYears*dy);
		int oldAge = (int) (oa - oa*0.5*STATS.WORK().RETIREMENT_AGE.decree().get(cl).getD(race));
		return ageDays >= oldAge;
	}
	
	public int pop(HTYPE type) {
		return pops[type.index()].get(null);
	}
	
	public int pop(Race r, HTYPE type) {
		return pops[type.index()].get(r);
	}
	
	public int pop(Race r, HTYPE type, int daysBack) {
		if (r == null)
			return pop(type);
		return pops[type.index()].get(r, daysBack);
	}
	
	public int total(HTYPE type) {
		int am = 0;
		for (Race r : RACES.all())
			am += pop(r, type);
		return am;
	}
	
	@Override
	public LIST<STAT> all() {
		return all;
	}
	
	public HISTORY_OBJECT<Race> demography(){
		return demo;
	}

	private static class Demography implements HISTORY_OBJECT<Race>, Addable, SAVABLE{

		private static int size = 32;
		private int[][] perRace = new int[RACES.all().size()][32];
		{D.gInit(StatsPopulation.class);}
		private final INFO info = new INFO(D.g("Demography"), D.g("DemographyDesc", "The different age groups of your citizens"));
		
		Demography(Init init){
			init.addable.add(this);
			init.savables.add(this);
		}

		@Override
		public double getD(Race t, int fromZero) {
			if (t == null) {
				int am = 0;
				for (int i = 0; i < RACES.all().size(); i++) {
					am += perRace[i][fromZero];
				}
				return am;
			}
			return perRace[t.index][fromZero];
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void addH(Induvidual i) {
			if (i.clas() == HCLASS.CITIZEN || i.clas() == HCLASS.CHILD) {
				
				perRace[i.race().index][getT(i)] += 1;
			}
			
		}

		@Override
		public void removeH(Induvidual i) {
			if (i.clas() == HCLASS.CITIZEN || i.clas() == HCLASS.CHILD) {
				perRace[i.race().index][getT(i)] -= 1;
			}
		}
		
		private int getT(Induvidual i) {
			double a = STATS.POP().AGE.indu().get(i);
			a /= STATS.POP().dy;
			a /= BOOSTABLES.PHYSICS().DEATH_AGE.get(i.clas(), i.race());
			a *= size;
			int t = (int) CLAMP.d(a, 0, size-1);
			return t;
		}


		@Override
		public void save(FilePutter file) {
			file.is(perRace);
			
		}


		@Override
		public void load(FileGetter file) throws IOException {
			file.is(perRace);
			
		}


		@Override
		public void clear() {
			for (int[] i : perRace)
				Arrays.fill(i, 0);
			
		}

		@Override
		public TIMECYCLE time() {
			return TIME.days();
		}

		@Override
		public int historyRecords() {
			return size;
		}

		@Override
		public double getD(Race t) {
			return getD(t, 0);
		}

	}
	
	public static final class StatsDeath {

		private final LIST<PopData> deaths;
		private final LIST<PopData> enters;
		private double newEntries = 0;
		private double timer = 0;
		public final GETTER_TRANSE<Induvidual, CAUSE_ARRIVE> arrive;

		private final double[][] wrongful = new double[HCLASS.ALL.size()][RACES.all().size()]; 
		
		StatsDeath(Init init) {
			
			ArrayList<PopData> deaths = new ArrayList<PopData>(CAUSE_LEAVE.ALL().size());
			for (CAUSE_LEAVE l : CAUSE_LEAVE.ALL()) {
				deaths.add(new PopData(l, init, true));
			}
			
			this.deaths = deaths;
			
			ArrayList<PopData> enters = new ArrayList<PopData>(CAUSE_ARRIVE.ALL().size());
			for (CAUSE_ARRIVE l : CAUSE_ARRIVE.ALL()) {
				enters.add(new PopData(l, init, true));
			}
			this.enters = enters;
			
			if (CAUSE_ARRIVE.ALL().size() > 16)
				throw new RuntimeException("Change to bigger data");
			
			final INT_OE<Induvidual> data = init.count.new DataNibble();
			
			arrive = new GETTER_TRANSE<Induvidual, CAUSE_ARRIVE>(){

				@Override
				public CAUSE_ARRIVE get(Induvidual f) {
					return CAUSE_ARRIVE.ALL().get(data.get(f));
				}

				@Override
				public void set(Induvidual f, CAUSE_ARRIVE t) {
					data.set(f, t.index());
				}
				
			};
			
			init.savables.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					for (double[] ds : wrongful)
						file.ds(ds);
					file.d(timer);
					file.d(newEntries);
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					for (double[] ds : wrongful)
						file.ds(ds);
					timer = file.d();
					newEntries = file.d();
				}
				
				@Override
				public void clear() {
					for (double[] ds : wrongful)
						Arrays.fill(ds, 0);
					timer = 0;
					newEntries = 0;
				}
			});
			
			init.upers.add(new Updatable2() {
				
				@Override
				public void update(double ds) {
					{
						double d = newEntries/128.0;
						if (d < 1)
							d = 0;
						newEntries -= d*ds;
						newEntries = CLAMP.d(newEntries, 0, Double.MAX_VALUE);
					}
					timer+= ds;
					if (timer < TIME.secondsPerDay) { 
						return;
					}
					
					timer -= TIME.secondsPerDay;
					for (int ci = 0; ci < HCLASS.ALL.size(); ci++) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							double c = wrongful[ci][ri];
							double d = c*0.05;
							if (d < 1)
								d = 1;
							c -= d;
							c = CLAMP.d(c, 0, c);
							wrongful[ci][ri] = c;
						}
					}
				}
			});


			
		}
		
		public double newEntries() {
			return newEntries /128;
		}
		
		public LIST<PopData> leaves(){
			return deaths;
		}
		
		public LIST<PopData> enters(){
			return enters;
		}
		
		public void reg(Induvidual i, CAUSE_ARRIVE c){
			if (c != null) {
				arrive.set(i, c);
				enters.get(c.index()).inc(i);
				if (c.fromoutside && i.player())
					newEntries ++;
			}
		}
		
		public void reg(Induvidual i, CAUSE_LEAVE c){
			if (c != null) {
				deaths.get(c.index()).inc(i);
				if (!c.natural) {
					wrongful[i.clas().index()][i.race().index] += c.defaultStanding();
				}
			}
		}
		
		public static class PopData {

			private final HistoryRace[] data;
			private final INFO info;

			PopData(INFO info, Init init, boolean save) {
				this.info = info;
				data = new HistoryRace[HCLASS.ALL.size()+1];
				for (int i = 0; i < data.length; i++) {
					data[i] = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
				}
				
				init.savables.add(new SAVABLE() {
					
					@Override
					public void save(FilePutter file) {
						for (HistoryRace r : data)
							r.save(file);
					}
					
					@Override
					public void load(FileGetter file) throws IOException {
						if (save)
							for (HistoryRace r : data)
								r.load(file);
					}
					
					@Override
					public void clear() {
						for (HistoryRace r : data)
							r.clear();
					}
				});
				
			}
			
			public HISTORY_COLLECTION<Race> statistics(HCLASS c){
				if (c == null)
					return data[data.length-1];
				return data[c.index()];
			}
			
			void inc(Induvidual i) {
				if (i.player()) {
					data[data.length-1].inc(i.race(), 1);
				}
				data[i.clas().index()].inc(i.race(), 1);
			}
			
			public INFO info() {
				return info;
			}
			
		}
		

		

		
	}

	public static class PopType implements GETTER_TRANS<Induvidual, PopType.Type> {

		private final INT_OE<Induvidual> d;
		private final Type[] all;

		public final Type IMMIGRANT;
		public final Type NATIVE;
		public final Type FORMER_SLAVE;
		public final Type EX_CON;
		
		PopType(Init init){
			d = init.count.new DataNibble();
			IMMIGRANT = new Type(0, "IMMIGRANTS", init);
			NATIVE = new Type(1, "NATIVES", init);
			FORMER_SLAVE = new Type(2, "FORMER_SLAVES", init);
			EX_CON = new Type(3, "EX_CON", init);
			new StatBoosterStat(EX_CON, new BBoost(BOOSTABLES.BEHAVIOUR().LAWFULNESS, 2, true));
			all = new Type[] {
				IMMIGRANT,NATIVE,FORMER_SLAVE,EX_CON
			};
			
			
			init.addable.add(new Addable() {
				
				@Override
				public void removeH(Induvidual i) {
					if (i.clas().player && i.clas() != HCLASS.SLAVE)
						all[d.get(i)].data.inc(-1);
				}
				
				@Override
				public void addH(Induvidual i) {
					if (i.clas().player && i.clas() != HCLASS.SLAVE)
						all[d.get(i)].data.inc(1);
				}
			});
		}
		
		@Override
		public Type get(Induvidual f) {
			return all[d.get(f)];
		}
		
		public Type getByIndex(int in) {
			return all[in];
		}
		
		public class Type extends STAT.STATFacade {
			
			public final int index;
			private final HistoryInt data = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
			
			Type(int index, String key, Init init) {
				super(key, init);
				this.index = index;
				init.savables.add(data);
				//info().setInt();
			}

			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				
				double pop = STATS.POP().POP.data().get(null, daysBack);
				double type = data.get(daysBack);
				double non = STATS.POP().POP.data().get(null, daysBack)-type;

				if (non <= 0)
					return 0;
				double v = type/non;
				v = CLAMP.d(v, 0, 1);
				v *= non/pop;
				return v;
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return STATS.POP().POP.data().get(null, daysback)+data.get(daysback);
			}
			
			public void set(Induvidual f) {
				if (f.clas().player && f.clas() != HCLASS.SLAVE) {
					all[d.get(f)].data.inc(-1);
					d.set(f, index);
					data.inc(1);
				}else {
					d.set(f, index);
				}
			}


			
		}
		
		
	}
	
	private static class Friend implements GETTER_TRANSE<Induvidual, ENTITY> {

		private final INT_OE<Induvidual> i;
		
		Friend(Init init){
			i = init.count.new DataInt();
		}
		
		@Override
		public ENTITY get(Induvidual f) {
			int i = this.i.get(f);
			if (i == 0)
				return null;
			ENTITY e = SETT.ENTITIES().getByID(i-1);
			if (e == null || e.isRemoved())
				return null;
			return e;
		}

		@Override
		public void set(Induvidual f, ENTITY t) {
			if (t == null)
				i.set(f, 0);
			else
				i.set(f, t.id()+1);
		}
		
	}
	
	
}
