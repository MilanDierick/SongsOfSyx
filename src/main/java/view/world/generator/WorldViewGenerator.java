package view.world.generator;

import game.GAME;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolManager;
import view.world.WorldIIMinimap;
import view.world.WorldView;
import world.World;

public class WorldViewGenerator extends VIEW.ViewSubSimple{
	
	final GameWindow window;
	
	final ToolManager tools;
	final Intr intr;
	
	public WorldViewGenerator(){
		
		this.window = WorldView.createwindow();
		new WorldIIMinimap(null, uiManager, window);
		tools = new ToolManager(uiManager, window);
		window.setZoomout(2);
		window.centererTile.set(World.TWIDTH()/2, World.THEIGHT()/2);
		intr = new Intr(this);
		new Stages(this);
		
		
	}

	@Override
	public void activate() {
		super.activate();
		window.stop();
	}
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		window.hover();
	}

	@Override
	protected void mouseClick(MButt button) {

	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		
		return true;
	}
	
	@Override
	protected void render(Renderer r, float ds, boolean hide) {

		window.crop(uiManager.viewPort());
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1(), window.view().y1());
		
		
	}


	

	
	
}
