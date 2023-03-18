package world.map.terrain;

import static world.World.*;

import snake2d.util.rnd.HeightMap;

class GeneratorLake{

	
	GeneratorLake(HeightMap height){
		
		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				double v = height.get(x, y);
				if (v < 0.25 && !MOUNTAIN().coversTile(x, y)) {
					if (v < 0.1)
						WATER().LAKE.deep.placeRaw(x, y);
					else
						WATER().LAKE.normal.placeRaw(x, y);
				}else {
					WATER().NOTHING.placeRaw(x, y);
				}
			}
		}
		
	}
	

}