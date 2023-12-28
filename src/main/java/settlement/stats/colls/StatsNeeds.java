package settlement.stats.colls;


import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import game.time.TIME;
import init.D;
import init.need.NEED;
import init.need.NEEDS;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.*;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.stat.*;
import settlement.stats.util.CAUSE_ARRIVE;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;
import view.sett.IDebugPanelSett;

public class StatsNeeds extends StatCollection{
	
	public final LIST<StatNeed> SNEEDS;
	public final LIST<STAT> OTHERS;
	public final STAT EXHASTION;
	public final StatDanger INJURIES;
	public final StatDanger EXPOSURE;
	public final INT_OE<Induvidual> DIRTINESS;
	
	public final StatsNeedsDisease disease;

	private static CharSequence ¤¤desc = "The current {0} need of a subject. As it increases, subjects will search out related services.";
	
	static {
		D.ts(StatsNeeds.class);
	}
	
	public StatsNeeds(StatsInit init){
		super(init, "NEEDS");
		
		
		INJURIES = new StatDanger("INJURIES", init, init.count.new DataByte());
		EXPOSURE = new StatDanger("EXPOSURE", init, init.count.new DataNibble());
		EXHASTION = new STATData("EXHAUSTION", init, init.count.new DataNibble1());
		OTHERS = new ArrayList<STAT>(INJURIES.COUNT, EXHASTION, EXPOSURE.COUNT);
		disease = new StatsNeedsDisease(init);
		DIRTINESS = init.count.new DataNibble();
		
		ArrayListGrower<StatNeed> all = new ArrayListGrower<>();
		
		for (NEED n : NEEDS.ALL()) {
			INT_OE<Induvidual> ii;
			if (n == NEEDS.TYPES().HUNGER) {
				ii = init.count.new DataByte(StatNeed.CHUNK*4) {
					
					@Override
					public void set(Induvidual i, int s) {
						super.set(i, s);
						STATS.FOOD().STARVATION.indu().set(i, s >= StatNeed.CHUNK*2 ? 1 : 0);
					};
				};
			}else {
				ii = init.count.new DataNibble1();
				
			}
			new StatNeed(all, n, init, ii);
		}
		SNEEDS = new ArrayList<StatNeed>(all);
		
		IDebugPanelSett.add("Cure insanity", new ACTION() {
			
			@Override
			public void exe() {
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (a.indu().hType() == HTYPE.DERANGED) {
							a.HTypeSet(HTYPE.SUBJECT, null, CAUSE_ARRIVE.CURED);
						}
						
					}
				}
			}
		});

		
		
		init.updatable.add(updater);
		
	}
	
	public double grime(Induvidual i) {
		return (DIRTINESS.get(i)>>1)/7.0;
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {
			
			Induvidual i = h.indu();
			

			if (INJURIES.count.get(h.indu()) != 0) {
				if (INJURIES.count.isMax(i)) {
					HumanoidResource.dead = h.lastLeaveCause() != null ? h.lastLeaveCause() : CAUSE_LEAVE.ACCIDENT;
					return;
				}else if (INJURIES.inDanger(i)) {
					INJURIES.count.inc(i, 4);
				}else {
					INJURIES.count.inc(i, (int) Math.ceil(1 + -0x0F * BOOSTABLES.PHYSICS().HEALTH.get(i)));
				}
			}
			
			if(disease.getter.get(i) == null && i.hType() != HTYPE.TOURIST) {
				for (StatNeed n : SNEEDS) {
					if (RND.rFloat() < n.need.rate.get(i))
						n.inc(i, 1);
					
//					if (((updateI)&0x0FF) < n.need.rate.get(i) * 0x0100) {
//						n.inc(i, 1);
//					}
					
				}
				
				if (((ui)&0x0FF) < BOOSTABLES.PHYSICS().SOILING.get(i) * 0x0100)
					DIRTINESS.inc(i, 1);
				if (SETT.TERRAIN().get(h.physics.tileC()).roofIs()) {
					if (SETT.ROOMS().map.is(h.physics.tileC())
							&& SETT.ROOMS().map.get(h.physics.tileC()).blueprint().makesDudesDirty())
						DIRTINESS.inc(i, 1);

				} else if (SETT.WEATHER().rain.getD() > 0 && !SETT.WEATHER().snow.rainIsSnow()) {
					DIRTINESS.inc(i, -1);
				}
				
			}
			
			if (day && i.clas() == HCLASS.CITIZEN && i.hType() != HTYPE.DERANGED) {
				if (DI*2.0*InsaneRate(i.race())/(1 + BOOSTABLES.BEHAVIOUR().SANITY.get(i)*20) > RND.rFloat()) {
					h.HTypeSet(HTYPE.DERANGED, CAUSE_LEAVE.OTHER, null);
				}
			}
			
			if (i.hType().player){
				Boostable b = BOOSTABLES.PHYSICS().RESISTANCE_COLD;
				double exposure = -SETT.WEATHER().temp.getEntityTemp();
				CAUSE_LEAVE cause = CAUSE_LEAVE.COLD;
				if (exposure < 0) {
					b = BOOSTABLES.PHYSICS().RESISTANCE_HOT;
					cause = CAUSE_LEAVE.HEAT;
					exposure = -exposure;
				}
				exposure *= CLAMP.d(2 - 3*SETT.FERTILITY().baseD.get(h.tc()), 1, 2);
				double protection = 1.0 - 1.0/(1.0+b.get(i)+(STATS.RAN().get(i, 19, 1)&0b1));
				if (protection > exposure) {
					EXPOSURE.count.inc(i, -4);
				}else {
					if (STATS.POP().pop(HTYPE.ENEMY) > 0) {
						;
					}else if (((TIME.days().bitsSinceStart() + STATS.RAN().get(i, 22, 2)) & 3) != 0 && (updateI & 3) != 0) {
						;
					}else if (SETT.TERRAIN().get(h.tc()).roofIs() && RND.rBoolean()) {
						;
					}else if (EXPOSURE.count.isMax(i)) {
						HumanoidResource.dead = cause;
						return;
					}else {
						int am = 1;
						EXPOSURE.count.inc(i, am);
						if (InsaneRate(i.race()) > 0 && ((STATS.RAN().get(i, 22, 4)) == 0) && i.hType() == HTYPE.SUBJECT && EXPOSURE.critical(i) ) {
							h.HTypeSet(HTYPE.DERANGED, cause, null);
						}

						
					}
				}
				
				
			}else {
				EXPOSURE.count.inc(i, -4);
			}
			
			disease.update16(h, updateI, day, updateI);
			
		}
		
		@Override
		public void init(Induvidual i) {
			if (RND.oneIn(8)) {
				DIRTINESS.incD(i, RND.rExpo());
			}
		};
		
		private final double DI = 1.0/(8*10);
		private double itime = -10;
		private final double[] insaneRate = new double[RACES.all().size()];
		
		private double InsaneRate(Race r) {
			if (TIME.currentSecond() - itime > 10) {
				double pop = STATS.POP().POP.data().get(null);
				if (pop < 500) {
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						int insane = STATS.POP().pop(race, HTYPE.DERANGED);
						if (insane > STATS.POP().POP.data(HCLASS.CITIZEN).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(RACES.clP(race, HCLASS.CITIZEN))*100)) {
							insaneRate[race.index()] = 0;
						}else {
							insaneRate[race.index()] = 0.001;
						}
					
					}
				}else {
					double popD = CLAMP.d((pop-500)/3000.0, 0, 1);
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						double v = popD;
						
						int insane = STATS.POP().pop(race, HTYPE.DERANGED);
						if (insane > STATS.POP().POP.data(HCLASS.CITIZEN).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(RACES.clP(race, HCLASS.CITIZEN))*15)) {
							v = 0;
						}
						insaneRate[r.index()] = v;
					}
				}
				
				itime = TIME.currentSecond();
			}
			return insaneRate[r.index()];
		}
		
	};
	
	public static final class StatDanger {
		
		public final STAT DANGER;
		public final STAT COUNT;
		public final INT_OE<Induvidual> count;
		
		private StatDanger(String key, StatsInit init, INT_OE<Induvidual> c) {
			
			count = new INT_OE<Induvidual>() {
				@Override
				public void set(Induvidual t, int s) {
					c.set(t, s);
					DANGER.indu().set(t, critical(t) ? 1 : 0);
				}

				@Override
				public int get(Induvidual t) {
					return c.get(t);
				}

				@Override
				public int min(Induvidual t) {
					return c.min(t);
				}

				@Override
				public int max(Induvidual t) {
					return c.max(t);
				}
			};
			
			COUNT = new STATData(key, init, count);
			DANGER = new STATData(null, init, init.count.new DataBit(), COUNT.info());
		}
		
		public boolean inDanger(Induvidual i) {
			return count.get(i) >= count.max(i)/2;
		}
		
		public boolean willDie(Induvidual i, double treatment) {
			
			if (inDanger(i)) {
				double chance = 1.0-treatment;
				chance = CLAMP.d(chance, 0, 1);
				int ran = (int) (chance*0x0FFFF);
				return ran >= STATS.RAN().get(i, 7, 16);
			}
			return false;
		}
		
		public void setNonDanger(Induvidual i) {
			if (inDanger(i))
				count.set(i, count.max(i)/2-1);
		}
		
		public boolean critical(Induvidual i) {
			return count.get(i) >= 3*(count.max(i)/4);
		}
		
	}
	

	public static class StatNeed implements INDEXED{
		
		private final int index;
		private final STAT stat;
		private static final int CHUNK = 0x010; 
		private final double pp = 1.0/CHUNK;
		public NEED need;
		
		StatNeed(LISTE<StatNeed> all, NEED ni, StatsInit init, INT_OE<Induvidual> ii) {
			this.index = all.add(this);
			
			stat = new STATData(ni.key, init, ii, new StatInfo(ni.nameNeed, new Str(¤¤desc).insert(0, ni.nameNeed)));
			need = ni;
		}
		
		public double getPrio(Induvidual h) {
			int i = stat.indu().get(h)-CHUNK;
			if (i > 0) {
				return CLAMP.d((i+1)*pp, 0, 1);
			}
			return 0;
		}
		
		public int iPrio(Induvidual h) {
			int i = stat.indu().get(h);
			if (i > CHUNK)
				return (int) (1 + 5*(i - CHUNK)*pp);
			return 0;
		}

		public void setPrio(Induvidual h, double prio) {
			int d = stat.indu().max(h)-CHUNK;
			stat.indu().set(h, (int) (CHUNK + Math.ceil(prio*d)));
		}
		
		public void fix(Induvidual h) {
			inc(h, -CHUNK);
		}
		
		public void fixMax(Induvidual h) {
			inc(h, -stat.indu().max(h));
		}

		private void inc(Induvidual h, int i) {
			stat.indu().inc(h, i);
		}

		@Override
		public int index() {
			return index;
		}
		
		public STAT stat() {
			return stat;
		}
		
		public int breakpoint() {
			return CHUNK;
		}
		
		protected void newValue(Induvidual i, int v) {
			
		}
		
	}
	
	public void initNeeds(Humanoid h) {
		for (StatNeed n : SNEEDS) {
			if (n.need.rate.get(h.indu()) > 0)
				n.stat.indu().set(h.indu(), StatNeed.CHUNK);
		}
	}

	
}
