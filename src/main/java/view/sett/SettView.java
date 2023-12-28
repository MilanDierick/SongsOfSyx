package view.sett;


import static game.GAME.*;

import java.io.IOException;

import game.GAME;
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
import view.interrupter.InterGuisection;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.invasion.SBattleView;
import view.sett.ui.SettUI;
import view.sett.ui.minimap.UIMinimap;
import view.subview.GameWindow;
import view.tool.ToolManager;
import view.ui.top.UIPanelTop;

public class SettView extends VIEW.ViewSub{
	
	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	public final Inters interrupters = new Inters();
	boolean hasPlaced = false;
	{
		UISettMap.clear();
	}
	private final SettViewStart start = new SettViewStart();
	public final UISettManagePanel ui;
	public final SettUI misc = new SettUI(uiManager);
	public final ISidePanels panels;
	public final ToolManager tools = new ToolManager(uiManager, window);
	public final IDebugPanelSett debug;
	public final UIMinimap mini;
	public final SBattleView battle;

	
	public class Inters{
		
		
		public final InterGuisection section = new InterGuisection(uiManager);
		public final InterGuisection debugsection = new InterGuisection(uiManager);
		
		public Inters(){
			
			
			
			
		}
		
		
	}
	
	public SettView(){
		
		UIPanelTop pan = new UIPanelTop(uiManager);
		//pan.addNoti();
		panels = new ISidePanels(uiManager, 0);
		ui = new UISettManagePanel(this, pan);
		
		window.setzoomoutMax(3);
		tools.setDefault(new ToolDefault(tools));
		debug = new IDebugPanelSett(uiManager);
		mini = new UIMinimap(pan, uiManager, UIPanelTop.HEIGHT, true, true, true, true, true, window);
		
		battle = new SBattleView();
	}
	
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {

		
	}
 
	@Override
	protected void mouseClick(MButt button) {
//		if (!VIEW.hideUI()) {
//			if (interrupters.manager.click(button)){
//				tools.manager.click(button);
//			}
//		}
		
	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
//		if (!VIEW.hideUI()) {
//			interrupters.manager.hoverTimer(mouseTimer, text);
//		}
	}

	
	@Override
	protected boolean update(float ds, boolean should){
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(THRONE.coo());
		}
		
		return true;
		
	}

	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		window.crop(uiManager.viewPort());
		s().render(r, (float) (ds*GAME.SPEED.speed()), window);
		
		if (window.consumeHover()) {
			SETT.LIGHTS().renderMouse(window.pixel().x(), window.pixel().y(), -window.pixels().relX(), -window.pixels().relY(), 5);
			
			if (window.hasZoomedOutMoreandConsumeThatMotherFZoom())
				mini.open();
			
		}

		
//		if (VIEW.hideUI()) {
//			VIEW.mouse().hide(true);
//			s().render(r, ds, window);
//		}else {
//			if (interrupters.manager.render(r, ds)){
//				tools.manager.render(r, ds, VIEW.hoverBox());
//				
//				double s = MButt.getWheelSpin();
//				if (s != 0 && !KEY.CLEAR_PLACE.isPressed()) {
//					int d = s < 0 ? 1 : -1;
//					if (d > 0 && window.zoomout() == 2)
//						interrupters.minimap.show();
//					else
//						window.zoomInc(d);
//					MButt.clearWheelSpin();
//				}
//				
//				
//			}
//		}
		
	
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
	
	@Override
	protected void save(FilePutter file) {
		window.saver.save(file);
		file.bool(hasPlaced);
		mini.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		uiManager.clear();
		window.saver.load(file);
		hasPlaced = file.bool();
		if (!hasPlaced)
			start.activate();
		mini.load(file);
		
	}
	
	public void clear() {
		hasPlaced = false;
		start.activate();
		mini.clear();
		battle.clear();
	}
	
	@Override
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		SETT.JOBS().render(r, s, data);
	}
	
	
}
