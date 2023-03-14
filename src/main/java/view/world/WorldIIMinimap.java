package view.world;

import static world.World.*;

import game.GAME;
import init.C;
import init.RES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.colors.GCOLORS_MAP;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GFrame;
import util.rendering.Minimap;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.subview.GameWindow;
import view.ui.UIPanelTop;
import world.World;
import world.entity.army.WArmy;

public final class WorldIIMinimap extends Interrupter{

	private final Minimap map = World.MINIMAP().map;
	private boolean clicked = false;
	private boolean expanded = true;
	private final GFrame frame = new GFrame();
	GuiSection buttons = new GuiSection();
	private final GameWindow window;
	
	public WorldIIMinimap(UIPanelTop top, InterManager m, GameWindow window){
		
		this.window = window;
		pin();
		frame.body().setWidth(map.width()).setHeight(map.height());
		frame.body().moveX2(C.WIDTH()-GFrame.MARGIN);
		frame.body().moveY1(GFrame.MARGIN);
		
		
		
		
		
		
		CLICKABLE b;
		
		if (top != null) {
			b = new WorldHeatmaps();
			buttons.addRight(0, b);
		}
		
		b = new GButt.Panel(SPRITES.icons().s.camera) {
			@Override
			protected void clickA() {
				CORE.getGraphics().makeScreenShot();
			};
		};
		b = KeyButt.wrap(b, KEYS.MAIN().SCREENSHOT);
		buttons.addRight(0, b);
		
		b = new GButt.Panel(SPRITES.icons().m.camera) {
			
			private final String question = "This will capture a huge 1:1 image of your world and"
					+ " save it in your screenshot folder. It will take some time. Proceed?";
			
			private ACTION yes = new ACTION() {
				
				@Override
				public void exe() {
					RES.loader().init();
					RES.loader().print("taking a super screenshot!");
					shotWorld.perform();
				}
			};
			
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(question, yes, null, true);
			};
		};
		b.hoverInfoSet("Take a super screenshot of the world");
		buttons.addRightC(8, b);
		
		b = new GButt.Panel(SPRITES.icons().m.plus) {
			@Override
			protected void clickA() {
				if (window.zoomout() > 0)
					window.setZoomout(window.zoomout()-1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(window.zoomout() > 0);
			}
		};
		b = KeyButt.wrap(b, KEYS.MAIN().ZOOM_IN);
		b.hoverInfoSet("" + KEYS.MAIN().MOD.repr() + DicMisc.¤¤MouseWheelAdd);
		buttons.addRightC(10, b);
		
		b = new GButt.Panel(SPRITES.icons().m.minus) {
			@Override
			protected void clickA() {
				if (window.zoomout() < 3)
					window.setZoomout(window.zoomout()+1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(window.zoomout() < 3);
			}
		};
		b = KeyButt.wrap(b, KEYS.MAIN().ZOOM_OUT);
		
		b.hoverInfoSet("" + KEYS.MAIN().MOD.repr() + DicMisc.¤¤MouseWheelAdd);
		buttons.addRightC(0, b);

		GuiSection pan = new GuiSection();
		pan.add(UI.PANEL().panelL.get(DIR.N, DIR.E), 0, 0);
		while(pan.body().width() <= map.width()+UI.PANEL().panelL.dim())
			pan.addRightC(0, UI.PANEL().panelL.get(DIR.N, DIR.E, DIR.W));
		
		buttons.body().moveX2(C.WIDTH()-4);
		buttons.body().moveY1(0);
		pan.body().moveX2(C.WIDTH());
		buttons.add(pan);
		buttons.moveLastToBack();
		buttons.body().moveY1(top == null ? 0 : UIPanelTop.HEIGHT);

			frame.body().moveY1(buttons.body().y2());
	
		show(m);
	}
	
	public void render(SPRITE_RENDERER r, GameWindow window) {
		
		if (!expanded)
			return;
		
		buttons.render(r, 0);
		
		RECTANGLE pos = frame.body();
		map.render(r, pos.x1(), pos.y1());
		frame.render(r, 0);

		for (int ai = 0; ai < World.ENTITIES().armies.max(); ai++) {
			WArmy a = World.ENTITIES().armies.get(ai);
			if (a == null)
				continue;

			int cx = map.width()*a.ctx()/World.TWIDTH();
			int cy = map.height()*a.cty()/World.THEIGHT();
			
			cx += pos.x1();
			cy += pos.y1();
			OPACITY.O25TO100.bind();
			COLOR.BLACK.render(r, cx-4, cx+4, cy-4, cy+4);
			COLOR.WHITE100.render(r, cx-3, cx+3, cy-3, cy+3);
			COLOR c = GCOLORS_MAP.get(a.faction());
			
			c.render(r, cx-2, cx+2, cy-2, cy+2);
			
		}
		OPACITY.unbind();
		COLOR.unbind();
		
		clicked = clicked && MButt.LEFT.isDown();
		if (clicked)
			move(window);
		
		int x1 = pos.x1() + (int)((map.width())*window.pixels().cX()/(float)PWIDTH());
		int y1 = pos.y1() + (int)((map.height())*window.pixels().cY()/(float)PHEIGHT());
		
		int miniW = (int) ((map.width())*window.pixels().width()/PWIDTH());
		int miniH = (int) ((map.height())*window.pixels().height()/PHEIGHT());
		
		x1 -= miniW/2;
		y1 -= miniH/2;
		
		int x2 = x1 + miniW;
		int y2 = y1 + miniH;
		
		if(x1 < pos.x1()){
			x1 = pos.x1();;
		}
		
		if(y1 < pos.y1()){
			y1 = pos.y1();
		}
		
		if (y2 > pos.y2()){
			y2 = pos.y2();
		}
		
		if (x2 > pos.x2()){
			x2 = pos.x2();
		}
		
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, x1, x1+1, y1, y2);
		COLOR.WHITE100.render(r, x1+1, x1+2, y1, y2);
		COLOR.BLACK.render(r, x2-1, x2, y1, y2);
		COLOR.WHITE100.render(r, x2-2, x2-1, y1, y2);
		COLOR.BLACK.render(r, x1, x2, y1, y1+1);
		COLOR.WHITE100.render(r, x1+1, x2-1, y1+1, y1+2);
		COLOR.BLACK.render(r, x1, x2, y2-1, y2);
		COLOR.WHITE100.render(r, x1+1, x2-1, y2-2, y2-1);
		OPACITY.O25.bind();
		COLOR.BLACK.render(r, x1+2, x2-2, y1+2, y2-2);
		OPACITY.unbind();
		
		
	}
	
	private void move(GameWindow window){
		
		if (clicked || expanded){
			clicked = true;
			int x1 = (int) ((VIEW.mouse().x() - frame.body().x1())*(float)(PWIDTH()/map.width()));
			int y1 = (int) ((VIEW.mouse().y() - frame.body().y1())*(float)(PHEIGHT()/map.height()));
			window.centerAt(x1, y1);
		}
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return buttons.hover(mCoo) || mCoo.isWithinRec(frame);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button != MButt.LEFT)
			return;
		if (buttons.click())
			return;
		else
			move(window);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		render(r, window);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void hoverTimer(GBox text) {
		
	}
	
	private final SUPER_SCREENSHOT shotWorld = new SUPER_SCREENSHOT(){
		
		private final static int zoomout = 2;
		private final int winW = (C.WIDTH())<<zoomout;
		private final int winH = (C.HEIGHT())<<zoomout;
		private Rec current = new Rec(winW, winH);
		
		@Override
		public boolean renderAndHasNext() {
			
			if (current.y1() >= World.PHEIGHT())
				return false;
			
			GAME.world().render(CORE.renderer(), 0, zoomout, current, 0, 0);
			current.incrX(winW);
			if (current.x1() >= World.PWIDTH()) {
				current.incrY(winH);
				current.moveX1(0);
			}
			return true;
		}
		
		@Override
		public int getWidth() {
			return World.PWIDTH()>>zoomout;
		}
		
		@Override
		public int getHeight() {
			return World.PHEIGHT()>>zoomout;
		}

		@Override
		public void init() {
			current.set(0, winW, 0, winH);
			
		}
	};
	
}
