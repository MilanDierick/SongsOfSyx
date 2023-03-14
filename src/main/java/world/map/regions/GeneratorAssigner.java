package world.map.regions;

import static world.World.*;

import init.RES;
import init.config.Config;
import snake2d.CORE;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.Polymap;
import world.World;

class GeneratorAssigner {

	public final static int maxSize = Config.WORLD.REGION_SIZE;
	
	private final Polymap polly = new Polymap(TWIDTH(), THEIGHT(), (int) (40*(TWIDTH()/256.0)), 1.0);
	private final Region none = null;
	private Region temp;
	
	public GeneratorAssigner(int nr) {
		temp = REGIONS().getByIndex(nr);
		nr++;
		
		
		
		//make land regions
		for (COORDINATE c : TBOUNDS()) {
			if (nr >= Regions.MAX)
				break;
			if (assignRegion(c.x(), c.y(), REGIONS().getByIndex(nr), 250))
				nr++;
		}
		CORE.checkIn();
		//join small lands with big ones
		for (COORDINATE c : TBOUNDS()) {
			Region r = REGIONS().setter.get(c);
			if (r == temp) {
				Region r2 = join(c.x(), c.y(), r);
				if (r2 != null)
					assign(c.x(), c.y(), temp, r2);
				else {
					assign(c.x(), c.y(), temp, none);
				}	
			}
		}
		CORE.checkIn();
		//make big waters
		for (COORDINATE c : TBOUNDS()) {
			if (nr >= Regions.MAX)
				break;
			if (assignWater(c.x(), c.y(), REGIONS().getByIndex(nr), 250)) {
				nr++;
			}
		}
		CORE.checkIn();
		//join small waters
		for (COORDINATE c : TBOUNDS()) {
			Region r = REGIONS().setter.get(c);
			if (r == temp) {
				Region r2 = joinWater(c.x(), c.y(), r);
				if (r2 != null)
					assign(c.x(), c.y(), temp, r2);
				else {
					assign(c.x(), c.y(), temp, none);
				}	
			}
		}
		
		CORE.checkIn();
		
		//make small lands /waters
		for (COORDINATE c : TBOUNDS()) {
			if (nr >= Regions.MAX)
				break;
			Region r = REGIONS().setter.get(c);
			if (r == null) {
				if (assignRegion(c.x(), c.y(), REGIONS().getByIndex(nr), 16))
					nr++;
				else if(assignWater(c.x(), c.y(), REGIONS().getByIndex(nr), 50))
					nr++;
			}
		}
		
		CORE.checkIn();
		
		//join what can be joined
		for (COORDINATE c : TBOUNDS()) {
			Region r = REGIONS().setter.get(c);
			if (r == temp) {
				Region r2 = join(c.x(), c.y(), r);
				assign(c.x(), c.y(), r, r2);
			}
		}
	}
	
	private boolean assignWater(int tx, int ty, Region nr, int minSize) {
		
		
		if (REGIONS().setter.get(tx, ty) != none || !World.WATER().coversTile.is(tx, ty))
			return false;
		
		
		int maxDist = 400;
		
		polly.checkInit();
		polly.checker.set(tx, ty, true);
		
		polly.checker.set(tx, ty, true);
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(tx, ty, 0);
		int size = 0;
		int area = 0;
		int dist = 0;
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (!World.WATER().coversTile.is(t) || REGIONS().setter.get(t.x(), t.y()) != none) {
				continue;
			}
			size ++;
			area += 2;
			dist = (int) Math.max(COORDINATE.tileDistance(tx, ty, t.x(), t.y()), dist);
			if (!polly.checker.is(t)) {
				if (area > maxSize)
					break;
				if (dist > maxDist)
					continue;
			}
			polly.checker.set(t, true);
			
			double v = t.getValue();
			
			REGIONS().setter.set(t, nr);
			if (t.x() < x1)
				x1 = t.x();
			if (t.x() > x2)
				x2 = t.x();
			if (t.y()< y1)
				y1 = t.y();
			if (t.y() > y2)
				y2 = t.y();
			
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d)) {
					
					double q = v + d.tileDistance();
					double dd = polly.checker.is(t, d) ? q : q+100;
					RES.flooder().pushSmaller(t, d, dd);
					
				}
			}
			
		}

		RES.flooder().done();
		if (size < minSize) {
			assign(tx, ty, nr, temp);
			return false;
		}
		nr.isWater = true;
		
		return true;
	}
	
	private boolean assignRegion(int tx, int ty, Region nr, int minSize) {
		
		if (REGIONS().setter.get(tx, ty) != none || !REGIONS().placable.is(tx, ty))
			return false;
		
		int maxDist = 200;
		
		polly.checkInit();
		polly.checker.set(tx, ty, true);
		
		polly.checker.set(tx, ty, true);
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(tx, ty, 0);
		int size = 0;
		int area = 0;
		int dist = 0;
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (!REGIONS().placable.is(t) || REGIONS().setter.get(t.x(), t.y()) != none) {
				continue;
			}
			size ++;
			area += Math.ceil(FERTILITY().map.get(t.x(), t.y())*10);
			dist = (int) Math.max(COORDINATE.tileDistance(tx, ty, t.x(), t.y()), dist);
			if (!polly.checker.is(t)) {
				if (area > maxSize)
					break;
				if (dist > maxDist)
					continue;
			}
			polly.checker.set(t, true);
			
			double v = t.getValue();
			if (WATER().isRivery.is(t.x(), t.y())) {
				v+= 200;
			}
			
			
			REGIONS().setter.set(t, nr);
			if (t.x() < x1)
				x1 = t.x();
			if (t.x() > x2)
				x2 = t.x();
			if (t.y()< y1)
				y1 = t.y();
			if (t.y() > y2)
				y2 = t.y();
			
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d)) {
					if (MOUNTAIN().heighter.get(t) >= 1 && MOUNTAIN().heighter.get(t, d) >= 1)
						continue;
					
					double q = v + d.tileDistance();
					double dd = polly.checker.is(t, d) ? q : q+100;
					RES.flooder().pushSmaller(t, d, dd);
					
				}
			}
			
		}

		RES.flooder().done();
		
		if (size < minSize) {
			assign(tx, ty, nr, temp);
			return false;
		}
		
		boolean ok = false;
		for (int y = y1; y < y2 && !ok; y++) {
			for (int x = x1; x < x2; x++) {
				if (World.REGIONS().setter.is(tx,ty,nr) && CapitolPlacablity.region(x, y) == null) {
					ok = true;
					break;
					
				}	
			}
		}
		
		if (!ok) {
			assign(tx, ty, nr, temp);
			return false;
		}
		
		nr.isWater = false;
		
		return true;
	}

	
	private Region join(int tx, int ty, Region area) {
		RES.flooder().init(this);
		
		
		RES.flooder().pushSloppy(tx, ty, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (REGIONS().setter.get(t) == none)
				continue;
			if (REGIONS().setter.get(t) != area) {
				RES.flooder().done();
				return REGIONS().setter.get(t);
			}
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d)) {
					if (MOUNTAIN().heighter.get(t, d) >= 1)
						RES.flooder().pushSmaller(t, d, 10 + t.getValue()+d.tileDistance());
					else
						RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
		}
		
		RES.flooder().done();
		
		return none;
	}
	
	private Region joinWater(int tx, int ty, Region area) {
		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(tx, ty, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (REGIONS().setter.get(t) == none)
				continue;
			if (REGIONS().setter.get(t) != area) {
				RES.flooder().done();
				return REGIONS().setter.get(t);
			}
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d)) {
					if (World.WATER().coversTile.is(t, d))
						RES.flooder().pushSmaller(t, d, 10 + t.getValue()+d.tileDistance());
					else
						RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
		}
		
		RES.flooder().done();
		
		return none;
	}
	
	private void assign(int tx, int ty, Region old, Region newa) {
		
		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(tx, ty, 0);
		
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (REGIONS().setter.get(t) != old)
				continue;
			REGIONS().setter.set(t, newa);
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
			
		}
		
		RES.flooder().done();
	}

	
}
