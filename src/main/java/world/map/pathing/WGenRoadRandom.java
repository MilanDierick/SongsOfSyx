package world.map.pathing;

import static world.WORLD.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import world.WORLD;
import world.regions.Region;

final class WGenRoadRandom {

	private final WGenUtil wmap;
	
	
	public WGenRoadRandom(WGenUtil util) {
		
		this.wmap = util;
		
		for (Region r : REGIONS().all()) {
			if (r.info.area() > 0) {
				randomRoad(r);
			}
		}

		
	}

	private void randomRoad(Region r) {

		RES.flooder().init(this);
		RES.flooder().pushSloppy(r.info.cx(), r.info.cy(), 0);
		
		int amount = 100;

		while (RES.flooder().hasMore() && amount > 0) {
			PathTile t = RES.flooder().pollSmallest();

			if (r != REGIONS().map.get(t)) {
				continue;
			}

			for (DIR d : DIR.ALL) {
				if (WORLD.REGIONS().map.get(t, d) != r)
					continue;
			}
			
			if (!WORLD.ROADS().is(t)) {
				WORLD.ROADS().ROAD.set(t);
				WORLD.ROADS().minified.set(t, true);
				amount--;
			}
			
			
			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (IN_BOUNDS(dx, dy)) {
					
					if (WORLD.ROADS().is(dx, dy)) {
						RES.flooder().pushSmaller(dx, dy, 0);
					}
					double v = 1;
					if (v >= 0 && wmap.polly.isEdge(dx, dy) && (!WATER().isBig.is(dx, dy))) {
						RES.flooder().pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
							
					}
				}
			}

		}

		RES.flooder().done();

	}
	

}
