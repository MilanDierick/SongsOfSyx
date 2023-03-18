package settlement.stats;

import init.D;
import init.boostable.BBoost;
import init.race.Race;
import settlement.entity.humanoid.*;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.stats.Init.Updatable;
import settlement.stats.STAT.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;

public class StatsAccess extends StatCollection {

	private final LIST<STAT> all;
	private static CharSequence ¤¤less = "¤Less exposure to {0}";
	private static CharSequence ¤¤more = "¤More exposure to {0}";
	
	public final StatsMonuments MONUMENTS;
	private static CharSequence ¤¤MonumentsD = "How many of this kind of monument there are divided by your total population.";
	private static CharSequence ¤¤Mless = "¤Less {0} Monuments.";
	private static CharSequence ¤¤Mmore = "¤More {0} monuments.";
	
	static {
		D.ts(StatsAccess.class);
	}

	StatsAccess(Init init) {
		super(init, "ACCESS");

		for (SettEnv e : SETT.ENV().environment.all()) {

			StatInfo info = new StatInfo(e.name,  e.names, e.desc);
			info.setOpinion(¤¤more, ¤¤less);
			STATData d = new STATData(e.key, init, init.count.new DataNibble(), info, e.standing);
			for (BBoost b : e.bonuses) {
				d.boosts.add(new StatBoosterStat(DicMisc.¤¤Access + ": " + e.name, d, b));
			}
		}

		init.updatable.add(updater);

		all = makeStats(init);
		MONUMENTS = new StatsMonuments(init);
	}

	private final Updatable updater = new Updatable() {

		private final ArrayList<SettEnv> alle = new ArrayList<>(SETT.ENV().environment.all());
		{
			alle.remove(SETT.ENV().environment.NOISE);
		}

		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {

			if (!HPoll.Handler.works(h) || STATS.WORK().EMPLOYED.get(h) == null
					|| !STATS.WORK().EMPLOYED.get(h).constructor().envValue(SETT.ENV().environment.NOISE)) {
				SettEnv e = SETT.ENV().environment.NOISE;
				accessCheck(h, all.get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
			}

			if (!SETT.ROOMS().map.is(h.physics.tileC())) {
				for (SettEnv e : alle) {
					accessCheck(h, all.get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
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

	@Override
	public LIST<STAT> all() {
		return all;
	}
	
	public static class StatsMonuments extends StatCollection {
		
		private final LIST<STAT> all;
		
		private StatsMonuments(Init init) {
			super(init, "MONUMENTS");
			
			for (ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS) {
				StatInfo info = new StatInfo(m.info.name, m.info.names, ¤¤MonumentsD);
				info.setOpinion(¤¤Mmore, ¤¤Mless);
				STATFacade s = new STAT.STATFacade(m.key, init, info, m.defaultStanding) {
					
					@Override
					double getDD(HCLASS s, Race r, int daysBack) {
						if (pdivider(null, null, daysBack) == 0)
							return m.area() > 0 ? 1 : 0;
						return (double)m.area()/pdivider(null, null, daysBack);
					}
				};
				s.info().setMatters(true, false);
				s.info().setInt();
			}

			all = makeStats(init);
		}

		@Override
		public LIST<STAT> all() {
			return all;
		}
		
		
	}

}
