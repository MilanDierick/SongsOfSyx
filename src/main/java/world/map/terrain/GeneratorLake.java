package world.map.terrain;

import static world.World.*;

import game.GameConRandom;
import snake2d.util.rnd.HeightMap;

class GeneratorLake{

	
	GeneratorLake(GameConRandom spec, HeightMap height){
		
		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				double v = height.get(x, y);
				if (v < 0.25 && !MOUNTAIN().coversTile(x, y)) {
					WATER().LAKE.placeRaw(x, y);
				}else {
					WATER().NOTHING.placeRaw(x, y);
				}
			}
		}
		
	}
	

}