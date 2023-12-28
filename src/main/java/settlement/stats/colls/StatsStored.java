package settlement.stats.colls;

import static settlement.main.SETT.*;

import init.D;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.StatsInit;
import settlement.stats.stat.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class StatsStored extends StatCollection{

	private final CharSequence ¤¤desc = "¤The amount of resources stored in warehouses per capita";
	private final CharSequence ¤¤more = "¤{0}, we need more of it.";
	
	public StatsStored(StatsInit init){
		super(init, "STORED");
		D.t(this);
		
		for (RESOURCE res : RESOURCES.ALL()) {
			
			StatInfo info = new StatInfo(res.name, res.names, ¤¤desc);
			info.setOpinion(¤¤more, null);
			
			STATFacade s = new STATFacade(res.key, init, info) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					if (pdivider(null, null, daysBack) == 0)
						return ROOMS().STOCKPILE.tally().amountsDay().get(res.bIndex()).get(daysBack) > 0 ? 1 : 0;
					return (double)ROOMS().STOCKPILE.tally().amountsDay().get(res.bIndex()).get(daysBack)/pdivider(null, null, daysBack);
				}
			};
			s.info().setMatters(true, false);
			s.info().setInt();
			
		}

		
	}

	public LIST<STAT> createTheOnesThatMatter(HCLASS cl){
		ArrayList<STAT> res = new ArrayList<>(all().size());
		
		for (STAT s : all()) {
			boolean added = false;
			for (Race r : RACES.all()) {
				if (!added && s.standing().max(cl, r) > 0) {
					res.add(s);
					added = true;
				}
				
			}
			
		}
		
		return res;
		
	}
	

	
}
