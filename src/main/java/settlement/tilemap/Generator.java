package settlement.tilemap;

import static settlement.main.SETT.*;

import init.*;
import init.settings.S;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.path.AvailabilityListener;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.LinkedList;

public final class Generator {

	private static CharSequence ¤¤Generating = "Generating";
	static {
		D.ts(Generator.class);
	}
	
	
	Generator(CapitolArea area) {

		AvailabilityListener.listenAll(false);
		
		RES.loader().print("generating map...");

		TileMap m = SETT.TILE_MAP(); 
		m.topology.clearAll();
		
		GeneratorUtil util = new GeneratorUtil();
		
		print("fertilizing");
		new GeneratorFertilityInit(area, util);
		
		print("mountainizing");
		new GeneratorMountain(area, util);
		
		print("making caves");
		final LinkedList<COORDINATE> caves = new LinkedList<>();
		new GeneratorCave(area, util, caves);
		
		print("Generating Rivers");
		new GeneratorRiver(area, util);
		
		print("filling lakes");
		new GeneratorLake(area, util);
		new GeneratorWaterFin(area, util);
		
		print("filling oceans");
		new GeneratorOcean(area, util);
		
		print("mineralizing");
		new GeneratorMinerals(area, util, caves);
		

		
		
		
		print("foresting");
		new GeneratorForest(area, util);

		RES.loader().print("fertilizing again...");
		new GeneratorFertilityFin(area, util);
		
		
		print("planting seeds");
		new GeneratorEdibles(area, util, caves);
		m.growth.generate(util);
		
		
		
		
		
		print("polishing..");
		
		
		for (int y = 0; y < THEIGHT; y++) {
			for (int x = 0; x < TWIDTH; x++) {
				m.topology.get(x, y).placeFixed(x, y);
				
				SETT.PATH().availability.updateAvailability(x, y);
			}
		}
		
		
	
		
		
		
		print("painting minimap...");
		
		paintMinimap();

		
		
		AvailabilityListener.listenAll(true);
		
	}
	
	private int printI = 0;
	
	private void print(String debug) {
		String s = ""+¤¤Generating;
		for (int i = 0; i < printI; i++)
			s += ".";
		if (S.get().developer || S.get().debug) {
			s += " " + debug;
		}
		printI++;
		printI%= 6;
		RES.loader().print(s);
	}
	
	static void paintMinimap() {
		byte[] cs = new byte[C.SETTLE_TSIZE * C.SETTLE_TSIZE * 4];
		for (int y = 0; y < C.SETTLE_TSIZE; y++) {
			for (int x = 0; x < C.SETTLE_TSIZE; x++) {

				int i = (y * C.SETTLE_TSIZE + x) * 4;

				COLOR c = TILE_MAP().minimap.get(x, y);
				
				
				cs[i + 0] = c.red();
				cs[i + 1] = c.green();
				cs[i + 2] = c.blue();
				cs[i + 3] = (byte) 255;
			}
		}
		SETT.MINIMAP().putPixels(cs);
	}
	


}
