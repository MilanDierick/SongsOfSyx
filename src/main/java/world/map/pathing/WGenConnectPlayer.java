package world.map.pathing;

import game.faction.FACTIONS;
import init.RES;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.Bitmap2D;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;

final class WGenConnectPlayer {

	private final IntChecker check = new IntChecker(WREGIONS.MAX);
	private final WGenUtil u;
	
	public WGenConnectPlayer(WGenUtil u, int playerX, int playerY) {
		this.u = u;
		
		for (COORDINATE c : WORLD.TBOUNDS())
			RES.flooder().setValue2(c, 0);
		
		connect(playerX, playerY);
		connectPlayer(playerX, playerY);

		
		for (Region r : WORLD.REGIONS().all()) {
			if (r.info.area() > 0 && !check.isSet(r.index())) {
				r. active = false;
				LOG.ln("Not reachable: " + r.info.name() + " " + r.cx() + " " + r.cy());
			}else {
				r.active = true;
			}
		}
		
		Flooder f = RES.flooder();
		f.init(this);
		f.pushSloppy(playerX, playerY, 0);
		
		Bitmap2D mark = new Bitmap2D(WORLD.TBOUNDS(), false);
		
		while (f.hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			mark.set(t, true);
			
			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, true)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					if (u.tmpRoute.is(dx, dy)) {
						RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance(), t);
					}
				}
			}
			
		}
		f.done();
		for (COORDINATE c : WORLD.TBOUNDS()) {
			u.tmpRoute.set(c, mark.is(c));
		}
	}
	
	private void connect(int playerX, int playerY) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(playerX, playerY, 1);
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			u.tmpRoute.set(t, true);
			double pv = t.getValue();
			
			for (DIR d : DIR.ALL) {
				if (u.tmpRoute.is(t, d) && WTRAV.can(t.x(), t.y(), d, true)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();					
					RES.flooder().pushSmaller(dx, dy, pv + d.tileDistance(), t);
				}
			}
			
		}
		
		RES.flooder().done();
	}
	
	
	int pen = WORLD.TAREA()*100;
	
	private void connectPlayer(int playerX, int playerY) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(playerX, playerY, 0);
		RES.flooder().setValue2(playerX, playerY, 0);
		check.init();
		check.isSetAndSet(FACTIONS.player().capitolRegion().index());
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			double pv = t.getValue2();
			
			Region r = WORLD.REGIONS().map.get(t);
			if (r != null && t.isSameAs(r.info.cx(), r.info.cy()) && !check.isSetAndSet(r.index())) {
				connectPlayer(t);
				pv = 0;
			}
			
			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					
					double v = pv + u.cost(dx, dy) * d.tileDistance();
					if (u.tmpRoute.is(dx, dy) && WTRAV.can(t.x(), t.y(), d, true)) {
						if (RES.flooder().pushSmaller(dx, dy, v, t) != null) {
							RES.flooder().setValue2(dx, dy, v);
						}
					}else {
						if (RES.flooder().pushSmaller(dx, dy, v+pen, t) != null) {
							RES.flooder().setValue2(dx, dy, v);
						}
						
					}
					
				}
			}
			
		}
		
		RES.flooder().done();
	}
	
	private void connectPlayer(PathTile dest) {
		
		
		
		
		PathTile t = dest;
		while(t != null) {
			if (WORLD.WATER().isBig.is(t) && WORLD.ROADS().HARBOUR.is(t)) {
				RES.coos().get().set(t);
				RES.coos().inc();
			}
			u.tmpRoute.set(t, true);
			t = t.getParent();
		}
		
		WTRAV.makeRoad(dest);
		
		PathTile from = dest;
		t = from.getParent();
		while(t != null) {
			if (WORLD.ROADS().HARBOUR.is(t)) {
				if (t.getParent() != null && WTRAV.isGoodLandTile(from.x(), from.y()) && WTRAV.isGoodLandTile(t.getParent().x(), t.getParent().y())) {
					WORLD.ROADS().ROAD.set(t);
				}
			}
			from = t;
			t = t.getParent();
		}
		
		while(RES.coos().getI() > 0) {
			RES.coos().dec();
			WORLD.ROADS().HARBOUR.set(RES.coos().get());
		}
		
	}

	
	
}
