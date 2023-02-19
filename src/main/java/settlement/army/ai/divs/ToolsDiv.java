package settlement.army.ai.divs;

import static settlement.army.ai.divs.Plans.Plan.*;
import static settlement.main.SETT.*;

import settlement.army.Army;
import settlement.army.formation.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.misc.CLAMP;

final class ToolsDiv {

	private final Tools t;
	
	ToolsDiv(Tools tools){
		this.t = tools;
	}
	
	public boolean needsFixing(DivFormation dest, int men, Army a, DIV_FORMATION f) {
		if (f != dest.formation() || dest.deployed() > men || (men > dest.deployed() && dest.hasExtraRoom()) || !t.deployer.isValid(dest, a)) {
			return true;
		}
		return false;
	}
	
	public int inPosition(DivPosition current, DivFormation dest, int dist) {
		int am = 0;
		int max = CLAMP.i(current.deployed(), 0, dest.deployed());
		for (int i = 0; i < max; i++) {
			if (dest.pixel(i).tileDistanceTo(current.pixel(i)) < dist)
				am++;
		}
		return am;
	}
	
	public int distanceAverageFromCurrentToNext(DivPositionAbs current, DivPositionAbs next) {
		
		int dist = 0;
		int am = CLAMP.i(current.deployed(), 0, next.deployed());
		
		for (int i = 0; i < am; i++) {
			dist += next.pixel(i).tileDistanceTo(current.pixel(i));
		}
		return dist/am;
		
	}
	
	private final Coo coo = new Coo();
	public COORDINATE getSafeCentrePixel(DivFormation dest) {
		if (dest.deployed() == 0)
			return dest.centrePixel();
		int x = 0, y = 0;
		for (int i = 0; i < dest.deployed(); i++) {
			x += dest.pixel(i).x();
			y += dest.pixel(i).y();
		}
		x /= dest.deployed();
		y /= dest.deployed();
		if (DivPlacability.pixelIsBlocked(x, y, dest.formation().size, a)) {
			return dest.centrePixel();
		}
		coo.set(x, y);
		return coo;
	}
	
	public COORDINATE getSafeCentreTile(DivPositionAbs dest) {
		if (dest.deployed() == 0)
			return coo;
		int x = 0, y = 0;
		for (int i = 0; i < dest.deployed(); i++) {
			x += dest.tile(i).x();
			y += dest.tile(i).y();
		}
		x /= dest.deployed();
		y /= dest.deployed();
		
		int bestI = 0;
		double bestValue = Double.MAX_VALUE;
		
		for (int i = 0; i < dest.deployed(); i++) {
			double dist = dest.tile(i).tileDistanceTo(x, y);
			if (DivPlacability.tileIsOK(x, y, a) && dist < bestValue) {
				dist = bestValue;
				bestI = i;
			}
		}
		
		coo.set(dest.tile(bestI));
		return coo;
	}
	
	public int distanceMaxFromCurrentToNext(DivPositionAbs current, DivPositionAbs next) {
		
		double dist = 0;
		int am = CLAMP.i(current.deployed(), 0, next.deployed());
		
		for (int i = 0; i < am; i++) {
			dist  = Math.max(dist, next.pixel(i).tileDistanceTo(current.pixel(i)));
		}
		return (int) dist;
		
	}
	
	public int distanceTO(int x, int y, DivPosition next) {
		
		int dist = 0;
		int am = next.deployed();
		if (am == 0)
			return 0;
		
		for (int i = 0; i < next.deployed(); i++) {
			dist += next.pixel(i).tileDistanceTo(x, y);
		}
		return dist/am;
		
	}
	
	public boolean intersectsSomewhat(DivFormation a, DivFormation b) {
		t.pather.filler.init(this);
		int s = 0;
		for (int i = 0; i < a.deployed(); i++) {
			COORDINATE c  = a.tile(i);
			if (IN_BOUNDS(c)) {
				t.pather.filler.fill(c.x(), c.y());
				s++;
			}
		}
		
		int k = 0;
		for (int i = 0; i < b.deployed(); i++) {
			COORDINATE c  = b.tile(i);
			if (IN_BOUNDS(c)) {
				if (t.pather.filler.isFilled(c.x(), c.y())) {
					k++;
				}
			}

		}

		t.pather.filler.done();
		
		return k > s/2;
	}
	
}
