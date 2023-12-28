package world.regions.map;

import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.regions.Region;
import world.regions.centre.WorldCentrePlacablity;

final class GeneratorCenter {

	
	public GeneratorCenter() {
		
	}


	
	static void init(Region a) {
		if (a == null)
			return;
		if (a.info.area() == 0)
			return;
	
		if (WorldCentrePlacablity.regionC(a.info.cx(), a.info.cy()) != null)
			LOG.ln(a + " " + WorldCentrePlacablity.regionC(a.info.cx(), a.info.cy()));
		
		int bx = -1;
		int by = -1;
		double bv = 0;
		
		for (COORDINATE c : a.info.bounds()) {
			
			double v = value(c.x(), c.y(), a)*RND.rFloat();

			if (v > bv) {
				bv = v;
				bx = c.x();
				by = c.y();
			}
		}
		
		if (bv > 0) {
			a.info.centreSet(bx, by);
		}
		
	}
	
	private static double value(int tx, int ty, Region r) {
		if (!test(tx, ty, r))
			return 0;
		
		double v = 1;
		for (DIR d : DIR.ORTHO) {
			if (WORLD.WATER().has.is(tx, ty, d)) {
				v += 0.1;
			}
			if (WORLD.MOUNTAIN().haser.is(tx, ty, d))
				v += 0.1;
		}
		v *= WORLD.FERTILITY().map.get(tx, ty)*4;
		return v;
	}
	
	private static boolean test(int tx, int ty, Region r) {
		
		if (r.index() == 0)
			return true;
		
		if (!WORLD.REGIONS().map.is(tx,ty,r))
			return false;
		if (WorldCentrePlacablity.regionC(tx, ty) != null)
			return false;
		return true;
		
	}
	
}
