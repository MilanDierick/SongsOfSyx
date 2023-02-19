package init.config;

import init.paths.PATHS;
import snake2d.util.file.Json;

public final class ConfigBattle {

	public final double DAMAGE;
	public final double BLOCK_CHANCE;
	public final int TRAINING_DEGRADE;
	public final int MEN_PER_DIVISION;
	public final int DIVISIONS_PER_ARMY;
	public final int DIVISIONS_PER_BATTLE;
	public final int MEN_PER_ARMY;
	public final int REGION_MAX_DIVS;
	public final int REGION_MAX_MEN;
	
	ConfigBattle(){
		Json j = new Json(PATHS.CONFIG().get("Battle"));
		DAMAGE = j.d("DAMAGE", 0, 10000);
		BLOCK_CHANCE = j.d("BLOCK_CHANCE", 0, 10000);
		TRAINING_DEGRADE = j.i("TRAINING_DEGRADE", 0, 50);
		MEN_PER_DIVISION = j.i("MEN_PER_DIVISION", 1, 255);
		DIVISIONS_PER_ARMY = j.i("DIVISIONS_PER_ARMY", 1, 126);
		DIVISIONS_PER_BATTLE = DIVISIONS_PER_ARMY*2;
		MEN_PER_ARMY = MEN_PER_DIVISION*DIVISIONS_PER_ARMY;
		REGION_MAX_DIVS = j.i("REGION_MAX_DIVS", 0, 127);
		REGION_MAX_MEN = REGION_MAX_DIVS*MEN_PER_DIVISION;
	}
	
}
