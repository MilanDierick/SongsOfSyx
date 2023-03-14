package world.map.buildings;

import static world.World.*;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import world.World;

public final class WorldGeneratorBuildings {

	public WorldGeneratorBuildings() {
		
		
	}
	

	public void clear() {
		World.BUILDINGS().clear();
	}
	
	public void generate() {

		final HeightMap map = new HeightMap(TWIDTH(), THEIGHT(), 16, 4);
		
		final double max = 4 + 2.8;

		for (COORDINATE c : TBOUNDS()) {
			
			BUILDINGS().map.set(c, null);
			
			if (World.REGIONS().isCentre.is(c))
				continue;
			
			if (WATER().coversTile.is(c))
				continue;

			double connected = 0;
			double freshWater = 0;

			for (DIR d : DIR.ALL) {
				if (BUILDINGS().roads.is(c, d))
					connected += 1.0 / d.tileDistance();
				if (WATER().fertile.is(c, d))
					freshWater += 1.0 / d.tileDistance();
			}

			connected /= max;
			connected = Math.pow(connected, 0.5);

			freshWater /= max;
			freshWater = Math.pow(freshWater, 0.5);

			if (mine(c, connected)) {
				BUILDINGS().mine.placer.place(c.x(), c.y(), null, null);
			}
			
			else if(farm(c, connected, freshWater)) {
				BUILDINGS().farm.placer.place(c.x(), c.y(), null, null);
			}
			
			else if (village(c, connected, map)) {
				BUILDINGS().village.placer.place(c.x(), c.y(), null, null);
			}
		}
		
	}

	public boolean village(COORDINATE c, double connectivity,HeightMap map) {
		if (MOUNTAIN().coversTile(c.x(), c.y()))
			return false;

		double mul = 0;
		
//		for (DIR d : DIR.ALL) {
//			if (BUILDINGS().village.placer.is(c, d))
//				return false;
//			if (BUILDINGS().farm.placer.is(c, d))
//				mul += 0.2;
//		}
		
		double chance = 0.1 + 0.9*connectivity;
		chance *= 0.25 + 2.5*(GROUND().getter.get(c).fertility()-0.25)/0.75;
		chance *= map.get(c);
		
		chance+= mul;
		
		return RND.rFloat() < chance;
	}

	public boolean farm(COORDINATE c, double connectivity, double freshWater) {
		if (MOUNTAIN().coversTile(c.x(), c.y()))
			return false;

		
		double ch = 1.5*(GROUND().getter.get(c).fertility()-0.25)/0.75;
		ch *= 0.1 + 0.9*(1.0-CLIMATE().getter.get(c).seasonChange);
		ch += freshWater*0.5 + freshWater*RND.rFloat();
		
		if (Math.pow(RND.rFloat(), 2) < ch)
			return true;
//		

		
//		double chance = FERTILITY().map.get(c);
//		if (Math.pow(RND.rFloat(), 1.5) < chance)
//			return true;
		return false;

	}

	public boolean mine(COORDINATE c, double connectivity) {

		if (FOREST().is.is(c))
			return false;

		if (WATER().has.is(c))
			return false;

		if (BUILDINGS().roads.is(c))
			return false;

		if (MOUNTAIN().haser.is(c))
			return RND.oneIn(5-connectivity*3);

		return RND.oneIn(50);
	}
	
}
