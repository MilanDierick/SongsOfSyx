package settlement.tilemap;

import static settlement.main.SETT.*;
import static settlement.main.SettlementGrid.*;

import init.C;
import init.RES;
import settlement.main.*;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.*;
import world.World;
import world.map.terrain.WorldMountain;

class GeneratorMountain {

	private final double radius;
	private int PS = 8;
	private final TileMap m = TILE_MAP();

	private final HeightMap height = new HeightMap(TWIDTH, THEIGHT, 16, 1);

	GeneratorMountain(CapitolArea area, GeneratorUtil util) {

		

		radius = util.json.d("MOUNTAIN_SIZE", 0.1, 1.0)*100;
		WorldMountain.AreaTileMountain wt = World.MOUNTAIN().area;
		
		/**
		 * Big outlines, using the pollymap
		 */
		util.polly.checkInit();
		int i = 0;
		for (SettlementGrid.Tile t : GRID.tiles()) {
			COORDINATE c = area.ts().get(i++);
			for (DIR d : t.getDirs()) {
				if (wt.is(c.x(), c.y(), d))
					mountenize(t.coo(d).x(), t.coo(d).y(), util);
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
//			if (RND.oneIn(500)) {
//				int x = (int) (t.x() + (RND.rBoolean() ? RND.rExpo()*100 : -RND.rExpo()*100)) ;
//				int y =(int) (t.y() + (RND.rBoolean() ? RND.rExpo()*100 : -RND.rExpo()*100)) ;
//				util.polly.checker.set(x, y, true);
//			}
			
		}
		RES.filler().done();
		
		//random mounatin
//		int r = (int) (util.json.d("MOUNTAIN_RANDOM", 0, 1.0)*25);
//		
//		for (int k = RND.rInt(1+r); k > 0; k--) {
//			
//			int x = RND.rInt(TWIDTH);
//			int y = RND.rInt(THEIGHT);
//			util.polly.checker.set(x, y, true);
//			for (int l = RND.rInt(4); l > 0; l--) {
//				int x2 = x + RND.rInt(20);
//				int y2 = y + RND.rInt(20);
//				util.polly.checker.set(x2, y2, true);
//			}
//			
//			
//		}

		/**
		 * Adding mounatin on edge of map
		 */
		for (int qy = 0; qy < TILES; qy++) {
			

			COORDINATE c = area.ts().get(qy*TILES);
			
			int startY,endY;
			
			startY = wt.borders(c.x(), c.y(), DIR.NW) ? 0 : QUAD_HALF;
			endY = wt.borders(c.x(), c.y(), DIR.SW) ? QUAD_SIZE : QUAD_HALF;
			startY+= QUAD_SIZE*qy;
			endY += QUAD_SIZE*qy;
			
			for (int y = startY; y < endY; y++) {
				util.polly.checker.set(0, y/PS, true);
			}
			
			c = area.ts().get(qy*TILES+TILES-1);
			
			startY = wt.borders(c.x(), c.y(), DIR.NE) ? 0 : QUAD_HALF;
			endY = wt.borders(c.x(), c.y(), DIR.SE) ? QUAD_SIZE : QUAD_HALF;

			startY+= QUAD_SIZE*qy;
			endY += QUAD_SIZE*qy;
			
			for (int y = startY; y < endY; y++) {
				util.polly.checker.set(C.SETTLE_TSIZE-1, y, true);
			}
		}
		
		for (int qx = 0; qx < TILES; qx++) {
			
			
			COORDINATE c;
			int startX,endX;
			
			
			c = area.ts().get(qx);
			
			startX = wt.borders(c.x(), c.y(), DIR.NW) ? 0 :QUAD_HALF;
			endX = wt.borders(c.x(), c.y(), DIR.NE) ? QUAD_SIZE : QUAD_HALF;

			startX+= QUAD_SIZE*qx;
			endX += QUAD_SIZE*qx;
			
			for (int x = startX; x < endX; x++) {
				util.polly.checker.set(x, 0, true);
			}
			
			c = area.ts().get(TILES*(TILES-1)+qx);
			startX = wt.borders(c.x(), c.y(), DIR.SW) ? 0 : QUAD_HALF;
			endX = wt.borders(c.x(), c.y(), DIR.SE) ? QUAD_SIZE : QUAD_HALF;

			startX+= QUAD_SIZE*qx;
			endX += QUAD_SIZE*qx;
			
			for (int x = startX; x < endX; x++) {
				util.polly.checker.set(x, C.SETTLE_TSIZE-1, true);
			}
		}
		
		/**
		 * Clearing path for water and arrivals
		 */
		i = 0;
		for (SettlementGrid.Tile t : GRID.tiles()) {
			if (area.arrivalTile() == i) {
				clear(DIR.N, t.coo(DIR.C).x(), t.coo(DIR.C).y(), util.polly);
				clear(DIR.S, t.coo(DIR.C).x(), t.coo(DIR.C).y(), util.polly);
				clear(DIR.E, t.coo(DIR.C).x(), t.coo(DIR.C).y(), util.polly);
				clear(DIR.W, t.coo(DIR.C).x(), t.coo(DIR.C).y(), util.polly);
			}
			
			COORDINATE c = area.ts().get(i++);
			if (World.WATER().has.is(c)) {
				for (DIR d : t.getDirs()) {
					if (World.WATER().has.is(c, d)) {
						clear(d.perpendicular(), t.coo(d).x(), t.coo(d).y(), util.polly);
					}
				}
			}
		}
		
		
		RES.flooder().init(this);
		
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				
				if (util.polly.checker.is(x, y)) {
					m.topology.MOUNTAIN.placeRaw(x, y);
					
					RES.flooder().pushSloppy(x, y, 0);
					double v2 = 0;
					if (RND.oneIn(5)) {
						v2 = RND.rFloat(30);
						
					}
					RES.flooder().setValue2(x, y, v2);
				}
				
				
			}
		}
		
		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			if (t.getValue() >= t.getValue2())
				continue;

			for (DIR d : DIR.ORTHO) {
				
				int x = t.x() + d.x();
				int y = t.y() + d.y();
				if (IN_BOUNDS(x, y)) {
					if (RES.flooder().hasBeenPushed(x, y))
						continue;
					
					double v = t.getValue()+d.tileDistance();
					double dd = v/t.getValue2();
					
					
					if (dd < 0.3) {
						m.topology.MOUNTAIN.placeRaw(x, y);
						
					}
					
					if (RES.flooder().pushSloppy(x, y, v) != null) {
						RES.flooder().setValue2(x, y, t.getValue2());
					}
					
				}
				
			}
			
		}
		
		RES.flooder().done();
		
		fertilize(util);
		makeHeight(util);
		
		
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				if (m.topology.MOUNTAIN.is(x, y)) {
					//util.fer.set(x, y, GeneratorFertilityFin.T_ROCK);
					if (isSolo(x, y)) {
						m.topology.ROCK.placeAmount(x, y, 0.5 + RND.rFloat()*0.5);
					}else {
						GROUND().ROCK.placeFixed(x, y);
						util.height.increment(x, y, 1.0);
					}
				
				}else {
					
					double h = util.height.get(x, y)*(0.3+0.7*height.get(x, y));
					h = Math.pow(h, 1.2);
					h += RND.rFloat0(0.05);
					if (h > 0.50) {
						h -= 0.50;
						h /= 0.50;
						h = Math.pow(h, 0.8);
						h *= 1.0 + RND.rSign()*RND.rExpo();
						//h = Math.pow(h, 1.5);
						//h *= h*h;
						double a = h;
						m.topology.ROCK.placeAmount(x, y, a);
						util.fer.set(x, y, util.fer.get(x, y)*(0.25+0.75*(1.0-a)));
						//util.fer.increment(x, y, -0.5*a);
					}else if(RND.oneIn(250)) {
						double a = RND.rFloatP(3);
						m.topology.ROCK.placeAmount(x, y, a);
						util.fer.target(x, y, 0.5, a);
					}
				}
			}
		}
	}
	
	private void fertilize(GeneratorUtil util) {
		
		RES.flooder().init(this);
		
		HeightMap hh = new HeightMap(TWIDTH, THEIGHT, 32, 8);
		
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				if (m.topology.MOUNTAIN.is(x, y)) {
					util.fer.set(x, y, hh.get(x, y));
					for (DIR d : DIR.ORTHO)
						if (!m.topology.MOUNTAIN.is(x, y, d)) {
							RES.flooder().pushSloppy(x, y, 0);
							break;
						}
				}
					
			}
		}
		{
			double ma = 5;
			
			while(RES.flooder().hasMore()) {
				PathTile t = RES.flooder().pollSmallest();
				if (t.getValue() >= ma)
					continue;
				double v = t.getValue()/ma;
				util.fer.set(t.x(), t.y(), GeneratorFertilityFin.T_ROCK*(1.0-v) + util.fer.get(t.x(), t.y())*v);
				for (DIR d : DIR.ALL) {
					int dx = t.x()+d.x();
					int dy = t.y()+d.y();
					if (SETT.IN_BOUNDS(dx, dy))
						RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance());
					
				}
				
			}
		}
		
		RES.flooder().done();
		
		{
			double am = 8;
			int dx = -1;
			int dy = -1;
			
			double d = 0.1;
			
			for (int y = 0; y < C.SETTLE_TSIZE; y++) {
				for (int x = 0; x < C.SETTLE_TSIZE; x++) {
					if (m.topology.MOUNTAIN.is(x, y)) {
						for (int i = 0; i < am; i++) {
							int tx = x + i*dx;
							int ty = y + i*dy;
							if (!SETT.IN_BOUNDS(tx, ty))
								break;
							if (!m.topology.MOUNTAIN.is(tx, ty)) {
								double dd = d*(1.0 - i/am);
								util.fer.increment(tx, ty, dd);
							}
							
							
						}
					}
						
				}
			}
			
		}
	}
	
	private void makeHeight(GeneratorUtil util) {
		
		RES.flooder().init(this);
		
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				
				if (m.topology.MOUNTAIN.is(x, y)) {
					RES.flooder().pushSloppy(x, y, 0);
					util.height.set(x, y, 1.0);
					double v2 = 0;
					if (RND.oneIn(5)) {
						v2 = RND.rFloat(30);
						
					}
					RES.flooder().setValue2(x, y, v2);
				}
				
				
			}
		}
		
		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			if (t.getValue() >= t.getValue2())
				continue;

			for (DIR d : DIR.ORTHO) {
				
				int x = t.x() + d.x();
				int y = t.y() + d.y();
				if (IN_BOUNDS(x, y)) {
					if (RES.flooder().hasBeenPushed(x, y))
						continue;
					
					double v = t.getValue()+d.tileDistance();
					double dd = v/t.getValue2();
					

					if (t.getValue2() > 20) {
						double h = util.height.get(x, y);
						h = h*dd + (1-dd);
						h*= RND.rFloat1(0.3);
						util.height.set(x, y, h);
					}
					
					
					
					
					if (RES.flooder().pushSloppy(x, y, v) != null) {
						RES.flooder().setValue2(x, y, t.getValue2());
					}
					
				}
				
			}
			
		}
		
		RES.flooder().done();
	}

	private boolean isSolo(int tx, int ty) {
		for (int i = 0; i < DIR.NORTHO.size(); i++) {
			DIR d = DIR.NORTHO.get(i);
			if (is(tx, ty,d) && is(tx, ty,d.next(1)) && is(tx, ty,d.next(-1)))
				return false;
		}
		return true;
	}
	
	private boolean is(int tx, int ty, DIR d) {
		if (!IN_BOUNDS(tx, ty, d))
			return true;
		return TERRAIN().MOUNTAIN.is(tx,ty,d);
	}
	
	private void clear(DIR d, int startX, int startY, Polymap p) {
		for (int i = 0; i < QUAD_HALF; i++) {
			p.checker.set(startX, startY, false);
			if (IN_BOUNDS(startX-1, startY))
				p.checker.set(startX, startY, false);
			if (IN_BOUNDS(startX-1, startY-1))
				p.checker.set(startX, startY, false);
			if (IN_BOUNDS(startX, startY-1))
				p.checker.set(startX, startY, false);
			p.checker.set(startX, startY, false);
			startX += d.x();
			startY += d.y();
		}
	}
	
	private void mountenize(int tx, int ty, GeneratorUtil util) {
		
		double radius = this.radius*RND.rFloat1(0.10);
		final double radius2 = radius*radius;
		tx += RND.rInt0(20);
		ty += RND.rInt0(20);

		for (int y = (int) -radius; y < radius; y++) {
			for (int x = (int) -radius; x < radius; x++) {
				int dx = tx + x;
				int dy = ty + y;
				if (!IN_BOUNDS(dx, dy))
					continue;
				double r = x*x+y*y;
				if (r > radius2)
					continue;
				util.polly.checker.set(dx/PS, dy/PS, true);
			}
		}

	}
	


}
