package settlement.tilemap;

import static settlement.main.SETT.*;

import init.C;
import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.Path;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;

public class GeneratorCave {

	
	private final Path.PathSync p = new Path.PathSync(5000);
	private final int caveSize;
	private final double tunnels;
	
	GeneratorCave(CapitolArea area, GeneratorUtil util, LinkedList<COORDINATE> caves){
		
		int amount = (int) (util.json.d("CAVE_AMOUNT", 0, 1.0)*300);
		caveSize = (int) (util.json.d("CAVE_SIZE", 0, 1.0)*30);
		tunnels = util.json.d("CAVE_TUNNELS", 0, 1);
		
		for (int i = 0; i < amount; i++) {
			int x = RND.rInt(TWIDTH);
			int y = RND.rInt(THEIGHT);
			if (cave(area, util, x, y)) {
				caves.add(new Coo(x, y));
			}
			
		}
		
		
		RES.flooder().init(this);
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {
				if (TERRAIN().CAVE.is(x, y))
					RES.flooder().pushSloppy(x, y, 0);
			}
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			TERRAIN().CAVE.placeRaw(t.x(), t.y());
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (TERRAIN().MOUNTAIN.is(t, d)) {
					if (RND.oneIn(3)) {
						RES.flooder().pushSmaller(t, d, 1);
					}else {
						RES.flooder().close(t, d, 0);
					}
				}
				
			}
		}
		RES.flooder().done();
		
	}
	
	private boolean cave(CapitolArea area, GeneratorUtil util, int x, int y) {
		
		RES.flooder().init(this);
		LinkedList<Coo> coos = new LinkedList<>();
		
		util.polly.checkInit();
		util.polly.checker.set(x, y, true);
		
		RES.flooder().pushSloppy(x, y, 0);
		
		for (int i = RND.rInt(caveSize); i > 0; i--) {
			int x2 = x + RND.rInt0(10+caveSize);
			int y2 = y + RND.rInt0(10+caveSize);
			if (SETT.IN_BOUNDS(x2, y2)) {
				if (!util.polly.checker.is(x2, y2)) {
					util.polly.checker.set(x2, y2, true);
					RES.flooder().pushSloppy(x2, y2, 0);
					coos.add(new Coo(x2, y2));
				}
				
			}
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (!TERRAIN().MOUNTAIN.is(t)) {
				RES.flooder().done();
				return false;
			}
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (!util.polly.checker.is(t, d)) {
					if (t.getValue() < 5)
						RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}else {
					RES.flooder().pushSmaller(t, d, 0);
				}
				
			}
		}
		RES.flooder().done();
		RES.flooder().init(this);
		for (Coo c : coos) {
			RES.flooder().pushSloppy(c, 0);
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (TERRAIN().MOUNTAIN.is(t)) {
				TERRAIN().CAVE.placeRaw(t.x(), t.y());
			}
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (util.polly.checker.is(t, d))
					RES.flooder().pushSmaller(t, d, 0);
			}
		}
		RES.flooder().done();
//		if (RND.oneIn(2))
//			tunnel(area, util,x, y);
		
		int a = RND.rInt((int) (1 + coos.size()*tunnels*10));
		
		for (int i = 0; i < a; i++) {
			Coo c = coos.removeFirst();
			tunnel(area, util,c.x(), c.y());
			coos.add(c);
		}
			
		
		
		return true;
		
	}
	
	private void tunnel (CapitolArea area, GeneratorUtil util, int startX, int startY) {
		
		final Path.COST cm = new Path.COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				if (!IN_BOUNDS(toX, toY))
					return -1;

				if (!util.polly.isEdge(toX, toY)) {
					return 5;
				}
				
				if (TERRAIN().MOUNTAIN.is(toX, toY))
					return 1;
				
				if (TERRAIN().CAVE.is(toX, toY))
					return 2;
				
				return 0;

			}
		};
		
		final Path.DEST dm = new Path.DEST() {
			@Override
			protected boolean isDest(int x, int y) {
				if (TERRAIN().CAVE.is(x, y)) {
					int d = Math.abs(x-startX);
					d += Math.abs(y-startY);
					return d > 60;
				}
				return !TERRAIN().MOUNTAIN.is(x, y);
			}
			
			@Override
			protected float getOptDistance(int x, int y) {
				// TODO Auto-generated method stub
				return 0;
			}
		};

		if (!RES.astar().getNearest(p, cm, dm, startX, startY)) {
			return;
		}

		RES.flooder().init(this);

		int max = 60 + RND.rInt(60);
		
		do {
			int x = p.x();
			int y = p.y();
			RES.flooder().pushSmaller(x, y, 0);
		} while (p.setNext() && max-- >= 0);

		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollGreatest();
			if (TERRAIN().MOUNTAIN.is(t))
			TERRAIN().CAVE.placeRaw(t.x(), t.y());
			if (TERRAIN().MOUNTAIN.is(t, DIR.E))
				TERRAIN().CAVE.placeRaw(t.x()+1, t.y());
			
			
			
		}
		
		RES.flooder().done();
		
	}
	
}
