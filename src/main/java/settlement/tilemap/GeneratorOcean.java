package settlement.tilemap;

import static settlement.main.SETT.*;
import static settlement.main.SettlementGrid.*;

import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SettlementGrid;
import settlement.tilemap.GeneratorUtil.FertilityTmp;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import world.World;


class GeneratorOcean {
	
	private final HeightMap height;
	private final FertilityTmp fer;
	private final double table;

	private final int MARGIN = (int) (QUAD_SIZE / 2.2);

	private final double MAX_VALUE = MARGIN;
	private final double ferValue = -3;

	GeneratorOcean(CapitolArea area, GeneratorUtil util) {
		table = area.getWatertabe();

		this.fer = util.fer;
		this.height = util.height;

		RES.flooder().init(this);

		for (int i = 0; i < GRID.tiles().size(); i++) {
			COORDINATE c = area.ts().get(i);
			SettlementGrid.Tile ut = GRID.tile(i);
			if (World.WATER().OCEAN.is.is(c)) {
				add(ut.coo(DIR.C).x(), ut.coo(DIR.C).y());
				for (DIR d : DIR.ORTHO) {
					if (World.WATER().OCEAN.is.is(c, d)) {
						add(ut.coo(d).x(), ut.coo(d).y());
					}
				}
			}
		}

		generateWater(MAX_VALUE);

		RES.flooder().done();
		
	}

	private void place(int x, int y) {
		TERRAIN().WATER.DEEP.placeRaw(x, y);
		TERRAIN().WATER.isSalty.set(x, y, true);
		fer.increment(x, y, ferValue);
		//height.set(x, y, 0);
	}

	private void add(int x, int y) {
		PathTile t = RES.flooder().pushSloppy(x, y, 0);
		if (t != null)
			t.setValue2(0);

	}

	private void generateWater(double max) {

		Flooder f = RES.flooder();

		while (f.hasMore()) {

			PathTile t = f.pollSmallest();
			if (t.getValue() >= max) {
				break;
			}
			if (t.getValue() > 4*max/5 ) {
				TERRAIN().WATER.placeRaw(t.x(), t.y());
				TERRAIN().WATER.isSalty.set(t, true);
				fer.increment(t.x(), t.y(), ferValue);
			}else {
				place(t.x(), t.y());
			}
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				int x = t.x() + DIR.ALL.get(i).x();
				int y = t.y() + DIR.ALL.get(i).y();

				if (IN_BOUNDS(x, y)) {
					double d = DIR.ALL.get(i).tileDistance();
					double radius = t.getValue2() + d;
					float h = (float) height.get(x, y);
					double value = 0.8*radius;
					value += max*h*h*h;
					if (value > max)
						value = max;
					if (h < table)
						value = 0;
					PathTile t2 = RES.flooder().pushSloppy(x, y, (float) value);
					if (t2 != null)
						t2.setValue2((float) radius);
				}

			}
		}
		
		double delta = 15.0;
		max += delta;
		
		while (f.hasMore()) {

			PathTile t = f.pollSmallest();
			double v = t.getValue();
			if (v >= max) {
				break;
			}
				double dd = 1.0 - (v - max+delta)/delta;
				if (dd < 0)
					dd = 0;
				if (dd > 1)
					dd = 1;
				double fe = dd*ferValue*(0.7 + 0.3*RND.rFloat());
				
					
//				else if(fe > -0.4)
//					
				fer.increment(t.x(), t.y(), fe);
//				if (fer.get(t.x(), t.y()) >)
//					TERRAIN().WATER.placeRaw(t.x(), t.y());
				
			for (int i = 0; i < DIR.ALL.size(); i++) {
				int x = t.x() + DIR.ALL.get(i).x();
				int y = t.y() + DIR.ALL.get(i).y();

				if (IN_BOUNDS(x, y)) {
					double d = DIR.ALL.get(i).tileDistance();
					double radius = t.getValue2() + d;
					float h = (float) height.get(x, y);
					double value = 0.8*radius;
					value += max*h*h*h;
					if (value > max)
						value = max;
					PathTile t2 = RES.flooder().pushSloppy(x, y, (float) value);
					if (t2 != null)
						t2.setValue2((float) radius);
				}

			}
		}

	}
	
	
}
