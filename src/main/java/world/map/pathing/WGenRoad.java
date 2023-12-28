package world.map.pathing;

import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.map.pathing.WGenPorts.Port;
import world.regions.Region;
import world.regions.WREGIONS;

final class WGenRoad implements MAP_OBJECT<WGenRoad.RReg>{

	private final IntChecker dests = new IntChecker(WREGIONS.MAX);
	private final WGenUtil u;
	public final RReg[] all = new RReg[WREGIONS.MAX];
	private double[] dists = new double[WREGIONS.MAX];
	private final WGenPorts ports;
	
	public WGenRoad(WGenUtil util, WGenPorts ports) {
		this.ports = ports;
		this.u = util;
		for (Region r : WORLD.REGIONS().all()) {
			if (r.info.area() > 0) {
				all[r.index()] = new RReg(r);
			}
		}
		
		Tree<RReg> sort = new Tree<WGenRoad.RReg>(WREGIONS.MAX) {
			
			@Override
			protected boolean isGreaterThan(RReg current, RReg cmp) {
				return current.lastValue > cmp.lastValue;
			}
		};
		
		for (RReg r : all) {
			if (r != null) {
				r.init(dests, this, util);
				
				sort.add(r);
			}
		}
		
		while(sort.hasMore()) {
			RReg r = sort.pollSmallest();
			
			if (r.changed) {
				r.changed = false;
				sort.add(r);
			}else if(findNextConnection(r))
				sort.add(r);
		}
		
	}

	private boolean findNextConnection(RReg r) {
		
		dests.init();
		
		double longest = 0;
		for (RDistance d : r.neighs) {
			dests.isSetAndSet(d.to.reg.index());
			dists[d.to.reg.index()] = d.dist;
			longest = Math.max(longest, d.dist);
		}
		
		RReg nn = tryNeighs(r, dests, longest);
		
		if (nn != null) {
			nn.remove(r);
			r.remove(nn);
			return true;
		}
		
		PathTile t = r.findNext(dests, this, u);
		

		
		if (t == null)
			return false;
		
		connect(t);
		RReg to = get(t);
		to.remove(r);
		r.remove(to);
		
		to.dists.add(new RDistance(r, t.getValue()));
		r.dists.add(new RDistance(to, t.getValue()));
		
		return true;
		
		
	}

	private RReg tryNeighs(RReg home, IntChecker check, double longest) {
		
		if (home.dists.size() == 0)
			return null;
		
		Flooder f = RES.flooder();
		f.init(this);
		
		for (RDistance d : home.dists) {
			if (!check.isSet(d.to.reg.index()))
				f.pushSloppy(d.to.reg.cx(), d.to.reg.cy(), 0);
		}
		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue() > longest)
				break;
			
			RReg current = get(t);
			
			if (check.isSet(current.reg.index())) {
				if (t.getValue() < dists[current.reg.index()]) {
					f.done();
					return current;
					
				}
			}
			
			for (RDistance d : current.dists) {
				if (d.to != home)
					f.pushSmaller(d.to.reg.cx(), d.to.reg.cy(), t.getValue() + d.dist, t);
			}
			
		}
		f.done();
		return null;

	}
	

	
	void connect(PathTile t) {
		
		WTRAV.makeRoad(t);
		
		Port from = null;
		double pDist = 0;
		
		while(t != null) {
			u.tmpRoute.set(t, true);
			
			Port p = ports.ports.get(t);
			if (p != null) {
				if (from != null) {
					from.push(p, pDist-t.getValue());
					p.push(from, pDist-t.getValue());
					from = null;
					pDist = 0;
				}else {
					from = p;
					pDist = t.getValue();
				}
			}
			t = t.getParent();
			
		}
		
	}
	
	
	

	static class RReg {
		
		public final Region reg;
		private double lastValue = Double.MAX_VALUE;
		private final ArrayListGrower<RDistance> neighs = new ArrayListGrower<>();
		public final ArrayListGrower<RDistance> dists = new ArrayListGrower<>();
		boolean changed = false;
		
		RReg(Region home){
			this.reg = home;
		}
		
		void remove(RReg to) {
			for (RDistance d : neighs) {
				if (d.to == to) {
					neighs.remove(d);
					
					return;
				}
			}
			changed = true;
			lastValue = Double.MAX_VALUE;
			for (RDistance d : neighs) {
				if (d.dist < lastValue)
					lastValue = d.dist;
			}
			
		}
		
		void init(IntChecker check, MAP_OBJECT<RReg> map, WGenUtil u){
			
			check.init();
			
			for (RDistance d : neighs) {
				check.isSetAndSet(d.to.reg.index());
			}
			
			
			Flooder f = RES.flooder();
			f.init(this);
			f.pushSloppy(reg.info.cx(), reg.info.cy(), 0);
			while (f.hasMore()) {
				PathTile t = f.pollSmallest();
				RReg current = map.get(t);
				if (current == null)
					continue;
				
				if (current != this) {
					if (!check.isSet(current.reg.index())) {
						if (t.isSameAs(current.reg.cx(), current.reg.cy()))  {
							check.isSetAndSet(current.reg.index());
							neighs.add(new RDistance(current, t.getValue()));
							current.neighs.add(new RDistance(this, t.getValue()));
							if (t.getValue() < lastValue)
								lastValue = t.getValue();
						}
					}else {
						continue;
					}
				}
				push(t, map, u);

			}
			f.done();
		}
		

		
		PathTile findNext(IntChecker check, MAP_OBJECT<RReg> map, WGenUtil u) {
			
			if (neighs.size() == 0)
				return null;
			
			check.init();
			
			for (RDistance d : neighs) {
				check.isSetAndSet(d.to.reg.index());
			}
			
			
			Flooder f = RES.flooder();
			f.init(this);
			f.pushSloppy(reg.info.cx(), reg.info.cy(), 0);
			while (f.hasMore()) {
				PathTile t = f.pollSmallest();
				RReg current = map.get(t);
				if (current == null)
					continue;
				
				if (current != this) {
					if (check.isSet(current.reg.index())) {
						if (t.isSameAs(current.reg.cx(), current.reg.cy()))  {
							f.done();
							return t;
						}
					}else {
						continue;
					}
				}
				push(t, map, u);

			}
			f.done();
			neighs.clear();
			return null;
		}
		
		private void push(PathTile t, MAP_OBJECT<RReg> map, WGenUtil u) {
			RReg current = map.get(t);
			boolean w = WORLD.WATER().isBig.is(t);
			for (DIR d : DIR.ALL) {
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					if (w && WORLD.WATER().isBig.is(dx, dy))
						continue;
					double v = u.cost(dx, dy) + WTRAV.extracost(dx, dy, d);
					RReg to = map.get(dx, dy);
					if (to != current && !d.isOrtho())
						continue;
					if (current != this && current != to)
						continue;
					if (WTRAV.can(t.x(), t.y(), d, true))
						v*= 0.5;
//					else if (WORLD.WATER().isBig.is(dx, dy))
//						continue;
					RES.flooder().pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
					
				}
			}
		}
		
	}

	public static class RDistance {
		
		final RReg to;
		final double dist;
		
		RDistance(RReg to, double dist){
			this.to = to;
			this.dist = dist;
		}
		
	}
	

	@Override
	public WGenRoad.RReg get(int tile) {
		Region r = WORLD.REGIONS().map.get(tile);
		if (r != null)
			return all[r.index()];
		return null;
	}

	@Override
	public WGenRoad.RReg get(int tx, int ty) {
		Region r = WORLD.REGIONS().map.get(tx, ty);
		if (r != null)
			return all[r.index()];
		return null;
	}

}
