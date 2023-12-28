package world.regions;

import game.faction.FACTIONS;
import snake2d.util.misc.ACTION;
import world.WORLD;
import world.regions.map.WGenRegionMap;

public class WGenRegions {

	WGenRegions(){
		
	}
	
	public void generate(int px, int py, ACTION printer) {
		
		printer.exe();
		clear();
		printer.exe();
		
		WGenRegionMap m = new WGenRegionMap(this);
		m.generateAll(px, py, printer);
		
		WORLD.REGIONS().initAfterGenerated();
		
		
		
	}
	
	public void clear() {
		while(FACTIONS.NPCs().size() > 0) {
			FACTIONS.remove(FACTIONS.NPCs().get(0), false);
		}
		WORLD.REGIONS().clear();
	}
	
}
