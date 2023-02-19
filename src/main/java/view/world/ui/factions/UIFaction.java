package view.world.ui.factions;

import game.faction.Faction;
import game.faction.npc.FactionNPC;
import view.interrupter.InterManager;
import view.main.VIEW;

public final class UIFaction{

	private final UIFactionList list = new UIFactionList();
	final UIFactionDetail detail = new UIFactionDetail();
	
	public UIFaction(InterManager m){

	}
	
	public void open(Faction r) {
		detail.activate((FactionNPC) r);
	}
	
	public void openList(Faction r) {
		list.open(r, true);
	}
	
	public boolean listIsOpen() {
		return VIEW.world().panels.added(list);
	}
	
	public void close() {
		VIEW.world().panels.clear();
	}
	

}
