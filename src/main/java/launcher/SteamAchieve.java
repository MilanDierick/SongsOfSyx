package launcher;

import game.statistics.GCOUNTS;
import integrations.INTEGRATIONS;
import snake2d.util.file.Json;

final class SteamAchieve {


	private volatile boolean working = false;
	private volatile Exception e = null;
	
	
	public SteamAchieve() {

	}
	
	public synchronized boolean isDone() {
		return !working && !isError();
	}
	
	public synchronized boolean isError() {
		return e != null;
	}
	
	public synchronized Exception exception() {
		return e;
	}
	
	public void work() {
		if (working || isError())
			return;
		working = true;
		e = null;
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				INTEGRATIONS.init(false, true);
				
				try {
					INTEGRATIONS.achieve("first_game");
					Json j = GCOUNTS.getJson();
					if (j != null) {
						a("buildings_count", j, "ROOMS_BUILT");
						a("pop_count", j, "SUBJECTS");
						a("playtime", j, "TIME_PLAYED");
					}
					INTEGRATIONS.achivementsFlush();
					INTEGRATIONS.dispose();
				}catch(Exception e) {
					SteamAchieve.this.e = e;
				}
				working = false;
			}
		};
		
		Thread t = new Thread(r);
		
		t.start();
	}
	
	private static void a(String skey, Json json, String key) {
		if (json.has(key)) {
			INTEGRATIONS.achieve(skey, json.i(key));
		}
	}

}
