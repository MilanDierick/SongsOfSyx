package world.map.regions;

import static world.World.*;

import game.GAME;
import init.RES;
import init.config.Config;
import snake2d.Path;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.ArrayCooShort;
import world.World;

final class GeneratorRoad {

	public void generate() {
		new Gen();
	}
	
	public void clear() {
		for (COORDINATE c : World.TBOUNDS()){
			World.BUILDINGS().roads.clear(c);
			World.BUILDINGS().roads.MINIFIER.clear(c);
		}
	}
	
	private static class Gen {
		
		private final ArrayCooShort neighs = new ArrayCooShort(100);
		private final IntChecker checker = new IntChecker(Regions.MAX);
		private final Polymap polly = new Polymap(TWIDTH(), THEIGHT(), 1.5);

		public Gen() {

//			for (COORDINATE c : TBOUNDS()) {
//				if (polly.isEdge(c.x(), c.y()))
//					BUILDINGS().roads.set(c);
//			}
			//

			//

			// for (COORDINATE c : TBOUNDS()) {
			// stop[c.y()][c.x()] = RND.oneIn(10);
			// }

			for (COORDINATE c : TBOUNDS()) {
				Region r = REGIONS().setter.get(c);
				if (r != null && c.isSameAs(r.cx(), r.cy())) {
					fill(r, c);
					roadify(c);
					BUILDINGS().roads.set(c);
				}
			}
			
			for (COORDINATE c : TBOUNDS()) {
				Region r = REGIONS().setter.get(c);
				if (r != null && c.isSameAs(r.cx(), r.cy())) {
					randomRoad(r, c);
				}
			}

			for (COORDINATE c : TBOUNDS()) {
				if (!BUILDINGS().roads.is(c) && BUILDINGS().roads.is(c, DIR.E) && BUILDINGS().roads.is(c, DIR.S)
						&& !BUILDINGS().roads.is(c, DIR.SE)) {
					BUILDINGS().roads.set(c);
					if (BUILDINGS().roads.MINIFIER.is(c, DIR.E) && BUILDINGS().roads.MINIFIER.is(c, DIR.S))
						BUILDINGS().roads.MINIFIER.set(c);
					
				}
			}

			for (COORDINATE c : TBOUNDS()) {
				if (!BUILDINGS().roads.is(c))
					continue;
				int i = 0;
				for (DIR d : DIR.ALL)
					if (BUILDINGS().roads.is(c, d))
						i++;
				if (i == 0)
					continue;
				boolean connect = false;
				for (DIR d : DIR.ALL)
					if (BUILDINGS().roads.is(c, d)) {
						if (!connect)
							connect = true;
						else
							i--;

					} else
						connect = false;
				if (i == 1)
					BUILDINGS().roads.clear(c);

			}

		}
		
		public void generate() {
			
		}

		
		
		private void fill(Region r, COORDINATE start) {

			checker.init();
			neighs.set(0);

			RES.filler().init(this);

			RES.filler().filler.set(start);

			while (RES.filler().hasMore()) {
				COORDINATE t = RES.filler().poll();
				Region r2 = REGIONS().setter.get(t);
				if (r2 != r) {
					if (r2 == null || checker.isSet(r2.index()))
						continue;
					checker.isSetAndSet(r2.index());
					neighs.get().set(r2.cx(), r2.cy());
					neighs.set(neighs.getI() + 1);
				}

				for (DIR d : DIR.NORTHO) {
					RES.filler().fill(t, d);
				}
			}

			RES.filler().done();
		}

		private final Path.COST cost = new Path.COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				// if (BUILDINGS().roads.is(toX, toY))
				// return 0.1;

				if (WATER().has.is(fromX, fromY)) {
					if (WATER().canCrossRiver(fromX, fromY, toX, toY))
						return 1;
					return -1;
				}
				if (MOUNTAIN().coversTile(toX, toY))
					return -1;

				return (0.2 + ((polly.isEdge(toX, toY) ? 0 : 0.4) + (FOREST().is.is(toX, toY) ? 0.2 : 0)))
						* (BUILDINGS().roads.is(toX, toY) ? 0.5 : 1.0);
			}
		};

		private void roadify(COORDINATE start) {

			int max = neighs.getI();
			Region r1 = REGIONS().setter.get(start);

			for (int i = 0; i < max; i++) {
				neighs.set(i);

				Region r2 = REGIONS().setter.get(neighs.get());
				RES.flooder().init(this);
				RES.flooder().pushSloppy(start, 0);

				while (RES.flooder().hasMore()) {
					PathTile t = RES.flooder().pollSmallest();
					if (t.getValue() > 100)
						break;

					if (REGIONS().setter.get(t) != r1 && REGIONS().setter.get(t) != r2)
						continue;
					if (t.isSameAs(neighs.get())) {
						makeRoad(t);
						break;
					}
					for (DIR d : DIR.ALL) {
						int dx = t.x() + d.x();
						int dy = t.y() + d.y();
						if (IN_BOUNDS(dx, dy)) {
							double v = cost.getCost(t.x(), t.y(), dx, dy);
							if (v >= 0) {
								RES.flooder().pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
							}
						}
					}

				}

				RES.flooder().done();

			}

		}

		private void randomRoad(Region r, COORDINATE start) {

			RES.flooder().init(this);
			RES.flooder().pushSloppy(start, 0);
			
			int amount = 100*r.area()/Config.WORLD.REGION_SIZE;

			while (RES.flooder().hasMore() && amount > 0) {
				PathTile t = RES.flooder().pollSmallest();

				if (r != REGIONS().setter.get(t)) {
					continue;
				}

				if (!BUILDINGS().roads.is(t)) {
					BUILDINGS().roads.set(t);
					BUILDINGS().roads.MINIFIER.set(t);
					amount--;
				}
				
				for (DIR d : DIR.ALL) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					if (IN_BOUNDS(dx, dy)) {
						
						if (BUILDINGS().roads.is(dx, dy)) {
							RES.flooder().pushSmaller(dx, dy, 0);
						}
						double v = cost.getCost(t.x(), t.y(), dx, dy);
						if (v >= 0 && polly.isEdge(dx, dy) && (!WATER().is(dx, dy) && !WATER().RIVER_SMALL.is(dx, dy))) {
							RES.flooder().pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
								
						}
					}
				}

			}

			RES.flooder().done();

		}

		private void makeRoad(PathTile dest) {

			while (dest != null) {
				BUILDINGS().roads.set(dest);
				PathTile next = dest.getParent();
				if (next != null) {

					DIR d = DIR.get(dest, next);
					if (!d.isOrtho()) {
						d = d.next(1);
						int dx1 = dest.x() + d.x();
						int dy1 = dest.y() + d.y();
						int dx2 = dest.x() + d.next(-2).x();
						int dy2 = dest.y() + d.next(-2).y();

						if (!BUILDINGS().roads.is(dx1, dy1) && !BUILDINGS().roads.is(dx2, dy2)) {
							if (cost.getCost(dest.x(), dest.y(), dx1, dy1) >= 0) {
								BUILDINGS().roads.set(dx1, dy1);
							} else if (cost.getCost(dest.x(), dest.y(), dx2, dy2) >= 0) {
								BUILDINGS().roads.set(dx2, dy2);
							}

						}

					}

				}

				dest = next;
			}

		}

		public static void makePlayer() {
			Integer in = new Integer(2);
			
			RES.flooder().init(in);
			RES.flooder().pushSloppy(GAME.player().kingdom().realm().capitol().cx(), GAME.player().kingdom().realm().capitol().cy(), 0);
			
			while (RES.flooder().hasMore()) {
				PathTile t = RES.flooder().pollSmallest();

				if (REGIONS().setter.get(t) != GAME.player().kingdom().realm().capitol()) {
					continue;
				}

				if (BUILDINGS().roads.is(t)) {
					while(t != null) {
						BUILDINGS().roads.set(t);
						t = t.getParent();
					}
					break;
				}
				
				for (DIR d : DIR.ORTHO) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					if (IN_BOUNDS(dx, dy)) {
						RES.flooder().pushSmaller(dx, dy, t.getValue() + d.tileDistance(), t);
					}
				}

			}
			
		}
		
	}
	


}
