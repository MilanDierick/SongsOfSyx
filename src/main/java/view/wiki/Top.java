package view.wiki;

import init.C;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;

final class Top extends GuiSection{

	public static int HEIGHT = 38;
	
	
	public Top(WIKI wiki) {
		body().setWidth(C.WIDTH());
		body().setHeight(HEIGHT);
		
		GButt.ButtPanel exit = new GButt.ButtPanel(SPRITES.icons().m.exit) {
			
			@Override
			protected void clickA() {
				wiki.exit();
			}
			
		};
		
		exit.body.moveX2(body().x2()-2);
		exit.body.centerY(body());
		add(exit);
		GHeader h = new GHeader(WIKI.¤¤name);
		h.body().centerIn(body());
		add(h);
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		Colors.border.render(r, body());
		Colors.bg.render(r, body(), -1);
		super.render(r, ds);
	}
	
}
