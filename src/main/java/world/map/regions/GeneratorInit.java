package world.map.regions;

import static world.World.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

final class GeneratorInit {

	
	static boolean init(int sx, int sy) {
		RES.filler().init(GeneratorInit.class);
		RES.filler().filler.set(sx, sy);
		
		Region a = REGIONS().setter.get(sx, sy);
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		int area = 0;
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (!REGIONS().setter.is(c, a)) {
				continue;
			}
			area++;
			if (c.x() < x1)
				x1 = c.x();
			if (c.x() > x2)
				x2 = c.x();
			if (c.y()< y1)
				y1 = c.y();
			if (c.y() > y2)
				y2 = c.y();
			for (DIR d : DIR.ORTHO)
				if (TBOUNDS().holdsPoint(c, d))
					RES.filler().filler.set(c, d);
			
		}
		
		x2 += 1;
		y2 += 1;
		a.bounds.set(x1, x2, y1, y2);
		
		RES.filler().done();
		RES.flooder().init(GeneratorInit.class);
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (IN_BOUNDS(x, y) && REGIONS().setter.is(x, y, a)) {
					for (DIR d : DIR.ALL) {
						if (!REGIONS().setter.is(x, y, d, a)) {
							RES.flooder().pushSloppy(x, y, 0);
							break;
						}
					}
				}
				
			}
		}
		
		PathTile centre = null;
		PathTile backup = null;
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (REGIONS().setter.is(t, a) && CapitolPlacablity.region(t.x(), t.y()) == null) {
				centre = t;
			}else {
				backup = t;
			}
			
			for (DIR d : DIR.ALL) {
				if (REGIONS().setter.is(t, d, a))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		
		
		
		RES.flooder().done();
		
		int tz = 0;
		
		if (centre == null) {
			a.init(backup.x(), backup.y(), backup.x(), backup.y(), area, tz);
			return false;
		}
		
		
		if (centre.getValue() > 14)
			tz = 3;
		else if (centre.getValue() > 4)
			tz = 2;
		else if (centre.getValue() > 2)
			tz = 1;
		a.init(centre.x(), centre.y(), centre.x(), centre.y(), area, tz);
		return true;
	}
	
}
