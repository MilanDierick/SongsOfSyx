package world.entity;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import snake2d.Path.COST;
import snake2d.Path.DEST;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.BOOLEAN_OBJECT;
import world.World;
import world.entity.caravan.Shipment;
import world.map.regions.*;

public final class WPathing {
	
	private static final RegPath regPath = new RegPath();
	
	public final static COST cost = new COST() {
		@Override
		public double getCost(int fromX, int fromY, int toX, int toY) {

			if (World.WATER().has.is(fromX, fromY)) {
				if (World.WATER().RIVER.is(fromX, fromY)) {
					if (World.WATER().has.is(toX, toY) && !World.WATER().RIVER.is(toX, toY))
						return 30;
					return 1;
				}
				if (!World.WATER().has.is(toX, toY))
					return 8;
				else
					return World.WATER().coversTile.is(toX, toY) ? 5 : 1;
			}else if(World.WATER().has.is(toX, toY)) {
				return 32;
			}
			
			
			if (World.MOUNTAIN().heighter.get(toX, toY) >= 1)
				return 16;
			if (World.BUILDINGS().roads.is(toX, toY))
				return 1;
			return 4;
		}
	};
	
	private static WorldPathCost costRegion;
	private static Region destRegion;
	private static int destTargetX, destTargetY;
	
	private static final IntChecker checker = new IntChecker(Regions.MAX);
	private static final ArrayList<FactionDistance> tmp = new ArrayList<>(FACTIONS.MAX);
	private static final FactionDistance[] holders = new FactionDistance[FACTIONS.MAX];
	
	static {
		for (int i = 0; i < holders.length; i++) {
			holders[i] = new FactionDistance();
		}
	}

	private final static float SQRT2 = (float) Math.sqrt(2);
	
	private final static COST cost2 = new COST() {
		@Override
		public double getCost(int fromX, int fromY, int toX, int toY) {

			Region from = World.REGIONS().getter.get(fromX, fromY);
			Region to = World.REGIONS().getter.get(toX, toY);
			if (from != null && to != null && !costRegion.canMove(from, to))
				return BLOCKED;
			return cost.getCost(fromX, fromY, toX, toY);
		}
	};
	
	private final static Checker checker2 = new Checker();
	private static class Checker {
		
		private short[] checks = new short[0];
		private short checkI = 0;
		
		void init() {
			checkI ++;
			if (checks.length != World.REGIONS().all().size()) {
				checks = new short[World.REGIONS().all().size()];
				checkI = 0;
			}else if(checkI == 0) {
				for (int i = 0; i < checks.length; i++)
					checks[i] = 0;
				checkI++;
			}
		}
		
		COST check(RegPath p) {
			init();
			if (p.size() == 0)
				return cost3;
			checks[p.get(0).index()] = checkI;
			for (int i = 0; i < p.size(); i++) {
				Region r = p.get(i);
				checks[r.index()] = checkI;
				for (int d = 0; d < r.distances(); d++) {
					checks[r.distanceNeigh(d).index()] = checkI;
				}
			}
			return cost3;
		}
		
		boolean checked(Region r) {
			if (r == null)
				return false;
			
			return checks[r.index()] == checkI;
		}
		
		private final COST cost3 = new COST() {
			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				Region from = World.REGIONS().getter.get(fromX, fromY);
				Region to = World.REGIONS().getter.get(toX, toY);
				if (!costRegion.canMove(from, to))
					return BLOCKED;
				if (to != null && !checked(to))
					return BLOCKED;
				return cost.getCost(fromX, fromY, toX, toY);
			}
		};
		
	}
	
	private final static DEST dest = new DEST() {
		
		@Override
		protected boolean isDest(int x, int y) {
			return World.REGIONS().getter.is(x, y, destRegion);
		}
		
		@Override
		protected float getOptDistance(int x, int y) {
			x = Math.abs(x-destTargetX);
			y = Math.abs(y-destTargetY);
			
			if (x > y){
				return SQRT2*y + x-y;
			}else if(x < y){
				return SQRT2*x + y-x;
			}else{
				return SQRT2*x;
			}
		}
	};

	
	public static boolean path(int sx, int sy, int dx, int dy, WPath path) {
		return path(sx, sy, dx, dy, path, WorldPathCost.dummy);
	}
	
	public static boolean path(int sx, int sy, int dx, int dy, WPath path, WorldPathCost cost) {
		
		Region from = getStart(sx, sy);
		Region to = World.REGIONS().getter.get(dx, dy);
		path.clear();
		
		RegPath p = regPath(from, to, cost, Integer.MAX_VALUE);
		if (p == null)
			return false;
		
		costRegion = cost;
		path.destX = (short) dx;
		path.destY = (short) dy;
		if (p.size() <= 2) {
			if (RES.pathTools().astar.getShortest(path, WPathing.cost, sx, sy, dx, dy, true))
				return true;
		}else {
			destRegion = p.get(2);
			destTargetX = p.get(2).cx();
			destTargetY = p.get(2).cy();
			if (RES.pathTools().astar.getNearest(path, checker2.check(p), dest, sx, sy)) {
				return true;
			}
		}
		
		GAME.Notify(" " + sx + " " + sy + " "+ dx + " " + dy);
		path.clear();
		return false;
	}
	
	public static boolean intercept(int sx, int sy, WEntity other, WPath path, WorldPathCost cost) {
		
		
		if (!other.path().isValid()) {
			
			return path(sx, sy, other.ctx(), other.cty(), path, cost);
		}
		
		path.clear();
		
		Region from = getStart(other.ctx(), other.cty());
		Region to = World.REGIONS().getter.get(other.path().destX(), other.path().destY());
		
		RegPath p = regPath(from, to, cost, Integer.MAX_VALUE);

		if (p == null)
			return path(sx, sy, other.ctx(), other.cty(), path, cost);
		
		to = getStart(sx, sy);
		RES.flooder().init(path);
		
		for (int ri = 0; ri < p.size(); ri++) {
			Region r = p.get(ri);
			RES.flooder().pushSloppy(r.cx(), r.cy(), p.distance-p.dists[ri]);
		}
		
		
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region current = World.REGIONS().getter.get(t);
			if (current == to) {
				RES.flooder().done();
				regPath.clear();
				regPath.distance = t.getValue();
				
				int i = 1;
				while(t != null) {
					Region r = World.REGIONS().getter.get(t);
					regPath.add(r);
					regPath.dists[i++] = (int) t.getValue();
					t = t.getParent();
				}
				
				
				
				return intercept(sx, sy, other, path, regPath, cost);
			}
			
			for (int i = 0; i < current.distances(); i++) {
				Region r2 = current.distanceNeigh(i);
				if (!cost.canMove(current, r2))
					continue;
				double v = t.getValue()+current.distance(i);
				RES.flooder().pushSmaller(r2.cx(), r2.cy(), v, t);
			}
			
		}
		
		RES.flooder().done();
		return false;
		
	}
	
	private static boolean intercept(int sx, int sy, WEntity other, WPath path, RegPath regPath, WorldPathCost cost) {
		
		path.destX = (short) other.ctx();
		path.destY = (short) other.cty();
		costRegion = cost;
		if (regPath.size() > 3) {
			destRegion = regPath.get(3);
			destTargetX = regPath.get(3).cx();
			destTargetY = regPath.get(3).cy();
			if (RES.pathTools().astar.getNearest(path, cost2, dest, sx, sy)) {
				return true;
			}
			path.destX = -1;
			path.destY = -1;
			return false;
		}
		
		if (sx == other.ctx() && sy == other.cty()) {
			return path(sx, sy, other.ctx(), other.cty(), path, cost);
		}
		
		if (!other.path().isValid())
			return path(sx, sy, other.ctx(), other.cty(), path, cost);
		
		WPath tmp = other.path().copyTmp();
		RES.flooder().init(path);
		RES.flooder().pushSloppy(tmp.x(), tmp.y(), 0);
		int dx = tmp.x();
		int dy = tmp.y();
		double d = 0;
		while(tmp.hasNext()) {
			tmp.setNext();
			if ((dx-tmp.x())*(dy-tmp.y()) != 0) {
				d+= SQRT2;
			}else
				d+= 1;
			RES.flooder().pushSloppy(tmp.x(), tmp.y(), d);
			RES.flooder().setValue2(tmp.x(), tmp.y(), d);
		}
		if (RES.flooder().hasBeenPushed(sx, sy)) {
			RES.flooder().done();
			RES.flooder().init(path);
			PathTile p = RES.flooder().pushSloppy(sx, sy, 0);
			while(tmp.x() != sx && tmp.y() != sy) {
				if (!tmp.setPrev())
					throw new RuntimeException();
			}
			while(tmp.hasPrev()) {
				p = RES.flooder().pushSloppy(tmp.x(), tmp.y(), d, p);
				tmp.setPrev();
			}
			path.set(p);
			RES.flooder().done();
			return true;
		}
		
		//regular pathfinding to sx,sy

		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			if (t.x() == sx && t.y() == sy) {
				PathTile pp = t;
				while(pp.getParent() != null)
					pp = pp.getParent();
				t = reverse(t, null);
				path.set(t);
				
				RES.flooder().done();
				return true;
			}
			
			double vv = t.getValue()-t.getValue2();
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dd = DIR.ALL.get(di);
				int ddx = t.x()+dd.x();
				int ddy = t.y()+dd.y();
				if (!World.IN_BOUNDS(ddx, ddy))
					continue;
				double c = cost2.getCost(t.x(), t.y(), ddx, ddy);
				if (c > 0) {
					PathTile tt =  RES.flooder().pushSmaller(ddx, ddy, vv+c, t);
					if (tt != null)
						RES.flooder().setValue2(tt, 0);
				}
			}
			
		}
		path.destX = (short) -1;
		path.destY = (short) -1;
		RES.flooder().done();
		return false;
	}
	
	private static PathTile reverse(PathTile t, PathTile newParent) {
		PathTile oldParent = t.getParent();
		t.parentSet(newParent);
		if (oldParent != null)
			return reverse(oldParent, t);
		return t;
	}
	
	public static void checkRegionMovement(int sx, int sy, WorldPathCost cost) {
		Region start = getStart(sx, sy);
		RES.flooder().init(regPath);
		RES.flooder().pushSloppy(start.cx(), start.cy(), 0);
		
		checker.init();
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region r = World.REGIONS().getter.get(t);
			checker.isSetAndSet(r.index());
			for (int i = 0; i < r.distances(); i++) {
				Region d = r.distanceNeigh(i);
				if (cost.canMove(r, d))
					RES.flooder().pushSmaller(d.cx(), d.cy(), t.getValue() + r.distance(i));
			}
		}
		
		RES.flooder().done();
	}
	
	
	public static boolean checkRegionIs(Region r) {
		return checker.isSet(r.index());
	}
	
	private static Region getStart(int sx, int sy) {
		Region start = World.REGIONS().getter.get(sx, sy);
		if (start != null)
			return start;
		
		RES.flooder().init(regPath);
		RES.flooder().pushSloppy(sx, sy, 0);
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			start = World.REGIONS().getter.get(t);
			if (start != null) {
				RES.flooder().done();
				return start;
			}
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (World.IN_BOUNDS(dx, dy)) {
					double c = cost.getCost(t.x(), t.y(), dx, dy);
					if (c > 0) {
						RES.flooder().pushSloppy(dx, dy, t.getValue()+c);
					}
				}
			}
		}
		RES.flooder().done();
		return null;
		
	}
	
	public static COORDINATE random(Region r) {
		for (int i = 0; i < 256; i++) {
			int x1 = r.bounds().x1() + RND.rInt(r.bounds().width());
			int y1 = r.bounds().y1() + RND.rInt(r.bounds().height());
			if (World.REGIONS().getter.is(x1, y1, r) && !World.WATER().isOCEAN.is(x1, y1) && !World.WATER().isLaky.is(x1, y1)) {
				Coo.TMP.set(x1, y1);
				return Coo.TMP;
			}
		}
		Coo.TMP.set(r.cx()+RND.rInt0(1), r.cy()+RND.rInt0(1));
		return Coo.TMP;
	}
	
	public static RegPath regPath(Region from, Region to, int maxDistance) {
		return regPath(from, to, WorldPathCost.dummy, maxDistance);
	}
	
	public static RegPath regPath(int sx, int sy, int dx, int dy, int maxDistance) {
		Region start = getStart(sx, sy);
		Region dest = getStart(dx, dy);
		return regPath(start, dest, maxDistance);
	}
	
	public static Region findAdjacentRegion(int sx, int sy, BOOLEAN_OBJECT<Region> founder) {
		final Region o = World.REGIONS().getter.get(sx, sy);
		Region start = o;
		
		if (start != null && founder.is(start))
			return start;
		
		RES.flooder().init(regPath);
		RES.flooder().pushSloppy(sx, sy, 0);
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			start = World.REGIONS().getter.get(t);
			if (start != null && founder.is(start)) {
				RES.flooder().done();
				return start;
			}
			if (start== null ||( start != o && !start.isWater()))
				continue;
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (World.IN_BOUNDS(dx, dy)) {
					double c = cost.getCost(t.x(), t.y(), dx, dy);
					if (c > 0) {
						RES.flooder().pushSloppy(dx, dy, t.getValue()+c);
					}
				}
			}
		}
		RES.flooder().done();
		return null;
		
	}
	
	public static Region findAdjacentRegion(Region o, BOOLEAN_OBJECT<Region> founder) {
		Region start = o;
		
		if (start != null && founder.is(start))
			return start;
		
		RES.flooder().init(regPath);
		RES.flooder().pushSloppy(o.cx(), o.cy(), 0);
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			start = World.REGIONS().getter.get(t);
			if (start != null && founder.is(start)) {
				RES.flooder().done();
				return start;
			}
			if (start != o && !start.isWater())
				continue;
			
			for (int i = 0; i < start.distances(); i++) {
				Region to = start.distanceNeigh(i);
				double v = start.distance(i);
				RES.flooder().pushSloppy(to.cx(), to.cy(), t.getValue()+v);
			}
		}
		RES.flooder().done();
		return null;
		
	}
	
	public static Region findRegion(int sx, int sy, BOOLEAN_OBJECT<Region> founder) {
		Region o = World.REGIONS().getter.get(sx, sy);
		Region start = o;
		
		if (start != null && founder.is(start))
			return start;
		
		RES.flooder().init(regPath);
		RES.flooder().pushSloppy(sx, sy, 0);
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			start = World.REGIONS().getter.get(t);
			if (start != o)
				continue;
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (World.IN_BOUNDS(dx, dy)) {
					double c = cost.getCost(t.x(), t.y(), dx, dy);
					if (c > 0) {
						RES.flooder().pushSloppy(dx, dy, t.getValue()+c);
					}
				}
			}
		}
		RES.flooder().done();
		return null;
		
	}
	
	public static RegPath regPath(Region from, Region to, WorldPathCost cost, int maxDistance) {
		if (from == null || to == null)
			return null;
		if (from.area() <= 0 || to.area() <= 0)
			return null;
		
		regPath.clear();
		if (from == to) {
			regPath.add(from);
			regPath.distance = 0;
			return regPath;
		}
		
		RES.flooder().init(regPath);
		
		RES.flooder().pushSloppy(from.cx(), from.cy(), 0);
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			Region current = World.REGIONS().getter.get(t);
			if (current == to) {
				regPath.distance = t.getValue();
				RES.flooder().done();
				int i = 0;
				while(t != null) {
					Region r = World.REGIONS().getter.get(t);
					regPath.add(r);
					regPath.dists[i++] = (int) t.getValue();
					t = t.getParent();
				}
				regPath.reverse();
				return regPath;
			}
			if (t.getValue() >= maxDistance)
				break;
			
			for (int i = 0; i < current.distances(); i++) {
				Region r2 = current.distanceNeigh(i);
				if (!cost.canMove(current, r2))
					continue;
				double v = t.getValue()+current.distance(i);
				RES.flooder().pushSmaller(r2.cx(), r2.cy(), v, t);
			}
			
		}
		RES.flooder().done();
		return null;
	}
	
	public static LIST<FactionDistance> getFactions(Faction f) {
		return getFactions(f, Shipment.MAX_DISTANCE);
	}
	
	public static LIST<FactionDistance> getFactions(Faction f, int maxDistance) {
		
		if (f.capitolRegion() == null)
			return null;
		
		int hi = 0;
		tmp.clear();
		
		RES.flooder().init(tmp);
		
		for (Region from : f.kingdom().realm().regions()) {
			RES.flooder().pushSloppy(from.cx(), from.cy(), 0);
		}
		
		checker.init();
		checker.isSetAndSet(f.index());
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			
			Region current = World.REGIONS().getter.get(t);
			FRegions r = REGIOND.REALM(current);
			
			
			
			if (r != null && !checker.isSet(r.faction().index())) {
				checker.isSetAndSet(r.faction().index());
				FactionDistance ff = holders[hi++];
				
				ff.f = REGIOND.faction(current);
				ff.distance = (int) t.getValue();
				
				tmp.add(ff);
			}
			
			if (t.getValue() > maxDistance)
				break;
			
			for (int i = 0; i < current.distances(); i++) {
				Region r2 = current.distanceNeigh(i);
				double v = t.getValue();
				if (r2.faction() == null || r2.faction().capitolRegion() != r2)
					v += current.distance(i);
				RES.flooder().pushSmaller(r2.cx(), r2.cy(), v, t);
			}
			
		}
		RES.flooder().done();
		return tmp;
	}

	public static final class FactionDistance {
		
		private FactionDistance() {
			
		}
		
		public Faction f;
		public int distance;
		
	}
	
	public interface WorldPathCost {
		public static WorldPathCost dummy = new WorldPathCost() {
			
			@Override
			public boolean canMove(Region a, Region b) {
				return true;
			}
		};
		public boolean canMove(Region a, Region b);
	}
	
	public static final class RegPath extends ArrayList<Region>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public double distance;
		private int[] dists = new int[FACTIONS.MAX];
		
		private RegPath() {
			super(FACTIONS.MAX);
		}
	}
}
