package integrations;

import com.codedisaster.steamworks.SteamException;

public class INTEGRATIONS {

	private static volatile INTEGRATIONS I;
	
	public static void init(boolean log, boolean achieve) {
		
		
		if (I != null) {
			throw new RuntimeException("Already inited");
		}
		try {
			new INTEGRATIONS(log, achieve);
		}catch(Throwable e) {
			e.printStackTrace();
			I = null;
		}
		
	}
	

	private final RPCHandler handler;
	private final SteamClient steam;
	private final SteamStats stats;
	private final SteamScreenshot screens;
	private static boolean logging = false;
	
	private INTEGRATIONS(boolean logging, boolean achieve) throws SteamException {
		INTEGRATIONS.logging = logging;
		I = this;
		steam = SteamClient.init();

		handler = new RPCHandler();
		if (steam != null) {
			if (achieve)
				stats = new SteamStats();
			else
				stats = null;
			screens = new SteamScreenshot();
		}else {
			stats = null;
			screens = null;
		}
		
		log("INTEGRATION INITED");
		log("STEAM: " + steamRunning());
		log("DISCORD: " + true);
	}
	
	
	public static void dispose() {
		if (I != null) {
			I.handler.dispose();

			if (I.steam != null) {
				if (I.stats != null)
					I.stats.dispose();
				I.screens.dispose();
				I.steam.dispose();
			}
			I = null;
		}
	}
	
	public static void updateRPC(INTER_RPC rpc) {
		
		if (I != null) {
			I.handler.update(rpc);
		}
	}
	
	public static boolean steamRunning() {
		return I != null && I.steam != null && I.steam.running();
	}
	
	public static boolean inited() {
		return I != null;
	}
	
	static void log(Object object) {
		if (logging) {
			System.out.println("[INTEGRATIONS] " + object);
		}
	}
	
	public static void achieve(String key, int value) {
		if (I != null && I.stats != null) {
			I.stats.setStat(key, value);
		}
	}
	
	public static void achieve(String key) {
		if (I != null && I.stats != null) {
			I.stats.setAchieved(key);
		}
	}
	
	public static void reset() {
		if (I != null && I.stats != null) {
			
			I.stats.Reset(true);
		}
	}
	
	public static void achieveInc(String key, int value) {
		if (I != null && I.stats != null) {
			I.stats.incStat(key, value);
		}
	}

	public static void achivementsFlush() {
		if (I != null && I.stats != null) {
			I.stats.storeStats();
		}
	}
	
}
