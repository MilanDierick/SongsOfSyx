package view.tool;

import init.C;
import init.RES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolPlacer.placeFunc;

final class PlacableSingleTool extends placeFunc {

	private PlacableSingle placable;
	@Override
	void updateHovered(float ds, GameWindow window, boolean pressed) {
		
		
	}
	

	@Override
	void render(SPRITE_RENDERER r, float ds, GameWindow window) {
		
		int tx = window.tile().x();
		int ty = window.tile().y();
		
		placable.init(tx, ty);
		
		CharSequence problem = placable.isPlacable(tx, ty);
		
		if (problem == null) {
			int t = 0;
			RES.filler().init(this);
			RES.filler().fill(tx, ty);
			while(RES.filler().hasMore()) {
				COORDINATE c = RES.filler().poll();
				t++;
				int mask = 0;
				for (DIR d : DIR.ORTHO) {
					int dx = c.x()+d.x();
					int dy = c.y()+d.y();
					if (!SETT.IN_BOUNDS(dx, dy))
						continue;
					if (RES.filler().isFilled(dx, dy) || (placable.isPlacable(dx, dy) == null && placable.expandsTo(c.x(), c.y(), dx, dy))){
						mask |= d.mask();
						RES.filler().fill(dx, dy);
					}
				}
				render(r, mask, c.x(), c.y(), true, window);
			}
			RES.filler().done();
			placable.placeInfo(VIEW.hoverBox(), t);
		}else {
			render(r, 0, tx, ty, false, window);
			VIEW.hoverBox().error(problem);
		}
		COLOR.unbind();
	}
	
	private void render(SPRITE_RENDERER r, int mask, int tx, int ty, boolean placable, GameWindow window) {
		if (placable)
			SPRITES.cons().color.ok.bind();
		else
			SPRITES.cons().color.blocked.bind();
		int x = (tx-window.tile().x())*C.TILE_SIZE+window.tile().rel().x();
		int y = (ty-window.tile().y())*C.TILE_SIZE+window.tile().rel().y();
		this.placable.renderPlaceHolder(r, mask, x, y, tx, ty, placable);
	}

	@Override
	void click(GameWindow window) {
		int tx = window.tile().x();
		int ty = window.tile().y();
		
		CharSequence problem = placable.isPlacable(tx, ty);
		if (problem != null)
			return;
		
		placable.placeFirst(tx, ty);
		
		
		RES.filler().init(this);
		RES.filler().fill(tx, ty);
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			placable.placeExpanded(c.x(), c.y());
			for (DIR d : DIR.ORTHO) {
				int dx = c.x()+d.x();
				int dy = c.y()+d.y();
				if (!SETT.IN_BOUNDS(dx, dy))
					continue;
				if ((placable.isPlacable(dx, dy) == null && placable.expandsTo(c.x(), c.y(), dx, dy))){
					RES.filler().fill(dx, dy);
				}
			}
		}
		RES.filler().done();
	}

	@Override
	void activate(PLACABLE placer, GameWindow window) {
		placable = (PlacableSingle) placer;
	}

	@Override
	void clickRelease(GameWindow window) {
		
	}

	@Override
	LIST<CLICKABLE> gui() {
		return null;
	}

	
};