package settlement.army.ai.general;

import init.C;
import init.RES;
import settlement.army.Army;
import settlement.army.Div;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class DivFinder {

	private final Context c;
	private final AbsMap map = new AbsMap(1);
	private IntChecker dCheck = new IntChecker(RES.config().BATTLE.DIVISIONS_PER_ARMY);
	
	public DivFinder(Context c){
		this.c = c;
	}
	
	public int init(LIST<GDiv> divs) {
		
		map.clear();
		dCheck.init();
		int am = 0;
		
		for (GDiv d : divs) {
			if (!d.active) {
				continue;
			}

			int tx = d.tx;
			int ty = d.ty;
			if (SETT.IN_BOUNDS(tx, ty)) {
				am++;
				map.set(AbsMap.getI(tx, ty), 1);
				dCheck.isSetAndSet(d.index());
			}
		}
		return am;
	}
	
	public GDiv get(PathTile t) {
		
		if (map.get(t.x(), t.y()) == 0)
			return null;
		
		ArrayList<Div> res = c.tmpList;
		res.clearSloppy();
		int cx = (t.x()*AbsMap.size + AbsMap.size/2)*C.TILE_SIZE;
		int cy = (t.y()*AbsMap.size + AbsMap.size/2)*C.TILE_SIZE;
		{
			
			int dist = AbsMap.size*2*C.TILE_SIZE;
			ArmyAIUtil.quads().getNearest(res, cx, cy, dist, c.army, null);
		}
		
		if (t.getParent() != null) {
			cx = (t.getParent().x()*AbsMap.size + AbsMap.size/2)*C.TILE_SIZE;
			cy = (t.getParent().y()*AbsMap.size + AbsMap.size/2)*C.TILE_SIZE;
		}
		
		GDiv best = null;
		double bestDist = Double.MAX_VALUE;
		int am = 0;
		
		for (Div d : res) {
			GDiv gd = c.divs.get(d.indexArmy());
			if (dCheck.isSet(gd.index())) {
				int tx = gd.tx;
				int ty = gd.ty;
				int ax = tx>>AbsMap.scroll;
				int ay = ty>>AbsMap.scroll;
				if (t.isSameAs(ax, ay)) {
					double dist = COORDINATE.tileDistance(cx, cy, tx, ty);
					am++;
					if (dist < bestDist) {
						bestDist = dist;
						best = gd;
					}
				}
			}
			
		}
		if (best == null || am == 1) {
			map.set(t.x(), t.y(), 0);
		}
		if (best != null)
			dCheck.isSetAndSet(best.index());
		
		return best;
	}
	
	public Div get(Army a, int tx, int ty, int cx, int cy) {
		
		ArrayList<Div> res = c.tmpList;
		res.clearSloppy();
		{
			int dist = AbsMap.size*8*C.TILE_SIZE;
			ArmyAIUtil.quads().getNearest(res, tx*C.TILE_SIZE, ty*C.TILE_SIZE, dist, a, null);
		}
		
		Div best = null;
		double bestDist = Double.MAX_VALUE;
		
		for (Div d : res) {
			d.order().status.get(c.status);
			double dist = COORDINATE.tileDistance(cx, cy, c.status.currentPixelCX()>>C.T_SCROLL, c.status.currentPixelCY()>>C.T_SCROLL);
			if (dist < bestDist) {
				bestDist = dist;
				best = d;
			}
		}
		return best;
	}
	
}
