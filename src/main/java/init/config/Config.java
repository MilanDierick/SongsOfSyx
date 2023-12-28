package init.config;

import init.paths.PATHS;
import snake2d.Errors;
import snake2d.util.file.Json;

public class Config {
	
	static {
		if (!PATHS.inited()) {
			throw new RuntimeException("paths must be inited first!");
		}
	}
	
	private static Json j = null;
	
	static{
		j = new Json(PATHS.CONFIG().get("Battle"));
	}
	public static final ConfigBattle BATTLE = new ConfigBattle();
	static{
		j = new Json(PATHS.CONFIG().get("Sett"));
	}
	public static final ConfigSett SETT = new ConfigSett();
	
	private Config(){
		
	}
	
	public static final class ConfigBattle {

		public final double DAMAGE = j.d("DAMAGE", 0, 10000);
		public final double BLOCK_CHANCE = j.d("BLOCK_CHANCE", 0, 10000);
		public final int TRAINING_DEGRADE = j.i("TRAINING_DEGRADE", 0, 50);
		public final int MEN_PER_DIVISION = j.i("MEN_PER_DIVISION", 1, 255);
		public final int DIVISIONS_PER_ARMY = j.i("DIVISIONS_PER_ARMY", 1, 126);
		public final int DIVISIONS_PER_BATTLE = DIVISIONS_PER_ARMY*2;
		public final int MEN_PER_ARMY = MEN_PER_DIVISION*DIVISIONS_PER_ARMY;
		public final int REGION_MAX_DIVS = j.i("REGION_MAX_DIVS", 0, 127);
		public final int REGION_MAX_MEN = REGION_MAX_DIVS*MEN_PER_DIVISION;
		
		ConfigBattle(){
			

		}
		
	}
	
	public static final class ConfigSett {

		public final double HAPPINESS_EXPONENT = j.d("HAPPINESS_EXPONENT");
		public final int TOURIST_PER_YEAR_MAX = j.i("TOURIST_PER_YEAR_MAX");
		public final double TOURIST_CRETIDS = j.d("TOURIST_CRETIDS");
		public final int DIMENSION = j.i("DIMENSION", 256, 16000);
		
		
		ConfigSett(){
			if (DIMENSION % 32 != 0)
				throw new Errors.DataError("SETT DIMENSION MUST BE A MULTIPLE OF 32");
		}
		
	}
	
}
