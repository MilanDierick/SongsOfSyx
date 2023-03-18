package settlement.tilemap;
import static settlement.main.SETT.*;

import init.C;
import init.RES;
import settlement.main.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import world.World;

class GeneratorLake {

	private final int PS = 4;
	private final Polymap polly;
	private final double radius;
	
	
	GeneratorLake(CapitolArea area, GeneratorUtil util) {
		
		radius = util.json.d("LAKE_SIZE", 0.1, 1.0)*200;
		
		polly = util.polly;
		polly.checkInit();
		
		for (int i = 0; i < GRID.tiles().size(); i++) {
			SettlementGrid.Tile t = GRID.tile(i);
			//WorldWater.AreaTileWater w = area.water(i);
			COORDINATE wt = area.ts().get(i);
			
			if (!World.WATER().LAKE.is.is(wt))
				continue;
			for (DIR d : t.getDirs()) {
				if (World.WATER().LAKE.is.is(wt, d)) {
					sink(t.cooInner(d));
				}
			}
		}
		
		/**
		 * refining the edges with smaller polly samples
		 */
		RES.filler().init(this);
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				
				if (util.polly.checker.is(x/PS, y/PS)) {
					RES.filler().fill(x, y);
				}
				
				
			}
		}
		
		util.polly.checkInit();
		while(RES.filler().hasMore()){
			
			COORDINATE t = RES.filler().poll();
			util.polly.checker.set(t, true);
			/**
			 * Adding some scattered shite
			 */
			if (RND.oneIn(500)) {
				int x = (int) (t.x() + (RND.rBoolean() ? RND.rExpo()*20 : -RND.rExpo()*20)) ;
				int y =(int) (t.y() + (RND.rBoolean() ? RND.rExpo()*20 : -RND.rExpo()*20)) ;
				util.polly.checker.set(x, y, true);
			}
			
		}
		RES.filler().done();
		
		int lakes = (int) (util.json.d("LAKE_SMALL", 0.0, 1.0)*30);
		int islands = (int) (util.json.d("LAKE_ISLANDS", 0.0, 1.0)*100);

		double table = 0;
		for (COORDINATE c : area.ts()) {
			if (World.IN_BOUNDS(c.x(), c.y()))
				table+= World.GROUND().getter.get(c).fertility();
		}
		
		table /= (double) GRID.tiles().size();
		
		lakes *= table;
		
		if (lakes != 0) {
			for (int i = RND.rInt(lakes); i > 0; i--) {
				int x = RND.rInt(TWIDTH);
				int y = RND.rInt(THEIGHT);
				util.polly.checker.set(x, y, true);
				for (int l = 10 + RND.rInt(40); l > 0; l--) {
					int x2 = x + RND.rInt0(20);
					int y2 = y + RND.rInt0(20);
					util.polly.checker.set(x2, y2, true);
				}
			}
		}
		
		if (islands != 0) {
			
			for (int i = RND.rInt(islands); i > 0; i--) {
				int x = RND.rInt(TWIDTH);
				int y = RND.rInt(THEIGHT);
				util.polly.checker.set(x, y, false);
				for (int l = RND.rInt(20); l > 0; l--) {
					int x2 = x + RND.rInt0(30);
					int y2 = y + RND.rInt0(30);
					util.polly.checker.set(x2, y2, false);
				}
			}
		}


		
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (polly.checker.is(c.x(), c.y())) {
				if (util.height.get(c) < 0.8) {
					TERRAIN().WATER.placeRaw(c.x(), c.y());
					GROUND().fertilityGet(0.5).placeRaw(c.x(), c.y());
				}
			}
		}
		
	}
	
	public void sink(int cx, int cy) {
		
		for (int y1 = (int) (-radius); y1 < radius; y1++) {
			int ty = y1 + cy;
			if (ty < 0 || ty >= C.SETTLE_TSIZE)
				continue;
			for (int x1 = (int) (-radius); x1 < radius; x1++) {
				int tx = cx + x1;
				if (tx < 0 || tx >= C.SETTLE_TSIZE)
					continue;
				double d = Math.sqrt(x1*x1 + y1*y1);
				if (d < radius) {
					
					polly.checker.set(tx/PS, ty/PS, true);
				}
				
				
			}
		}
	}
	
	public void sink(COORDINATE c) {
		sink(c.x(), c.y());
	}



}

