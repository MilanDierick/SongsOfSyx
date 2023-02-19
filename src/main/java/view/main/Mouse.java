package view.main;

import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.util.datatypes.*;
import snake2d.util.sprite.SPRITE;

public class Mouse implements COORDINATE{

	private final Coo coo = new Coo();
	private final SPRITE sprites ;
	private boolean hidden = false;
	private SPRITE overlay = null; 
	
	Mouse(){
		sprites = UI.decor().mouse;
	}
	
	void render(Renderer r, float ds){
		
		if (hidden) {
			hidden = false;
			return;
		}
		
		sprites.render(r, coo.x(), coo.y());
		if (overlay != null){
			overlay.render(r, coo.x()+10, coo.y());
		}
		overlay = null;

	}
	
	public void setReplacement(SPRITE o){
		overlay = o;
	}

	public void hide(boolean hide){
		hidden = hide;
	}

	@Override
	public int x() {
		return coo.x();
	}

	@Override
	public int y() {
		return coo.y();
	}

	@Override
	public boolean isWithinRec(RECTANGLE shape) {
		return coo.isWithinRec(shape);
	}

	Coo getCoo() {
		return coo;
	}

}
