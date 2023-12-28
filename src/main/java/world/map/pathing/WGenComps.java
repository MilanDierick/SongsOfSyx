package world.map.pathing;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.*;
import world.WORLD;
import world.regions.Region;

final class WGenComps {

	final int nothing = 0;
	public final Bitsmap2D map = new Bitsmap2D(0, 16, WORLD.TBOUNDS());
	public static final int max = 8;
	private MAP_BOOLEAN isser;
	public final int MAX;
	private Bitmap2D used = new Bitmap2D(WORLD.TBOUNDS(), false);
	final Bitsmap2D dirMap = new Bitsmap2D(0, 8, WORLD.TBOUNDS());
	
	
	WGenComps(MAP_BOOLEAN isser){
		this.isser = isser;
		int id = 1;
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = WORLD.REGIONS().map.get(c);
			if (r != null && r.active() && isser.is(c)) {
				if (c.isSameAs(r.cx(), r.cy()))
					map.set(c, id++);
			}
//			if (WORLD.ROADS().HARBOUR.is(c))
//				map.set(c, id++);
			
			boolean must = false;
			boolean neigh = false;
			if (r != null) {
			for (DIR d : DIR.ALL) {
				if (!must && !neigh && canPath(c.x(), c.y(), d) && r != WORLD.REGIONS().map.get(c, d))
					must = true;
				if (!neigh && map.get(c, d) != 0)
					neigh = true;
			}
			}
			
			if (must) {
				map.set(c, id++);
			}
			
		}
		
		int fixed = id;
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (isser.is(c) && map.get(c) == nothing) {
				fill(c, id++);
			}
			
		}
		
		boolean[] tested = new boolean[id];
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int i = map.get(c);
			if (i != nothing && !tested[i] && i >= fixed) {
				tested[i] = true;
				if (WORLD.ROADS().HARBOUR.is(c))
					continue;
				join(c.x(), c.y(), i, fixed);
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int i = map.get(c);
			if (i != nothing && i >= fixed) {
				
				boolean remove = false;
				boolean canRemove = true;
				for (DIR d : DIR.ALL) {
					if (canPath(c.x(), c.y(), d)){
						int oi = map.get(c, d);
						remove |= oi != nothing && oi < fixed;
						canRemove &= i == oi || oi == nothing;
					}
					
					
				}
				
				if (remove || canRemove)
					map.set(c, nothing);
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int i = map.get(c);
			if (i != nothing && i >= fixed) {
				
				for (DIR d : DIR.ALL) {
					if (canPath(c.x(), c.y(), d)){
						int oi = map.get(c, d);
						if (oi != nothing) {
							map.set(c, nothing);
							break;
						}
					}
					
				}
					
			}
		}
		
		id = 1;
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int i = map.get(c);
			if (i != nothing) {
				map.set(c, id++);
			}
			
		}
		
		MAX = id;
		
		
		
	}
	
	private void join(int tx, int ty, int id, int fixed) {
		
		RES.filler().init(this);
		RES.filler().fill(tx, ty);
		int neigh = nothing;
		int size = 0;
		while(RES.filler().hasMore()) {
			
			COORDINATE c = RES.filler().poll();
			int oid = map.get(c);
			if (id != oid && oid >= fixed) {
				if (neigh == 0)
					neigh = oid;
				continue;
			}
			size ++;
			for (DIR d : DIR.ALL) {
				if (canPath(c.x(), c.y(), d))
					RES.filler().fill(c, d);
			}
		}
		RES.filler().done();
		
		if (size > max)
			return;
		
		if (neigh == nothing) {
			return;
		}
		
		
		RES.filler().init(this);
		RES.filler().fill(tx, ty);
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			int oid = map.get(c);
			if (id != oid) {
				continue;
			}
			map.set(c, neigh);
			for (DIR d : DIR.ALL) {
				RES.filler().fill(c, d);
			}
		}
		RES.filler().done();
		
	}
	
	private void fill(COORDINATE c, int id) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(c, 0);
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (map.get(t) != nothing) {
				continue;
			}
			if (t.getValue() >= max) {
				int am = 0;
				for (DIR d : DIR.ALL) {
					if (canPath(t.x(), t.y(), d))
						am++;
				}
				if (am <= 2)
					continue;
			}
			map.set(t, id);
			
			for (DIR d : DIR.ALL) {
				if (canPath(t.x(), t.y(), d)) {
					RES.flooder().pushSloppy(t.x(), t.y(), d, t.getValue()+d.tileDistance(), t);
				}
			}
		}
		RES.flooder().done();

	}
	
	

	
	public WComp[] makeComps() {
		Bitmap1D checked = new Bitmap1D(MAX, false);
		IntChecker ch = new IntChecker(MAX);
		
		WComp[] comps = new WComp[MAX];
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int i = map.get(c);
			if (i != nothing && !checked.get(i)){
				checked.set(i, true);
				comps[i] = fix(c.x(), c.y(), ch);
			}
		}
		
		return comps;
	}
	
	
	int connections;
	
	WComp fix(final int tx, final int ty, IntChecker ch) {
		final int id = map.get(tx, ty);
		short[] data = new short[0];
		
		ch.init();
		RES.flooder().init(this);
		RES.flooder().pushSloppy(tx,  ty, 0);
		RES.flooder().setValue2(tx, ty, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			int oi = map.get(t);
			if (oi != nothing && oi != id) {
				if (!ch.isSetAndSet(oi)) {
					markUsed(t);					
					data = push(data, oi, Math.round(t.getValue2()));
					connections ++;
				}
				continue;
			}
			double cost = 1;
			
			if (used.is(t))
				cost *= 0.5;
			for (DIR d : DIR.ALL) {
				if (canPath(t.x(), t.y(), d)) {
					double v = WTRAV.cost(t.x(), t.y(), d);
					if (RES.flooder().pushSmaller(t, d, t.getValue()+v*cost, t) != null)
						RES.flooder().setValue2(t, d, t.getValue()+v);
				}
			}
			
		}
		RES.flooder().done();
		
		
		return new WComp(id, tx, ty, data);
	}
	
	private void markUsed(PathTile t) {
		PathTile from = null;
		
		
		while(t != null) {
			used.set(t, true);
			if (from != null) {
				DIR d = DIR.get(from, t);
				dirMap.set(from, dirMap.get(from) | (1<<d.id()));
				dirMap.set(t, dirMap.get(t) | (1<<d.perpendicular().id()));
				
			}
			from = t;
			t = t.getParent();
		}
	}
	
	private short[] push(short[] data, int to, int dist) {
		short[] ndists = new short[data.length+2];
		for (int i = 0; i < data.length; i++) {
			ndists[i] = data[i];
		}
		ndists[data.length] = (short) to;
		ndists[data.length+1] = (short) dist;
		return ndists;
	}


	public boolean canPath(int fromX, int fromY, DIR d) {
		
		if (!isser.is(fromX, fromY))
			return false;
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		if (!isser.is(toX, toY))
			return false;
		
		if (!WTRAV.can(fromX, fromY, d, false))
			return false;

		return true;
		
		
	}



	
}
