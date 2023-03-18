package world.map.regions;

import static world.World.*;

import game.faction.FACTIONS;
import snake2d.util.datatypes.COORDINATE;
import world.WorldGen;

public final class WorldGeneratorRegions {

	
	public void clear() {
		
		REGIONS().clear();
		new GeneratorRoad().clear();
		
	}
	
	public void setPlayer(int px, int py) {
		Region player = REGIONS().getByIndex(0);
		for (int y = 0; y < CapitolPlacablity.TILE_DIM; y++) {
			for (int x = 0; x < CapitolPlacablity.TILE_DIM; x++) {
				REGIONS().setter.set(px-CapitolPlacablity.TILE_DIM/2+x,py-CapitolPlacablity.TILE_DIM/2+y, player);
				
			}
		}
		GeneratorInit.init(px, py);
		player.centreSet(px, py);
		REGIOND.OWNER().setCapitol(px, py, FACTIONS.player());
		player.name().clear().add(FACTIONS.player().appearence().name());
		player.init(px, py, py, py, CapitolPlacablity.TILE_DIM*CapitolPlacablity.TILE_DIM, 0);
	}
	
	public void generateAllAreas() {
		Region first = null;
		for (int i = 0; i < REGIONS().all().size(); i++) {
			if (REGIONS().all().get(i).area() == 0) {
				first = REGIONS().all().get(i);
				
				break;
			}
		}
		
		new GeneratorAssigner(first.index());
		
	}
	
	public void initAll(boolean hasPlacedPlayer) {
		
		final boolean[] inited = new boolean[Regions.MAX];
		
		for (COORDINATE c : TBOUNDS()) {
			Region r = REGIONS().setter.get(c);
			
			if (r == null)
				continue;
			if (inited[r.index()])
				continue;
			if (hasPlacedPlayer && r.index() == 0)
				continue;
			inited[r.index()] = true;
			GeneratorInit.init(c.x(), c.y());
			GeneratorCenter.init(r);
		}
		
	}
	
	public void init(int rx, int ry) {
		GeneratorInit.init(rx, ry);
	}
	
	private static GeneratorFlavour flav;
	
	public void randomiseNames() {
		for (int i = 0; i < REGIONS().all().size(); i++) {
			if (REGIONS().all().get(i).area() != 0) {
				randomiseNames(REGIONS().all().get(i));
			}
		}
	}
	
	public void randomiseNames(Region r) {
		if (flav == null) {
			flav = new GeneratorFlavour();
		}
		flav.init(r);
	}
	
	public void populate(Region r) {
		if (r.area > 0 && !r.isWater)
			new GeneratorPopulator().init(r);
	}
	
	
	public void populateAll() {
		GeneratorPopulator p = new GeneratorPopulator();
		for (Region r : REGIONS().all()) {
			if (r.area > 0 && !r.isWater)
				p.init(r);
		}
		
	}
	
	public void makeRoads() {
		new GeneratorRoad().generate();
	}
	
	public void makeDistances() {
		new GeneratorDistances();
	}
	
	public void makeFactions(WorldGen gen) {
		new GeneratorFactions().generate(gen);
	}
	
	public void finish(WorldGen gen) {
		new GeneratorFinish().generate(gen);
	}
	
	
	
}
