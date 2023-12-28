package view.tool;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import view.subview.GameWindow;

public abstract class PlacableMulti implements PLACABLE{

	private final CharSequence name;
	public final CharSequence desc;
	private final SPRITE icon;
	private final PLACABLE undo;
	PLACER_TYPE previous; 
	int prevSize = -1;
	
	public PlacableMulti(CharSequence name){
		this(name, null, null, null);
	}
	
	public PlacableMulti(CharSequence name, CharSequence desc, SPRITE icon){
		this(name, desc, icon, null);
	}
	
	public PlacableMulti(CharSequence name, CharSequence desc, SPRITE icon, PLACABLE undo){
		this.name = name;
		this.desc = desc;
		if (icon == null)
			icon = SPRITES.icons().m.cancel;
		this.icon = icon;
		this.undo = undo;
	}
	
	public void updateRegardless(GameWindow window) {
		
	};
	
	@Override
	public SPRITE getIcon() {
		return icon;
	}

	@Override
	public CharSequence name() {
		return name;
	}

	@Override
	public PLACABLE getUndo() {
		return undo;
	}
	
	@Override
	public void hoverDesc(GBox box) {
		box.title(name);
		box.text(desc);
	}
	
	public CharSequence desc() {
		return desc;
	}
	
	public boolean canBePlacedAs(PLACER_TYPE t) {
		return true;
	}
	
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return false;
	}
	
	public void finishPlacing(AREA placedArea) {
		
	}
	
	public abstract CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type);
	public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
		return null;
	}
	public abstract void place(int tx, int ty, AREA area, PLACER_TYPE type);
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area, PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
		if (isPlacable)
			SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
		else
			SPRITES.cons().BIG.outline_dashed.render(r, mask, x, y);
	}
	
	public void placeInfo(GBox b, int oktiles, AREA a) {
		if (a.body().width() > 1 && a.body().height() > 1) {
			GText t = b.text();
			t.add(a.body().width()).add('x').add(a.body().height()).adjustWidth();
			t.s().add('(').add(oktiles).add(')');
			b.add(t);
				
		}
	}
	


}
