package world.map.terrain;

import static world.World.*;

import game.GAME;
import game.GameConRandom;
import init.RES;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.HeightMap;

public final class WorldTerrainGenerator {
	
	public WorldTerrainGenerator(GameConRandom spec){
		create(spec);
	}
	
	private void create(GameConRandom spec){
		
		
		
		RES.loader().print("elevating.");
		HeightMap height = new HeightMap(TWIDTH(), THEIGHT(), TWIDTH()/8, 4);
		
		for (COORDINATE c : TBOUNDS()) {
			height.increment(c, spec.map().h(c.x(), c.y(), TWIDTH(), THEIGHT()));
			
		}
		
		
		RES.loader().print("elevating");
		new GeneratorMountains(spec, height);
		RES.loader().print("filling lake");
		new GeneratorLake(spec, height);
		RES.loader().print("making oceans");
		new GeneratorOcean(spec, height);
		
//		new GeneratorElevator(spec, height);
//		RES.loader().print("elevating...");
//		new GeneratorOcean(spec, height);
//		
		RES.loader().print("creating some rivers...");
		new GeneratorRiver();
		
		RES.loader().print("fixing...");
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				WATER().get(x, y).pplace(x, y);
				WATER().get(x, y-1).pplace(x, y-1);
				WATER().get(x-1, y).pplace(x-1, y);
				MOUNTAIN().fix(x, y);
			}
		}
		
		float[][] fertility = new float[THEIGHT()][TWIDTH()]; 
		RES.loader().print("fertilizing...");
		new GeneratorSeasoner(GAME.world(), height, spec.lat.getValue(), fertility);
		RES.loader().print("foresting...");
		new GeneratorForest(fertility);

		
//		RES.loader().print("mountainfix...");
//		new _GeneratorMountainElevator(m);
//		
//		new _GeneratorDetailer(m);
		
		
	}	
	
	
}
