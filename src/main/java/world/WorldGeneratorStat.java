package world;

import init.biomes.TERRAIN;
import init.biomes.TERRAINS;

public final class WorldGeneratorStat {
	
	private int[] terrains = new int[TERRAINS.ALL().size()];
	
	WorldGeneratorStat() {
		
	}
	
	public int tiles(TERRAIN t) {
		return terrains[t.index()];
	}
	
}