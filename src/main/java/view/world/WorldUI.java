package view.world;

import view.interrupter.InterManager;
import view.world.ui.army.UIArmies;
import view.world.ui.camps.UICampList;
import view.world.ui.factions.UIFaction;
import view.world.ui.regions.UIRegions;

public class WorldUI {

	public final UIRegions region = new UIRegions();
	public final UIFaction faction;
	public final UIArmies armies = new UIArmies();
	public final UICampList camps = new UICampList();
	
	WorldUI(InterManager m){
		faction = new UIFaction(m);
	}
}
