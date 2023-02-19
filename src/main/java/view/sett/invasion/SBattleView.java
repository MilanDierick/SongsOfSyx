package view.sett.invasion;


import static game.GAME.*;

import java.io.IOException;

import init.C;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.battle.*;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap;
import view.subview.GameWindow;
import view.ui.UIPanelTop;

public final class SBattleView extends VIEW.ViewSub{
	

	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	private final DivSelection selection = new DivSelection();
	private final BattlePlacer placer = new BattlePlacer(window, selection);
	private final BattleRenderer renderer = new BattleRenderer(selection);
	final ISidePanels panels;
	final BattlePanel panel; 
	final UIMinimap minimap;
	
	public SBattleView(){
		UIPanelTop pp = new UIPanelTop(uiManager, false);
		panels = new ISidePanels(uiManager, 0);
		minimap = new UIMinimap(pp, uiManager, UIPanelTop.HEIGHT, false, false, false, false, false, window);
		
		panel = new BattlePanel(panels, window, pp, selection, false);
		new UISelection(uiManager, selection, true);
		
		window.setzoomoutMax(3);
	}
	
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (!uiManager.isHovered())
			window.hover();
	}
 
	@Override
	protected void mouseClick(MButt button) {
		placer.click(button);
		
	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		placer.hoverTimer(text);
		
	}

	
	@Override
	protected boolean update(float ds, boolean should){
		
		window.update(ds);
		placer.update(!uiManager.isHovered());
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(THRONE.coo());
		}
		
		return true;
	}

	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		window.crop(uiManager.viewPort());
		renderer.add();
		if (VIEW.hideUI()) {
			s().render(r, ds, window);
			return;
		}
		s().render(r, ds, window);
		
		
		if (window.consumeHover()) {
			SETT.LIGHTS().renderMouse(window.pixel().x(), window.pixel().y(), -window.pixels().relX(), -window.pixels().relY(), 5);
			
			if (window.hasZoomedOutMoreandConsumeThatMotherFZoom())
				minimap.open();
		}
	}
	
	public GameWindow getWindow(){
		return window;
	}
	
	public void clearAllInterrupters(){
		uiManager.clear();
	}
	
	@Override
	public void activate() {
		window.stop();
		window.copy(VIEW.s().getWindow());
		super.activate();
		
	}
	
	void close(){
		VIEW.s().getWindow().copy(window);
		VIEW.s().activate();
	}
	
	@Override
	protected void save(FilePutter file) {
		window.saver.save(file);
		minimap.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		window.saver.load(file);
		selection.clear();
		minimap.load(file);
	}
	
	public void clear() {
		selection.clear();
		minimap.clear();
	}
	
	@Override
	protected void afterTick() {
		selection.clearHover();
	}


	@Override
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		renderer.renderBelow(r, data);
	}
	
	
}
