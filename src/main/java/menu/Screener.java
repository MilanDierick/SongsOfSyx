package menu;

import init.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;

abstract class Screener extends view.menu.Screener{

	public static RECTANGLE inner = new Rec(bounds.width()-50, bounds.height()-75).moveC(C.DIM().cX(), C.DIM().cY());
	
	private final Rec shadow = new Rec(bounds.width(), bounds.height()-50).moveC(C.DIM().cX(), C.DIM().cY());
	public Screener(CharSequence title, COLOR color) {
		super(title, color);
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		Background.shadow = shadow;
		super.render(r, ds);
	}
}
