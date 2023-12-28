package settlement.tilemap.generator;

import static settlement.main.SETT.*;

import game.GAME;
import init.RES;
import settlement.entry.EntryPoints;
import settlement.entry.EntryPoints.EntryPoint;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.Path;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LinkedList;
import world.WORLD;
import world.map.pathing.WTRAV;

final class GeneratorRoads {

	private final Bitmap2D edge = new Bitmap2D(SETT.TILE_BOUNDS, false);
	private final Path.PathFancy path = new Path.PathFancy(5000);
	private final EntryPoints ees = SETT.ENTRY().points;
	
	final Path.COST cm = new Path.COST() {

		@Override
		public double getCost(int fromX, int fromY, int toX, int toY) {

			if (SETT.TERRAIN().WATER.BRIDGE.is(toX, toY))
				return 0.2;
			
			if (!SETT.TERRAIN().get(toX, toY).clearing().isEasilyCleared()) {
				return 25;
			}
			
			if (RES.flooder().getValue2(toX, toY) != 0)
				return 10;
			
			if (SETT.TERRAIN().get(toX, toY) != SETT.TERRAIN().NADA) {
				return 2;
			}
			
			if (!edge.is(toX, toY))
				return 2;
			
			if (SETT.FLOOR().getter.get(toX, toY) != null)
				return 0.2;
			
			return 1;
			
		}
	};
	
	GeneratorRoads(CapitolArea area){
		
		{
			Polymap polly = new Polymap(SETT.TWIDTH, SETT.THEIGHT, 8, 1);
			for (COORDINATE c : SETT.TILE_BOUNDS) {
				edge.set(c, polly.isEdge(c.x(), c.y()));
				SETT.MAINTENANCE().disabled.set(c, false);
			}
		}
		
		
		boolean[] isToActivate = new boolean[ees.all().size()];
		LinkedList<EntryPoint> toActivate = new LinkedList<>();
		LinkedList<EntryPoint> toFindOther = new LinkedList<>();
		for (EntryPoint e : ees.all()) {
			
			int wx = area.tiles().x1()+e.wCooD().x();
			int wy = area.tiles().y1()+e.wCooD().y();
			
			int m = WORLD.PATH().dirMap().get(wx, wy);
			if ((e.dirOut.bit & m) != 0 && WORLD.PATH().route.is(wx, wy, e.dirOut)) {
				
				if (WTRAV.isGoodLandTile(wx, wy)) {
					isToActivate[e.index()] = true;
					toActivate.add(e);
				}else {
					toFindOther.add(e);
				}
			}
			
		}

		for (EntryPoint notFindable : toFindOther) {
			
			double dist = Double.MAX_VALUE;
			EntryPoint best = null;
			
			int fx = area.tiles().x1()+notFindable.wCooD().x()+notFindable.dirOut.x();
			int fy = area.tiles().y1()+notFindable.wCooD().y()+notFindable.dirOut.y();

			
			for (EntryPoint e : ees.all()) {
				
				if (e == notFindable)
					continue;
				
				int wx = area.tiles().x1()+e.wCooD().x();
				int wy = area.tiles().y1()+e.wCooD().y();
				
				if (WTRAV.isGoodLandTile(wx, wy)) {
					double d = e.distanceValue(fx, fy);
					if (d < dist) {
						best = e;
						dist = d;
					}
				}				
			}
			
			if (best != null && !isToActivate[best.index()]) {
				isToActivate[best.index()] = true;
				toActivate.add(best);
			}
		}
		
		if (!area.isBattle && toActivate.size() == 0)
			GAME.Warn("No active entry points exist");
		
		for (EntryPoint e : toActivate) {
			adjust(e);
		}
		
		Flooder f = RES.flooder();
		
		{
			f.init(this);
			for (COORDINATE c : SETT.TILE_BOUNDS) {
				if (!SETT.TERRAIN().get(c.x(), c.y()).clearing().isEasilyCleared()) {
					f.pushSloppy(c, 0);
					f.setValue2(c, 1);
				}else
					f.setValue2(c, 0);
			}
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				for (DIR d : DIR.ALL) {
					if (SETT.IN_BOUNDS(t, d))
						f.setValue2(t.x(), t.y(), d, 1);
				}
			}
			f.done();
		}
		
		Coo start = new Coo(SETT.TWIDTH/2, SETT.THEIGHT/2);
		{
			f.init(this);
			f.pushSloppy(SETT.TWIDTH/2, SETT.THEIGHT/2, 0);
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				
				if (cm.getCost(0, 0, t.x(), t.y()) < 2) {
					start.set(t);
					break;
				}
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d))
						f.pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
				
			}
			f.done();
		}
		

		
		
		
		for (EntryPoint e : toActivate) {
			road(start, e.coo());
		}
		
		
		
		
	}
	
	private void road(COORDINATE from, COORDINATE to) {
		

		if (RES.pathTools().astar.getShortest(path, cm, from.x(), from.y(), to.x(), to.y())) {
			while(true) {
				place(path.x(), path.y());
				if (!path.setNext())
					break;
			}
			place(from.x(), from.y());
			place(to.x(), to.y());
		}
		
	}
	
	private static void place(int tx, int ty) {
		if (SETT.TERRAIN().WATER.is.is(tx, ty)) {
			int x = tx;
			int y = ty;
			if (IN_BOUNDS(x, y)) {
				if (TERRAIN().WATER.SHALLOW.is(x, y) && RND.oneIn(4))
					TERRAIN().NADA.placeRaw(x, y);
				else if (TERRAIN().WATER.DEEP.is(x, y)) {
					TERRAIN().WATER.BRIDGE.placeRaw(x, y);
					if (TERRAIN().WATER.DEEP.is(x+1, y+1))
						TERRAIN().WATER.BRIDGE.placeRaw(x+1, y+1);
					for (DIR d : DIR.ORTHO){
						if (RND.oneIn(8) && TERRAIN().WATER.DEEP.is(x+d.x(), y+d.y()))
							TERRAIN().WATER.BRIDGE.placeRaw(x+d.x(), y+d.y());
					}
				}
				
			}
		}else {
			placeRoad(tx, ty);
			for (DIR d : DIR.ALL) {
				if (!SETT.TERRAIN().WATER.is.is(tx, ty, d)) {
					placeRoad(tx+d.x(), ty+d.y());
				}
			}
		}
		
		
		
	}
	
	private static void placeRoad(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty)) {
			SETT.TERRAIN().NADA.placeFixed(tx, ty);
			SETT.FLOOR().defaultRoad.placeFixed(tx, ty);
			SETT.FLOOR().degrade.set(tx, ty, 1.0);
			SETT.MAINTENANCE().disabled.set(tx, ty, true);
		}
		
	}
	
	private void adjust(EntryPoint e){
		
		double bestV = Double.MAX_VALUE;
		final int ox = e.body.cX();
		final int oy = e.body.cY();
		final Coo best = new Coo(ox, oy);
		
		final Coo start = new Coo();
		
		for (COORDINATE c : e.body) {
			start.set(c);
			break;
		}

		for (COORDINATE c : e.body) {
			
			if (SETT.TERRAIN().get(c.x(), c.y()) != SETT.TERRAIN().NADA) {
				if (start.isSameAs(c)) {
					continue;
				}
				
				int cx = (start.x()+c.x())/2;
				int cy = (start.y()+c.y())/2;
				
				int size = Math.abs(start.x()-c.x()) + Math.abs(start.y()-c.y());
				size = CLAMP.i(size, 0, 8);
				int dist = Math.abs(cx-ox) + Math.abs(cy-oy);
				
				double v = dist;
				v /= (size+1);
				if (v < bestV) {
					bestV = v;
					best.set(cx, cy);
				}
				start.set(c);
			}
			
		}
		
		ees.map.set(best, true);
		
		
	}
	
	
	
}
