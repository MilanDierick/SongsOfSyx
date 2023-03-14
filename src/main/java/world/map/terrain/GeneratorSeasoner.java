package world.map.terrain;

import static world.World.*;

import init.RES;
import init.biomes.CLIMATES;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import world.World;

class GeneratorSeasoner {
	
	private double heightMul = 0.5;
	private double climateMul = 0.2;
	
	GeneratorSeasoner(World m, HeightMap height, double value, float[][] climate){

		double equator = THEIGHT()*value;
		double dSouth = THEIGHT()-equator;
		
		for (COORDINATE c : TBOUNDS()) {
			double d = 0;
			if (c.y() <= equator) {
				d = c.y()/equator;
				d = d*(1.0-heightMul) + heightMul*(1.0-height.get(c));
				
				double cl = c.y()/equator;
				cl = cl*(1.0-climateMul) + climateMul*(1.0-height.get(c));
				
				cl*= RND.rFloat1(0.3);
				
				if (cl < 0.3) {
					CLIMATE().setter.set(c, CLIMATES.COLD());
				}else {
					CLIMATE().setter.set(c, CLIMATES.TEMP());
				}
				
			}else {
				d = 1.0 - (c.y()-equator)/dSouth;
				d = d*(1.0-heightMul) + heightMul*(1.0-height.get(c));
				
				double cl = 1.0 - (c.y()-equator)/dSouth;
				cl = cl*(1.0-climateMul) + climateMul*(1.0-height.get(c));
				cl*= RND.rFloat1(0.3);
				if (cl < 0.3) {
					CLIMATE().setter.set(c, CLIMATES.HOT());
				}else {
					CLIMATE().setter.set(c, CLIMATES.TEMP());
				}
			}
			climate[c.y()][c.x()] = (float) d + RND.rFloat0(0.05);
			BUILDINGS().roads.clear(c);
		}
		
		RES.flooder().init(this);
		
		double waterBonus = 1.0;
		double waterSpread = 8.0;
		
		for (COORDINATE c : TBOUNDS()) {
			if (WATER().has.is(c.x(), c.y())) {
				if (WATER().fertile.is(c.x(), c.y())) {
					RES.flooder().pushSloppy(c, waterBonus*waterSpread);
				}else {
					RES.flooder().pushSloppy(c, waterBonus*waterSpread*0.85);
				}
			}
		}
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollGreatest();
			double bonus = t.getValue()/waterSpread;
			climate[t.y()][t.x()] += 0.7*heightMul*bonus*bonus;
//			
//			climate[t.y()][t.x()] *= 1.0+bonus;
			for (DIR d : DIR .ALL) {
				double v = t.getValue()-d.tileDistance();
				if (v > 0 && IN_BOUNDS(t.x(), t.y(), d)) {
					RES.flooder().pushGreater(t, d, v);
				}
				
			}
		}
		
		RES.flooder().done();
		
		double mValue = 0.6;
		for (int y = 0; y < THEIGHT(); y++) {
			double mountain = 0;
			for (int x = 0; x< TWIDTH(); x++) {
				if (MOUNTAIN().is(x, y)) {
					mountain = CLAMP.d(mountain+2, 0, 10);
				}
				if(mountain > 0) {
					
					double d = Math.pow(mountain/10, 1.5);
					climate[y][x] -= mValue*d;
					mountain = CLAMP.d(mountain-1, 0, 10);
					
				}
			}
			mountain = 0;
			for (int x = TWIDTH()-1; x>=0; x--) {
				if (MOUNTAIN().is(x, y)) {
					mountain = CLAMP.d(mountain+2, 0, 10);
				}
				if(mountain > 0) {
					
					double d = Math.pow(mountain/10, 1.5);
					climate[y][x] += mValue*d;
					mountain = CLAMP.d(mountain-1, 0, 10);
					
				}
			}
		}
		
		double highest = 0;
		
		{
			
			for (COORDINATE c : TBOUNDS()) {
				double d = climate[c.y()][c.x()];
				if (d > highest)
					highest = d;
			}
		}
		
		
		for (COORDINATE c : TBOUNDS()) {
			double d = climate[c.y()][c.x()]/highest;
			FERTILITY().setter.set(c, d);
			

			d -= 0.20;
			d /= 0.8;
			d *= 2.5;
			
			int i = (int) Math.round((GROUND().all().size()-1)*(1.0-d));
			i = CLAMP.i(i, 0, GROUND().all().size()-1);
			GROUND().all().get(i).placeRaw(c.x(), c.y());
			
		}
		
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				GROUND().getter.get(x, y).place(x, y, null, null);
			}
		}
		
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				GROUND().getter.get(x, y).place(x, y, null, null);
			}
		}
		
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				GROUND().getter.get(x, y).place(x, y, null, null);
			}
		}
		
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				GROUND().getter.get(x, y).place(x, y, null, null);
			}
		}
		
	}
	
}
