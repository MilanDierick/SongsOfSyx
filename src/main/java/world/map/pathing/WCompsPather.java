package world.map.pathing;

import game.faction.Faction;
import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.regions.Region;

public final class WCompsPather {

	private final WComps comps;
	private final IntChecker destCheck;
	private final Flooder f = RES.flooder();
	public final DestResult dest;
	public final CPath cPath;
	
	
	WCompsPather(WComps comps){
		this.comps = comps;
		destCheck = new IntChecker(comps.maxID());
		dest = new DestResult(comps);
		cPath = new CPath(comps);
	}
	
	public COORDINATE rnd(Region home) {
		RES.filler().init(this);
		RES.filler().fill(home.cx(), home.cy());
		RES.coos().set(0);
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (!home.is(c))
				continue;
			if (!WORLD.REGIONS().map.centre.is(c) && !WORLD.WATER().isBig.is(c)) {
				RES.coos().get().set(c);
				RES.coos().inc();
			}
			
			int md = comps.dirMap.get(c);
			for (DIR d : DIR.ALL) {
				if ((md & (1<<d.id())) != 0)
					RES.filler().fill(c, d);
			}
		}
		RES.filler().done();
		
		if (RES.coos().getI() == 0) {
			RES.coos().get().set(home.cx(), home.cy());
			RES.coos().inc();
		}
		RES.coos().set(RND.rInt(RES.coos().getI()));
		return RES.coos().get();
		
		
	}
	
	public COORDINATE rndDist(int cx, int cy, int dist) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(cx, cy, 0);
		PathTile backup = null;
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();

			if (c.getValue() >= dist) {
				RES.flooder().done();
				return c;
			}
			
			if (!WORLD.REGIONS().map.centre.is(c)) {
				backup = c;
			}
			
			int md = comps.dirMap.get(c);
			DIR d = DIR.ALL.rnd();
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if ((md & (1<<d.id())) != 0 && !WORLD.REGIONS().map.cTile.is(c, d))
					RES.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				d = d.next(1);
			}
		}
		RES.filler().done();
		return backup;
		
		
	}
	
	public COORDINATE rndDistOwn(int cx, int cy, int dist) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(cx, cy, 0);
		PathTile backup = null;
		
		Faction fa = WORLD.REGIONS().map.get(cx, cy).faction();
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();

			Region rr = WORLD.REGIONS().map.get(c);
			
			if (rr != null) {
				if (rr.faction() != null && rr.faction() != fa)
					continue;
			}
			
			if (c.getValue() >= dist) {
				RES.flooder().done();
				return c;
			}
			
			if (!WORLD.REGIONS().map.cTile.is(c)) {
				backup = c;
			}
			
			int md = comps.dirMap.get(c);
			DIR d = DIR.ALL.rnd();
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if ((md & (1<<d.id())) != 0 && !WORLD.REGIONS().map.cTile.is(c, d))
					RES.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				d = d.next(1);
			}
		}
		RES.filler().done();
		return backup;
		
		
	}
	
	
	public PathTile get(int sx, int sy, int destX, int destY, WTREATY treaty) {
		
		if (sx == destX && sy == destY) {
			RES.flooder().init(this);
			PathTile t = RES.flooder().close(sx, sy, 0);
			RES.flooder().done();
			return t;
		}
		
		if (!comps.route.is(destX, destY))
			return null;
		
		if (!WORLD.PATH().COMPS.route.is(sx, sy)) {
			
			DIR d = DIR.get(sx, sy, destX, destY);
			if (WORLD.PATH().COMPS.route.is(sx, sy, d))
				;
			else if (WORLD.PATH().COMPS.route.is(sx, sy, d.next(1)))
				d = d.next(1);
			else if (WORLD.PATH().COMPS.route.is(sx, sy, d.next(-1)))
				d = d.next(-1);
			else
				return null;
			sx += d.x();
			sy += d.y();
		}
		
		
		
		if (!dest.find(sx, sy, destX, destY, treaty))
			return null;
		
		if (dest.dest != null)
			return dest.dest;

		LIST<WComp> ccs = cPath.find(sx, sy, dest, treaty);
		if (ccs == null) {
			return null;
		}

		return findInCPath(ccs, sx, sy, destX, destY);
		
	}
	
	public PathTile findInCPath(LIST<WComp> ccs, int sx, int sy, int destX, int destY) {
		destCheck.init();

		for (WComp c : ccs) {
			destCheck.isSetAndSet(c.id);
		}
		
		f.init(this);
		f.pushSloppy(sx,  sy, 0);
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			WComp c = comps.get(t);
			
			double v = t.getValue();
				
			if (t.isSameAs(destX, destY)) {
				f.done();
				return t;
			}
			
			if (c != null) {
				if (!destCheck.isSet(c.id))
					continue;
				else {
					v = 0;
				}
					
			}
			
			
			push(t, v, comps);
			
		}
		f.done();
		
		return null;
	}
	
	
	static void push(PathTile t, double v, WComps comps) {
		int md = comps.dirMap.get(t);
		for (DIR d : DIR.ALL) {
			if ((md & (d.bit)) != 0)
				RES.flooder().pushSmaller(t, d, v+d.tileDistance()*cost(t.x(), t.y(), d), t);
		}
	}
	
	public static int cost(int fromX, int fromY, DIR d) {
		if (WORLD.WATER().isBig.is(fromX, fromY)) {
			return 1;
		}
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		if (WORLD.WATER().isBig.is(toX, toY))
			return WTRAV.PORT_PENALTY;
		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY))
			return 12;
		if (WORLD.FOREST().amount.get(fromX, fromY) == 1.0)
			return 4;
		return 3;
	}
	
	final static class CPath {
		
		public int distance;
		
		private final IntChecker startCheck;
		private final double[] startDists;
		private final ArrayList<WComp> starts;
		private final Flooder f = RES.flooder();
		private final WComps comps;
		
		CPath(WComps comps){
			this.comps = comps;
			startCheck = new IntChecker(comps.maxID());
			startDists = new double[comps.maxID()];
			starts = new ArrayList<WComp>(comps.maxID());
		}
		
		public LIST<WComp> find(int sX, int sY, DestResult dest, WTREATY treaty) {
			distance = 0;
			startCheck.init();
			starts.clear();
			WComp comp = comps.get(sX, sY);
			if (comp == null) {
				f.init(this);
				f.pushSloppy(sX, sY, 0);
				while(f.hasMore()){
					
					PathTile t = f.pollSmallest();
					WComp c = comps.get(t);
					
					if (c != null) {
						if (!startCheck.isSetAndSet(c.id)) {
							distance = (int) t.getValue();
							startDists[c.id] = t.getValue();
							starts.add(c);
						}
						continue;
					}
					
					push(t, t.getValue(), comps);
					
				}
				f.done();
			}else {
				startDists[comp.id] = 0;
				starts.add(comp);
			}
			
			if (starts.size() == 0)
				return null;
			
			return findCompPath(dest, treaty);
		}
		
		private LIST<WComp> findCompPath(DestResult dest, WTREATY treaty) {
			f.init(this);
			
			for (int i = 0; i < starts.size(); i++) {
				WComp c = starts.get(i);
				double dist = startDists[c.id];
				if (dest.destCheck.isSet(c.id)) {
					dist += dest.destDists[c.id];
				}
				f.pushSmaller(c.x, c.y, dist);
			}
			starts.clearSloppy();
			
			
			while(f.hasMore()) {
				
				PathTile t = f.pollSmallest();
				WComp c = comps.get(t);
				if (dest.destCheck.isSet(c.id)) {
					f.done();
					distance = (int) t.getValue();
					t = RES.flooder().reverse(t);
					while(t != null) {
						starts.add(comps.get(t));
						t = t.getParent();
					}
					
					return starts;
				}
				
				for (int i = 0; i < c.edges(); i++) {
					WComp to = c.edge(i);
					double dist = c.dist(i);
					if (!treaty.can(t.x(), t.y(), to.x, to.y, t.getValue()+dist))
						continue;
					if (dest.destCheck.isSet(to.id)) {
						dist += dest.destDists[to.id];
					}
					f.pushSmaller(to.x, to.y, t.getValue()+dist, t);
				}
				
			}
			f.done();
			return null;
			
		}
		
	}
	
	final static class DestResult {
		
		public PathTile dest;
		
		private final IntChecker destCheck;
		private final double[] destDists;
		private final Flooder f = RES.flooder();
		private final WComps comps;
		
		DestResult(WComps comps){
			this.comps = comps;
			destCheck = new IntChecker(comps.maxID());
			destDists = new double[comps.maxID()];
		}
		
		public boolean find(int sx, int sy, int destX, int destY, WTREATY treaty) {
			
			this.dest = null;
			
			destCheck.init();
			boolean found = false;
			WComp comp = comps.get(destX, destY);
			if (comp == null) {
				f.init(this);
				f.pushSloppy(destX, destY, 0);
				while(f.hasMore()){
					
					PathTile t = f.pollSmallest();
					if (t.isSameAs(sx, sy)) {
						f.done();
						t = f.reverse(t);
						dest = t;
						return true;
					}
					
					WComp c = comps.get(t);
					
					if (c != null) {
						if (!treaty.can(destX, destY, t.x(), t.y(), t.getValue()))
							continue;
						if (!destCheck.isSetAndSet(c.id)) {
							destDists[c.id] = t.getValue();
							found = true;
						}
						continue;
					}
					push(t, t.getValue(), comps);
					
				}
				f.done();
			}else {
				found = true;
				destCheck.isSetAndSet(comp.id);
				destDists[comp.id] = 0;
			}
			
			if (!found)
				return false;
			return true;
		}
		
	}
	
	
	
	
}
