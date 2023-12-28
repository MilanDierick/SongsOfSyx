package settlement.tilemap.generator;

import static settlement.main.SETT.*;

import settlement.main.SETT;
import settlement.tilemap.growth.TGrowth;
import settlement.tilemap.growth.TGrowth.Grower;
import settlement.tilemap.terrain.TGrowable;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;

class GeneratorGrowth {

	private final TGrowth gg = SETT.TILE_MAP().growth;

	private final Grower[] generators = new Grower[] {
		gg.tree,
		gg.tree,
		gg.tree,
		gg.tree,
		gg.tree,
		gg.tree,
		gg.mushroom,
		gg.mushroom,
		gg.flower,
		gg.flower,
		gg.flower,
		gg.flower,
		gg.bush,
		gg.bush,
		gg.bush,
		gg.bush, };

	private final Grower[] treea = new Grower[] { gg.mushroom, gg.mushroom, gg.flower, gg.flower, gg.bush, gg.bush, gg.bush, gg.bush, };

	private final Grower[] edibles = new Grower[128];

	GeneratorGrowth() {
		HeightMap ferMap = new HeightMap(TWIDTH, THEIGHT, 8, 2);

		double tot = 0;
		for (TGrowable g : SETT.TERRAIN().GROWABLES) {
			tot += g.growable.growthValue * g.growable.availability(SETT.ENV().climate());
		}

		if (tot == 0)
			return;
		int am = (int) (edibles.length * (tot / SETT.TERRAIN().GROWABLES.size()));

		int i = 0;

		for (TGrowable g : SETT.TERRAIN().GROWABLES) {
			double d = g.growable.growthValue * g.growable.availability(SETT.ENV().climate());
			d /= tot;
			int a = (int) Math.ceil(am * d);

			for (; i < edibles.length && a > 0; i++, a--) {
				edibles[i] = gg.growable.get(g.growable.index());
			}
		}

		for (int y = 0; y < SETT.TWIDTH; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				generate(x, y, ferMap);
			}
		}

		ferMap = new HeightMap(TWIDTH, THEIGHT, 16, 8);

		for (int y = 0; y < SETT.TWIDTH; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				double d = ferMap.get(x, y);
				d *= d;
				if (d > 0.9)
					gg.permanent.set(x + y * TWIDTH, true);

				if (TERRAIN().NADA.is(x, y)) {
					Grower g = gg.type(x, y);
					double f = gg.growMaxAmount(x, y);
					if (f > 0) {
						
						g.setRoots(x, y, f);

					}
				}
				if (TERRAIN().TREES.isTree(x, y)) {
					TERRAIN().TREES.amount.DM.set(x, y, 1.0);
				}

			}
		}

	}

	void generate(int x, int y, HeightMap map) {

		Grower g = gg.get(x, y);
		if (g != gg.nothing) {
			gg.max_amount.set(x, y, g.currentAmount(x, y));
			g.set(x, y);
			gg.permanent.set(x + TWIDTH * y, true);

			return;
		} else {
			int t = 0;
			for (DIR d : DIR.ORTHO) {
				if (TERRAIN().TREES.isTree(x + d.x(), y + d.y())) {
					t++;
				}

			}
			if (t > 0 && RND.oneIn(5 - t)) {
				g = treea[RND.rInt(treea.length)];
				double am = RND.rFloat();
				gg.max_amount.set(x, y, am);
				g.set(x, y);
				gg.permanent.set(x + y * TWIDTH, true);
				return;
			}

		}

		double f = sample(x, y, map, 0.55);
		if (f > 0) {
			g = gg.tree;
		} else {
			f = sample(x + 64, y, map, 0.7);
			if (f > 0) {
				g = gg.flower;
			} else {
				f = sample(x, y + 64, map, 0.8);
				if (f > 0) {
					g = gg.mushroom;
				} else {
					f = sample(x + 64, y + 64, map, 0.4);
					if (f > 0) {
						g = gg.bush;
						f = Math.sqrt(f);
					} else if (RND.oneIn(4)) {
						f = RND.rFloat();
						g = generators[RND.rInt(generators.length)];
					}
				}
			}
		}

		if (g == gg.nothing)
			return;
		gg.max_amount.set(x, y, f);
		g.set(x, y);

	}

	private double sample(int x, int y, HeightMap map, double max) {
		double d = map.get(x % TWIDTH, y % THEIGHT) * RND.rFloat1(0.3);
		if (d > max) {
			d -= max;
			d /= 1.0 - max;
			return d;
		}
		return 0;
	}

}