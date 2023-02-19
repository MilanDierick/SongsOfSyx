package settlement.tilemap;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.*;
import snake2d.Path;
import snake2d.Path.COST;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.World;

class GeneratorWaterFin {



	GeneratorWaterFin(CapitolArea area, GeneratorUtil util) {

		RES.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (!TERRAIN().WATER.is(c)) {
				RES.flooder().pushSloppy(c, 0);
				RES.flooder().setValue2(c, 1+ RND.rExpo()*50);
			}
		}
		
		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			double v2 = t.getValue();
			
			if (TERRAIN().WATER.is(t)) {
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
		
		fixWays(area, util);
		
//		RES.flooder().init(this);
//		
//		for (COORDINATE c : SETT.TILE_BOUNDS) {
//			if (TERRAIN().WATER.is(c)) {
//				RES.flooder().pushSloppy(c, 0);
//				RES.flooder().setValue2(c, 4 + Math.pow(1.0-CLAMP.d(util.height.get(c), 0, 1), 2)*50);
//				
//			}
//		}
//		
//		while(RES.flooder().hasMore()) {
//			
//			PathTile t = RES.flooder().pollSmallest();
//			double v = t.getValue();
//			double v2 = t.getValue2();
//			double f = v/v2;
//			if (f > 1)
//				continue;
//			util.fer.target(t.x(), t.y(), 1f, 1.0-f);
//			
//			for (DIR d : DIR.ALL) {
//				
//				int x = t.x() + d.x();
//				int y = t.y() + d.y();
//				if (IN_BOUNDS(x, y)) {
//					if (RES.flooder().pushSmaller(x, y, v+d.tileDistance()) != null) {
//						RES.flooder().setValue2(x, y, t.getValue2());
//					}
//				}
//			}
//			
//		}
//		
//		RES.flooder().done();

		
	}
	
	private void fixWays(CapitolArea area, GeneratorUtil util) {
		
		final Path.PathSync p = new Path.PathSync(5000);
		util.checker.init();
		
		final COST c = new COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {
				if (TERRAIN().WATER.is(toX, toY))
					return 10;
				if (TERRAIN().WATER.DEEP.is(toX, toY))
					return 20;
				return 1;
			}
			
		};
		for (int i = 0; i < GRID.tiles().size(); i++) {
			COORDINATE coo = area.ts().get(i);
			SettlementGrid.Tile ut = GRID.tile(i);
			if (!World.WATER().isLaky.is(coo) && !World.WATER().isOceany.is(coo)) {
				int sx = ut.coo(DIR.NW).x();
				int sy = ut.coo(DIR.NW).y();
				int dx = ut.coo(DIR.SE).x();
				int dy = ut.coo(DIR.SE).y();
				RES.astar().getShortest(p, c, sx, sy, dx, dy);
				fixWays(p, util);
				
				
				sx = ut.coo(DIR.NE).x();
				sy = ut.coo(DIR.NE).y();
				dx = ut.coo(DIR.SW).x();
				dy = ut.coo(DIR.SW).y();
				RES.astar().getShortest(p, c, sx, sy, dx, dy);
				fixWays(p, util);
				
			}
		}
	}
	
	void fixWays(Path p, GeneratorUtil util) {
		while(!p.isDest()) {
		
			if(TERRAIN().WATER.isWater(p.x(), p.y()) && ! util.checker.is(p)) {
				int x = p.x();
				int y = p.y();
				if (IN_BOUNDS(x, y)) {
					TERRAIN().WATER.placeRaw(x, y);
					if (RND.oneIn(4))
						TERRAIN().NADA.placeRaw(x, y);
					for (DIR d : DIR.ORTHO){
						if (TERRAIN().WATER.DEEP.is(x+d.x(), y+d.y()))
							TERRAIN().WATER.placeRaw(x+d.x(), y+d.y());
					}
				}
				
			}
			util.checker.set(p, true);
			p.setNext();
		}
	}



}
