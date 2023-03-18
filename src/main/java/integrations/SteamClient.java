package integrations;

import com.codedisaster.steamworks.*;

final class SteamClient {

	private static final long interval = 5*1000;
	
	
	private SteamUtils clientUtils;
	private SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {
		@Override
		public void onSteamShutdown() {
			
		}
	};
	private final Thread Callbacker;
	private volatile boolean die = false;
	
	private SteamClient(){
		clientUtils = new SteamUtils(clUtilsCallback);
		
		
		Callbacker = new Thread(new Runnable() {
			long last = 0;
			Thread t = Thread.currentThread();
			@Override
			public void run() {
				
				while(!die && t.isAlive() && INTEGRATIONS.inited() && SteamAPI.isSteamRunning()) {
					long now = System.currentTimeMillis();
					//System.out.println(INTEGRATIONS.inited() + " " + SteamAPI.isSteamRunning());
					if (now-last > interval) {
						SteamAPI.runCallbacks();
						last = now;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
				}
				
			}
		});
		Callbacker.start();
	}
	
	void dispose() {
		die = true;
		Callbacker.interrupt();
		try {
			Callbacker.join(interval);
		} catch (InterruptedException e) {
			
		}
		clientUtils.dispose();
		SteamAPI.shutdown();
	}
	
	static SteamClient init() {
		try {
			SteamAPI.loadLibraries();
			
			if (SteamAPI.init() && SteamAPI.isSteamRunning()) {
				return new SteamClient();
			}
		} catch (SteamException e) {
			e.printStackTrace();
		}
		return null;
	}
	


	public boolean running() {
		return SteamAPI.isSteamRunning();
	}
	
}
