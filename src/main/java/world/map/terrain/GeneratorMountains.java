package world.map.terrain;

import static world.WORLD.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import world.WORLD;
import world.WorldGen;

class GeneratorMountains{

	
	private final int pen = WORLD.TAREA();
	
	GeneratorMountains(HeightMap height, WorldGen spec){

		for (COORDINATE c : TBOUNDS()) {
			if (height.get(c) > 0.75) {
				MOUNTAIN().placeRaw(c.x(), c.y());
			}else {
				MOUNTAIN().clear(c.x(), c.y());
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			RES.flooder().setValue2(c, 0);
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.MOUNTAIN().getHeight(c.x(), c.y()) == 0) {
				fill(c.x(), c.y());
				break;
			}else {
				
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.MOUNTAIN().getHeight(c.x(), c.y()) == 0 && RES.flooder().getValue2(c.x(), c.y()) == 0) {
				connect(c.x(), c.y());
				fill(c.x(), c.y());
			}
		}
	}
	
	private void fill(int sx, int sy) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(sx, sy, 0);
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			t.setValue2(1);
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (!WORLD.IN_BOUNDS(dx, dy))
					continue;
				if (WORLD.MOUNTAIN().getHeight(dx, dy) > 0)
					continue;
				if (RES.flooder().getValue2(dx, dy) == 1)
					continue;
				RES.flooder().pushSmaller(dx,dy, t.getValue()+d.tileDistance(), t);
			}
			
		}
		RES.flooder().done();
	}

	private void connect(int sx, int sy) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(sx, sy, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (t.getValue2() == 1) {
				RES.flooder().done();
				while(t != null) {
					MOUNTAIN().clear(t.x(), t.y());
					t = t.getParent();
				}
				return;
			}
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(t, d)) {
					double v = WORLD.MOUNTAIN().getHeight(t.x()+d.x(), t.y()+d.y()) > 0 ? pen : 1;
					RES.flooder().pushSmaller(t, d, t.getValue()+v*d.tileDistance(), t);
				}
			}
			
		}
		RES.flooder().done();
		
		
	}
	

	


}