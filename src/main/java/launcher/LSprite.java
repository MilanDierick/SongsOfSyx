package launcher;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;

class LSprite extends RENDEROBJ.RenderImp{

	protected SPRITE sprite;
	protected OpacityImp opacity = new OpacityImp(OpacityImp.O100);
	protected ColorImp mask = new ColorImp(ColorImp.WHITE100);
	private final Rec bounds = new Rec();
	
	public LSprite(COLOR c){
		mask.set(c);
	}
	
	public LSprite(SPRITE s) {
		this(s, 0, 0);
	}
	
	public LSprite(SPRITE s, COLOR c){
		this(s);
		mask.set(c);
	}
	
	public LSprite(SPRITE s, float x1, float y1) {
		bounds.set(x1, x1 + s.width(), y1, y1 + s.height());
		this.sprite = s;
	}
	
	public void replaceSprite(SPRITE newSprite, DIR d){
		this.sprite = newSprite;
		if (sprite == null){
			d.reposition(bounds, 0, 0);
		}else{
			d.reposition(bounds, newSprite.width(), newSprite.height());
		}
	}
	
	
	public OpacityImp getOpacity(){
		return opacity;
	}
	
	public ColorImp getColor(){
		return mask;
	}

	@Override
	public Rec body() {
		return bounds;
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (sprite == null)
			return;
		opacity.bind();
		mask.bind();
		sprite.render(r, bounds);
		ColorImp.unBind();
		OPACITY.unbind();
		
	}
	
}
