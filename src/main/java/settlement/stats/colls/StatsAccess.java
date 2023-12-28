package settlement.stats.colls;

import game.boosting.*;
import init.D;
import init.race.Race;
import settlement.entity.humanoid.*;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.stats.*;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.*;
import settlement.stats.util.StatBooster;
import settlement.stats.util.StatBooster.StatBoosterStat;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;

public class StatsAccess extends StatCollection {

	private static CharSequence ¤¤less = "¤Less exposure to {0}";
	private static CharSequence ¤¤more = "¤More exposure to {0}";
	
	public final StatsMonuments MONUMENTS;
	public final StatsA ACCESS;
	private static CharSequence ¤¤MonumentsD = "How many of this kind of monument there are divided by your total population.";
	private static CharSequence ¤¤Mless = "¤Less {0} Monuments.";
	private static CharSequence ¤¤Mmore = "¤More {0} monuments.";
	public final Bench bench;
	
	static {
		D.ts(StatsAccess.class);
	}

	public StatsAccess(StatsInit init) {
		super(init, "OTHER");
		ACCESS = new StatsA(init);
		
		
		init.coll = this;
		bench = new Bench(init);
		MONUMENTS = new StatsMonuments(init);
	}



	public static class StatsA extends StatCollection {

		
		private StatsA(StatsInit init) {
			super(init, "ACCESS");
			for (SettEnv e : SETT.ENV().environment.all()) {

				StatInfo info = new StatInfo(e.name,  e.names, e.desc);
				info.setOpinion(¤¤more, ¤¤less);
				STATData d = new STATData(e.key, init, init.count.new DataNibble(), info);
				d.standing = new StatStanding(d, 0, e.standing);
				
				ACTION ac = new ACTION() {
					
					@Override
					public void exe() {
						for (BoostSpec sp : e.bonuses.all()) {
							
							StatBooster vv = new StatBoosterStat(d, true);
							Booster bo = new BoosterWrap(vv, sp.booster.info, sp.booster.to(), sp.booster.isMul);
							d.boosters.push(bo, sp.boostable);
							
						}
					}
				};
				BOOSTING.connecter(ac);
				
			}
			init.updatable.add(updater);
		}

		private final StatUpdatableI updater = new StatUpdatableI() {

			private final ArrayList<SettEnv> alle = new ArrayList<>(SETT.ENV().environment.all());
			{
				alle.remove(SETT.ENV().environment.NOISE);
			}

			@Override
			public void update16(Humanoid h, int updateI, boolean day, int ui) {

				if (!HPoll.Handler.works(h) || STATS.WORK().EMPLOYED.get(h) == null
						|| !STATS.WORK().EMPLOYED.get(h).constructor().envValue(SETT.ENV().environment.NOISE)) {
					SettEnv e = SETT.ENV().environment.NOISE;
					accessCheck(h, all().get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
				}

				if (!SETT.ROOMS().map.is(h.physics.tileC())) {
					for (SettEnv e : alle) {
						accessCheck(h, all().get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
					}
				}

			}

			private void accessCheck(Humanoid h, INT_OE<Induvidual> data, double value, double deg) {
				Induvidual i = h.indu();
				int v = (int) Math.ceil(value);
				if (v > data.get(i) * 2)
					data.inc(i, 2);
				if (v > data.get(i))
					data.inc(i, 1);
				else if (v < data.get(i) && RND.oneIn(8 * (1 / deg))) {
					data.inc(i, -1);
				}
			}

			@Override
			public void init(Induvidual i) {

			};

		};
	}
	
	public static class StatsMonuments extends StatCollection {

		
		private StatsMonuments(StatsInit init) {
			super(init, "MONUMENTS");
			
			for (ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS) {
				StatInfo info = new StatInfo(m.info.name, m.info.names, ¤¤MonumentsD);
				info.setOpinion(¤¤Mmore, ¤¤Mless);
				STATFacade s = new STATFacade(m.key, init, info) {
					
					@Override
					protected double getDD(HCLASS s, Race r, int daysBack) {
						if (pdivider(null, null, daysBack) == 0)
							return m.area() > 0 ? 1 : 0;
						return (double)m.area()/pdivider(null, null, daysBack);
					}
				};
				s.standing = new StatStanding(s, 0, m.defaultStanding);
				s.info().setMatters(true, false);
				s.info().setInt();
			}

		}

		
	}
	
	public static final class Bench {
		
		public final STAT access;
		public final STAT quality;
		public final STAT total;
		private Bench(StatsInit init){
			access = new STATData(null, init, init.count. new DataBit());
			quality = new STATData(null, init, init.count. new DataCrumb());
			access.info().setMatters(false, false);
			quality.info().setMatters(false, false);
			INT_OE<Induvidual> ii = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return access.indu().get(t)*(1+quality.indu().get(t));
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 4;
				}

				@Override
				public void set(Induvidual t, int i) {
					
				}
			
			};
			
			StatInfo info = new StatInfo(SETT.ROOMS().BENCH.info.name, DicMisc.¤¤Access + ": " + SETT.ROOMS().BENCH.info.name);
			
			total = new STATFacade("BENCH", init, ii, info) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					return access.data(s).getD(r, daysBack)*(1+quality.data(s).getD(r, daysBack));
				}
			};
		}
		
	}

}
