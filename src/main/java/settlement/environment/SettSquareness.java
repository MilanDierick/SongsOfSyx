package settlement.environment;

import init.D;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import util.info.INFO;

public final class SettSquareness {

	public final INFO info;
	
	SettSquareness(){
		D.t(this);
		info = new INFO(D.g("Shape"), D.g("desc", "Depending on species, subjects prefer different shapes to your rooms. Some like it square, others more organic."));
	}
	
	public double getPercent(AREA area) {
		return get(getp(area), 100);
	}
	
	private double get(double v, double t) {
		if (t == 0)
			return 0.5;
		return v/t;
		
		
	}
	
	private int getp(AREA area) {
		
		int straight = 0;
		int turns = 0;
		
		for (COORDINATE c : area.body()) {
			if (isEdge(area, c)) {
				if (isStraight(area, c))
					straight++;
				else
					turns ++;
				
			}
		}
		
		int v = 100;
		
		if (turns > 4) {
			turns-= 4;
			double d = 7.0*turns/straight;
			d = CLAMP.d(d, 0, 1);
			v -= 100*d;
		}
		return v;
		
	}
	
	private boolean isStraight(AREA area, COORDINATE c) {
		for (DIR d : DIR.ORTHO) {
			if (area.is(c, d) && area.is(c, d.perpendicular())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isEdge(AREA area, COORDINATE c) {
		if (!area.is(c)) {
			return false;
		}
		for (DIR d : DIR.ORTHO) {
			if (!area.is(c, d))
				return true;
		}
		return false;
	}

	
}
