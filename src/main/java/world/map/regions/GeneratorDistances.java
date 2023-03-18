package world.map.regions;

import static world.World.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayListShort;
import world.entity.WPathing;
import world.entity.caravan.Shipment;

final class GeneratorDistances {

	
	private final IntChecker neighCheck = new IntChecker(Regions.MAX);
	private final ArrayListShort neighTemp = new ArrayListShort(Regions.MAX*2);
	
	public GeneratorDistances() {
		
		for (Region r : REGIONS().all()) {
			if (r.area > 0)
				fix(r);
			
		}
		
		
	}

	void fix(Region r){

		neighCheck.init();
		neighTemp.clear();
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(r.cx(), r.cy(), 1);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (t.getValue() >= Shipment.MAX_DISTANCE)
				continue;
			Region r2 = REGIONS().getter.get(t);

	
			if (r2 != null && r2 != r) {
				if (neighCheck.isSet(r2.index()))
					continue;
				if (t.isSameAs(r2.cx(), r2.cy())) {
					neighTemp.add(r2.index());
					neighTemp.add((int) t.getValue());
					neighCheck.isSetAndSet(r2.index());
					continue;
				}
			}
			for (DIR d : DIR.ALL) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (IN_BOUNDS(dx, dy)) {
					if (r2 != r && r2 != null && !REGIONS().getter.is(dx, dy, r2))
						continue;
					double v = t.getValue() + d.tileDistance()*WPathing.cost.getCost(t.x(), t.y(), dx, dy);
					RES.flooder().pushSmaller(dx, dy, v);
				}
			}
		}
		
		short[] distances = new short[neighTemp.size()];
		for (int i = 0; i < distances.length; i++)
			distances[i] = (short) neighTemp.get(i);
		r.distances = distances;
		
		RES.flooder().done();
		
	}

	
}
