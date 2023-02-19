package init.config;

import init.paths.PATHS;
import snake2d.util.file.Json;

public final class ConfigWorld {

	public final int TILE_POPULATION;
	public final double TRIBUTE;
	public final double TRADE_COST_PER_TILE;
	public final int POPULATION_MAX_CAPITOL;
	public final int REGION_SIZE;
	
	ConfigWorld(){
		Json j = new Json(PATHS.CONFIG().get("World"));
		TILE_POPULATION = j.i("TILE_POPULATION", 1, 100);
		TRIBUTE = j.d("REGION_TRIBUTE_AMOUNT");
		TRADE_COST_PER_TILE = j.d("TRADE_COST_PER_TILE", 0, 1000);
		POPULATION_MAX_CAPITOL = j.i("POPULATION_MAX_CAPITOL", 101, 256000);
		REGION_SIZE = j.i("REGION_GENERATION_SIZE", 100, 10000);
	}
	
}
