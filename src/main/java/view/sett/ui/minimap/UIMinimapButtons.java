package view.sett.ui.minimap;

import game.GAME;
import init.C;
import init.D;
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
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.common.SuperSc;
import util.gui.misc.GButt;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;
import view.subview.GameWindow;

final class UIMinimapButtons extends GuiSection {

	private static CharSequence ¤¤hotspots = "Hot-spots";
	private static CharSequence ¤¤minipanels = "Mini Panels";
	private static CharSequence ¤¤ToggleOverlay = "¤Toggle Overlay: ";
	private static CharSequence ¤¤HideUI = "¤Cinematic mode + Hide UI. Cancel by right click or ESC.";
	static {
		D.ts(UIMinimapButtons.class);
	}

	public final static int height = 36;
	
	private final SUPER_SCREENSHOT shotSett = new SUPER_SCREENSHOT(2){
		
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
	
	public UIMinimapButtons(int y1, int width, UIMinimap panel, GameWindow w, boolean resources, boolean hotspots, boolean nobles, boolean species, boolean heat) {
		
		
		int ww = 30;
		int hh = 26;
		
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
			}.setDim(ww, hh).hoverInfoSet(DicMisc.¤¤Overlays);
			
			buttons.addRight(0, c);
		}
		
		
		b = new GButt.ButtPanel(SPRITES.icons().s.camera) {
			@Override
			protected void clickA() {
				CORE.getGraphics().makeScreenShot();
			};
			
		}.setDim(ww, hh);
		buttons.addRight(0, KeyButt.wrap(b, KEYS.MAIN().SCREENSHOT));
		
		b = new GButt.ButtPanel(SPRITES.icons().s.cameraBig) {
			
			private final SuperSc sst = new SuperSc("SUPER_WORLD", shotSett);
			
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(sst, this, true);
			};
		}.setDim(ww, hh);
		b.hoverInfoSet(SuperSc.¤¤name);
		buttons.addRightC(0, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().s.cancel) {
			
			@Override
			protected void clickA() {
				VIEW.hide();
			};
		}.setDim(ww, hh);
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
		}.setDim(ww, hh);
		buttons.addRight(0, KeyButt.wrap(b, KEYS.MAIN().ZOOM_IN));
		
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
		}.setDim(ww, hh);
		buttons.addRight(0, KeyButt.wrap(b, KEYS.MAIN().ZOOM_OUT));
		
		COLOR cView = new ColorImp(47, 23, 0);
		b = new GButt.ButtPanel(SPRITES.icons().s.minimap) {
			@Override
			protected void clickA() {
				panel.minimap.show();
			};
		}.setDim(ww, hh).bg(cView);
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
			}.setDim(ww, hh).hoverInfoSet(¤¤minipanels);
			buttons.addRightC(0, c);
		}
		
		
		body().setWidth(width);
		body().setHeight(height);
		buttons.body().moveCY(body().cY());
		buttons.body().moveX2(body().x2()-6);
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
		if (s == null)
			throw new RuntimeException();
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
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		GCOLOR.UI().panBG.render(r, body());
		GCOLOR.UI().borderH(r, body(), 0);
//		GCOLOR.UI().border(r, body().x1(), body().x1()+3, body().y1(), body().y2());
//		GCOLOR.UI().border(r, body().x1(), body().x2(), body().y2(), body().y2()+3);
		super.render(r, ds);
	}

}
