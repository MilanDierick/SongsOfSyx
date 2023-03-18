package view.battle;

import java.io.IOException;

import game.GAME;
import init.C;
import init.config.Config;
import init.sprite.SPRITES;
import settlement.army.Army;
import settlement.army.Div;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.dic.DicArmy;
import util.gui.misc.GBox;
import util.gui.slider.GSliderVer;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;

final class UIPanelUnitCards extends ISidePanel{

	
	private static final int divsPerRow = 6;
	private DivButtons sButtons;
	private DivButton[] buttons = new DivButton[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int rowTop;
	private final int max;
	private final int rows;
	private final DivSelection selection;
	private final Army army;
	
	final INTE target = new INTE() {
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return max;
		}
		
		@Override
		public int get() {
			return rowTop;
		}
		
		@Override
		public void set(int top) {
			int x1 = sButtons.body().x1();
			int y1 = sButtons.body().y1();
			sButtons.clear();
			rowTop = top;
			
			int i = rowTop*divsPerRow;
			
			for (int y = 0; y < rows; y++) {
				for (int x = 0; x < divsPerRow; x++) {
					
					if (i < Config.BATTLE.DIVISIONS_PER_ARMY) {
						RENDEROBJ b = buttons[i];
						b.body().moveX1(SPRITES.armyCard().width()*x);
						b.body().moveY1(SPRITES.armyCard().height()*y);
						sButtons.add(b);
					}
					i++;
				}
			}
			sButtons.body().moveX1(x1);
			sButtons.body().moveY1(y1);
		}
	};

	public UIPanelUnitCards(Army army, DivSelection selection) {
		titleSet(DicArmy.¤¤Army);
		section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (hoveredIs()) {
					float f = MButt.clearWheelSpin();
					if (f > 0) {
						target.inc(-1);
					}else if (f < 0 )
						target.inc(1);
				}
				super.render(r, ds);
			};
		};
		section.body().setWidth(divsPerRow*SPRITES.armyCard().width());
		
		
		sButtons = new DivButtons(HEIGHT, army, selection);
		section.add(sButtons);
		this.army = army;
		
		
		for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++) {
			buttons[i] = new DivButton(army.divisions().get(i), selection);
		}
		this.selection = selection;

		rows = HEIGHT/SPRITES.armyCard().height();
		
		
		max = CLAMP.i((int) Math.ceil((double)Config.BATTLE.DIVISIONS_PER_ARMY/divsPerRow) - rows, 0, Integer.MAX_VALUE);
		
		target.set(0);
		
		GSliderVer s = new GSliderVer(target, HEIGHT-C.SG*8);
		s.body().moveX1(section.body().x2());
		section.add(s);
		
		
		
		
		
	}

	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (DivButton b : buttons) {
				file.i(b.div.index());
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			
			for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++) {
				buttons[i] = new DivButton(army.divisions().get(file.i()), selection);
			}
			target.set(0);
		}
		
		@Override
		public void clear() {
			
			for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++) {
				buttons[i] = new DivButton(army.divisions().get(i), selection);
			}
		}
	};
	

	
	private class DivButtons extends GuiSection{
		

		
		
		DivButtons(int height, Army army, DivSelection selection) {
			
		}
		
		@Override
		public boolean click() {
			
			DivButton.lastClicked = null;
			
			boolean ret = super.click();
			
			if (DivButton.lastClicked == null)
				return ret;
			
			if (KEYS.MAIN().MOD.isPressed()) {
				selection.toggle(DivButton.lastClicked.div);
				DivButton.lastClicked = null;
			}else if(KEYS.MAIN().UNDO.isPressed()) {
				boolean foundSmallest = false;
				boolean foundClicked = false;
				for (int i = 0; i < buttons.length; i++) {
					
					if (selection.selected(buttons[i].div))
						foundSmallest = true;
					
					if (foundClicked) {
						selection.deSelect(buttons[i].div);
					}else if(foundSmallest) {
						selection.select(buttons[i].div);
					}else {
						selection.deSelect(buttons[i].div);
					}
					
					if (buttons[i] == DivButton.lastClicked) {
						foundClicked = true;
					}
					
				}
				DivButton.lastClicked = null;
			}else {
				selection.clear();
				selection.select(DivButton.lastClicked.div);
			}
				
			
			
			return ret;
		};
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			if (isHoveringAHoverElement() && DivButton.lastClicked != null) {
				
				DivButton b = (DivButton) hovered();
				DivButton a = DivButton.lastClicked;
				
				if (a != b && a.div.army() == b.div.army())
					COLOR.GREEN100.render(r, b.body().x1(), b.body().x1()-C.SG*4, b.body().y1(), b.body.y2());
				
				if (!MButt.LEFT.isDown() && a != b && a.div.army() == b.div.army()) {
					
					int ai = -1;
					
					for (int i = 0; i < buttons.length; i++) {
						if (buttons[i] == a) {
							if (ai != -1) {
								GAME.Error("ai " + ai);
							}
							ai = i;
						}
					}
					
					if (ai == -1 ) {
						GAME.Error(a + " " + b + " " + ai);
					}
					
					for (int i = ai; i < buttons.length-1; i++) {
						buttons[i] = buttons[i+1];
					}
					
					
					int bi = -1;
					for (int i = 0; i < buttons.length-1; i++) {
						if (buttons[i] == b) {
							if (bi != -1) {
								GAME.Error("bi " + bi);
							}
							bi = i;
						}
					}
					
					if (bi == -1) {
						GAME.Error(a + " " + b + " " + ai + " " + bi);
					}
					
					for (int i = buttons.length-1; i > bi; i--) {
						buttons[i] = buttons[i-1];
						if (buttons[i] == b) {
							buttons[i-1] = a;
							break;
						}
							
					}
					buttons[bi] = a;
					
					target.set(rowTop);
					
				}
			}
			if (!MButt.LEFT.isDown())
				DivButton.lastClicked = null;
			
			
		}
		
		
		
	}
	
	static class DivButton extends ClickableAbs{

		private final Div div;
		private final DivSelection selection;
		private static DivButton lastClicked;
		
		DivButton(Div div, DivSelection selection){
			body.setDim(SPRITES.armyCard());
			this.div = div;
			this.selection = selection;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isSelected = selection.selected(div);
			isHovered = selection.hovered(div) || lastClicked == this;
			
			SPRITES.armyCard().render(div, body().x1(), body().y1(), r, ds, isActive, isSelected, isHovered);
			
		}
		
		@Override
		protected void clickA() {
			lastClicked = this;
			if (MButt.LEFT.isDouble() && div.menNrOf() > 0) {
				VIEW.s().battle.getWindow().centerer.set(div.reporter.body().cX(), div.reporter.body().cY());
				VIEW.inters().popup.show(VIEW.b().hoverer.get(div), this);
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (VIEW.inters().popup.showing())
				return;
			div.hoverInfo((GBox)text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				selection.hover(div);
				return true;
			}
			return false;
		}
		
	}
}
