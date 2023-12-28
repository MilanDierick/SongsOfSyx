package world.map.pathing;

import static world.WORLD.*;

import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap2D;
import world.WORLD;
import world.regions.Region;

final class WGenUtil {

	public final Polymap polly = new Polymap(TBOUNDS(), 6, 1);
	public final Bitmap2D tmpRoute = new Bitmap2D(WORLD.TBOUNDS(), false);
	
	public final Bitsmap2D wmap;
	public final int wmapMax;
	public final ACTION astep;
	
	public WGenUtil(ACTION astep) {
		
		this.astep = astep;
		
		int wi = 0;
		{
			for (COORDINATE c : WORLD.TBOUNDS()) {
				RES.flooder().setValue2(c, 0);
			}
			
			wi = 1;
			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				if (WATER().isBig.is(c) && RES.flooder().getValue2(c.x(), c.y()) == 0)
					if (assignWater(wi, c.x(), c.y())) {
						wi++;
					}
			}
			wi--;
			
			wmap = new Bitsmap2D(0, 32-Integer.numberOfTrailingZeros(wi), WORLD.TBOUNDS());
			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				wmap.set(c, (int)CLAMP.d(RES.flooder().getValue2(c.x(), c.y()) -1, 0, wi));
			}
		}
		wmapMax = wi;
		
	}
	
	private boolean assignWater(int i, int x, int y) {
		Rec rec = new Rec();
		rec.setDim(1).moveX1Y1(x, y);
		Region reg = null;
		boolean hasTwoRegs = false;

		RES.flooder().init(this);
		RES.flooder().pushSloppy(x, y, 0);
		
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			t.setValue2(i);
			
			if (!hasTwoRegs) {
				Region r = WORLD.REGIONS().map.get(t);
				if (r != null) {
					if (reg == null)
						reg = r;
					else if (r != reg) {
						hasTwoRegs = true;
					}
				}	
			}
			

			rec.unify(t.x(), t.y());
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (WORLD.WATER().isBig.is(dx, dy) && WTRAV.can(t.x(), t.y(), d, false)) {
					RES.flooder().pushSmaller(dx, dy, t.getValue() + d.tileDistance(), t);
				}
			}
		}
		RES.flooder().done();
		return true;
	}

	
	public double cost(int dx, int dy) {
		double v = (0.4 + (polly.isEdge(dx, dy) ? 0 : 0.6))*WPATHING.getTerrainCost(dx, dy);
		Region r = WORLD.REGIONS().map.get(dx, dy);
		for (int i = 0; i < DIR .ORTHO.size(); i++)
			if (r != WORLD.REGIONS().map.get(dx, dy, DIR.ORTHO.get(i))) {
				v *= 2;
				break;
			}
		if (WORLD.WATER().isBig.is(dx, dy)) {
			v*= 2;
			if (!WORLD.WATER().coversTile.is(dx, dy))
				v*= 3;
			if (!tmpRoute.is(dx, dy))
				v*= 3;
		}
		
		return v;
	}
	

	
	public  void connectWay(PathTile t) {
		while(t != null) {
			tmpRoute.set(t, true);
			t = t.getParent();
		}
	}

	
}
