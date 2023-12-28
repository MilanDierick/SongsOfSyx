package world;

import init.paths.PATHS;
import snake2d.util.file.Json;

public final class WConfig {

	static {
		if (!PATHS.inited()) {
			throw new RuntimeException("paths must be inited first!");
		}
	}
	
	public static final Data data = new Data();
	
	public static final class Data {
		
		public final int TILE_POPULATION;
		public final double TRIBUTE;
		public final double TRADE_COST_PER_TILE;
		public final int POPULATION_MAX_CAPITOL;
		public final int REGION_SIZE;
		public final int WORLD_SIZE;
		public final double FOREST_AMOUNT;
		public final double LOYALTY_FROM_POP;
		
		Data(){
			Json j = json("General");
			TILE_POPULATION = j.i("TILE_POPULATION", 1, 100);
			TRIBUTE = j.d("REGION_TRIBUTE_AMOUNT");
			TRADE_COST_PER_TILE = j.d("TRADE_COST_PER_TILE", 0, 1000);
			POPULATION_MAX_CAPITOL = j.i("POPULATION_MAX_CAPITOL", 101, 256000);
			REGION_SIZE = j.i("REGION_GENERATION_SIZE", 100, 10000);
			WORLD_SIZE = j.i("TILE_DIMENSION", 128, 512);
			FOREST_AMOUNT = j.d("FOREST_AMOUNT", 0, 1);
			LOYALTY_FROM_POP = j.d("POPULATION_LOYALTY_PENALTY", 0, 10000);
		}
	}
	
	public static Json json(String resource) {
		return new Json(PATHS.WORLD().folder("config").init.get(resource));
	}
	
	
}
