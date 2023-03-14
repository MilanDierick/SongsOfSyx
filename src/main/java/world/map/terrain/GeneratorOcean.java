package world.map.terrain;

import static world.World.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;

class GeneratorOcean {

	private double limit = 0.5;
	
	GeneratorOcean(HeightMap height) {
		
		int size = TWIDTH()*THEIGHT();
		size = size/8 + RND.rInt(size/4);
		int max = (int) (Math.sqrt(size)*2);
		
		COORDINATE c = getStart(height);
		if (c == null)
			return;
		
		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(c, 1.0);
		
		while(RES.flooder().hasMore() && size-- > 0) {
			PathTile t = RES.flooder().pollSmallest();
			double h = height.get(t.x(), t.y());
			double v = t.getValue() + 1;
			if (h > limit) {
				continue;
			}
			if (!WATER().fertile.is(t.x(), t.y()) && MOUNTAIN().is(t.x(), t.y()))
				continue;
			if (h < limit*0.8)
				WATER().OCEAN.deep.placeRaw(t.x(), t.y());
			else
				WATER().OCEAN.normal.placeRaw(t.x(), t.y());
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!TBOUNDS().holdsPoint(t, d))
					continue;
				RES.flooder().pushSmaller(t.x(), t.y(), d, v);
			}
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			double h = height.get(t.x(), t.y());
			if (h > limit) {
				continue;
			}
			
			double v = t.getValue();
			
			if (!WATER().fertile.is(t.x(), t.y())) {
				if (v > max) {
					if (h > limit + (v-max)/20.0)
						continue;
				}
					
			}else {
				v = max;
			}
			
			if (MOUNTAIN().is(t.x(), t.y()))
				continue;
			
			if (h < limit*0.8)
				WATER().OCEAN.deep.placeRaw(t.x(), t.y());
			else
				WATER().OCEAN.normal.placeRaw(t.x(), t.y());
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!TBOUNDS().holdsPoint(t, d))
					continue;
				RES.flooder().pushSmaller(t.x(), t.y(), d, t.getValue()+d.tileDistance());
			}
			
			
		}
		
		RES.flooder().done();
		
	}

	private COORDINATE getStart(HeightMap height) {
		
		for (int i = 0; i < 50000; i++) {
			int tx = RND.rInt(TWIDTH());
			int ty = RND.rInt(THEIGHT());
			if (!MOUNTAIN().is(tx, ty) && height.get(tx, ty) <= limit*0.5)
				return new Coo(tx, ty);
		}
		return null;
		
	}
	
}
