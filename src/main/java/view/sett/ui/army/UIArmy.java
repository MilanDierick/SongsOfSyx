package view.sett.ui.army;

import static settlement.main.SETT.*;

import init.config.Config;
import settlement.army.ArmyManager;
import settlement.army.Div;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.dic.DicArmy;
import view.interrupter.ISidePanel;
import view.sett.IDebugPanelSett;

public final class UIArmy extends ISidePanel{

	private final Hoverer hoverer = new Hoverer();	
	final TrainingSpec spec = new TrainingSpec();
	
	public UIArmy(ArmyManager m){
		titleSet(DicArmy.¤¤Conscripts);
		
		ArrayList<Div> selection = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		
		this.section.add(new Info());
		this.section.addRelBody(8, DIR.S, new Actions(selection));
		this.section.addRelBody(8, DIR.S, new DivList(HEIGHT-this.section.body().height()-16, selection));
		
		IDebugPanelSett.add(new FormationDebugPlacer(ARMIES().player()));
		IDebugPanelSett.add(new FormationDebugPlacer(ARMIES().enemy()));
		
		
	}


	public void hover(Div div, GUI_BOX box) {
		hoverer.hover(box, div);
	}
	
	public boolean hoverSendOutProblem(LIST<Div> divs, GUI_BOX box) {
		return Actions.hoverSendOutProblem(divs, box);
	}
	
}
