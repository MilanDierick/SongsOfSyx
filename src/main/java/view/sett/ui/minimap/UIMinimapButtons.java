package view.sett.ui.minimap;

import game.GAME;
import init.*;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.overlay.Addable;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.GButt;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;
import view.subview.GameWindow;

final class UIMinimapButtons extends GuiSection {

	private static CharSequence ¤¤screenshot_question = "This will capture a huge 1:1 image of your settlement and save it in your screenshot folder. It will take some time. Proceed?";
	private static CharSequence ¤¤screenshot_desc = "Take a huge screen-shot of your whole settlement. Screen-shots are saved in your local files, reachable through the game launcher.";
	private static CharSequence ¤¤hotspots = "Hot-spots";
	private static CharSequence ¤¤minipanels = "Mini Panels";
	private static CharSequence ¤¤ToggleOverlay = "¤Toggle Overlay: ";
	private static CharSequence ¤¤HideUI = "¤Cinematic mode + Hide UI. Cancel by right click or ESC.";
	static {
		D.ts(UIMinimapButtons.class);
	}

	private final SUPER_SCREENSHOT shotSett = new SUPER_SCREENSHOT(){
		
		private final static int zoomout = 2;
		private final int winW = (C.WIDTH())<<zoomout;
		private final int winH = (C.HEIGHT())<<zoomout;
		private Rec current = new Rec(winW, winH);
		
		@Override
		public boolean renderAndHasNext() {
			
			if (current.y1() >= SETT.PHEIGHT)
				return false;
			
			GAME.s().render(CORE.renderer(), 0, zoomout, current, 0, 0);
			current.incrX(winW);
			if (current.x1() >= SETT.PWIDTH) {
				current.incrY(winH);
				current.moveX1(0);
			}
			return true;
		}
		
		@Override
		public int getWidth() {
			return SETT.PWIDTH>>zoomout;
		}
		
		@Override
		public int getHeight() {
			return SETT.PHEIGHT>>zoomout;
		}

		@Override
		public void init() {
			current.set(0, winW, 0, winH);
		}
	};
	
	private final GETTER_IMP<Addable> thing = new GETTER_IMP<Addable>();
	
	public UIMinimapButtons(int y1, UIMinimap panel, GameWindow w, boolean resources, boolean hotspots, boolean nobles, boolean species, boolean heat) {
		
		
		int ss = 24;
		
		GButt b;
		GuiSection buttons = new GuiSection();
		
		if (heat) {
			GuiSection s = new GuiSection();
			
			int i = 0;
			for (Addable a : SETT.OVERLAY().all()) {
				if (a.key != null) {
					CLICKABLE cc = ontop(a, thing);
					s.add(cc, (i%2)*cc.body().width(), (i/2)*cc.body().height());
					i++;
				}
				
			}
			

			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().s.eye) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(s, this);
				}
				
				@Override
				protected void renAction() {
					if (hoveredIs() && MButt.RIGHT.consumeClick()) {
						thing.set(null);
					}
					
					if (thing.get() != null) {
						thing.get().add();
					}
					
					selectedSet(thing.get() != null);
				};
			}.hoverInfoSet(DicMisc.¤¤Overlays);
			buttons.addRight(0, c);
		}
		
		
		b = new GButt.ButtPanel(SPRITES.icons().s.camera) {
			@Override
			protected void clickA() {
				CORE.getGraphics().makeScreenShot();
			};
			
		}.setDim(ss, ss);
		buttons.addRight(8, KeyButt.wrap(b, KEYS.MAIN().SCREENSHOT));
		
		b = new GButt.ButtPanel(new ICON.SMALL.Twin(SPRITES.icons().s.camera, SPRITES.icons().s.plus)) {
			
			
			private ACTION yes = new ACTION() {
				
				@Override
				public void exe() {
					RES.loader().init();
					RES.loader().print(DicMisc.¤¤Generating);
					shotSett.perform();
				}
			};
			
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(¤¤screenshot_question, yes, null, true);
			};
		}.setDim(ss, ss);
		b.hoverInfoSet(¤¤screenshot_desc);
		buttons.addRightC(0, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().s.cancel) {
			
			@Override
			protected void clickA() {
				VIEW.hide();
			};
		}.setDim(ss, ss);
		b.hoverInfoSet(¤¤HideUI);
		buttons.addRightC(0, b);

		
		b = new GButt.ButtPanel(SPRITES.icons().s.magnifier) {
			@Override
			protected void clickA() {
				if (w.zoomout() > 0)
					w.setZoomout(w.zoomout()-1);
			};
			@Override
			protected void renAction() {
				activeSet(w.zoomout() > 0);
			}
		}.setDim(ss, ss);
		buttons.addRight(8, KeyButt.wrap(b, KEYS.MAIN().ZOOM_IN));
		
		b = new GButt.ButtPanel(SPRITES.icons().s.minifier) {
			@Override
			protected void clickA() {
				if (w.zoomout() < w.zoomoutmax())
					w.setZoomout(w.zoomout()+1);
				
			};
			@Override
			protected void renAction() {
				activeSet(w.zoomout() < w.zoomoutmax());
			}
		}.setDim(ss, ss);
		buttons.addRight(8, KeyButt.wrap(b, KEYS.MAIN().ZOOM_OUT));
		
		COLOR cView = new ColorImp(47, 23, 0);
		b = new GButt.ButtPanel(SPRITES.icons().s.minimap) {
			@Override
			protected void clickA() {
				panel.minimap.show();
			};
		}.setDim(ss, ss).bg(cView);
		buttons.addRight(0, KeyButt.wrap(b, KEYS.MAIN().MINIMAP));
		
		if (species || nobles || resources || hotspots) {

			GuiSection s = new GuiSection();
			if (species)
				s.addDownC(0, exp(panel.species, HCLASS.CITIZEN.names));
			
			if (resources)
				s.addDownC(0, exp(panel.resources, DicRes.¤¤Resource));
			if (hotspots)
				s.addDownC(0, exp(panel.hs, ¤¤hotspots));
			
			CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().s.menu) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(s, this);
				}
			}.hoverInfoSet(¤¤minipanels);
			buttons.addRightC(8, c);
		}
		
		

		
		add(buttons);
	
	}
	
	private CLICKABLE ontop(Addable add, GETTER_IMP<Addable> thing) {
		 ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				if (thing.get() == add)
					thing.set(null);
				else
					thing.set(add);
				
			}
		};
		CLICKABLE c = new GButt.ButtPanel(UI.FONT().H2.getText(add.name)) {
			@Override
			protected void clickA() {
				a.exe();
			};
			
			@Override
			protected void renAction() {
				selectedSet(thing.get() == add);
			};
			
		}.setDim(250, 30).align(DIR.W).hoverTitleSet(add.name).hoverInfoSet(add.desc);
		c = KeyButt.wrap(a, c, KEYS.SETT(), "TOGGLE_OVERLAY_" + add.key, add.name, ¤¤ToggleOverlay + " " + add.name);
		return c;
	}
	
	private RENDEROBJ exp(Expansion s, CharSequence name) {
		CLICKABLE b = new GButt.ButtPanel(name) {
			@Override
			protected void clickA() {
				s.visableSet(!s.visableIs());
			};
			
			@Override
			protected void renAction() {
				selectedSet(s.visableIs());
			}
		}.setDim(140, 32);
		return b;
	}
	
	void clearOverlay() {
		thing.set(null);
	}

}
