package view.tool;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLORS_MAP;
import util.gui.misc.GBox;
import view.subview.GameWindow;

public abstract class PlacableSimpleTile implements PLACABLE{

	private final CharSequence name;
	private final CharSequence desc;
	private final PLACABLE undo;
	
	public PlacableSimpleTile(CharSequence name){
		this(name, null, null);
	}
	
	public PlacableSimpleTile(CharSequence name, CharSequence desc){
		this(name, desc, null);
	}
	
	public PlacableSimpleTile(CharSequence name, CharSequence desc, PLACABLE undo){
		this.name = name;
		this.desc = desc;
		this.undo = undo;
	}
	
	@Override
	public SPRITE getIcon() {
		return SPRITES.icons().m.cancel;
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
	
	public abstract CharSequence isPlacable(int tx, int ty);
	public abstract void place(int tx, int ty);
	
	public void renderPlaceHolder(SPRITE_RENDERER r, int tx, int ty, int cx, int cy, boolean isPlacable) {
		if (!isPlacable)
			GCOLORS_MAP.map_ok.bind();
		else
			GCOLORS_MAP.map_not_ok.bind();
		SPRITES.cons().BIG.dashedThick.get(0).renderC(r, cx, cy);
		COLOR.unbind();
	}


	public void renderOverlay(GameWindow window) {
		
	}
	


}
