package world.map.terrain;

import static world.World.*;

import game.GAME;
import init.RES;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import util.dic.DicMisc;
import world.World;
import world.WorldGen;
import world.WorldGen.WorldGenMapType;

public final class WorldGeneratorTerrain {
	
	
	public WorldGeneratorTerrain(){
		
	}

	
	public void clear() {
		World.GROUND().clear();
		World.MOUNTAIN().clear();
		World.FOREST().clear();
		World.WATER().clear();
	}
	
	public void generateAll(WorldGen spec) {
		
		RES.loader().minify(true, DicMisc.¤¤Generating);
		RES.loader().print(DicMisc.¤¤Generating);
		
		RND.setSeed(spec.seed);
		RES.loader().minify(true, DicMisc.¤¤Generating);
		
		RES.loader().print(DicMisc.¤¤Generating);
		
		HeightMap height = new HeightMap(TWIDTH(), THEIGHT(), TWIDTH()/8, 4);
		
		if (spec.map != null) {
			WorldGenMapType m = new WorldGenMapType(spec.map); 
			
			for (COORDINATE c : TBOUNDS()) {
				height.increment(c, m.h(c.x(), c.y(), TWIDTH(), THEIGHT()));
			}
		}
		
		
		
		
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorMountains(height);
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorLake(height);
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorOcean(height);
		
//		new GeneratorElevator(spec, height);
//		RES.loader().print("elevating...");
//		new GeneratorOcean(spec, height);
//		
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorRiver();
		
		RES.loader().print(DicMisc.¤¤Generating);
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				WATER().get(x, y).pplace(x, y);
				WATER().get(x, y-1).pplace(x, y-1);
				WATER().get(x-1, y).pplace(x-1, y);
				MOUNTAIN().fix(x, y);
			}
		}
		
		float[][] fertility = new float[THEIGHT()][TWIDTH()]; 
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorSeasoner(GAME.world(), height, spec.lat, fertility);
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorForest(fertility);
		RES.loader().print(DicMisc.¤¤Generating);
		new GeneratorMinables(height);
		RES.loader().print(DicMisc.¤¤Generating);
		MINIMAP().repaint();
		RES.loader().print(DicMisc.¤¤Generating);
		
	}
	
}
