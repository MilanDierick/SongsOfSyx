package view.tool;

import init.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolPlacer.placeFunc;

final class PlacableSimpleTileTool extends placeFunc {

	private PlacableSimpleTile placable;
	@Override
	void update(float ds, GameWindow window, boolean pressed) {
		
		
	}
	

	@Override
	void render(SPRITE_RENDERER r, float ds, GameWindow window) {
		
		int tx = window.tile().x();
		int ty = window.tile().y();
		
		placable.renderOverlay(tx, ty, r, ds, window);
		CharSequence problem = placable.isPlacable(tx, ty);
		
		if (problem == null) {
			placable.renderPlaceHolder(r, window.tile().rel().x()+C.TILE_SIZEH, window.tile().rel().y()+C.TILE_SIZEH, false);
		}else {
			placable.renderPlaceHolder(r, window.tile().rel().x()+C.TILE_SIZEH, window.tile().rel().y()+C.TILE_SIZEH, true);
			VIEW.hoverBox().error(problem);
		}
		COLOR.unbind();
	}

	@Override
	void click(GameWindow window) {
		int tx = window.tile().x();
		int ty = window.tile().y();
		
		CharSequence problem = placable.isPlacable(tx, ty);
		if (problem != null)
			return;
		
		placable.place(tx, ty);
		
	}

	@Override
	void activate(PLACABLE placer, GameWindow window) {
		placable = (PlacableSimpleTile) placer;
	}

	@Override
	void clickRelease(GameWindow window) {
		
	}

	@Override
	LIST<CLICKABLE> gui() {
		return null;
	}

	
};