package settlement.army.ai.general;

import init.C;
import init.config.Config;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.ai.general.Groups.GroupLine;
import settlement.army.ai.general.GroupsIniter.GroupDiv;
import settlement.army.ai.general.Pathing.PATHCOST;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;

final class GroupMover {

	
	private final Context c;
	private ArrayList<GDiv> findable = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private GroupMoverTracer tracer;
	private GroupMoverDiv mover;
	private final VectorImp vec = new VectorImp();
	
	private final int depth = 8;
	private final int MAXW = 440;
	
	GroupMover(Context c){
		this.c = c;
		tracer = new GroupMoverTracer(c);
		mover = new GroupMoverDiv(c);
	}
	

	
	public GroupLine setLine(GroupDiv group, GroupLine line) {
		int men = 0;
		GroupDiv leader = group;
		
		line.firstRowDeployed = 0;
		line.extraRows = 0;
		
		;
		
		int xx = 0;
		int yy = 0;
		int i = 0;
		{
			double fire = 0;
			GroupDiv n = group;
			while(n != null) {
				men += n.div.div().menNrOf();
				fire += DivMorale.PROJECTILES.getD(n.div.div());
				line.divsAll.set(n.div.index(), true);
				line.divsTmp.set(n.div.index(), true);
				
				if (n.goesStraight) {
					xx += n.div.tx;
					yy += n.div.ty;
					i++;
				}
				
				n = n.next();
			}
			fire /= men;
			line.shouldRun = fire > 0.25;
		}
		
		
		
		int w = CLAMP.i(men/depth, 2, MAXW);
		line.width = w;
		
		if (i > 1) {
			xx /= i;
			yy /= i;
			
//			GroupDiv best = leader;
//			double bestD = Double.MAX_VALUE;
//			GroupDiv n = group;
//			while(n != null) {
//				if (n.goesStraight) {
//					int dx = xx-n.div.tx;
//					int dy = yy-n.div.ty;
//					double di = dx*dx +dy*dy;
//					if (dx*dx +dy*dy < bestD) {
//						if (di < bestD) {
//							best = n;
//							bestD = di;
//						}
//					}
//				}
//				n = n.next();
//			}
			
			//leader = best;
			
		}else {
			xx = leader.div.tx;
			yy = leader.div.ty;
		}
		
		setPath(leader);
		initLine(leader, line, xx, yy);
		
//		line.start.set(leader.destX, leader.destY);
//		line.v.set(xx, yy, leader.t.centre);
//		line.v.rotate90();
//		
//			
//		line.start.increment(line.v.nX()*-w/2, line.v.nY()*-w/2);
		
		return line;

	}
	
	private void initLine(GroupDiv leader, GroupLine line, int xx, int yy) {
		
		line.start.set(leader.destX, leader.destY);
		
		if (SETT.PATH().availability.get(line.start).isSolid(c.army)) {
			line.v.set(leader.nx, leader.ny);
			line.width = CLAMP.i(line.width/2, 5, MAXW);
			line.start.increment(line.v.nX()*-line.width/2, line.v.nY()*-line.width/2);
			return;
		}
		
		line.v.set(xx, yy, leader.t.centre);
		

		
//		{
//			double nx = 0;
//			double ny = 0;
//			GroupDiv d = leader;
//			while(d != null) {
//				if (d.goesStraight) {
//					double m = 1.0/vec.set(d.div.tx, d.div.ty, d.t.centre);
//					nx += m*vec.nX();
//					ny += m*vec.nY();
//				}
//				d = d.next();
//			}
//			line.v.set(nx, ny);
//		}
//		
//		{
//			double nx = 0;
//			double ny = 0;
//			GroupDiv d = leader;
//			int m = leader.t.coos.getI();
//			while(d != null) {
//				if (d.goesStraight) {
//					
//					leader.t.coos.set(0);
//					double bestD = Double.MAX_VALUE;
//					int bi = 0;
//					for (int i  = 0; i < m; i++) {
//						double dist = leader.t.coos.set(i).tileDistanceTo(d.div.tx, d.div.ty);
//						if (dist < bestD) {
//							bestD = dist;
//							bi = i;
//						}
//					}
//					
//					
//					vec.set(d.div.tx, d.div.ty, d.t.coos.set(bi));
//					nx += 1.0/bestD*vec.nX();
//					ny += 1.0/bestD*vec.nY();
//				}
//				d = d.next();
//			}
//			line.v.set(nx, ny);
//		}
		
		{
			
			int m = leader.t.coos.getI();
			for (int i  = 0; i < m; i++) {
				
				GroupDiv d = leader;
				double bestD = Double.MAX_VALUE;
				GroupDiv bi = null;
				while(d != null) {
					if (d.goesStraight) {
						
						double dist = leader.t.coos.set(i).distance(d.div.tx, d.div.ty);
						if (dist <bestD) {
							bestD = dist;
							bi = d;
						}
					}
					d = d.next();
				}
				
				if (bi != null) {
					if (bestD < bi.closestDist) {
						bi.closestDist = bestD;
						bi.closest = i;
					}
				}
				
				
			}
			
			GroupDiv d = leader;
			double nx = 0;
			double ny = 0;
			int x = 0;
			int y = 0;
			int am = 0;
			while(d != null) {
				if (d.goesStraight && d.closest != -1) {
					double dist = vec.set(d.div.tx, d.div.ty, leader.t.coos.set(d.closest));
					nx += 1.0/dist*vec.nX();
					ny += 1.0/dist*vec.nY();
					x += leader.t.coos.get().x();
					y += leader.t.coos.get().y();
					am ++;
				}
				d = d.next();
			}
			

			
			if (nx != 0 || ny != 0 && am > 0) {
				line.v.set(nx, ny);
				xx = x/am;
				yy = y/am;
			}
		}
		
		
		//
		line.v.rotate90();
		
		
		{
			
			
			double m = vec.set(xx-leader.destX, yy-leader.destY);
			
			double dot = vec.nX()*line.v.nX() + vec.nY()*line.v.nY();
			m*= dot;

			double dx = line.v.nX();
			double dy = line.v.nY();
			if (m < 0) {
				m = -m;
				dx = -dx;
				dy = -dy;
			}
			
			double dd =0;
			while(m > 0.1) {
				double d = CLAMP.d(m, 0, 1);
				m-= d;
				dd += d;
				int lx = (int) (leader.destX+dx*dd);
				int ly = (int) (leader.destY+dy*dd);
				
				if (!SETT.IN_BOUNDS(lx, ly) || SETT.PATH().availability.get(lx, ly).isSolid(c.army)) {
					break;
				}
				line.start.set(lx, ly);
			}
			
		}
		
		if (!SETT.IN_BOUNDS(line.start)) {
			line.width = 0;
			line.firstRowDeployed = 0;
			return;
		}
		

		if (SETT.PATH().availability.get(line.start).isSolid(c.army)) {
			line.width = CLAMP.i(line.width/2, 5, line.width);
			line.start.increment(line.v.nX()*-line.width/2, line.v.nY()*-line.width/2);
		}else {
			
			
			double dpow = CLAMP.d((double)(2.0*leader.t.men) / (line.width*depth), 0.5, 1);
			line.width = CLAMP.i((int) (line.width*dpow), 5, MAXW);
			
			int lefts = 0;
			int rights = 0;
			int w = 1;
			int i = 1;
			int sx = line.start.x();
			int sy = line.start.y();
			line.start.increment(line.v.nX()*-line.width/2, line.v.nY()*-line.width/2);
			while(w <= line.width && lefts<5 && rights<5) {
				
				if (lefts < 5) {
					int x = (int) (sx - line.v.nX()*i);
					int y = (int) (sy - line.v.nY()*i);
					if (!SETT.IN_BOUNDS(x, y))
						lefts = 5;
					else if (SETT.PATH().availability.get(x, y).isSolid(c.army)){
						lefts++;
					}else {
						int am = 1 + lefts;
						
						if (w + lefts > line.width) {
							lefts = 5;
						}else {
							lefts = 0;
							line.start.set(x, y);
							w += am;
						}
					}
				}
				if (rights < 5) {
					int x = (int) (sx + line.v.nX()*i);
					int y = (int) (sy + line.v.nY()*i);
					if (!SETT.IN_BOUNDS(x, y))
						rights = 5;
					else if (SETT.PATH().availability.get(x, y).isSolid(c.army)){
						rights++;
					}else {
						int am = 1 + rights;
						
						if (w + rights > line.width) {
							rights = 5;
						}else {
							rights = 0;

							w += am;
						}
					}
				}
				
				i++;
			}
			if (w <= 5) {
				line.start.set(sx, sy);
				line.start.increment(line.v.nX()*-2, line.v.nY()*-2);
				line.width = 5;
			}else {
				line.width = w-1;
			}
			
		}
		
	}
	
	
	private void setPath(GroupDiv leader) {
		
		int destX = leader.destX;
		int destY = leader.destY;
		
		Div enemy = c.divFinder.get(c.army.enemy(), destX, destY, leader.div.tx, leader.div.ty);
		
		if (enemy != null) {
			enemy.order().status.get(c.status);
			destX = c.status.currentPixelCX() >> C.T_SCROLL;
			destY = c.status.currentPixelCY() >> C.T_SCROLL;		
		}
		
		PathTile t = c.pathing.getPath(leader.div.tx, leader.div.ty, c.pmap.CostNoPath, destX, destY);

		if (t == null) {
			return;
		}
		
		PathTile dest = t;
		while(t.getParent() != null) {
			
			if (SETT.PATH().availability.get(t).isSolid(c.army))
				dest = t;
			else if (ArmyAIUtil.map().hasEnemy.is(t, c.army))
				dest = t;
			
			t = t.getParent();
		}
		
		int moveDist = dest.parents();
		
		if (SETT.PATH().availability.get(dest).isSolid(c.army)) {
			if (moveDist < 20)
				moveDist = 0;
			else
				moveDist = moveDist/2;
		}else {
			if (moveDist < 3)
				moveDist = 1;
			else
				moveDist = moveDist/2;
		}

		
		for (int i = 0; i < moveDist && dest.getParent() != null; i++) {
			dest = dest.getParent();
		}
//		
		if (dest.getParent() != null) {
			DIR d = DIR.get(dest.getParent(), dest).next(2);
			leader.nx = d.xN();
			leader.ny = d.yN();
		}
		
		leader.destX = dest.x();
		leader.destY = dest.y();
		
	}
	
	public boolean moveToLine(GroupLine l) {
		
		
		{

			findable.clearSloppy();
			
			for (int di = 0; di < c.divs.size(); di++) {
				GDiv d = c.divs.get(di);
				if (l.divsTmp.get(d.index())) {
					d.init();
					if (d.active) {
						findable.add(d);
					}
				}
			}
			if (c.divFinder.init(findable) == 0) {
				return false;
			}	
		}

		
		int left = l.left;
		int right = l.width-l.right-5;
		int tw = l.width-(l.left+l.right);
		
		if (tw < 2) {
			l.extraRows += l.biggestH+2;
			l.left = 0;
			l.right = 0;
			l.biggestH = 0;
			left = l.left;
			right = l.width-l.right-5;
			tw = l.width-(l.left+l.right);
			
			if (l.extraRows > 24) {
				l.divsTmp.clear();
				return false;
			}
		}
		
		int destTx1 = (int) (l.startX() + l.v.nX()*left);
		int destTy1 = (int) (l.startY() + l.v.nY()*left);
		
		if (!SETT.IN_BOUNDS(destTx1, destTy1)) {
			l.divsTmp.clear();
			return false;
		}
		
		int r = 0;
		
		if (tw > 3 && l.left > l.right) {
			
			destTx1 = (int) (l.startX() + l.v.nX()*right);
			destTy1 = (int) (l.startY() + l.v.nY()*right);
			r = 1;
		}
		{
			GDiv d = tracer.trace(destTx1, destTy1, l);
			if (d != null) {
				moveToLine(d, l, r == 0, null, c.pmap.CostNoPath);
				return true;
			}
		}
		
		if (!SETT.IN_BOUNDS(destTx1, destTy1)) {
			l.divsTmp.clear();
			return false;
		}
		
		int dx1 = destTx1 >> AbsMap.scroll;
		int dy1 = destTy1 >> AbsMap.scroll;
		
		
		PATHCOST cost = l.extraRows == 0 ? c.pmap.CostNoPath : c.pmap.CostNoPath;
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		f.pushSloppy(dx1, dy1, 0);
		f.setValue2(dx1, dy1, r);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			double v = t.getValue();
			
			GDiv div = c.divFinder.get(t);
			
			if (div != null) {
				if (!l.divsTmp.get(div.index()))
					continue;
				
				f.done();
				div.updated = true;
				
				moveToLine(div, l, t.getValue2() == 0, t, cost);
				return true;
			}
			
			
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					if (f.pushSmaller(dx,  dy, v+cost.abs().get(dx, dy)*d.tileDistance(), t) != null) {
						f.setValue2(dx, dy, t.getValue2());
					}
				}
			}
			
		}
				
		f.done();

		return false;

	}
	
	private void moveToLine(GDiv div, GroupLine l, boolean isLeft, PathTile t, PATHCOST cost) {
		
		
		
		int left = l.left;
		int tw = l.width-l.left - l.right;
		
		int sx = (int) (l.startX() + l.v.nX()*left);
		int sy = (int) (l.startY() + l.v.nY()*left);
		
		if (!isLeft) {
			
			int w = (int) Math.ceil(div.div().menNrOf()/depth);
			if (w > tw)
				w = tw;
			int right = l.width-l.right-w;
			sx = (int) (l.startX() + l.v.nX()*right);
			sy = (int) (l.startY() + l.v.nY()*right);
		}
		
		int w = (int) Math.ceil((double)div.div().menNrOf()/depth);
		double dw = (double)tw/w;
		
		if (w == 0) {
			div.updated = true;
			return;
		}
		
		if (dw > 2) {
			
		}else if(dw < 1) {
			w = tw;
		}else if(dw < 1.5){
			w = tw;
		}else {
			w = (int) Math.ceil(tw/2);
		}
		
		if (tw -w == 0) {
			
		}else if(tw -w < 3) {
			w = tw;
		}else if (w > tw) {
			w = tw;
		}
		if (w == 0)
			return;
		if (w == 0) {
			throw new RuntimeException(tw + " " + l.width + " " + l.left + " " + l.right);
		}
		
		deploy(l, sx, sy, div, w, t, cost);
		l.firstRowDeployed += w;
		if (isLeft) {
			l.left+= w;
		}else
			l.right += w;
		
		
	}
	
	private void deploy(GroupLine l, int tx, int ty, GDiv d, int width, PathTile t, PATHCOST cost) {
		
		d.div().settings.running = l.shouldRun;
		if (mover.moreIntoFormation(d, l, tx, ty, width, t, l.extraRows == 0)) {
			d.updated = true;
			l.biggestH = Math.max(d.div().menNrOf()/depth, l.biggestH);
		}
		l.divsTmp.set(d.index(), false);
		
		
		
	}
	
	public boolean waitForMovement() {
		
		return false;
	}

	
}
