package settlement.tilemap.generator;

import static settlement.main.SETT.*;
import static settlement.main.SettlementGrid.*;

import init.RES;
import settlement.main.*;
import settlement.tilemap.TileMap;
import snake2d.*;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.LinkedList;
import world.WORLD;

class GeneratorRiver {

	private final HeightMap height;
	private final TileMap m;
	private final int width;
	private final GeneratorUtil util;
	private final Polymap polly;
	
	private final Path.PathFancy p = new Path.PathFancy(5000);

	GeneratorRiver(CapitolArea area, GeneratorUtil util) {

		util.checker.init();
		
		this.util = util;
		polly = util.polly;
		height = util.height;
		this.m = TILE_MAP();
		width = util.json.i("RIVER_WIDTH", 1, 10);
		for (int i = 0; i < GRID.tiles().size(); i++) {
			SettlementGrid.Tile ut = GRID.tile(i);
			build(area.ts().get(i), ut);
		}
		
		RES.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (util.checker.is(c)) {
				RES.flooder().pushSloppy(c, 0);
				double h = 1+util.fer.get(c);
				if (h < 0) {
					h = 0;
				}
					
				h = Math.sqrt(h/4.0);
				RES.flooder().setValue2(c, h*20);
				//util.fer.set(c, 0);
				
			}
		}
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
			GROUND().fertilityGet(0.5).placeRaw(t.x(), t.y());
			
			double v = t.getValue();
			

			if (v >= t.getValue2()) {
				continue;
			}
			
			util.height.increment(t, 0.4*(1.0-v/t.getValue2()));

			for (int i = 0; i < DIR.ALL.size(); i++) {
				
				
				DIR d = DIR.ALL.get(i);
				int x = d.x() + t.x();
				int y = d.y() + t.y();
				
				if (!IN_BOUNDS(x, y))
					continue;
				
				v = d.tileDistance();
				v *= 1+Math.pow(height.get(x, y), 4)*1.5;
				v += t.getValue();
				if (TERRAIN().MOUNTAIN.is(x, y))
					v += 2;
				if (v >= t.getValue2())
					continue;
				if (RES.flooder().pushSmaller(x, y, (float) v) != null) {
					RES.flooder().setValue2(x, y, t.getValue2());
				}

			}
		}
		
		RES.flooder().done();

		
	}

	private void build(COORDINATE wtt, SettlementGrid.Tile ut) {

		if (!WORLD.WATER().isRivery.is(wtt))
			return;

		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR dStart = DIR.ORTHO.get(i);
			if (WORLD.WATER().isRivery.is(wtt, dStart)) {
				int startX = ut.coo(dStart).x();
				int startY = ut.coo(dStart).y();

				boolean delta = WORLD.WATER().isDELTA.is(wtt);
				boolean connected = false;
				for (int j = 0; j < i; j++) {
					DIR dEnd = DIR.ORTHO.get(j);
					if (WORLD.WATER().isRivery.is(wtt, dEnd)) {
						connected = true;
					}
				}
				for (int j = i + 1; j < DIR.ORTHO.size(); j++) {
					DIR dEnd = DIR.ORTHO.get(j);
					if (WORLD.WATER().isRivery.is(wtt, dEnd)) {
						connected = true;
						int endX = ut.coo(dEnd).x();
						int endY = ut.coo(dEnd).y();
						boolean sDelta = delta & !WORLD.WATER().RIVER.is(wtt, dStart);
						boolean eDelta = delta & !WORLD.WATER().RIVER.is(wtt, dEnd);
						pave(positions(startX, startY, dStart, sDelta), positions(endX, endY, dEnd, eDelta), ut.bounds, sDelta ? 2.0 : 1.0, eDelta ? 2.0 : 1.0);
					}
				}
				if (!connected) {
					LinkedList<Coo> end = new LinkedList<>();
					end.add(new Coo(ut.coo(DIR.C).x(), ut.coo(DIR.C).y()));
					pave(
							positions(startX, startY, dStart, delta & !WORLD.WATER().RIVER.is(wtt, dStart)), 
							end, 
							ut.bounds, 1.0, 0);
				}
			}

		}

	}

	private LinkedList<Coo> positions(int x, int y, DIR d, boolean delta){

		
		if (x < 0) {
			x = 0;
		}
		if (x >= TWIDTH) {
			x = TWIDTH-1;
		}
		if (y < 0) {
			y = 0;
		}
		if (y >= THEIGHT) {
			y = THEIGHT-1;
		}
		
		int start = delta ? QUAD_QUATER : 0;
		int width = this.width;
		if (delta)
			width*=2;
		
		final LinkedList<Coo> res = new LinkedList<>();
		if (d.x() != 0) {
			for (int i = start; i < QUAD_SIZE*2 && res.size() < width; i++ ) {
				if (IN_BOUNDS(x, y+i) && polly.isEdge(x, y+i)) {
					res.add(new Coo(x, y+i));
				}
				if (IN_BOUNDS(x, y-i) && polly.isEdge(x, y-i)) {
					res.add(new Coo(x, y-i));
				}
			}
		}else {
			for (int i = start; i < QUAD_SIZE*2 && res.size() < width; i++ ) {
				if (IN_BOUNDS(x+i, y) && polly.isEdge(x+i, y)) {
					res.add(new Coo(x+i, y));
				}
				if (IN_BOUNDS(x-i, y) && polly.isEdge(x-i, y)) {
					res.add(new Coo(x-i, y));
				}
			}
		}
		return res;
		
		
	}
	
	private void pave(LinkedList<Coo> starts, LinkedList<Coo> ends,RECTANGLE bounds, double startWidth, double endWidth) {

		final Path.COST cm = new Path.COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				if (!bounds.holdsPoint(toX, toY))
					return 50;

				if (m.topology.MOUNTAIN.is(toX, toY))
					return 20;

				if (m.topology.ROCK.is(toX, toY))
					return 20;

				if (!polly.isEdge(toX, toY))
					return 2.0;

				if (TERRAIN().WATER.SHALLOW.is(toX, toY))
					return 0.5;
				
				double mul = util.checker.is(toX, toY) ? 1.0 : 1.0;
				
				return mul; 

			}
		};
		
		LinkedList<Coo> flood = new LinkedList<>();
		
		while(!starts.isEmpty()) {
			
			Coo s = starts.removeFirst();
			Coo e = ends.removeFirst();
			ends.add(e);
			if (RES.astar().getShortest(p, cm, s.x(), s.y(), e.x(), e.y())) {
//				double dW = (endWidth - startWidth) / p.getLength();
//				if (endWidth == startWidth)
//					dW = 0;
//				double w = startWidth;
				do {
					if (!util.checker.is(p)) {
						//util.fer.set(p, 1.0);
						util.checker.set(p, true);
					}else if (!TERRAIN().WATER.SHALLOW.is(p)){
						//util.fer.increment(p, w); 
					}
					//w += dW;
					
				} while (p.setNext());
			}else {
				LOG.ln("nono");
			}
			flood.add(s);
			
		}
		
		
		RES.flooder().init(this);
		
		while(!flood.isEmpty()) {
			
			Coo s = flood.removeFirst();
			RES.flooder().pushSloppy(s, 0);
			
		}

		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
			double v = t.getValue();

			if (v >= t.getValue2()) {
				continue;
			}

			for (int i = 0; i < DIR.ALL.size(); i++) {
				
				
				DIR d = DIR.ALL.get(i);
				int x = d.x() + t.x();
				int y = d.y() + t.y();
				
				if (!IN_BOUNDS(x, y))
					continue;
				
				if (util.checker.is(t, d)) {
					RES.flooder().pushSmaller(x, y, 0);
				}

			}
		}
		
		RES.flooder().done();
		

	}


}
