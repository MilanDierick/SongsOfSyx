package settlement.tilemap;

import static settlement.main.SETT.*;

import init.RES;
import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import init.resources.Minable;
import init.resources.RESOURCES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;

final class GeneratorMinerals {

	private final HeightMap height = new HeightMap(TWIDTH, THEIGHT, 16, 16);
	private final HeightMap height2 = new HeightMap(TWIDTH, THEIGHT, 4, 2);
	private final TerrainCount tc;
	
	GeneratorMinerals(CapitolArea area, GeneratorUtil util, LinkedList<COORDINATE> caves) {

		
		tc = new TerrainCount();
		
		if (!area.config().minables)
			return;
		
		
		for (Minable m : RESOURCES.minables().all()) {
			make(util, m, 0.2+RND.rFloat()*0.2, 1);
		}
		
		for (TERRAIN t : TERRAINS.ALL()) {
			double total = 0;
			for (Minable m : RESOURCES.minables().all()) {
				total += area.info.get(t)*m.terrain(t);
			}
			log(t.name + " " + total + " " + area.info.get(t));
			int fail = 1000;
			double am = total;
			while(am > 0 && fail-- > 0) {
				double target = RND.rFloat()*total;
				for (Minable m : RESOURCES.minables().all()) {
					target -= area.info.get(t)*m.terrain(t);
					if (target <= 0) {
						log(m.resource.name + " " + area.info.get(t)*m.terrain(t) + " " + total);
						double size = RND.rExpo();
						double amount = 1.0-RND.rExpo();
						am -= size*amount;
						make(util, m, t, amount, size*300);
						break;
					}
				}
			}
			log("");
		}
		
		for (Minable m : RESOURCES.minables().all()) {
			log(m.resource.name + " " + SETT.MINERALS().totals.get(m));
		}
		
		
	
		
	}
	
	private void log(Object o) {
		
	}
	
	private void make(GeneratorUtil util, Minable m, double a, double size) {
		
		double tiles = 0;
		for (TERRAIN t : TERRAINS.ALL())
			tiles += m.terrain(t);
		
		for (int i = 40; i > 0; i--) {
			double td = RND.rFloat()*tiles;
			for (TERRAIN t : TERRAINS.ALL()) {
				td -= m.terrain(t);
				if (td <= 0) {
					if (make(util, m, t, a, size))
						return;
					break;
				}
			}
			
		}
		mineralize(util, m, a, size);
	}
	
	private boolean make(GeneratorUtil util, Minable m, TERRAIN t, double a, double size) {
		
		int tile = tc.tiles(t);
		if (tile <= 0)
			return false;;
		tile = RND.rInt(tile);
		int x = tc.x(t, tile);
		int y = tc.y(t, tile);
		return mineralize(util, x, y, m, a, size);
	}
	
	private final MAP_BOOLEAN placable = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tile) {
			return !MINERALS().getter.is(tile) && !TERRAIN().WATER.DEEP.is(tile);
		}

		@Override
		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && is(tx+ty*TWIDTH);
		}
		
	};
	
	private void mineralize(GeneratorUtil util, Minable t, double amount, double size) {
		int i = 5000;
		while(i-- > 0) {
			int x = RND.rInt(TWIDTH);
			int y = RND.rInt(THEIGHT);
			if (mineralize(util, x, y, t, amount, size))
				return;
		}
		LOG.err("Could not mieralize!");
	}
	
	private boolean mineralize(GeneratorUtil util, int x, int y, Minable t, double amount, double size) {
		
		Rec rec = new Rec(x, x+1, y, y+1);
		
		if (!placable.is(x, y))
			return false;
		
		log("making " + t.resource.name + " " + amount + " " + size);
		
		MAP_BOOLEANE check = util.polly.getScaled(4);
		util.polly.checkInit();
		
		check.set(x, y, true);
		
		if (size > 0){
		
			double dx = size*RND.rFloat();
			double dy = size-dx;
			double m = dx > dy ? dx : dy;
			
			dx *= RND.rSign();
			dy *= RND.rSign();
			dx /= m;
			dy /= m;
			
			for (int i = 0; i < m; i++) {
				if (RND.oneIn(16)) {
					dx *= RND.rSign();
					dy *= RND.rSign();
				}
				
				
				int ddx = (int) (x + dx*i);
				int ddy = (int) (y + dy*i);
				check.set(ddx, ddy, true);
				
			}
		}
		
		
		
		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(x, y, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			if (!placable.is(c))
				continue;
			if (c.getValue() > 8)
				continue;
			double v = 1-c.getValue()/8;
			double f2 = height.get(c)*height2.get(c);
			f2 *=f2;
			f2*=3;
			v*= f2;
			if (v > 0.1) {
				rec.unify(c.x(), c.y());
				MINERALS().getter.set(c, t);
				MINERALS().amountD.set(c, (0.1 + 0.9*amount)*v);
				v*= 2;
				if (v > 1)
					v = 1;
				
//				
//				double fer = util.fer.get(c);
//				fer += t.fertilityIncrease*v;
//				util.fer.set(c, CLAMP.d(fer, 0, 1));
				if (TERRAIN().clearing.get(c).isEasilyCleared())
					TERRAIN().NADA.placeFixed(c.x(), c.y());
			}
			
			double q = check.is(c) ? 0 : 1;
			
			DIR dir = DIR.ALL.get(RND.rInt(DIR.ALL.size()));
			for (int di = 0; di < DIR.ALL.size(); di++) {
				if (IN_BOUNDS(c, dir))
					RES.flooder().pushSmaller(c, dir, c.getValue()+dir.tileDistance()*q, c);
				dir = dir.next(1);
			}
			
			
		}
		
		RES.flooder().done();
		
		
		return true;
	}
	
	private static class TerrainCount {
		
		private final int[] tiles;
		private final int[] marks = new int[TERRAINS.ALL().size()+1];
		
		TerrainCount(){
			
			tiles = new int[SETT.TAREA];
			marks[0] = 0;
			marks[marks.length-1] = SETT.TAREA;

			for (int i = 0; i < TERRAINS.ALL().size()/2; i++) {
				check(TERRAINS.ALL().get(i), TERRAINS.ALL().get(TERRAINS.ALL().size()-1-i));
			}
			if ((TERRAINS.ALL().size()&1) != 0) {
				check(TERRAINS.ALL().get(TERRAINS.ALL().size()/2));
			}
			
		}
		
		private void check(TERRAIN a, TERRAIN b) {
			
			int ai = marks[a.index()];
			int bi = marks[b.index()+1]-1;
			
			for (COORDINATE c : SETT.TILE_BOUNDS) {
				if (is(a, c.x(), c.y()))
					tiles[ai++] = (c.y() << 16) | c.x();
				if (is(b, c.x(), c.y())) {
					tiles[bi--] = (c.y() << 16) | c.x();
				}
			}
			marks[a.index()+1] = ai;
			marks[b.index()] = bi;
		}
		
		private void check(TERRAIN a) {
			int ai = marks[a.index()];
			
			for (COORDINATE c : SETT.TILE_BOUNDS) {
				if (is(a, c.x(), c.y()))
					tiles[ai++] = (c.y() << 16) | c.x();
			}
			marks[a.index()+1] = ai;
		}
		
		private boolean is(TERRAIN t, int tx, int ty) {
			if (t == TERRAINS.FOREST()) {
				return TERRAIN().TREES.isTree(tx, ty);
			}else if(t == TERRAINS.MOUNTAIN()) {
				return TERRAIN().CAVE.is(tx, ty) || (TERRAIN().MOUNTAIN.is(tx, ty) && !TERRAIN().MOUNTAIN.isFilled(tx, ty)); 
			}else if(t == TERRAINS.NONE()) {
				return TERRAIN().clearing.get(tx, ty).isEasilyCleared();
			}else if(t == TERRAINS.OCEAN()) {
				return TERRAIN().WATER.is(tx, ty) && !TERRAIN().WATER.isOpen(tx, ty); 
			}else if(t == TERRAINS.WET()) {
				return TERRAIN().WATER.is(tx, ty) && !TERRAIN().WATER.isOpen(tx, ty);
			}
			throw new RuntimeException("" + t.name);
		}
		
		public int tiles(TERRAIN t) {
			return marks[t.index()+1] - marks[t.index()];
		}
		
		public int x(TERRAIN t, int i) {
			i+= marks[t.index()];
			int r = tiles[i];
			return r & 0x0FFFF;
		}
		
		public int y(TERRAIN t, int i) {
			i+= marks[t.index()];
			int r = tiles[i];
			return (r>>16) & 0x0FFFF;
		}
		
	}

}
