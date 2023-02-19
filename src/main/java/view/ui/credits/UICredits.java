package view.ui.credits;

import static util.dic.DicRes.*;

import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.GuiSwitch;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.interrupter.ISidePanel;

public final class UICredits extends ISidePanel {


	public final ICON.SMALL icon;
	
	private final RENDEROBJ soverview;
	private final RENDEROBJ stourism;
	private final GuiSwitch swi;
	
	public UICredits() {
		
		
		icon = SPRITES.icons().s.money;
		titleSet(¤¤Treasury);

		
		GuiSection nav = new GuiSection();
		
		nav.addRightC(8, new GButt.ButtPanel(DicMisc.¤¤Overview) {
			
			@Override
			protected void clickA() {
				swi.currentSet(soverview);
			};
			
			@Override
			protected void renAction() {
				selectedSet(soverview == swi.current());
			};
			
		}.pad(6, 2));
		nav.addRightC(8, new GButt.ButtPanel(DicMisc.¤¤Tourists) {
			
			@Override
			protected void clickA() {
				swi.currentSet(stourism);
			};
			
			@Override
			protected void renAction() {
				selectedSet(stourism == swi.current());
			};
			
		}.pad(6, 2));
		
		section.add(nav);

		int h = HEIGHT-nav.body().height()-16;
		soverview = new COverview(h);
		stourism = new Tourism(h);
		
		swi = new GuiSwitch(DIR.N, soverview, stourism);

		section.addRelBody(8, DIR.S, swi.section());
		

	}

	public ISidePanel tourists() {
		swi.currentSet(stourism);
		return this;
	}


}
