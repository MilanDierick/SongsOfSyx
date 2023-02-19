package world.map.regions;

import static world.World.*;

import init.D;
import init.RES;
import snake2d.CORE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.text.Str;
import world.World;

class Generator {

	
	
	private final boolean[] inited = new boolean[Regions.MAX];
	private final GeneratorInit init = new GeneratorInit();
	private final GeneratorAssigner assigner = new GeneratorAssigner();
	private static CharSequence ¤¤generating = "¤Generating Regions";
	static {
		D.ts(Generator.class);
	}
	

	public Generator() {
		
		clear();
		generate(1);
		finish();

	}
	
	void clear() {
		for (Region r : REGIONS().all())
			r.clear();
		
		for (COORDINATE c : TBOUNDS()) {
			REGIONS().setter.set(c, null);
		}
	}
	
	void generate(int nr) {
		RES.loader().print(Str.TMP.clear().add(¤¤generating));
		
		assigner.generate();
		
		
		CORE.checkIn();
		for (COORDINATE c : TBOUNDS()) {
			Region r = REGIONS().setter.get(c);
			
			if (r == null)
				continue;
			if (inited[r.index()])
				continue;
			if (r.isWater())
				init.initWater(c);
			else
				init.init(c);	
			inited[r.index()] = true;
			
			
			
		}
		
		
	}
	
	void finish() {
		new GeneratorRoad();
		new GeneratorDistances();
	}

	

	
	static boolean test(int tx, int ty, Region r) {
		if (!World.REGIONS().setter.is(tx,ty,r))
			return false;
		if (CapitolPlacablity.b(tx, ty))
			return false;
		
		for (int dy = -1; dy < 3; dy++) {
			for (int dx = -1; dx < 3; dx++) {
				int x = tx+dx;
				int y = ty+dy;
				if (x < 1 || x >= World.TWIDTH()-1 || y < 1 || y >= World.THEIGHT()-1)
					return false;
				if (!World.REGIONS().setter.is(tx+dx,ty+dy,r))
					return false;
			}
		}
		return true;
		
	}



	
}
