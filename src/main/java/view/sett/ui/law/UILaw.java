package view.sett.ui.law;

import snake2d.util.datatypes.DIR;
import util.dic.DicMisc;
import view.interrupter.ISidePanel;

public class UILaw extends ISidePanel{

	
	public UILaw(){
		titleSet(DicMisc.¤¤Law);
		section.addRelBody(16, DIR.S, new CrimeChart(14));
		section.addRelBody(16, DIR.S, new LawChart(14));
		section.addRelBody(16, DIR.S, new Settings(HEIGHT - section.body().height()-24));
	}

}
