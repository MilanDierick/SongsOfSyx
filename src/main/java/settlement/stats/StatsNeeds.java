package settlement.stats;


import game.time.TIME;
import init.boostable.*;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.Init.Updatable;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.INT_O.INT_OE;
import view.sett.IDebugPanelSett;

public class StatsNeeds extends StatCollection{
	
	public final StatNeed DIRTINESS;
	public final StatNeed HUNGER;
	
	public final StatNeed THIRST;
	public final StatNeed CONSTIPATION;
	
	public final StatNeed RELIGION;
	public final StatNeed MEDICAL;
	
	public final StatNeed GROOMING;
	
	public final LIST<StatNeed> NEEDS;
	public final LIST<STAT> OTHERS;
	
	public final STAT EXHASTION;
	
	public final StatDanger INJURIES;
	public final StatDanger EXPOSURE;
	public final StatsNeedsDisease disease;
	
	private final LIST<STAT> all;
	
	StatsNeeds(Init init){
		super(init, "NEEDS");
		LinkedList<StatNeed> all = new LinkedList<>();
		DIRTINESS = new StatNeed("DIRTINESS", all, BOOSTABLES.RATES().SOILING, 4, init);
		
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
		
		INJURIES = new StatDanger("INJURIES", init, init.count.new DataByte());
		EXPOSURE = new StatDanger("EXPOSURE", init, init.count.new DataNibble());
		
		
		
		EXHASTION = new STAT.STATData("EXHAUSTION", init, init.count.new DataNibble1());
		
//		new StatsBoosts.StatBoosterStat(init, EXHASTION, new BoostableBoost(BOOSTABLES.BATTLE().OFFENCE, -0.4));
//		new StatsBoosts.StatBoosterStat(init, EXHASTION, new BoostableBoost(BOOSTABLES.BATTLE().DEFENCE, -0.4));
		new StatBoosterStat(EXHASTION, new BBoost(BOOSTABLES.PHYSICS().SPEED, 0.25, true));
		
		CONSTIPATION = new StatNeed("CONSTIPATION", all, BOOSTABLES.RATES().DEFECATION, 1, init);
		HUNGER = new StatNeed("HUNGER", all, BOOSTABLES.RATES().HUNGER, 3, init);
		THIRST = new StatNeed("THIRST", all, BOOSTABLES.RATES().THIRST, 1, init);
		RELIGION = new StatNeed("RELIGION", all, BOOSTABLES.RATES().PIETY, 1, init);
		MEDICAL = new StatNeed("MEDICAL", all, BOOSTABLES.RATES().DOCTOR, 1, init);
		GROOMING = new StatNeed("GROOMING", all, BOOSTABLES.RATES().GROOMING, 1, init);
		NEEDS = new ArrayList<StatNeed>(all);
		
		OTHERS = new ArrayList<STAT>(INJURIES.COUNT, EXHASTION, EXPOSURE.COUNT);
		init.updatable.add(updater);
		disease = new StatsNeedsDisease(init);
		
		
		
		this.all = makeStats(init);
	}
	
	private final Updatable updater = new Updatable() {
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {
			
			Induvidual i = h.indu();
			if(disease.getter.get(i) == null && i.hType() != HTYPE.TOURIST) {
				for (StatNeed n : NEEDS) {
					if (((updateI+n.index)&0x0FF) < n.rate.get(i) * 0x0100) {
						n.inc(i, 1);
						if (n == DIRTINESS) {
							if (SETT.TERRAIN().get(h.physics.tileC()).roofIs()) {
								if (SETT.ROOMS().map.is(h.physics.tileC())
										&& SETT.ROOMS().map.get(h.physics.tileC()).blueprint().makesDudesDirty())
									DIRTINESS.inc(i, 1);

							} else if (SETT.WEATHER().rain.getD() > 0 && !SETT.WEATHER().snow.rainIsSnow()) {
								DIRTINESS.inc(i, -1);
							}
						}
					}
					
				}
			}

			
			if (INJURIES.count.get(h.indu()) != 0) {
				if (INJURIES.count.isMax(i)) {
					HumanoidResource.dead = h.lastLeaveCause() != null ? h.lastLeaveCause() : CAUSE_LEAVE.ACCIDENT;
				}else if (INJURIES.critical(i)) {
					INJURIES.count.inc(i, 4);
				}else {
					INJURIES.count.inc(i, (int) (-0x0F * BOOSTABLES.PHYSICS().HEALTH.get(i)));
				}
			}
			
			if (day && i.clas() == HCLASS.CITIZEN && i.hType() != HTYPE.DERANGED) {
				
			
				if (DI*2.0*InsaneRate(i.race())/(1 + BOOSTABLES.BEHAVIOUR().SANITY.get(i)*20) > RND.rFloat()) {
					h.HTypeSet(HTYPE.DERANGED, CAUSE_LEAVE.OTHER, null);
				}
				

			}
			
			if (i.hType().player){
				BOOSTABLE b = BOOSTABLES.PHYSICS().RESISTANCE_COLD;
				double exposure = -SETT.WEATHER().temp.getEntityTemp();
				CAUSE_LEAVE cause = CAUSE_LEAVE.COLD;
				if (exposure < 0) {
					b = BOOSTABLES.PHYSICS().RESISTANCE_HOT;
					cause = CAUSE_LEAVE.HEAT;
					exposure = -exposure;
				}
				double protection = 1.0 - 1.0/(1.0+b.get(i)+(i.randomness2&0b1));
				if (protection > exposure) {
					EXPOSURE.count.inc(i, -4);
				}else {
					if (((TIME.days().bitsSinceStart() + i.randomness) & 3) != 0 && (updateI & 3) != 0) {
						;
					}else if (SETT.TERRAIN().get(h.tc()).roofIs() && RND.rBoolean()) {
						;
					}else if (EXPOSURE.count.isMax(i)) {
						HumanoidResource.dead = cause;
					}else {
						int am = 1 + RND.rInt(2);
						EXPOSURE.count.inc(i, am);
						if (((i.randomness & 0x0F) == 0) && i.hType() == HTYPE.SUBJECT && EXPOSURE.critical(i) ) {
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
				DIRTINESS.stat.indu().incD(i, RND.rExpo());
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
						if (insane > STATS.POP().POP.data(HCLASS.CITIZEN).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(HCLASS.CITIZEN, race)*15)) {
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
						if (insane > STATS.POP().POP.data(HCLASS.CITIZEN).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(HCLASS.CITIZEN, race)*15)) {
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
		
		private StatDanger(String key, Init init, INT_OE<Induvidual> c) {
			
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
			
			COUNT = new STAT.STATData(key, init, count);
			DANGER = new STAT.STATData(null, init, init.count.new DataBit(), COUNT.info(), null);
		}
		
		public boolean inDanger(Induvidual i) {
			return count.get(i) >= count.max(i)/2;
		}
		
		public boolean critical(Induvidual i) {
			return count.get(i) >= 3*(count.max(i)/4);
		}
		
	}
	
	public static class StatNeed implements INDEXED{
		
		private final int index;
		private final STAT stat;
		private final double pp = 1.0/0x010;
		public final BOOSTABLE rate;
		private static final int breakPoint = 15;
		
		StatNeed(String key, LISTE<StatNeed> all, BOOSTABLE p, int size, Init init) {
			this.index = all.add(this);
			stat = new STAT.STATData(key, init, init.count.new DataByte(0x010*(size+1)));
			rate = p;
		}
		
		public double getPrio(Induvidual h) {
			int i = stat.indu().get(h)-breakPoint;
			if (i > 0) {
				return CLAMP.d((i+1)*pp, 0, 1);
			}
			return 0;
		}

		public void setPrio(Induvidual h, double prio) {
			int d = stat.indu().max(h)-breakPoint;
			stat.indu().set(h, (int) (breakPoint + Math.ceil(prio*d)));
		}
		
		public void fix(Induvidual h) {
			inc(h, -0x010);
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
			return breakPoint;
		}
		
	}
	
	public void initNeeds(Humanoid h) {
		for (StatNeed n : NEEDS) {
			if (n.rate.get(h) > 0)
				n.stat.indu().set(h.indu(), StatNeed.breakPoint);
		}
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}

	
}
