package view.sett.ui.minimap;

import static settlement.main.SETT.*;

import game.GAME;
import init.C;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import util.gui.misc.GBox;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.subview.GameWindow;

final class ViewMiniMap extends Interrupter {

	private final Rec absBounds = new Rec(C.DIM());

	private final ViewMiniMapUI section;
	private final GameWindow window = new GameWindow(C.DIM(), SETT.PIXEL_BOUNDS, 0).setzoomoutMax(6);
	
	boolean hovered = false;
	
	private final ViewMinimapMap mini;
	
	private final GameWindow c;
	private final InterManager manager;
	
	public ViewMiniMap(UIMinimap m, InterManager i, GameWindow c) {
		this.manager = i;
		persistantSet();
		this.c = c;
		mini = new ViewMinimapMap(m);
		int zoomout = 4;
		while ((PIXEL_BOUNDS.width() >> zoomout) > C.WIDTH() || (PIXEL_BOUNDS.height() >> zoomout) > C.HEIGHT())
			zoomout++;
		if (zoomout > 6)
			zoomout = 6;
		window.setZoomout(4);
		window.setzoomoutMax(zoomout);
		section = new ViewMiniMapUI(i, window, zoomout, this);
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		mini.render(r, ds, window, absBounds, window.pixel(), section.currentE, hovered);
		hovered = false;

		return false;
	}
	
	
	
	public void showMin() {
		window.setZoomout(4);
		show();
	}
	
	public void show() {
		

		
		
		if (window.zoomout() < 4) {
			window.setZoomout(4);
			hide();
		}
		window.setFromOther(c);
		
		super.show(manager);
	}
	
	public void showFull() {
		window.setZoomout(window.zoomoutmax());
		window.centerAt(c.pixels().cX(), c.pixels().cY());
		up();
		
		super.show(manager);
	}
	
	@Override
	public void hide() {
		super.hide();
	}
	
	
	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			if (hovered) {
				window.zoomByMouse(4);
				c.setFromOther(window);
				hide();
			}else {
				section.click();
			}
			
		}else if(button == MButt.RIGHT) {
			hide();
		}
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		section.hover(mCoo);
		hovered = window.pixel().isWithinRec(SETT.PIXEL_BOUNDS) && ! section.hoveredIs();
		
		
		return true;
	}

	@Override
	protected void deactivateAction() {
		
	}
	
	private void up() {
		
		mini.update();
		
	}
	
	@Override
	protected boolean update(float ds) {
		
		if (window.zoomout() < 4) {
			c.setFromOther(window);
			hide();
		}

		if (KEYS.MAIN().MINIMAP.consumeClick()) {
			hide();
			return true;
		}

		
		
		
		GAME.SPEED.poll();
		if (!section.hoveredIs())
			window.hover();
		window.update(ds);
		up();

		return true;
	}

	

}
