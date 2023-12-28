package world.regions.data.gen;

import game.GAME;
import world.WORLD;
import world.WorldGen;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;

final class GeneratorFinish {

	public GeneratorFinish(RDInit init, WorldGen gen) {

		for (Region reg : WORLD.REGIONS().active()) {
			RD.UPDATER().BUILD(reg);
			RD.BUILDINGS().init(reg);
			for (RDUpdatable up : init.upers) {
				up.init(reg);
			}
		}
		
		GAME.factions().prime();
		WORLD.ARMIES().init();
		
		
		
	}

	

	
}
