package settlement.stats;

import game.faction.FACTIONS;
import game.tourism.TOURISM;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.stats.STAT.STATImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;

public class StatsGovern extends StatCollection{
	
	public final STAT tourismFriend;
	public final STAT tourismEnemy;
	public final STAT RICHES;
	
	private final LIST<STAT> all;
	
	StatsGovern(Init init){
		super(init, "GOVERN");
		
		tourismFriend = new STAT.STATImp("TOURISM_FRIEND", init) {
			
			@Override
			int getDD(HCLASS s, Race r) {
				double m = 1.0/(TOURISM.AMOUNT*16);
				double res = 0;
				for (Race other : RACES.all()) {
					res += STATS.POP().pop(other, HTYPE.TOURIST, 0)*m*r.pref().other(other);
				};
				return (int) (res*pdivider(s, r, 0));
			}
		};
		
		tourismEnemy = new STAT.STATImp("TOURISM_ENEMY", init) {
			
			@Override
			int getDD(HCLASS s, Race r) {
				double m = 1.0/(TOURISM.AMOUNT*16);
				double res = 0;
				for (Race other : RACES.all()) {
					res += STATS.POP().pop(other, HTYPE.TOURIST, 0)*m*(1.0-r.pref().other(other));
				};
				return (int) (res*pdivider(s, r, 0));
			}
		};
		
		RICHES = new STATImp("RICHES", init) {

			@Override
			int getDD(HCLASS s, Race r) {
				double d = FACTIONS.player().credits().credits()/500.0;
				d /= STATS.POP().POP.data(HCLASS.CITIZEN).get(null) + STATS.POP().POP.data(HCLASS.NOBLE).get(null);
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
			
		};
		RICHES.info().setMatters(true, false);
		
		all = makeStats(init);
	
		
	}

	@Override
	public LIST<STAT> all() {
		return all;
	}
	
	
	
}
