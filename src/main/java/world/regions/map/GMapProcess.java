package world.regions.map;

import java.util.LinkedList;

import init.RES;
import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.map.pathing.WPATHING;
import world.map.pathing.WTRAV;
import world.regions.map.GMapTmp.TmpRegion;

final class GMapProcess {

	private final GMapTmp map;
	
	private LinkedList<TmpRegion> neighs = new LinkedList<>();
	private float[] areas;
	private final IntChecker nCheck;
	
	public GMapProcess(int px, int py, GMapTmp map){
		
		
		this.map = map;
		areas = new float[map.all.length];
		nCheck = new IntChecker(map.all.length);
		
		Tree<TmpRegion> sort = new Tree<TmpRegion>(map.all.length) {

			@Override
			protected boolean isGreaterThan(TmpRegion current, TmpRegion cmp) {
				return current.area > cmp.area;
			}
		
		};
		for (TmpRegion r : map.all) {
			if (r != null && r != map.player)
				sort.add(r);
		}

		while(sort.hasMore()) {
			TmpRegion t = sort.pollSmallest();
			process2(t);
			if (!t.done && t.valid)
				sort.add(t);
		}
		
//		for (TmpRegion r : map.all) {
//			if (r != null && r.valid) {
//				if (!r.hasCentre(map)) {
//					r.valid = false;
//				}
//			}
//		}
		
		map.expand();
		
		for (TmpRegion r : map.all) {
			if (r != map.player && r != null && r.valid) {
				if (!r.hasCentre(map)) {
					TmpRegion o = findOkNeighbour(r.c.x(), r.c.y());
					if (o != null)
						o.absorb(r, map, false);
					else
						r.valid = false;
				}
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			TmpRegion i = map.get(c);
			if (i == null && i != map.player && WTRAV.isGoodLandTile(c.x(), c.y())) {
				TmpRegion o = findOkNeighbour(c.x(), c.y());
				if (o != null) {
					for (DIR d : DIR.ALLC) {
						if (map.get(c, d) == null && map.get(c, d) != map.player) {
							map.set(c, d, o);
						}
					}
				}
				
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			TmpRegion i = map.get(c);
			
			if (i != null)
				if (!i.hasCentre()) {
					i.valid = false;
				}
			
		}


		
		finish(px, py);
		
		
	}
	
	
	private void finish(int px, int py){
		
		for (TmpRegion r : map.all) {
			if (r != null) {
				r.done = false;
			}
		}
		
		map.player.done = true;
		map.player.valid = true;
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(px, py, 0);
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			TmpRegion r = map.get(t);
			if (r != null) {
				if (r.c.isSameAs(t))
					r.done = true;
			}
			
			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
		}
		RES.flooder().done();
		

		for (COORDINATE c : WORLD.TBOUNDS()) {
			TmpRegion i = map.get(c);
			if (i != null && !i.done)
				i.valid = false;
					
			
		}

		
		
		
	}
	

	private TmpRegion findOkNeighbour(int sx, int sy) {

		RES.flooder().init(this);
		RES.flooder().pushSmaller(sx, sy, 0);

		for (DIR d : DIR.ORTHO) {
			if (WORLD.IN_BOUNDS(sx, sy, d))
				RES.flooder().pushSmaller(sx, sy, d, 0);
		}
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			TmpRegion other = map.get(c);
			
			if (other == null)
				continue;
			
			if (other == map.player)
				continue;
			
			if (c.getValue() > 64)
				break;
			
			if (other.hasCentre()) {
				RES.flooder().done();
				return other;
			}
			
			{
				int terrain = terrain(c.x(), c.y());
				double v = 1;
				for (DIR d : DIR.ORTHO) {
					int dx = c.x()+d.x();
					int dy = c.y()+d.y();
					if (!WORLD.IN_BOUNDS(dx, dy))
						continue;
					if (WORLD.MOUNTAIN().coversTile(c.x(), c.y()))
						continue;
					if (terrain != terrain(dx, dy))
						v *= WPATHING.getTerrainCost(dx, dy);
					
					RES.flooder().pushSmaller(dx, dy, c.getValue()+v*d.tileDistance());
				}
			}
			
			
			
			
		}
		RES.flooder().done();
		
		return null;
	}
	
	
	
	private void process2(final TmpRegion reg) {
		
		
		if (!reg.valid)
			return;
		
		boolean hC = reg.hasCentre(map);
		if (reg.area > 200 && hC)
			reg.done = true;
		if (reg.done) {
			return;
		}
		int tot = fillNeighbours(reg);
		if (neighs.size() == 0) {
			if (!hC) {
				//LOG.ln(reg + " " + reg.c);
			}
			reg.done = true;
			return;
		}
		
		double bValue = 0;
		TmpRegion best = null;
		
		for (TmpRegion o : neighs) {
			double a = value(reg, o, (double)areas[o.id]/tot);
			if (o != map.player && a > bValue) {
				bValue = a;
				best = o;
			}
		}
		
		if (hC && bValue < 0.00003) {
			reg.done = true;
			return;
		}
		
		if (best == null) {
			reg.done = true;
			return;
		}
		if (best == map.player)
			throw new RuntimeException();
		reg.absorb(best, map, true);
		
	}
	
	private double value(final TmpRegion reg, final TmpRegion other, double area) {
		double v = area*area;
		v /= (other.area+reg.area);

		double dissim = 0;
		for (TERRAIN t : TERRAINS.ALL()) {
			if (t != TERRAINS.NONE()) {
				double a = (double)reg.terrains[t.index()]/reg.area;
				double b = (double)other.terrains[t.index()]/other.area;
				dissim += Math.abs(a-b);
			}
		}
		
		v /= 1.0+dissim;
		
		return v;
		
		
		
	}
	
	private int fillNeighbours(final TmpRegion reg) {
		
		nCheck.init();
		neighs.clear();
		
		RES.flooder().init(this);
		RES.flooder().pushSmaller(reg.c.x(), reg.c.y(), 0);
		int total = 0;
		
		while(RES.flooder().hasMore()) {
			
			
			PathTile c = RES.flooder().pollSmallest();
			
			TmpRegion other = map.get(c);
			if (other == null || other == map.player)
				continue;
			
			if (other != reg) {
				if (!nCheck.isSetAndSet(other.id)) {
					areas[other.id] = 0;
					neighs.add(other);
				}
				areas[other.id] += 1.0/c.getValue();
				total++;
				continue;
			}
			
			{
				double v = 1 + WORLD.FERTILITY().map.get(c);
				for (DIR d : DIR.ALL) {
					if (WTRAV.canLand(c.x(), c.y(), d, false)) {
						int dx = c.x()+d.x();
						int dy = c.y()+d.y();
						TmpRegion to = map.get(dx, dy);
						if (other != to && !d.isOrtho()) {
							continue;
						}
						RES.flooder().pushSmaller(dx, dy, c.getValue()+v*d.tileDistance());
					}	
				}
			}
			
			
			
			
		}
		RES.flooder().done();
		return total;
	}


	
	public static int terrain(int tx, int ty) {
		if (WORLD.MOUNTAIN().getHeight(tx, ty) > 0)
			return 1;
		
		if (WORLD.WATER().isBig.is(tx, ty)) {
			if (WORLD.WATER().fertile.is(tx, ty))
				return 2;
			return 3;
		}
		if (WORLD.FOREST().amount.get(tx, ty) == 1)
			return 4;
		return 0;
	
		
	}
	
}