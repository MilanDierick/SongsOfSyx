package init.sprite.UI;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class UIArmyCard {

	
	private final TILE_SHEET decorations;
	
	private final COLOR cPower = new ColorImp(114, 84, 33).shade(0.7);
		
	public UIArmyCard() throws IOException{
		
		decorations = new ITileSheet(PATHS.SPRITE_UI().get("ArmyCard"), 120, 108) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 3, 3, d.s16);
				s.full.setVar(0);
				s.full.paste(true);
				return d.s16.saveGui();
			}
		}.get();
	}
	
	public void renderThing(SPRITE_RENDERER r, int i, int x1, int y1) {
		decorations.render(r, i, x1, y1);
	}
	
	public void renderPower(int x1, int y1, SPRITE_RENDERER r, double l) {
		renderThing(x1, y1, r, l/100.0, 2);
	};
	
	
	public void renderThing(int x1, int y1, SPRITE_RENDERER r, double l, int v) {
		int i = CLAMP.i((int) (l*7), 0, 7);
		if (i == 0)
			return;
		
		cPower.bind();
		int am = i;
		for (int k = 0; k <= am; k++) {
			decorations.render(r, v, x1, y1+6*k);
		}
		COLOR.unbind();
	}


}