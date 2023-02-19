package settlement.room.main.placement;

import game.GAME;
import init.RES;
import settlement.main.SETT;
import settlement.misc.util.TileRayTracer;
import settlement.misc.util.TileRayTracer.Ray;
import settlement.room.main.ROOMA;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.construction.ConstructionData;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;

public final class UtilExtraCost {

	private double value = 0;
	private int tick = -1;
	
	private static final TileRayTracer tracer = new TileRayTracer(4);
	
	UtilExtraCost(){
		
		
	}
	
	public double get(ROOMA a) {
		if (GAME.updateI() == tick)
			return value;
		
		tick = GAME.updateI();
		
		value = pget(a);
		return value;
	}
	
	public double get(ROOMA a, int tx, int ty) {
		get(a);
		return CLAMP.d(RES.marker().v1.get(tx, ty)-1, 0, 4)/4.0;
	}
	
	public boolean is(int tx, int ty) {
		return ConstructionData.dExpensive.is(tx, ty, 1);
	}
	
	public static double expense(ROOMA a, RoomBlueprintImp blueprint) {
		if (blueprint.constructor().mustBeIndoors() && blueprint.constructor().usesArea()) {
			return pget(a)*2;
		}
		return 0;
	}
	
	private static double pget(ROOMA a) {
		
		RES.marker().init(UtilExtraCost.class);
		
		for (COORDINATE c : a.body()) {
			if (a.is(c)) {
				double v = support(a, c.x(), c.y());
				RES.marker().v1.set(c, v);
				RES.marker().v2.set(c, v);
			}
			
		}
		
		double total = 0;
		double exp = 0;
		double value = 0;
		
		for (COORDINATE c : a.body()) {
			if (a.is(c)) {
				double v = RES.marker().v1.get(c);
				total++;
				if (v >= 1) {
					ConstructionData.dExpensive.set(a, c, 0);
				}else {
					ConstructionData.dExpensive.set(a, c, 1);
					exp++;
				}
			}
			
		}
		
		RES.marker().done();
		
		
		

		
		if (total == 0)
			value = 0;
		else
			value = exp/total;
		value *=4;
		value = CLAMP.d(value, 0, 1);
		return value;
		
	}
	
	private static double support(AREA a, int tx, int ty) {
		
		double s = 0;
		
		tracer.checkInit();
		
		for (Ray r : tracer.rays()) {
			for (int i = 0; i < r.size(); i++) {
				int dx = tx+r.get(i).x();
				int dy = ty+r.get(i).y();
				if (!SETT.IN_BOUNDS(dx, dy))
					break;
				if (!a.is(dx, dy)) {
					if (!SETT.ROOMS().map.is(dx,dy) && tracer.check(r.get(i))) {
						s += CLAMP.d((double)(3.5-i)/3.5, 0, 1);
					}
					break;
				}
			}
		}
		return s;
		
//		double s = 0;
//		
//		for (DIR d : dirs) {
//			s += support(d, a, tx, ty);
//		}
//		
//		return s;
		
	}
	



	
}
