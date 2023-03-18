package integrations;

import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamScreenshotHandle;
import com.codedisaster.steamworks.SteamScreenshots;
import com.codedisaster.steamworks.SteamScreenshotsCallback;

final class SteamScreenshot {
	
	private SteamScreenshots ss;
	
	private SteamScreenshotsCallback ssCallback = new SteamScreenshotsCallback() {

		@Override
		public void onScreenshotReady(SteamScreenshotHandle local, SteamResult result) {
			INTEGRATIONS.log("Screenshot saved on disk!");
			INTEGRATIONS.log("Result: "+result);
		}

		@Override
		public void onScreenshotRequested() {
			INTEGRATIONS.log("Steam wants to take a screenshot!");
		}
		
	};
	
	public SteamScreenshot() {
		init();
	}

	private void init() {
		INTEGRATIONS.log("Register SteamScreenshot ...");
		ss= new SteamScreenshots(ssCallback);
		//Turning off Steams default screenshot functionality
		//ss.hookScreenshots(true);
	}
	
	public void addScreenshot(String path, String thumbnailPath, int width, int height) {
		ss.addScreenshotToLibrary(path, thumbnailPath, width, height);
	}
	
	public void dispose() {
		ss.dispose();
	}
	
	
}
