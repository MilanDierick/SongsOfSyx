package view.world.ui.regions;

import game.faction.FACTIONS;
import init.C;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.map.regions.Region;

public final class UIRegions {

	final UIRegionList list = new UIRegionList();
	final UIRegion detail = new UIRegion();
	final UIIndustry industry = new UIIndustry();
	
	public UIRegions() {
		
	}
	
	public void openList(Region f) {
		open(f, VIEW.world().panels);
	}
	
	public void open(Region f) {
		VIEW.world().panels.add(detail.get(f), true);
	}
	
	public void open(Region f, ISidePanels m) {
		if (f == null) {
			m.add(list.get(null), true);
		}else if (f.faction() != FACTIONS.player()) {
			m.add(list.get(f), true);
		}else {
			if (f.faction() == FACTIONS.player()) {
				boolean in = m.added(industry);
				m.add(list.get(f), true);
				m.add(detail.get(f), false);
				if (in)
					m.add(industry, false);
			}else {
				m.remove(detail);
				m.remove(industry);
			}
			
			
		}
		if (f != null) {
			int x1 = f.cx()*C.TILE_SIZE;
			int y1 = f.cy()*C.TILE_SIZE;
			VIEW.world().window.centerAt(x1, y1);
		}
	}
	
	void openFromList(Region f, ISidePanels m) {
		if (f == null) {
			m.add(list.get(null), true);
		}else {
			
			
			if (f.faction() == FACTIONS.player()) {
				boolean in = m.added(industry);
				m.add(list.get(null), false);
				m.add(detail.get(f), false);
				if (in)
					m.add(industry, false);
			}else {
				m.remove(detail);
				m.remove(industry);
			}
			
			int x1 = f.cx()*C.TILE_SIZE;
			int y1 = f.cy()*C.TILE_SIZE;
			VIEW.world().window.centerAt(x1, y1);
		}
	}
	
	public boolean listIsOpen(ISidePanels m) {
		return m.added(list);
	}
	
	public void close(ISidePanels m) {
		m.remove(list);
		m.remove(detail);
		m.remove(industry);
	}
	
}
