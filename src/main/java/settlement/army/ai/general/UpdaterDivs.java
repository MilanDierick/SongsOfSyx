package settlement.army.ai.general;


import init.RES;
import settlement.army.formation.DivPosition;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.ArrayList;

final class UpdaterDivs {
	
	private final Context c;
	
	UpdaterDivs(Context c){
		this.c = c;
		udiv = new UpdaterDiv(c);
	}

	private IntChecker dCheck = new IntChecker(RES.config().BATTLE.DIVISIONS_PER_ARMY);
	private final UpdaterDiv udiv;
	private final ArrayCooShort op = new ArrayCooShort(AbsMap.bounds.width()*AbsMap.bounds.height()/2); 
	private final ArrayList<GDiv> active = new ArrayList<>(RES.config().BATTLE.DIVISIONS_PER_ARMY);
	private final DivPosition pos = new DivPosition();
	
	public boolean update() {

		active.clearSloppy();
		for (GDiv d : c.divs) {
			if (!d.updated) {
				
				d.init();
				
				if (d.active)
					active.add(d);
				else if (d.hasPosition()) {
					d.updated = true;
					d.div().order().current.get(pos);
					c.pmap.markPos(pos);
				}
					
			}
		}
		
		
		

		int am = c.divFinder.init(active);
		
		
		
		
		if (am == 0) {
			return false;
		}
		
		dCheck.init();
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		pushTargets();
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			{
				GDiv d = c.divFinder.get(t);
				if (d != null) {
					
					f.done();
					t = getBestPath(d, t);
					udiv.update(d, t);
					d.updated = true;
					return true;
				}
			}
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				double v = 0;
				if (t.getParent() != null) {
					if (d != DIR.get(t.getParent(), t)) {
						v += 4;
					}
				}
				int dx = t.x() + DIR.ORTHO.get(di).x();
				int dy = t.y() + DIR.ORTHO.get(di).y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					double co = v + t.getValue() + c.pmap.cost.abs.get(dx, dy);
					f.pushSmaller(dx, dy, co, t);
				}
			}
		}
		
		f.done();
		return false;
		
	}
	
	private void pushTargets() {
		Flooder f = c.flooder.getFlooder();
		boolean any = false;
//		for (BattleLine l : c.battleLines.all()){
//			any |= l.pushTargetsAbs();
//		}
		
		if (!any){
			int sx = (c.getDestCoo().x()>>AbsMap.scroll);
			int sy = (c.getDestCoo().y()>>AbsMap.scroll);
			f.pushSloppy(sx, sy, 0);
		}
	}
	
	private PathTile getBestPath(GDiv gd, PathTile t) {
		int sx = t.x();
		int sy = t.y();
		
		double dist = t.getValue();
		
		storeOldPath(t);
		
		t = c.pathing.getAbsPath(sx<<AbsMap.scroll, sy<<AbsMap.scroll, c.pmap.CostNoPath.abs(), c.pathing.normal.abs());
		
		if (t.getValue() < dist/2) {
			return t;
		}
		
		return popOldPath(t);
		
	}
	
	private void storeOldPath(PathTile t) {
		int i = 0;
		while (t != null) {
			op.set(i++).set(t);
			t = t.getParent();
		}
	}
	
	private PathTile popOldPath(PathTile t) {
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		int m = op.getI();
		op.set(0);
		PathTile p = f.close(op.get().x(), op.get().y(), 0, null);
		for (int i = 1; i <= m; i++) {
			op.set(i);
			p = f.close(op.get().x(), op.get().y(), 0, p);
		}
		f.done();
		return p;
	}

	
	
	

	
}
