package settlement.stats.colls;

import init.biomes.BUILDING_PREFS;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.stats.*;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;

public class StatsEnv extends StatCollection{
	

	public final STAT BUILDING_PREF;
	public final STAT CLIMATE;
	public final STAT OTHERS;
	public final STAT ROUNDNESS;
	public final STAT CANNIBALISM;
	public final STAT UNBURRIED;

	public final STAT ACCESS_ROAD;
	public final STAT SKINNY_DIP;
	
	
	public StatsEnv(StatsInit init){
		super(init, "ENVIRONMENT");
		
		ACCESS_ROAD = new STATData("ACCESS_ROAD", init, init.count.new DataNibble());
		SKINNY_DIP = new STATData("SKINNY_DIP", init, init.count.new DataBit());
		BUILDING_PREF = new STATData("BUILDING_PREF", init, init.count.new DataNibble());
		
		CLIMATE = new STATFacade("CLIMATE", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				if (r == null) {
					double m = 0;
					for (Race rr : RACES.all()) {
						m += rr.population().climate(SETT.ENV().climate())*STATS.POP().POP.data(s).get(rr);
					}
					double p = STATS.POP().POP.data(s).get(null);
					if (p == 0)
						return m > 0 ? 1 : 0;
					return m/p;
				}
				return r.population().climate(SETT.ENV().climate());
			}
		};
		CLIMATE.standing = new StatStanding(CLIMATE, 1.0);
		CLIMATE.info().setMatters(true, false);
		
		OTHERS = new STATFacade("OTHERS", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				if (r == null) {
					double p = 0;
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						p += getDD(s, RACES.all().get(ri), daysBack)*STATS.POP().POP.data(s).get(RACES.all().get(ri), daysBack);
					}
					if (p == 0)
						return 0;
					return p/STATS.POP().POP.data(s).get(null, daysBack);
				}
				
				double pop = STATS.POP().POP.data(s).get(r, daysBack);
				if (pop == 0)
					return 1.0;
				pop = 0;
				double tot = 0;
				for (Race rr : RACES.all()) {
					double p = STATS.POP().POP.data(s).get(rr, daysBack);
					pop += p;
					tot += p*r.pref().race(rr);
				}
				if (pop == 0)
					return 1;
				tot /= pop;
				return CLAMP.d(tot, 0, 1);
			}
		};
		OTHERS.standing = new StatStanding(OTHERS, 1.0);
		OTHERS.info().setMatters(true, false);
		
		if (false)
			; // remove this
		ROUNDNESS = new STATData(null, init, init.count.new DataBit());
		
		CANNIBALISM = new STATFacade("CANNIBALISM", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				return SETT.ROOMS().CANNIBAL.cannHistory().getD(daysBack);
			}
		};
		CANNIBALISM.info().setMatters(true, false);
		
		UNBURRIED = new STATFacade("UNBURRIED", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double pop = 1.0 + STATS.POP().POP.data(null).get(null, daysBack);
				return 40*SETT.THINGS().corpses.addedHistory.get(daysBack)/pop; 
			}
			
			
		};
		UNBURRIED.info().setInt();
		UNBURRIED.info().setMatters(true, false);	

		init.updatable.add(updater);
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {
			
			Induvidual i = h.indu();
			
			{
				double res = h.race().pref().structure(BUILDING_PREFS.get(h.tc().x(), h.tc().y()));
				for (DIR d : DIR.ORTHO) {
					res += h.race().pref().structure(BUILDING_PREFS.get(h.tc().x()+d.x(), h.tc().y()+d.y()));
				}
				res /= 5;
				
				int d = (int) Math.ceil((0x0F*res));
				int n = BUILDING_PREF.indu().get(h.indu());
				
				if (d > n*2) {
					BUILDING_PREF.indu().inc(i, 2);
				}else if (d > n) {
					BUILDING_PREF.indu().inc(i, 1);
				}else if(d < n && (updateI&0x07) == 0) {
					BUILDING_PREF.indu().inc(i, -1);
				}	
			}
			
			Room r = SETT.ROOMS().map.get(h.physics.tileC());
			
			if (r == null) {
				accessCheck(h, ACCESS_ROAD.indu(), SETT.FLOOR().walkValue.get(h.physics.tileC()) * ACCESS_ROAD.indu().max(h.indu()));
			}
//			
//			if (SETT.ENV().environment.ROUNDNESS.area().get(h.physics.tileC()) > 0) {
//				ROUNDNESS.indu().set(i, SETT.ENV().environment.ROUNDNESS.get(h.physics.tileC()) > 0 ? 1 : 0);	
//			}
			
		}
		
	
		
		private void accessCheck(Humanoid h, INT_OE<Induvidual> data, double value) {
			if (!SETT.ROOMS().map.is(h.physics.tileC())) {
				Induvidual i = h.indu();
				int v = (int) Math.ceil(value);
				if (v > data.get(i)*2)
					data.inc(i, 2);
				if (v > data.get(i))
					data.inc(i, 1);
				else if (v < data.get(i) && RND.oneIn(3)) {
					data.inc(i, -1);
				}
			}
		}
		
		@Override
		public void init(Induvidual i) {
			
		};
		
	};

	
}
