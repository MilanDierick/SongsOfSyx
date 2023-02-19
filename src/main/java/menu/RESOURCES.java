package menu;

import java.io.IOException;

import init.biomes.CLIMATES;
import init.biomes.TERRAINS;
import init.paths.PATHS;
import init.race.RACES;
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
				CLIMATES.init();
				TERRAINS.init();
				new RACES();
				s = new RSprites();
				UI.init(RACES.all().get(0));
			}
		}.get("menu", PATHS.textureSize(), 0);
		sound = new RSound();
	}
	
}
