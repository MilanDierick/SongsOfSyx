package init.config;

import init.paths.PATHS;

public class Config {
	
	static {
		if (!PATHS.inited()) {
			throw new RuntimeException("paths must be inited first!");
		}
	}
	
	public static final ConfigBattle BATTLE = new ConfigBattle();
	public static final ConfigWorld WORLD = new ConfigWorld();
	
	private Config(){
		
	}
	
}
