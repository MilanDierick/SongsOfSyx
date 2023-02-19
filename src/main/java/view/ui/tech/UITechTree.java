package view.ui.tech;

import game.faction.FACTIONS;
import view.interrupter.ISidePanel;

public class UITechTree extends ISidePanel{

	final Tree tree;

	public UITechTree(){
		
		titleSet(FACTIONS.player().tech().info.name);
		
		section.add(new Info());
		
		tree = new Tree(HEIGHT-(section.getLastY2()+4));
		section.add(tree, 0, section.getLastY2()+4);
	}
	
	
}
