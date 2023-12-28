package view.battle;


import static game.GAME.*;

import java.io.IOException;

import game.GAME;
import game.battle.BattleState;
import init.C;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap;
import view.subview.GameWindow;
import view.ui.battle.UIBattle;
import view.ui.top.UIPanelTop;

public final class BattleView extends VIEW.ViewSub{
	

	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	private final DivSelection selection = new DivSelection();
	private final BattlePlacer placer = new BattlePlacer(window, selection);
	private final BattleRenderer renderer = new BattleRenderer(selection);
	public final ISidePanels panels;
	final BattlePanel panel; 
	final UIMinimap minimap;
	private BattleState state;
	public final DivHoverer hoverer = new DivHoverer();
	
	public BattleView(){
		new UIBattle();
		UIPanelTop pp = new UIPanelTop(uiManager, true);
		panels = new ISidePanels(uiManager, 0);
		minimap = new UIMinimap(pp, uiManager, UIPanelTop.HEIGHT, false, false, false, false, false, window);
		
		panel = new BattlePanel(panels, window, pp, selection, true);
		new UISelection(uiManager, selection, false);
		
		window.setzoomoutMax(3);
		new IDeploy(uiManager);
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

	
	public BattleState state() {
		return state;
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		
		state.update(ds*GAME.SPEED.speedTarget());
		
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
		
		selection.clearHover();
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
		super.activate();
	}
	
	public void activate(BattleState state) {
		this.state = state;
		window.stop();
		super.activate();
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
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		renderer.renderBelow(r, data);
	}
	
	@Override
	protected boolean canSave() {
		return false;
	}
	
	
}
