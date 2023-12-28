package settlement.army.ai.general;

import init.C;
import init.config.Config;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.army.ai.general.UtilLines.Line;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.*;

/**
 * uses the prelines, and sets them to divs. 
 * @author Jake
 *
 */
class StepMoveToLine {
	
	private final Context context;
	private final int maxRange = 1000;
	private final ArrayList<PathTile> blockedpaths = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY*2);
	
	public StepMoveToLine(Context context) {
		this.context = context;
	}
	
	public boolean setDivsToLine(MDivs freeLines, UtilLines plines, ArrayList<MDiv> toFill, Bitmap2D toBlock) {

		Flooder f = context.flooder.getFlooder();
		
		f.init(this);
		for (int i = 0; i < plines.lines(); i++) {
			Line l = plines.line(i);
			if (l.menMax > 0 && l.active) {
				
				int cx = (int) (l.tx1+l.dir.next(2).xN()*l.tileLength/2);
				int cy = (int) (l.ty1+l.dir.next(2).yN()*l.tileLength/2);
				
				
				cx -= l.dir.x()*l.back;
				cy -= l.dir.y()*l.back;
				int pe = l.back*3;
				
				if (SETT.IN_BOUNDS(cx, cy)) {
					f.pushSloppy(cx, cy, pe);
					f.setValue2(cx, cy, i);
				}
			}
		}
		
		fill(freeLines, plines, toFill, false, toBlock);
		return toFill.size() > 0;
	}
	
	public boolean setDivsToLineRanged(MDivs freeLines, UtilLines plines, ArrayList<MDiv> toFill, Bitmap2D toBlock) {

		for (int i = 0; i < plines.lines(); i++) {
			Line l = plines.line(i);
			l.back = Math.max(12, l.back);
		}
		
		Flooder f = context.flooder.getFlooder();
		
		f.init(this);
		for (int i = 0; i < plines.lines(); i++) {
			Line l = plines.line(i);
			if (l.menMax > 0 && l.active) {
				
				int cx = (int) (l.tx1+l.dir.next(2).xN()*l.tileLength/2);
				int cy = (int) (l.ty1+l.dir.next(2).yN()*l.tileLength/2);
				
				cx -= l.dir.x()*l.back;
				cy -= l.dir.y()*l.back;
				int pe = (l.back-12)*3;
				
				if (SETT.IN_BOUNDS(cx, cy)) {
					f.pushSloppy(cx, cy, pe);
					f.setValue2(cx, cy, i);
				}
			}
		}
		
		fill(freeLines, plines, toFill, true, toBlock);
		
		return toFill.size() > 0;
	}
	
	private void fill(MDivs freeLines, UtilLines plines, ArrayList<MDiv> toFill, boolean ranged, Bitmap2D toBlock) {

		blockedpaths.clearSloppy();

		for (int i = 0; i < plines.lines(); i++) {
			Line l = plines.line(i);
			l.mark = 0;
		}
		
		context.finder.clear();
		
		for (MDiv d : freeLines.activeDivs) {
			if (!d.isDeployed) {
				context.finder.add(d);
			}
			
		}
		
		Flooder f = context.flooder.getFlooder();
		
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (t.getValue() > maxRange) {
				break;
			}
				
			
			LIST<MDiv> ddd = context.finder.get(t.x(), t.y());
			if (ddd != null) {
				for (MDiv m : ddd) {
					if (!m.isDeployed && m.ranged == ranged) {
						
						Line l = plines.line((int) t.getValue2());
						
						if (l.mark == 1)
							break;
						l.mark = 1;
						m.lineBack = l.back;
						m.lineI = (int) t.getValue2();
						toFill.add(m);
						init(m, t, toBlock);
						
					}
				}
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(context, dx, dy);
					if (cost > 0) {
						if (f.pushSmaller(dx, dy, t.getValue()+dir.tileDistance()*cost, t) != null) {
							f.setValue2(dx, dy, t.getValue2());
						}
					}
				}
				
			}
		}
		
		while(blockedpaths.size() > 0) {
			PathTile dest = blockedpaths.removeLast();
			PathTile t = blockedpaths.removeLast();
			block(t, dest, toBlock);
		}
		
		f.done();
	}
	
	private void init(MDiv m, PathTile t, Bitmap2D toBlock) {
		m.isDeployed = true;
		m.distance = 0;
		PathTile d = t;
		while(d.getParent() != null) {
			m.distance += cost(context, d.x(), d.y());
			d = d.getParent();
		}
		
		PathTile dest = setDest(m, toBlock, t);

		
		blockedpaths.add(t);
		blockedpaths.add(dest);
		
		if (dest.getParent() != null) {
			m.destDir = DIR.get(m.tx, m.ty, dest);
		}else {
			m.destX = -1;
		}
	}
	
	private static double cost(Context context, int dx, int dy) {
		if (context.blob.is(dx, dy))
			return -1;
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.army)) {
			return 1 + SETT.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().environment.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}

	
	private static void block(PathTile t, PathTile dest, Bitmap2D toBlock) {
		while(t != dest) {
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				toBlock.set(t, DIR.ORTHO.get(i), true);
			}
			t = t.getParent();
		}
	}
	

	
	private PathTile setDest(MDiv div, Bitmap2D blocked, PathTile t) {
		
		if (initBlocked(div, blocked, DIR.C, t.x(), t.y(), t))
			return t;
		
		if (t.getParent() == null) {
			div.destX = t.x();
			div.destY = t.y();
			div.destDir = DIR.C;
			return t;
		}
		
		PathTile prev = null;
//		boolean b = true;
		while(t.getParent() != null) {	
			if (initBlocked(div, blocked, t))
				return t.getParent();
//			if (blocked.is(t)) {
//				if (!b) {
//					div.destX = t.x();
//					div.destY = t.y();
//					div.destDir = DIR.get(t, t.getParent());
//					return t;
//				}
//			}else
//				b = false;
			prev = t;
			
			t = t.getParent();
		}
		div.destX = t.x();
		div.destY = t.y();
		div.destDir = DIR.get(prev, t);
		return t;
	}
	
	private boolean initBlocked(MDiv div, Bitmap2D blocked, PathTile block) {
		if (initBlocked(div, blocked, DIR.get(block.getParent(), block), block.getParent().x(), block.getParent().y(), block))
			return true;
		if (initBlocked(div, blocked, DIR.get(block.getParent(), block), block.getParent().x(), block.y(), block))
			return true;
		if (initBlocked(div, blocked, DIR.get(block.getParent(), block), block.x(), block.getParent().y(), block))
			return true;
		return false;
	}
	
	private boolean initBlocked(MDiv div, Bitmap2D blocked, DIR d, int dx, int dy, PathTile block) {
		if (SETT.PATH().availability.get(dx,dy).isSolid(div.div.army())) {
			div.destX = dx;
			div.destY = dy;
			div.destDir = d;
			return true;
		}
		return false;
	}
	

}