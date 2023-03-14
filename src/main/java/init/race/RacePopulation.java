package init.race;

import init.D;
import init.biomes.*;
import snake2d.util.file.Json;
import util.keymap.KEY_COLLECTION;

public final class RacePopulation {

	public static CharSequence ¤¤reproductionRate = "Reproduction Rate";
	public static CharSequence ¤¤reproductionRateD = "The rate at which a species reproduces.";
	
	public static CharSequence ¤¤rarity = "¤Rarity";
	public static CharSequence ¤¤rarityD = "¤How rare this species is in Syx.";
	
	static {
		D.ts(RacePopulation.class);
	}
	public final double reproductionRate;
	public final double rarity;
	public final double maxCity;
	public final double immigrantsPerDay;
	private final double[] climates;
	private final double maxClimate;
	private final double[] terrains;
	private final double maxTerrain;
	
	RacePopulation(Json json) {
		if (!json.has("POPULATION")) {
			reproductionRate = 0;
			rarity = 0;
			maxCity = 1;
			immigrantsPerDay = 0;
			climates = new double[CLIMATES.ALL().size()];
			terrains = new double[TERRAINS.ALL().size()];
			maxClimate = 0;
			maxTerrain = 0;
		}else {
			json = json.json("POPULATION");
			
			reproductionRate = json.d("REPRODUCTION_REGION_PER_DAY", 0, 10000);
			rarity = json.d("MAX_REGION", 0, 1);
			maxCity = json.d("MAX_CITY", 0, 1);
			climates = KEY_COLLECTION.fill(CLIMATES.MAP(), json, 1);
			double m = 0;
			for (double c : climates)
				m = Math.max(c, m);
			maxClimate = m;
			terrains = KEY_COLLECTION.fill(TERRAINS.MAP(), json, 100);
			m = 0;
			for (double c : terrains)
				m = Math.max(c, m);
			maxTerrain = m;
			immigrantsPerDay = json.d("IMMIGRANTS_PER_DAY", 0, 100000);
		}
	
	}
	
	public double climate(CLIMATE c) {
		return climates[c.index()];
	}
	
	public double terrain(TERRAIN c) {
		return terrains[c.index()];
	}
	
	public double maxClimate() {
		return maxClimate;
	}
	
	public double maxTerrain() {
		return maxTerrain;
	}
	
	
}
