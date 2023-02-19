package view.world.generator;

import game.faction.FACTIONS;
import init.C;
import init.D;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanelL;
import view.interrupter.Interrupter;
import view.main.VIEW;

public class Interer extends Interrupter{

	GuiSection s = new GuiSection();
	private static CharSequence ¤¤YouSure = "¤You may still pick some unlocked titles. Start anyway?";
	
	static {
		D.ts(Interer.class);
	}
	
	Interer(WorldViewGenerator v){
		
		v.uiManager.add(this);
		pin();
		
		s.add(new Titles());
		
		s.addRelBody(25, DIR.S, new GButt.ButtPanel((SPRITE)UI.FONT().H2.getText("Go")) {
			@Override
			protected void clickA() {
				ACTION no = new ACTION() {
					
					@Override
					public void exe() {
						// TODO Auto-generated method stub
						
					}
				};
				if (FACTIONS.player().titles.selected() < 5 && FACTIONS.player().titles.unlocked() > FACTIONS.player().titles.selected()) {
					VIEW.inters().yesNo.activate(¤¤YouSure, a, no, true);
				}else {
					a.exe();
				}
			}
		});
		s.body().centerIn(C.DIM());
		
		GPanelL pan = new GPanelL();
		pan.body.setDim(800, 600);
		pan.body.centerIn(s);
		s.add(pan);
		s.moveLastToBack();
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			s.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		s.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		s.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		
		return false;
	}
	
	@Override
	public boolean canSave() {
		return false;
	}
	
	private final ACTION a = new ACTION() {
		
		@Override
		public void exe() {
			hide();
		}
	};

}
