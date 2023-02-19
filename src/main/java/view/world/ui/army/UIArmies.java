package view.world.ui.army;

import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.entity.army.WArmy;

public final class UIArmies {

	private final UIArmyList list = new UIArmyList();
	final UIArmy army = new UIArmy();
	public final DivCard divCard = new DivCard();
	
	public void openList(WArmy f) {
		openList(f, VIEW.world().panels);
	}
	
	public void open(WArmy f) {
		VIEW.world().panels.add(army.get(f), true);
	}
	
	public void openList(WArmy f, ISidePanels m) {
		
		m.add(list, true);
		if (f != null) {
			list.set(f);
			m.add(army.get(f), false);
			VIEW.world().window.centerer.set(f.body().cX(), f.body().cY());
		}else {
			m.add(list, true);
		}
			
	}
	
	public boolean listIsOpen(ISidePanels m) {
		return m.added(list);
	}
	
	public void close(ISidePanels m) {
		m.remove(list);
	}
	
}
