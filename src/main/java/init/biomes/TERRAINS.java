package init.biomes;

import static settlement.main.SETT.*;

import init.D;
import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.keymap.RCollection;
import world.World;

public final class TERRAINS {

	private static TERRAINS self;
	
	public static void init () {
		self = new TERRAINS();
	}
	
	private final TERRAIN OCEAN;
	private final TERRAIN WET;
	private final TERRAIN MOUNTAIN;
	private final TERRAIN FOREST;
	private final TERRAIN NONE;
	private final INFO info;
	private final LIST<TERRAIN> all;
	private final RCollection<TERRAIN> map;
	
	private TERRAINS() {
		D.t(TERRAINS.class);
		ArrayList<TERRAIN> ts = new ArrayList<>(40);
		String key = "TERRAIN";
		Json json = new Json(PATHS.CONFIG().get(key));
		info = new INFO(D.g("Terrain"), "");
		
		OCEAN = new TERRAIN(ts, "OCEAN", json, 
				D.g("Ocean"), 
				D.g("OceanD", "Salt water such as oceans. Good for trade."), true) {

				@Override
				public SPRITE icon() {
					return World.WATER().iconSalt;
				}

				@Override
				public double value(int wx, int wy) {
					double res = 0;
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						if (World.WATER().OCEAN.is.is(wx, wy, DIR.ORTHO.get(di)))
							res += 0.25;
					}
					return CLAMP.d(res, 0, 1);
				}

					
			
		};
		WET = new TERRAIN(ts, "WET", json, 
				D.g("Sweet", "Sweet water"), 
				D.g("SweetD", "Sweet water such as river beds or lakes. Often high fertility ground."), true) {
			
			@Override
			public SPRITE icon() {
				return World.WATER().iconSweet;
			}
			
			@Override
			public double value(int wx, int wy) {
				double res = 0;
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					if (World.WATER().LAKE.is.is(wx, wy, DIR.ORTHO.get(di)) || World.WATER().isRivery.is(wx, wy, DIR.ORTHO.get(di)))
						res += 0.25;
				}
				return CLAMP.d(res, 0, 1);
			}
			
		};
		MOUNTAIN = new TERRAIN(ts, "MOUNTAIN", json, 
				D.g("Mountain"), 
				D.g("MountainD", "Barren, but rich in mineral deposits."), true) {

					@Override
					public SPRITE icon() {
						return SETT.TERRAIN().MOUNTAIN.getIcon();
					}

					@Override
					public double value(int wx, int wy) {
						double res = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							if (World.MOUNTAIN().haser.is(wx, wy, DIR.ORTHO.get(di)))
								res += 0.25;
						}
						return CLAMP.d(res, 0, 1);
					}
			
		};
		FOREST = new TERRAIN(ts, "FOREST", json, 
				D.g("Forest"), 
				D.g("ForestD", "Forested areas. Good for lumber."), true) {

					@Override
					public SPRITE icon() {
						return World.FOREST().icon;
					}

					@Override
					public double value(int wx, int wy) {
						double res = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							if (World.MOUNTAIN().haser.is(wx, wy, DIR.ORTHO.get(di)))
								res += 0.25*World.FOREST().amount.get(wx, wy);
						}
						return res;
					}
			
		};
		NONE = new TERRAIN(ts, "NONE", json, 
				D.g("OpenLand", "Open Land"), 
				D.g("Open Land", ""), true) {
			
			@Override
			public SPRITE icon() {
				return World.GROUND().icon;
			}

			@Override
			public double value(int wx, int wy) {
				double res = 1;
				for (int ti = 0; ti < ALL().size(); ti++) {
					if (ti == index())
						continue;
					res -= ALL().get(ti).value(wx, wy);
				}
				
				return CLAMP.d(res, 0, 1);
			}
			
		};
		all = new ArrayList<>(ts);
		KeyMap<TERRAIN> m = new KeyMap<>();
		for (TERRAIN t : all)
			m.put(t.key, t);
		map = new RCollection<TERRAIN>(key, m) {

			@Override
			public TERRAIN getAt(int index) {
				return all.get(index);
			}

			@Override
			public LIST<TERRAIN> all() {
				return all;
			}
		};
	}
	
	public static final MAP_OBJECT<TERRAIN> sett = new MAP_OBJECT<TERRAIN>() {

		@Override
		public TERRAIN get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public TERRAIN get(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return NONE();
			if (TERRAIN().TREES.isTree(tx, ty))
				return FOREST();
			if (TERRAIN().MOUNTAIN.isMountain(tx, ty))
				return MOUNTAIN();
			if (TERRAIN().WATER.is(tx, ty) && GROUND().getter.get(tx, ty).fertility() == 0)
				return OCEAN();
			if (TERRAIN().WATER.is(tx, ty))
				return WET();
			return NONE();
		}
	
	};
	
	public static final MAP_OBJECT<TERRAIN> world = new MAP_OBJECT<TERRAIN>() {

		@Override
		public TERRAIN get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public TERRAIN get(int tx, int ty) {
			if (!World.IN_BOUNDS(tx, ty))
				return NONE();
			if (World.FOREST().is.is(tx, ty))
				return FOREST();
			if (World.MOUNTAIN().is(tx, ty))
				return MOUNTAIN();
			if (World.WATER().OCEAN.is.is(tx, ty))
				return OCEAN();
			if (World.WATER().fertile.is(tx, ty))
				return WET();
			return NONE();
		}
	
	};
	
	public static LIST<TERRAIN> ALL(){
		return self.all;
	}
	
	public static RCollection<TERRAIN> MAP(){
		return self.map;
	}
	
	public static TERRAIN OCEAN() {
		return self.OCEAN;
	}
	
	public static TERRAIN WET() {
		return self.WET;
	}
	public static TERRAIN MOUNTAIN() {
		return self.MOUNTAIN;
	}
	public static TERRAIN FOREST() {
		return self.FOREST;
	}
	public static TERRAIN NONE() {
		return self.NONE;
	}
	
	public static INFO INFO() {
		return self.info;
	}
	
	
	
}
