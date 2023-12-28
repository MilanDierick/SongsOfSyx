package view.world;

import view.interrupter.ISidePanels;
import view.interrupter.InterManager;
import view.tool.ToolManager;
import view.world.ui.army.UIArmies;
import view.world.ui.battle.UIWBattlePrompt;
import view.world.ui.camps.UICampList;
import view.world.ui.region.UIRegionView;

public class WorldUI {

	public final UIRegionView regions;
	public final UIArmies armies = new UIArmies();
	public final UICampList camps = new UICampList();
	public final UIWBattlePrompt battle = new UIWBattlePrompt();
	
	WorldUI(InterManager m, ISidePanels panels, ToolManager tools){
		regions = new UIRegionView(panels, tools);
	}
}
