package view.sett.ui.bottom;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanelS;
import view.main.VIEW;
import view.tool.PLACABLE;
import view.tool.ToolConfig;

class UIConstuctConfig implements ToolConfig{

	protected CharSequence name;
	protected GuiSection section = new GuiSection();
	private final GPanelS panel = new GPanelS();
	private GuiSection full = new GuiSection();
	CLICKABLE exit = new GButt.Glow(SPRITES.icons().s.cancel, UI.PANEL().panelM.get(0)) {
		@Override
		protected void clickA() {
			VIEW.s().tools.placer.deactivate();
		};
	};
	PLACABLE placer;
	
	protected UIConstuctConfig(CharSequence name) {
		this.name = name;
		panel.setButtBg();
		panel.setTitle(name);
		panel.titleCenter();
	}
	
	public void activate() {
		VIEW.s().tools.place(placer, this);
	}
	
	public boolean isActive() {
		return VIEW.s().tools.configCurrent() == this;
	}

	
	@Override
	public void addUI(LISTE<RENDEROBJ> uis) {
		full.clear();
		
		VIEW.s().tools.placer.stealButtons(full);
		if ( placer.getAdditionalButt() != null)
			for (CLICKABLE p : placer.getAdditionalButt())
				full.addRightC(0, p);
		full.body().centerX(C.DIM());
		full.addRelBody(C.SG*8, DIR.N, section);
		
		panel.setButtBg();
		panel.inner().set(full);
		panel.moveExit(exit);
		full.add(panel);
		full.moveLastToBack();
		full.body().moveY1(75);
		uis.add(full);
	}
	
	protected class Butt extends GButt.Panel {
		
		private final PLACABLE p;
		
		Butt(PLACABLE p){
			super(p.getIcon());
			this.p = p;
			if (placer == null)
				placer = p;
		}
		
		Butt(PLACABLE p, SPRITE icon){
			super(icon);
			this.p = p;
			if (placer == null)
				placer = p;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			p.hoverDesc((GBox)text);
		}
		
		@Override
		protected void clickA() {
			placer = p;
			activate();
		}
		
		@Override
		protected void renAction() {
			selectedSet(p == placer);
		}
		
	}
	
	
	
	
}
