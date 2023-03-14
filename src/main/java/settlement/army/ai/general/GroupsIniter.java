package settlement.army.ai.general;

import init.config.Config;
import settlement.army.ai.general.GroupsIniterTargets.Target;
import settlement.army.ai.general.Pathing.PATHCOST;
import settlement.army.ai.util.ArmyAIUtil;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class GroupsIniter {
	
	private final ArrayList<GroupDiv> all;
	private final ArrayList<GroupDiv> divs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<GroupDiv> groups = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final Context c;
	private final GroupsIniterTargets targets;
	private final IntChecker checker = new IntChecker(Config.BATTLE.DIVISIONS_PER_ARMY);
	
	GroupsIniter(Context c){
		this.c = c;
		targets = new GroupsIniterTargets(c);
		ArrayList<GroupDiv> all = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		for (GDiv d : c.divs)
			all.add(new GroupDiv(d));
		this.all = all;
	}
	
	public LIST<GroupDiv> init() {
		
		attachDivsToEnemyGroups();
		groupTheDivs();
		return groups;
		
		
	}
	
	public void debugGroup(GroupDiv d) {
		LOG.ln(d.div.div().index());
		GroupDiv t = d.next();
		while(t != null) {
			LOG.ln("   " + t.div.div().index());
			t = t.next;
		}
		LOG.ln("  ->> " + d.t.coo);
		int m = d.t.coos.getI();
		for (int i = 0; i < m; i++) {
			d.t.coos.set(i);
			LOG.ln("(" + d.t.coos.get().x() + " " + d.t.coos.get().y() + ")");
		}
		d.t.coos.set(m);
		LOG.ln();
		
	}

	

	
	private void attachDivsToEnemyGroups() {
		
		divs.clearSloppy();
		c.divFinder.init(c.divs);
		
		
		LIST<Target> tars = targets.get();
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);

		{
			int ti = 1;
			for (Target t : tars) {
				int m = t.coos.getI();
				for (int i = 0; i < m; i++) {
					t.coos.set(i);
					
					f.pushSloppy(t.coos.get().x()>>AbsMap.scroll, t.coos.get().y()>>AbsMap.scroll, 0);
					f.setValue2(t.coos.get().x()>>AbsMap.scroll, t.coos.get().y()>>AbsMap.scroll, ti);
				}
				t.coos.set(m);
				ti++;
			}
			
			int destX = c.getDestCoo().x() >> AbsMap.scroll;
			int destY = c.getDestCoo().y() >> AbsMap.scroll;
			f.pushSloppy(destX, destY, 0);
			f.setValue2(destX, destY, 0);
		}

		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			GDiv div = c.divFinder.get(t);
			if (div != null) {
				if (t.getValue2() == 0) {
					
				}
				else {
					GroupDiv dd = all.get(div.index());
					dd.t = tars.get((int) (t.getValue2()-1));
					if (dd.t == null)
						LOG.ln(tars.size() + " " + t.getValue2());
					dd.init(t, c.pmap.cost);
					divs.add(dd);
				}
			}
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					double co = t.getValue() + c.pmap.cost.abs.get(dx, dy)*d.tileDistance();
					if (f.pushSmaller(dx, dy, co, t) != null)
						f.setValue2(dx, dy, t.getValue2());
				}
			}
		}
		
		f.done();
	}
	
	private void groupTheDivs() {
		
		groups.clearSloppy();
		checker.init();
		AbsMap m = c.absMap;
		m.clear();
		for (GroupDiv d : divs) {
			m.set(d.ax, d.ay, 1);
		}
		
		while(!divs.isEmpty()) {
			GroupDiv d = divs.removeLast();
			if (checker.isSetAndSet(d.div.index()))
				continue;
			checker.isSetAndSet(d.div.index());
			createGroup(d);
		}
		
	}
	
	private void createGroup(GroupDiv leader) {
		
		if (leader.t.power <= 0)
			return;
		
		leader.next = null;
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		AbsMap m = c.absMap;
		int am = 0;
		for (GroupDiv d : divs) {
			if (checker.isSet(d.div.index()))
				continue;
			if (d.t != leader.t)
				continue;
			am++;
		}
		if (am == 0) {
			groups.add(leader);
			f.done();
			return;
		}
		
		f.pushSloppy(leader.ax, leader.ay, 0);
		
		while(f.hasMore() && leader.t.power > 0) {
			PathTile t = f.pollSmallest();
			double v = t.getValue();
			
			if (m.is(t, 1)) {
				boolean any = false;
				for (GroupDiv dd : divs) {
					if (checker.isSet(dd.div.index()))
						continue;
					any = true;
					if (!t.isSameAs(dd.ax, dd.ay))
						continue;
					if (dd.t != leader.t)
						continue;
					checker.isSetAndSet(dd.div.index());
					dd.next = leader.next;
					leader.next = dd;
					v = 0;
					dd.t.power -= dd.div.div().settings.power/2.0;
				}
				if (!any)
					m.set(t, 0);
			}

			if (v >= 64)
				continue;
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					
					
					f.pushSmaller(dx,  dy, v+c.pmap.CostNoPath.abs().get(dx, dy));
				}
			}
			
		}
		
		
		
		f.done();
		leader = setFirst(leader);
		groups.add(leader);
	}
	
	private GroupDiv setFirst(GroupDiv first) {
		
		double bestDist = Double.MAX_VALUE;
		GroupDiv n = first;
		GroupDiv leader = n;
		while(n != null) {
			
			if (n.dist < bestDist) {
				leader = n;
				bestDist = n.dist;
			}
			n = n.next();
		}
		if (first == leader)
			return first;
		n = first;
		while(n != null) {
			if (n.next == leader) {
				n.next = leader.next;
				break;
			}
			n = n.next();
		}
		leader.next = first;
		return leader;
		
	}
	
	static class GroupDiv {
		
		public Target t;
		public int ax,ay;
		public double dist;
		public int destX,destY;
		public double nx,ny;
		private static VectorImp vec = new VectorImp();
		public boolean goesStraight;
		public boolean hasStraightLine;
		private GroupDiv next; 
		public final GDiv div;
		
		public int closest;
		public double closestDist;
		
		GroupDiv(GDiv div){
			this.div = div;
		}
		
		void init(PathTile t, PATHCOST cost){
			closest = -1;
			closestDist = Double.MAX_VALUE;
			
			dist = t.getValue();
			ax = t.x();
			ay = t.y();
			
			
			if (pathIsStraight(t)) {
				
				vec.set(div.tx, div.ty, this.t.centre);
				vec.rotate90();
				goesStraight = true;
				nx = vec.nX();
				ny = vec.nY();
			}else {
				DIR d = DIR.get(t, t.getParent());
				nx = d.xN();
				ny = d.yN();
				goesStraight = false;
			}
			
			hasStraightLine = traceDest(t, cost);
			
			while(t.getParent() != null) {
				t = t.getParent();
			}
			
			int tx = (t.x() << AbsMap.scroll)+AbsMap.size/2;
			int ty = (t.y() << AbsMap.scroll)+AbsMap.size/2;
			
			
//			{
//				
//				
//				int moveDist = t.parents();
//				if (moveDist < 3)
//					moveDist = 1;
//				else
//					moveDist = moveDist/2;
//				
//				PathTile p = t;
//				for (int i = 0; i < moveDist && p != null; i++) {
//					
//					tx = p.x();
//					ty = p.y();
//					p = p.getParent();
//				}
//				
//				tx = (tx << AbsMap.scroll)+AbsMap.size/2;
//				ty = (ty << AbsMap.scroll)+AbsMap.size/2;
//				
//			}
			
			destX = tx;
			destY = ty;
			
//			while(t.getParent() != null) {
//				t = t.getParent();
//			}
//			
//			destX = (t.x()<< AbsMap.scroll)+AbsMap.size/2;
//			destY = (t.y()<< AbsMap.scroll)+AbsMap.size/2;
			
			
		}
		
		private boolean pathIsStraight(PathTile start) {
			PathTile t = start;
			if (t.getParent() == null)
				return true;
			DIR d = DIR.get(t, t.getParent());
			t = t.getParent();
			while(t.getParent() != null) {
				DIR d2 = DIR.get(t, t.getParent());
				if (d.x()*d2.x() + d.y()*d2.y() <= 0)
					return false;
				t = t.getParent();
			}
			return true;
		}
		
		private boolean traceDest(PathTile start, PATHCOST coster) {
			
			double oldV = start.getValue();
			COORDINATE c = start;
			while (start.getParent() != null)
				start = start.getParent();
			COORDINATE d = start;
			
			
			
			if (c.isSameAs(d))
				return true;
			
			double m = vec.set(c, d);
			double cost = 0;
			double x = c.x();
			double y = c.y();
			
			while(m > 0) {
				if (ArmyAIUtil.map().hasOtherAlly.is((int)x, (int)y, div.div()))
					return false;
				cost += coster.abs().get((int)x, (int)y);
				m-= 1;
				x += vec.nX();
				y += vec.nY();
			}
			
			if (cost*0.75 <= oldV)
				return true;
			
			return false;
		}
		
		public GroupDiv next() {
			return next;
		}
	}

	
}
