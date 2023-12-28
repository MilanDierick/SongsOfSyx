package settlement.tilemap.generator;

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
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import world.regions.map.RegionInfo;

final class GeneratorMinerals {

	private final HeightMap height2 = new HeightMap(TWIDTH, THEIGHT, 16, 4);
	private final HeightMap height3 = new HeightMap(TWIDTH, THEIGHT, 16, 4);
	private final TerrainCount tc;
	
	GeneratorMinerals(CapitolArea area, GeneratorUtil util, LinkedList<COORDINATE> caves) {

		
		tc = new TerrainCount();
		
		if (area.isBattle)
			return;
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			SETT.MINERALS().getter.set(c, null);
		}
		
		for (Minable m : RESOURCES.minables().all()) {
			
			double v = RegionInfo.minableBonus(m, area.minable(m));
			double ri = CLAMP.d(v, 0, 1);
			int size = (int) (350 + 1000*(v-ri));
			
			generate(util, m, ri, size);
			if (SETT.MINERALS().totals.get(m) <= 0)
				generate(util, m, ri, size);
		}

		
		for (Minable m : RESOURCES.minables().all()) {
			log(m.resource.name + " " + SETT.MINERALS().totals.get(m));
		}
		
		blurEdges();
	
		
	}
	
	private void log(Object o) {
		//LOG.ln(o);
	}
	
	private void generate(GeneratorUtil util, Minable m, double base, double size) {
		double s = size;
		while (s > 0) {
			double ai = size;
			if (size > 20) {
				ai = size*0.5+RND.rFloat()*0.5;
			}
			ai = CLAMP.d(ai, 0, s);

			s-= ai;
			double rich = RND.rFloat1(0.1);
			ai/= rich;
			
			make(util, m, base*rich, ai);
			
		}
		
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
		
		if (!placable.is(x, y))
			return false;
		
		log("making " + t.resource.name + " " + amount + " " + size);

		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(x, y, 0);
		RES.flooder().setValue2(x, y, 0);
		double nor = height3.get(x, y);
		
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			if (!placable.is(c))
				continue;
			double dh = 0.5 + height2.get(c)*0.5;
			size -= dh;
			if (size < 0)
				break;
			double dist = c.getValue2();
			
			MINERALS().getter.set(c, t);
			MINERALS().amountD.set(c, amount*dh);
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				if (IN_BOUNDS(c, dir)) {
					double v = height3.get(c, dir);
					v = Math.abs(v-nor);
					double ddsist = dist + dir.tileDistance();
					v += ddsist/64.0;
					if (RES.flooder().pushSmaller(c, dir, v) != null) {
						RES.flooder().setValue2(c, dir, ddsist);
					}
				}

			}
			
			
		}
		
		RES.flooder().done();
		
		
		return true;
	}
	
	private void blurEdges() {
		double e = 3.0;
		RES.flooder().init(this);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.MINERALS().amountInt.get(c)== 0)
				RES.flooder().pushSloppy(c, 0);
		}
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			if (c.getValue() > e)
				break;
			
			double am = 1.0-c.getValue()/e;
			SETT.MINERALS().amountD.set(c, SETT.MINERALS().amountD.get(c)*am);
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				if (IN_BOUNDS(c, dir) && SETT.MINERALS().amountInt.get(c) > 0) {
					double v = c.getValue()+dir.tileDistance();
					RES.flooder().pushSmaller(c, dir, v);
				}
			}
			
			
		}
		
		RES.flooder().done();
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
				return TERRAIN().WATER.SHALLOW.is(tx, ty) && !TERRAIN().WATER.open.is(tx, ty); 
			}else if(t == TERRAINS.WET()) {
				return TERRAIN().WATER.SHALLOW.is(tx, ty) && !TERRAIN().WATER.open.is(tx, ty);
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
