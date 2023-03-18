package view.sett.ui.law;

import init.sprite.SPRITES;
import settlement.stats.law.Curfew;
import settlement.stats.law.LAW;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.interrupter.ISidePanel;

public class UILaw extends ISidePanel{

	
	public UILaw(){
		titleSet(DicMisc.¤¤Law);
		section.addRelBody(16, DIR.S, new CrimeChart(14));
		section.addRelBody(16, DIR.S, new LawChart(14));
		
		GButt.ButtPanel cur;
		
		{
			
			cur = new GButt.ButtPanel(SPRITES.icons().m.building) {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(Curfew.¤¤name);
					text.text(Curfew.¤¤desc);
					
					if (LAW.curfew().isSetForADay())
						text.text(DicMisc.¤¤Deactivate);
					else
						text.text(DicMisc.¤¤Activate);
						
				}
				
				@Override
				protected void renAction() {
					selectedSet(LAW.curfew().isSetForADay());
				}
				
				@Override
				protected void clickA() {
					LAW.curfew().setForADay(!LAW.curfew().isSetForADay());
				}
			};
			
			cur.pad(14, 14);
			cur.body.moveX1(section.body().x2()+40);
			cur.body.centerY(section);
			
		}
		
		section.addRelBody(16, DIR.S, new Settings(HEIGHT - section.body().height()-24));
		
		section.add(cur);
	}

}
