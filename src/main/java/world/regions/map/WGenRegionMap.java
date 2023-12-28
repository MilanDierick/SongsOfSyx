package world.regions.map;

import static world.WORLD.*;

import game.faction.FACTIONS;
import snake2d.util.misc.ACTION;
import world.regions.Region;
import world.regions.WGenRegions;

public class WGenRegionMap {

	public WGenRegionMap(WGenRegions gg) {
		// TODO Auto-generated constructor stub
	}
	
	
	public int generateAll(int px, int py, ACTION lprinter) {
		
		lprinter.exe();
		
		GMapObstacle bounds = new GMapObstacle(px, py);
		lprinter.exe();
		GMapIds ids = new GMapIds(px, py, bounds);
		lprinter.exe();
		GMapTmp map = new GMapTmp(ids, px, py);
		lprinter.exe();
		new GMapProcess(px, py, map);
		lprinter.exe();
		int am = new GMapAssign(map, px, py).generate();
		lprinter.exe();
		
		Region player = REGIONS().getByIndex(0);
		player.info.centreSet(px, py);
		player.fationSet(FACTIONS.player());
		player.setCapitol();
		player.info.name().clear().add(FACTIONS.player().name);
		lprinter.exe();
		
		GeneratorFlavour flav = new GeneratorFlavour();
		for (int i = 0; i < REGIONS().all().size(); i++) {
			if (REGIONS().all().get(i).info.area() != 0) {
				flav.init(REGIONS().all().get(i));
			}
		}
		
		lprinter.exe();
		
		return am;
	}

	
	

	
}
