package world.map.regions;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import world.World;
import world.WorldGen;

final class GeneratorFinish {

	public GeneratorFinish() {

	}
	
	public void generate(WorldGen gen) {
		
		for (Faction f : FACTIONS.all()) {
			if (f.kingdom().realm().capitol() != null) {
				World.ARMIES().init(f);
				World.ai().init(f);
			}
		}
		World.ARMIES().mercenaries().randmoize();
		
		GAME.factions().prime();
		World.ARMIES().mercenaries().randmoize();
	}

	

	
}
