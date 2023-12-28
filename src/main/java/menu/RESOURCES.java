package menu;

import java.io.IOException;

import game.boosting.BOOSTING;
import init.paths.PATHS;
import init.sprite.UI.UI;
import util.spritecomposer.Initer;

final class RESOURCES {

	private static RSprites s;
	private static RSound sound;
	
	static RSprites s() {
		return s;
	}
	
	static RSound sound() {
		return sound;
	}
	
	public static void make(){

		new Initer() {
			
			@Override
			public void createAssets() throws IOException {
				UI.init();
				BOOSTING.init(null);
				s = new RSprites();
				
			}
		}.get("menu", PATHS.textureSize(), 0);
		sound = new RSound();
	}
	
}
