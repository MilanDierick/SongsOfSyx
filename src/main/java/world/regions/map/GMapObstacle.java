package world.regions.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Bitmap2D;
import world.WORLD;
import world.map.pathing.WTRAV;

class GMapObstacle extends Bitmap2D {
	
	private static int length = 5;

	
	GMapObstacle(int px, int py){
		super(WORLD.TWIDTH(), WORLD.THEIGHT(), true);

		final Bitmap2D blocked = new Bitmap2D(WORLD.TWIDTH(), WORLD.THEIGHT(), true);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (isBlocked(c))
				blocked.set(c, true);
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (!blocked.is(c))
				trace(c, blocked);
				
		}
		
		final Bitmap2D tmp = new Bitmap2D(WORLD.TWIDTH(), WORLD.THEIGHT(), true);
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			tmp.set(c, is(c));
		}
		
		outer:
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (!tmp.is(c))
				continue;
			for (DIR d : DIR.ORTHO) {
				if (!tmp.is(c, d) && !blocked.is(c, d))
					continue outer;
			}
			set(c, false);
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (blocked.is(c) && WORLD.FOREST().amount.get(c) == 1) {
				for (DIR d : DIR.ORTHO) {
					if (!blocked.is(c, d)) {
						set(c, true);
						break;
					}
				}
			}
		}
		
		for (DIR d : DIR.NORTHO) {
			for (int i = 0; i < 8; i++) {
				set(px+i*d.x(), py+i*d.y(), true);
			}
		}
		
//		WORLD.OVERLAY().debugg = new WorldOverlays.OverlayTile(true) {
//			
//			@Override
//			protected void render(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
//				
//				if (is(it.tile())) {
//					COLOR.BLACK.bind();
//					SPRITES.cons().BIG.outline.render(CORE.renderer(), 0, it.x(), it.y());
//					COLOR.unbind();
//				}
//			}
//		};
	}
	
	private boolean isBlocked(COORDINATE c) {
		if (!WTRAV.isGoodLandTile(c.x(), c.y()))
			return true;
		else if (WORLD.FOREST().amount.get(c) == 1) {
			for (DIR d : DIR.ALL) {
				if (WORLD.FOREST().amount.get(c, d) != 1)
					return false;
			}
			return true;
		}
		return false;
	}
	
	final DIR[] dirs = new DIR[] {
		DIR.W, DIR.NW, DIR.N
	};
	
	private void trace(COORDINATE c, Bitmap2D blocked) {
		for (DIR d : dirs) {
			if (trace(c, d, blocked) && trace(c, d.perpendicular(), blocked)) {
				traceSet(c, d, blocked);
				traceSet(c, d.perpendicular(), blocked);
			}
		}
		
	}
	
	private boolean trace(COORDINATE c, DIR direction, Bitmap2D blocked) {

		for (int i = 1; i <= length; i++) {
			if (blocked(c, i, direction, blocked)){
				return true;
			}
		}
		return false;
	}
	
	private void traceSet(COORDINATE c, DIR direction, Bitmap2D blocked) {

		set(c, true);
		for (int i = 1; i <= length; i++) {
			int dx = c.x()+i*direction.x();
			int dy = c.y()+i*direction.y();
			set(dx, dy, true);
			if (blocked(c, i, direction, blocked)){
				return;
			}

			
		}
	}
	
	private boolean blocked(COORDINATE c, int i, DIR d, Bitmap2D blocked) {
		int dx = c.x()+i*d.x();
		int dy = c.y()+i*d.y();
		return blocked.is(dx, dy);
		
	}

};