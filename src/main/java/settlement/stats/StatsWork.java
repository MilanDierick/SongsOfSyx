package settlement.stats;

import init.D;
import init.boostable.*;
import init.race.*;
import settlement.army.Div;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.RoomEmploymentSimple;
import settlement.room.main.RoomInstance;
import settlement.stats.Init.Disposable;
import settlement.stats.Init.Updatable;
import settlement.stats.SETT_STATISTICS.SettStatistics;
import settlement.stats.STAT.STATData;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;

public class StatsWork extends StatCollection{
	
	public final STAT RETIREMENT_AGE;
	public final STAT RETIREMENT_HOME;
	private final STAT RETIREMENT_HOME_ACCESS;
	private final STAT RETIREMENT_HOME_QUALITY;
	private final STAT RETIREMENT_HOME_TYPE;
	public final StatObject<RoomInstance> EMPLOYED;
	public final STAT WORK_FULFILLMENT;
	public final STAT WORK_TIME;
	private final LIST<STAT> all;
	private final SettStatistics health;
	
	StatsWork(Init init){
		super(init, "WORK");
		D.gInit(this);
		
		EMPLOYED = new Work(init);
	
		

		
		
		WORK_TIME = new STAT.STATData("WORK_TIME", init, init.count.new DataNibble(Humanoid.WORK_TICKS));
		
		StatDecree dec = new StatDecree(init, 0, 100, D.g("RetirementT", "Retirement age target"), 1);
		
		RETIREMENT_AGE = new STAT.STATImp("RETIREMENT_AGE", init) {

			@Override
			int getDD(HCLASS s, Race r) {
				return (int) (dec.get(s).get(r)*pdivider(s, r, 0));
			}
			
			@Override
			public int dataDivider() {
				return 100;
			}
			
		};
		RETIREMENT_AGE.addDecree(dec);

		
		WORK_FULFILLMENT = new STAT.STATData("WORK_FULFILLMENT", init, init.count.new DataNibble()) {
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				if (c == HCLASS.SLAVE)
					return STATS.POP().pop(r, HTYPE.SLAVE, daysback);
				return STATS.POP().pop(r, HTYPE.STUDENT, daysback) + STATS.POP().pop(r, HTYPE.RECRUIT, daysback) + STATS.POP().pop(r, HTYPE.SUBJECT, daysback);
			}
		};
		
		RETIREMENT_HOME = new STAT.STATFacade("RETIREMENT", init) {
			
			@Override
			double getDD(HCLASS s, Race r, int daysBack) {
				
				if (r != null) {
					
					double am = STATS.POP().pop(r, HTYPE.RETIREE, daysBack);
					double retAge = RETIREMENT_AGE.data(s).getD(r, daysBack);
					if (am == 0) {
						for (ROOM_RESTHOME h : r.pref().resthomes) {
							if (h.emp.employable() > 0)
								return r.pref().getWork(h.employment())*retAge;
						}
						return 0;
					}
					
					double access = RETIREMENT_HOME_ACCESS.data(s).getD(r, daysBack);
					double quality = RETIREMENT_HOME_QUALITY.data(s).getD(r, daysBack);
					double type = RETIREMENT_HOME_TYPE.data(s).getD(r, daysBack);
					
					
					
					double res = access*(0.5+quality*0.5*type);
					return res;
				}else {
					double tot = 0;
					double am = 0;
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race rr = RACES.all().get(ri);
						double a = STATS.POP().pop(r, HTYPE.RETIREE, daysBack)+1;
						am += a;
						tot += getDD(s, rr, daysBack)*a;
					}
					return tot /am; 
				}
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return 1;
			}
		};
		
		RETIREMENT_HOME_ACCESS = new StatRet(init, init.count. new DataBit(), 
				D.g("Access"), D.g("AccessD", "Access to retirement activities."));
		RETIREMENT_HOME_QUALITY = new StatRet(init, init.count. new DataNibble(), 
				D.g("Quality"), D.g("QualityD", "Quality and Degrade of retirement activities."));
		RETIREMENT_HOME_TYPE = new StatRet(init, init.count. new DataNibble(), 
				D.g("Type"), D.g("TypeD", "Subjects will try and find the activity that suits them best. (Highest work fulfillment)"));
		
		{
			health = new SettStatistics(init, "", "");
			double m = 0;
			for (RoomEmploymentSimple s : SETT.ROOMS().employment.ALLS()) {
				m = Math.max(m, s.healthFactor);
			}
			final double I = 1.0/m;
			new BBooster.BBoosterImp(DicMisc.¤¤Employment, new BBoost(BOOSTABLES.PHYSICS().HEALTH, 1, true), true, true, false) {
				
				@Override
				public double pvalue(Div v) {
					return I;
				}
				
				@Override
				public double pvalue(HCLASS c, Race r) {
					
					double e = EMPLOYED.stat().data(c).get(r);
					double pop = STATS.POP().POP.data(c).get(r);
					if (e == 0)
						return I;
					if (pop == 0)
						return I;
					
					return (health.data(c).get(r)/(256.0) + (pop-e))/pop;
				}
				
				@Override
				public double pvalue(Induvidual v) {
					RoomInstance r = EMPLOYED.get(v);
					if (r != null)
						return r.blueprintI().employment().healthFactor;
					return I;
				}
			};
		}
		

		
		init.updatable.add(updater);
		
		all = makeStats(init);
	}
	
	private static class StatRet extends STAT.STATData {
		
		StatRet(Init init, INT_OE<Induvidual> data, CharSequence name, CharSequence desc){
			super(null, init, init.count. new DataBit(), 
					new StatInfo(name, desc), null);
			info().setMatters(false, true);
		}
		
		@Override
		public int pdivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().pop(r, HTYPE.RETIREE, daysback);
		}
		
	}
	
	private final Updatable updater = new Updatable() {
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int ui) {
			Induvidual i = h.indu();
		
			if (day) {				
				WORK_TIME.indu().set(i, 0);
				if (STATS.MULTIPLIERS().OVERTIME.markIs(h)) {
					STATS.MULTIPLIERS().OVERTIME.consume(h);
					WORK_TIME.indu().setD(i, 1.0);
				}else if (STATS.MULTIPLIERS().DAY_OFF.markIs(h)) {
					STATS.MULTIPLIERS().DAY_OFF.consume(h);
					WORK_TIME.indu().setD(i, 1.0);
				}
			}
			
			if (HPoll.Handler.works(h)) {
				WORK_TIME.indu().inc(i, 1);
			}
			
			if (h.indu().hType() == HTYPE.RETIREE && EMPLOYED.get(h) != null && EMPLOYED.get(h).blueprint() instanceof ROOM_RESTHOME) {
				double d = ((ROOM_RESTHOME)EMPLOYED.get(h).blueprint()).quality(EMPLOYED.get(h));
				RETIREMENT_HOME_QUALITY.indu().setD(h.indu(), d);
			}
			
		}
		
		@Override
		public void init(Induvidual h) {
			
		};
	};
	
	
	private final class Work extends StatObject<RoomInstance> implements Disposable{

		private final INT_OE<Induvidual> data; 
		private final STATData stat;
		
		
		Work(Init init) {
			super(D.g("Employment"), D.g("EmploymentD", "This subject's place of work."));
			data = init.count.new DataShort();

			
			INT_OE<Induvidual> b = new INT_OE<Induvidual>(){

				@Override
				public int get(Induvidual t) {
					return t.hType().works && data.get(t) != 0 ? 1:0;
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
					// TODO Auto-generated method stub
					
				}
				
			};
			stat = new STATData("EMPLOYED", init, b) {
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					return workforce(r, daysback);
				}
			};
			
			init.disposable.add(this);
			
		}

		@Override
		public RoomInstance get(Induvidual f) {
			int i = data.get(f);
			if (i > 0)
				return (RoomInstance) SETT.ROOMS().getByIndex(i-1);
			return null;
		}

		@Override
		public void dispose(Humanoid h) {
			set(h, null);
		}

		
		@Override
		public void set(Humanoid h, RoomInstance t) {
			
			HOME home = STATS.HOME().GETTER.get(h, this);
			STATS.HOME().GETTER.set(h, null);
			
			Induvidual f = h.indu();
			
			stat.removeH(f);
			if (get(f) != null) {
				health.inc(f.clas(), f.race(), (int) (-get(f).blueprintI().employment().healthFactor*256), -1);
				get(f).employees().fire(h);
			}
			if (t!= null) {
				data.set(f, t.index()+1);
				t.employees().employ(h);;
				health.inc(f.clas(), f.race(), (int) (get(f).blueprintI().employment().healthFactor*256), -1);
			}else {
				data.set(f, 0);
			}
			
			stat.addH(f);

			setData(f, t);
			if (home != null) {
				
				if (home.availability() != null && home.availability().isValid(h)) {
					if (get(f) != null || home.occupants() < home.occupantsMax())
						STATS.HOME().GETTER.set(h, home);
				}
				home.done();
			}
			
			
			
		}
		
		private void setData(Induvidual f, RoomInstance t) {
			WORK_FULFILLMENT.indu().setD(f, 0);
			RETIREMENT_HOME_ACCESS.indu().setD(f, 0);
			RETIREMENT_HOME_QUALITY.indu().setD(f, 0);
			RETIREMENT_HOME_TYPE.indu().setD(f, 0);
			if (t == null)
				return;
			
			if (f.clas() == HCLASS.SLAVE || f.hType() == HTYPE.STUDENT ||  f.hType() == HTYPE.RECRUIT || f.hType() == HTYPE.SUBJECT) {
				double d = f.race().pref().getWork(t.blueprintI().employment());
				WORK_FULFILLMENT.indu().setD(f, d);
				
			}else if(f.hType() == HTYPE.RETIREE && t.blueprint() instanceof ROOM_RESTHOME) {
				RETIREMENT_HOME_ACCESS.indu().setD(f, 1);
				RETIREMENT_HOME_QUALITY.indu().setD(f, ((ROOM_RESTHOME)t.blueprint()).quality(t));
				RETIREMENT_HOME_TYPE.indu().setD(f, f.race().pref().getWork(t.blueprintI().employment()));
			}
			
		}

		@Override
		public STAT stat() {
			return stat;
		}
		
	}
	
	public int workforce() {
		return (STATS.POP().pop(HTYPE.SUBJECT) + STATS.POP().pop(HTYPE.SLAVE));
	}
	
	public int workforce(Race race, int daysBack) {
		return (STATS.POP().pop(race, HTYPE.SUBJECT, daysBack) + STATS.POP().pop(race, HTYPE.SLAVE, daysBack));
	}
	
	public int workforce(Race race) {
		return (STATS.POP().pop(race, HTYPE.SUBJECT) + STATS.POP().pop(race, HTYPE.SLAVE));
	}
	
	public int workforce(EGROUP g) {
		return STATS.POP().pop(g.r, g.t);
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}
	
}
