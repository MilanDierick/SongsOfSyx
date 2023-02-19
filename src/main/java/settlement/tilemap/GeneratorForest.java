package settlement.tilemap;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SettlementGrid;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import world.World;

class GeneratorForest {

	GeneratorForest(CapitolArea area, GeneratorUtil util) {

		double value = util.json.d("FOREST_AMOUNT", 0, 1);
		double density = util.json.d("FOREST_DENSITY", 0, 1);

		util.polly.checkInit();
		RES.flooder().init(this);

		for (int i = 0; i < GRID.tiles().size(); i++) {
			
			SettlementGrid.Tile ut = GRID.tile(i);
			double wf = World.FOREST().amount.get(area.ts().get(i));

			int a = RND.rInt(2) + (int) (value * wf * 20);

			while (a-- > 0) {
				int r = (int) (1 + Math.pow(RND.rFloat(), 2)* 40);
				int sx = ut.coo(DIR.W).x() + RND.rInt(SettlementGrid.QUAD_SIZE);
				int sy = ut.coo(DIR.N).y() + RND.rInt(SettlementGrid.QUAD_SIZE);

				while (r-- > 0) {
					int x = sx + RND.rInt0(40);
					int y = sy + RND.rInt0(40);
					if (IN_BOUNDS(x, y)) {
						util.polly.checker.set(x, y, true);
						RES.flooder().pushSloppy(x, y, 0);
						RES.flooder().setValue2(x, y, 6 + 30 * util.fer.get(x, y));
					}
				}
			}

		}

		while (RES.flooder().hasMore()) {

			PathTile t = RES.flooder().pollSmallest();
			if (!TERRAIN().NADA.is(t))
				continue;

			if (t.getValue() >= t.getValue2())
				continue;

			double v = 1.0 - t.getValue() / t.getValue2();
			v *= v;
			
			v *= density;
			v *= util.fer.get(t);

			if (RND.rFloat() <= v) {
				if (RND.oneIn(4) && TERRAIN().TREES.BIG.isPlacable(t.x(), t.y())) {
					TERRAIN().TREES.BIG.placeRaw(t.x(), t.y());
				} else if (TERRAIN().TREES.MEDIUM.isPlacable(t.x(), t.y())) {
					TERRAIN().TREES.MEDIUM.placeRaw(t.x(), t.y());
				} else {
					TERRAIN().TREES.SMALL.placeRaw(t.x(), t.y());
				}
				
			}

			for (DIR d : DIR.ALL) {
				if (IN_BOUNDS(t, d))
					if (RES.flooder().pushSmaller(t, d, t.getValue() + d.tileDistance()) != null)
						RES.flooder().setValue2(t, d, t.getValue2());
			}

		}

		RES.flooder().done();


		
		for (COORDINATE c : TILE_BOUNDS) {
			if (TERRAIN().NADA.is(c)) {
				for (DIR d : DIR.ORTHO) {
					if (TERRAIN().TREES.isTree(c.x()+d.x(),c.y()+d.y())) {
						if (RND.oneIn(4))
							TERRAIN().BUSH.placeRaw(c.x(), c.y());
						break;
					}
				}
			}
		}

	}

}
