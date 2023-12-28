package settlement.tilemap.generator;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.rnd.RND;

class GeneratorFish {



	GeneratorFish(CapitolArea area, GeneratorUtil util) {

		smoothDepth();
		smooth();
		makeFish();
		
		
	}
	
	
	private void smooth() {
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			RES.flooder().setValue2(c, 0);
		}
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.TERRAIN().WATER.SHALLOW.is(c)) {
				smooth(c);
			}
		}
		
	}
	
	private void smooth(COORDINATE start) {
		
		if (RES.flooder().getValue(start) != 0)
			return;
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(start, 0);
		
		int am = 0;
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (!SETT.TERRAIN().WATER.is.is(t)) {
				RES.flooder().done();
				return;
			}
			RES.flooder().setValue2(t, 1);
			if (SETT.TERRAIN().WATER.SHALLOW.is(t)) {
				am++;
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d)) {
						RES.flooder().pushSmaller(t, d, t.getValue()+1);
					}
				}
				
			}
		}
		
		RES.flooder().done();
		
		if (am > 100) {
			return;
		}
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(start, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (SETT.TERRAIN().WATER.SHALLOW.is(t) && SETT.TERRAIN().WATER.groundWaterSalt.is(t)) {
				SETT.TERRAIN().WATER.DEEP.placeRaw(t.x(), t.y());
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d)) {
						RES.flooder().pushSmaller(t, d, t.getValue()+1);
					}
				}
				
			}
		}
		
		RES.flooder().done();
	}
	
	private void smoothDepth(){
		
		Flooder f = RES.flooder();
		
		for (COORDINATE c : SETT.TILE_BOUNDS)
			f.setValue2(c, 0);
		for (int y = 0; y < SETT.THEIGHT; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				smooth(x, y);
			}
		}
	}
	
	private void smooth(int sx, int sy) {
		if (!is.is(sx, sy))
			return;
		
		Flooder f = RES.flooder();
		
		if (f.getValue2(sx, sy) != 0)
			return;
		
		f.init(this);
		
		f.pushSloppy(sx, sy, 0);
		
		double tiles = 0;
		
		double edgeValue = SETT.TAREA*2;
		
		while(f.hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			f.setValue2(t, 1);
			if (t.getValue() >= edgeValue) {
				break;
			}
			
			tiles ++;
			
			for (DIR d : DIR.ORTHO) {
				if (SETT.IN_BOUNDS(t, d)) {
					f.pushSmaller(t, d, t.getValue() + 1 + (is.is(t, d) ? 0 : edgeValue));
				}
			}
		}
		
		if (tiles < 50) {
			f.done();
			f.init(this);
			f.pushSloppy(sx, sy, 0);
			
			while(f.hasMore()) {
				
				PathTile t = RES.flooder().pollSmallest();
				SETT.TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d) && is.is(t, d)) {
						f.pushSmaller(t, d, t.getValue() + 1);
					}
				}
			}
			
			
			
		}

		while(f.hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			int am = 0;
			if (is.is(t)) {
				for (DIR d : DIR.ORTHO) {
					if (SETT.TERRAIN().WATER.SHALLOW.is(t, d))
						am++;
				}
				if (am >= 2)
					SETT.TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
			}
			
			
		}
		
		f.done();
	}
	
	private void makeFish() {
		Flooder f = RES.flooder();
		
		SETT.TERRAIN().WATER.deepSeaFishSpot.clear();
		
		for (COORDINATE c : SETT.TILE_BOUNDS)
			f.setValue2(c, 0);
		
		for (int y = 0; y < SETT.THEIGHT; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				make(x, y);
			}
		}
	}
	
	private void make(int sx, int sy) {
		
		if (!is.is(sx, sy))
			return;
		
		Flooder f = RES.flooder();
		
		if (f.getValue2(sx, sy) != 0)
			return;
		
		f.init(this);
		
		f.pushSloppy(sx, sy, 0);
		
		double tiles = 0;
		
		double edgeValue = SETT.TAREA*2;
		
		while(f.hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			f.setValue2(t, 1);
			if (t.getValue() >= edgeValue) {
				break;
			}
			
			tiles += TERRAIN().WATER.groundWaterSalt.is(sx, sy) ? 1 : 0.5;
			
			for (DIR d : DIR.ORTHO) {
				if (SETT.IN_BOUNDS(t, d)) {
					f.pushSmaller(t, d, t.getValue() + 1 + (is.is(t, d) ? 0 : edgeValue));
				}
			}
		}
		
		double am = tiles/250.0;
		am = Math.min(am, f.pushed());
		
		double delta = f.pushed()/am;
		double d = RND.rFloat()*delta;
		
		while(f.hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			f.setValue2(t, 1);
			d -= 1;
			
			if (d <= 0) {
				TERRAIN().WATER.deepSeaFishSpot.set(t, true);
				d += RND.rFloat()*delta;
			}
			
		}
		
		f.done();
		
	}
	
	private MAP_BOOLEAN is = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return TERRAIN().WATER.DEEP.is(tx, ty) || TERRAIN().WATER.BRIDGE.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			// TODO Auto-generated method stub
			return false;
		}
	};



}
