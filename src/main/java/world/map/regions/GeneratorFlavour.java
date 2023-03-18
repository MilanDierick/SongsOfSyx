package world.map.regions;

import static world.World.*;

import init.paths.PATHS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import world.World;

final class GeneratorFlavour {


	
	public GeneratorFlavour() {
		
	}

	private final Json jland = new Json(PATHS.NAMES().get("WorldAreas"));
	private final LandCounter cMisc = new LandCounter(jland.texts("MISC"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return MOUNTAIN().is(tx, ty);
			}
		};
	private final LandCounter cOcean = new LandCounter(jland.texts("OCEAN"), 0.15) {
		@Override
		boolean count(int tx, int ty) {
			return WATER().OCEAN.normal.is(tx, ty);
		}
	};
	private final LandCounter cLake = new LandCounter(jland.texts("LAKE"), 0.15) {
		@Override
		boolean count(int tx, int ty) {
			return WATER().LAKE.normal.is(tx, ty);
		}
	};
	
	private final CharSequence[] sOceans = jland.texts("OCEAN_ADDONS");
	private final CharSequence[] sLakes  = jland.texts("LAKE_ADDONS");
	private final CharSequence[] sIslands  = jland.texts("ISLAND_ADDONS");
		
	private final LandCounter[] counts = new LandCounter[] {
		new LandCounter(jland.texts("MOUNTAIN"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return MOUNTAIN().is(tx, ty);
			}
		},
		new LandCounter(jland.texts("FOREST"), 0.4) {
			@Override
			boolean count(int tx, int ty) {
				return FOREST().is.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("RIVER"), 0.1) {
			@Override
			boolean count(int tx, int ty) {
				return WATER().isRivery.is(tx, ty);
			}
		},
		cLake,
		cOcean,
		new LandCounter(jland.texts("STEPPE"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return ty < World.THEIGHT()/2 && GROUND().getter.get(tx, ty).fertility() < 0.2;
			}
		},
		new LandCounter(jland.texts("DESERT"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return ty > World.THEIGHT()/2 && GROUND().getter.get(tx, ty).fertility() < 0.2;
			}
		},
	};
	
	void init(Region r) {
		if (r == null || r.area() == 0)
			return;
		

		
		if (r.isWater()) {
			if (World.WATER().LAKE.is.is(r.cx(), r.cy())) {
				r.name().clear().add(sLakes[RND.rInt(sLakes.length)]);
				r.name().insert(0, cLake.getName());
			}else {
				r.name().clear().add(sOceans[RND.rInt(sOceans.length)]);
				r.name().insert(0, cOcean.getName());
			}
			return;
		}
		
		for (LandCounter c : counts) {
			c.count = 0;
			c.value = 0;
		}
		
		for (COORDINATE coo : r.bounds()) {
			if (!REGIONS().setter.is(coo, r))
				continue;
			for (LandCounter c : counts) {
				if (c.count(coo.x(), coo.y()))
					c.count++;
			}
		}
		
		LandCounter bestFit = cMisc;
		
		for (LandCounter c : counts) {
			c.value = c.count/(double)r.area();
			c.value /= c.treshold;
			if (c.value > 1.0 && c.value > bestFit.value)
				bestFit = c;
		}
		
		if (isIsland(r)) {
			r.name().clear().add(sIslands[RND.rInt(sIslands.length)]);
			r.name().insert(0, bestFit.getName());
		}else {
			r.name().clear().add(bestFit.getName());
		}

	}

	private boolean isIsland(Region r) {
		for (COORDINATE c : r.bounds()) {
			if (!REGIONS().setter.is(c, r))
				continue;
			for (DIR d : DIR.ORTHO)
				if (!REGIONS().setter.is(c, d, r) && !WATER().has.is(c)) {
					return false;
				}
		}
		return true;
	}
	
	abstract class LandCounter {
		
		private final String[] names;
		private int nameI =0;
		private double treshold;
		private double value;
		
		LandCounter(String[] names, double treshold){
			for (int i = 0; i < names.length; i++) {
				String old = names[i];
				int k = RND.rInt(names.length);
				names[i] = names[k];
				names[k] = old;
			}
			
			this.names = names;
			this.treshold = treshold;
		}
		
		String getName() {
			int i = nameI;
			nameI++;
			if (nameI >= names.length)
				nameI = 0;
			return names[i];
		}
		
		int count;
		abstract boolean count(int tx, int ty);
		
	}
	
}
