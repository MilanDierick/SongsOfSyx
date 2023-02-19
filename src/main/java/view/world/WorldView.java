package view.world;

import static world.World.*;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import init.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolManager;
import view.ui.UIPanelTop;
import view.world.generator.WorldViewGenerator;
import world.World;

public class WorldView extends VIEW.ViewSub{
	
	public final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			PIXELS(),
			C.TILE_SIZE*5);

	
	public final ToolManager tools;
	public final WorldViewGenerator viewGenerator = new WorldViewGenerator(window);
	public final WorldUI  UI  = new WorldUI(uiManager);
	public final ISidePanels panels = new ISidePanels(uiManager, 0);
	final IDebugPanelWorld debug = new IDebugPanelWorld(uiManager);
	
	public WorldView(){
		
		UIPanelTop p = new UIPanelTop(uiManager);
		p.addNoti();
		new WorldViewPanel(p);
		new WorldIIMinimap(p, uiManager);
		tools = new ToolManager(uiManager, window);
		tools.setDefault(new ToolDefault(tools));
	}

	@Override
	public void activate() {
		super.activate();
		
		window.stop();
		tools.set(null, null, true);
		
	}
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
	}

	@Override
	protected void mouseClick(MButt button) {

	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		
		
		
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
		}
		return true;
	}
	
	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		
		window.crop(uiManager.viewPort());
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1()<<window.zoomout(), window.view().y1()<<window.zoomout());
		
	}

	@Override
	protected void save(FilePutter file) {
		window.saver.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		window.saver.load(file);
		World.MINIMAP().repaint();
//		for (Region r : World.REGIONS().all()) {
//			
//		}
	}

	@Override
	protected void afterTick() {
		
	}
	
}
