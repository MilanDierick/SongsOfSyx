package init.race.appearence;

import init.race.RACES;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;

public final class RPortrait {

	public static final int P_WIDTH = PortraitFrame.TILES_X*PortraitFrame.TILE_SIZE;
	public static final int P_HEIGHT = PortraitFrame.TILES_Y*PortraitFrame.TILE_SIZE;
	private PortraitFrame[] frames;
	private final TILE_SHEET sheet;
	
	RPortrait(RColors colors, Json json, TILE_SHEET sheet, ExtraSprite extra){
		Json[] js = json.jsons("PORTRAIT");
		frames = new PortraitFrame[js.length];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = new PortraitFrame(colors, js[i], i);
		}
		this.sheet = sheet;
	}

	public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale, boolean isDead) {
		for (PortraitFrame f : frames) {
			f.render(r, sheet, RACES.sprites().pFilth, RACES.sprites().pBlood, x1, y1, indu, isDead, scale);
		}
	}
	
	
}
