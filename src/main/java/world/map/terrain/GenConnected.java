package world.map.terrain;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.sets.Bitmap2D;
import world.WORLD;
import world.map.pathing.WTRAV;
import world.regions.centre.WorldCentrePlacablity;

final class GenConnected extends Bitmap2D{

	private final Bitmap2D tested = new Bitmap2D(WORLD.TBOUNDS(), false);
	
	public GenConnected() {
		super(WORLD.TBOUNDS(), false);
		Rec tBound = new Rec(WORLD.TBOUNDS());
		
		boolean hasOne = false;
		for (COORDINATE c : tBound) {
			if (WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
				fill(c.x(), c.y());
				hasOne = true;
				break;
			}
		}
		
		if (!hasOne) {
			RES.flooder().init(this);
			PathTile t = RES.flooder().pushSloppy(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2, 0);
			t = RES.flooder().pushSmaller(WORLD.TWIDTH()/2+1, WORLD.THEIGHT()/2+1, 0, t);
			RES.flooder().done();

			while(t != null) {
				WORLD.MOUNTAIN().clear(t.x(), t.y());
				WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
				t = t.getParent();
			}
			fill(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2);
			if (!tested.is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2)) {
				throw new RuntimeException("WTF " + WORLD.MOUNTAIN().is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2) + " " + WORLD.WATER().isBig.is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2));
			}
		}
		
		for (COORDINATE c : tBound) {
			if (!tested.is(c) && WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
				connect(c);
				fill(c.x(), c.y());
			}
		}
	}

	private void connect(COORDINATE c) {
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(c, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (tested.is(t)) {
				
				RES.flooder().done();
				fix(t);
				RES.flooder().reverse(t);
				fix(t);
				return;
			}
			

			
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(t, d)) {
					if (WTRAV.can(t.x(), t.y(), d, false))
						RES.flooder().pushSmaller(t, d, t.getValue()+1, t);
					else
						RES.flooder().pushSmaller(t, d, t.getValue()+15, t);
				}
			}
			
		}
		
		throw new RuntimeException();
		
		
	}
	

	
	private void fill(int sx, int sy) {
		RES.filler().init(this);
		RES.filler().fill(sx, sy);
		
		for (DIR d : DIR.ORTHO) {
			if (WTRAV.isGoodLandTile(sx+d.x(), sy+d.y()) && WTRAV.can(sx, sy, d, false)) {
				RES.filler().fill(sx, sy, d);
			}
		}
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			
			if (!WTRAV.isGoodLandTile(c.x(), c.y()))
				continue;
			tested.set(c, true);
			
			for (DIR d : DIR.ORTHO) {
				RES.filler().fill(c, d);
			}
			
		}
		RES.filler().done();
		
	}
	
	private void fix(PathTile t) {
		
		if (t.getParent() == null) {
			WORLD.MOUNTAIN().clear(t.x(), t.y());
			WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
			return;
		}
		
		PathTile from = t;
		t = t.getParent();
		
		while(t != null) {
			DIR d = DIR.get(from, t);
			if (!WTRAV.can(from.x(), from.y(), d, false)) {
				WORLD.MOUNTAIN().clear(from.x(), from.y());
				WORLD.WATER().NOTHING.placeRaw(from.x(), from.y());
				WORLD.MOUNTAIN().clear(t.x(), t.y());
				WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
			}
			from = t;
			t = t.getParent();
		}
	}
	
}
