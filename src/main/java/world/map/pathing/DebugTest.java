package world.map.pathing;

import init.RES;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import world.WORLD;
import world.regions.Region;

class DebugTest {

	public DebugTest() {
		ArrayCooShort coos = new ArrayCooShort(WORLD.TAREA());
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.PATH().route.is(c)) {
				coos.get().set(c);
				coos.inc();
			}

		}

		int max = coos.getI();
		test("mixed", coos, max);

		coos.set(0);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = WORLD.REGIONS().map.get(c);
			if (r != null && WORLD.PATH().route.is(c) && c.isSameAs(r.cx(), r.cy())) {
				coos.get().set(c);
				coos.inc();
			}
		}
		
		test("capitols", coos, max);

	}
	
	private void test(String title, ArrayCooShort coos, int max) {
		LOG.ln(title);
		test("simple", simple, coos, max, 1000);
		test("normal", make, coos, max, 1000);
		test("fancy", makeFancy, coos, max, 1000);
	}
	
	private void test(String title, TEST test, ArrayCooShort coos, int max, int amount) {
		long now = System.currentTimeMillis();
		int fails = 0;
		for (int i = 0; i < amount; i++) {

			coos.set(RND.rInt(max));
			int sx = coos.get().x();
			int sy = coos.get().y();

			coos.set(RND.rInt(max));
			int dx = coos.get().x();
			int dy = coos.get().y();

			PathTile t = test.make(WORLD.WATER().is(sx, sy), sx, sy, dx, dy);
			if (t == null) {
				fails++;
			}
		}
		LOG.ln(title + " " + (System.currentTimeMillis() - now) + " " + fails);
	}

	interface TEST {
		PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy);
	}

	public final TEST simple = new TEST() {

		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			if (!WORLD.PATH().route.is(fromX, fromY) || !WORLD.PATH().route.is(tox, toy)) {
				return null;
			}	

			RES.flooder().init(WPATHING.class);
			RES.flooder().pushSloppy(fromX, fromY, 0);
			while (RES.flooder().hasMore()) {

				PathTile t = RES.flooder().pollSmallest();
				if (t.isSameAs(tox, toy)) {
					RES.flooder().done();
					return t;
				}
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					if (WORLD.PATH().route.is(dx, dy))
						RES.flooder().pushSmaller(dx, dy, t.getValue() + d.tileDistance()*WPATHING.getTerrainCost(t.x(), t.y()), t);
				}

			}
			RES.flooder().done();
			return null;
		}
	};
	
	public final TEST make = new TEST() {


		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			if (!WORLD.PATH().route.is(fromX, fromY) || !WORLD.PATH().route.is(tox, toy))
				return null;

			RES.flooder().init(WPATHING.class);
			
			RES.flooder().pushSloppy(fromX, fromY, 0);
			while (RES.flooder().hasMore()) {

				PathTile t = RES.flooder().pollSmallest();
				if (t.isSameAs(tox, toy)) {
					RES.flooder().done();
					return t;
				}
				process(t);

			}

			RES.flooder().done();
			return null;
		}
		
		private void process(PathTile t) {
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (WORLD.PATH().route.is(dx, dy)) {
					if (WORLD.WATER().isBig.is(t)) {
						if (WORLD.WATER().canTravelToByBoat(t.x(), t.y(), d) || WORLD.PATH().route.is(dx, dy, d)) {
							RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()*WPATHING.getTerrainCost(dx, dy), t);
						}
					}else {
						if (WORLD.WATER().isBig.is(dx, dy)) {
							if (WORLD.PATH().route.is(dx, dy))
								RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()+WTRAV.PORT_PENALTY, t);
						}else {
							RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()*WPATHING.getTerrainCost(dx, dy), t);
						}
					}
				}
			}
			
			
		}
		
		
	};
	
	public final TEST makeFancy = new TEST() {
		
		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			PathTile t = WORLD.PATH().path(fromX, fromY, tox, toy);
			if (t == null) {
				LOG.ln(fromX + " " + fromY + " " + tox + " " + toy);
			}
			return t;
			
		}
		
		
	};

}
