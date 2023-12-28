package settlement.tilemap.generator;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

class GeneratorWaterFin {



	GeneratorWaterFin(CapitolArea area, GeneratorUtil util) {

		addDepth(area, util);
		
	}
	
	private void addDepth(CapitolArea area, GeneratorUtil util) {
		RES.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (!TERRAIN().WATER.SHALLOW.is(c)) {
				RES.flooder().pushSloppy(c, 0);
				RES.flooder().setValue2(c, 1+ RND.rExpo()*50);
			}
		}
		
		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			double v2 = t.getValue();
			
			if (TERRAIN().WATER.SHALLOW.is(t)) {
				if (v2 <  4) {
					TERRAIN().NADA.placeRaw(t.x(), t.y());
				}else {
					double v = v2*(Math.pow(CLAMP.d(util.height.get(t), 0, 1), 1.7));
					if (v > 4)
						TERRAIN().WATER.DEEP.placeRaw(t.x(), t.y());
				}
				
			}
			
			for (DIR d : DIR.ALL) {
				
				int x = t.x() + d.x();
				int y = t.y() + d.y();
				if (IN_BOUNDS(x, y)) {
					double v = v2;
					if (v2 >= 4) {
						v += d.tileDistance();
					}else {
						v += d.tileDistance()*t.getValue2();
					}
					if (v > 0) {
						if (RES.flooder().pushSmaller(x, y, v) != null) {
							RES.flooder().setValue2(x, y, t.getValue2());
						}
					}
					
					
				}
				
			}
			
		}
		
		RES.flooder().done();
		
		//fixWays(area, util);
		
		SETT.TERRAIN().WATER.groundWater.clear();
		RES.flooder().init(this);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (TERRAIN().WATER.SHALLOW.is(c)) {
				RES.flooder().pushSloppy(c, 0);
			}
		}
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (t.getValue() > 8)
				break;
			SETT.TERRAIN().WATER.groundWater.set(t, true);
			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		RES.flooder().done();
	}




}
