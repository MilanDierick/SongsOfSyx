package world.map.pathing;

import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayListGrower;
import world.WORLD;
import world.map.pathing.WGenPorts.*;
import world.map.pathing.WGenRoad.RDistance;
import world.map.pathing.WGenRoad.RReg;
import world.regions.Region;

class WGenPortCondense {

	private final WGenRoad rr;
	
	WGenPortCondense(WGenUtil u, WGenPorts pps, WGenRoad rr){
		
		this.rr = rr;
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = WORLD.REGIONS().map.get(c);
			if (r != null) {
				Port p = pps.ports.get(c);
				if (p != null) {
					processPort(p);
				}
			}
			
		}
		
		for (Port p : pps.allports) {
			if (p.totalConnections <= 0) {
				p.dists.clear();
				WORLD.ROADS().HARBOUR.clear(p.coo);
				if (u.tmpRoute.is(p.coo)) {
					u.tmpRoute.set(p.coo, false);
					for (DIR d : DIR.ORTHO) {
						if (!WORLD.WATER().isBig.is(p.coo) && u.tmpRoute.is(p.coo, d) && !WORLD.WATER().isBig.is(d.perpendicular()) && u.tmpRoute.is(p.coo, d.perpendicular())) {
							u.tmpRoute.set(p.coo, true);
							WORLD.ROADS().ROAD.set(p.coo);
							break;
						}
					}
				}
				
			}else {
				connect(p, u);
			}
		}
		

		
		
		
		
		
		
	}
	
	private void connect(Port p, WGenUtil u) {

		Region home = p.home.reg;
		
		Flooder f = RES.flooder();
		f.init(this);
		f.pushSloppy(p.coo, 0);
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			Region current = WORLD.REGIONS().map.get(t);
			if (current == null || current != home)
				continue;
			
			if (t.isSameAs(current.cx(), current.cy())) {
				f.done();
				WTRAV.makeRoad(t);
				u.connectWay(t);
			}
			

			for (DIR d : DIR.ALL) {
				
				if (WTRAV.can(t.x(), t.y(), d, false) && !WORLD.WATER().isBig.is(t,d)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					double v = u.cost(dx, dy);
					if (WTRAV.can(t.x(), t.y(), d, true))
						v*= 0.25;
					f.pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
				}
			}

		}
		f.done();
		
	}

	private void processPort(Port p) {
		p.totalConnections = p.group.ports.size()-1;
		for (PortGroup g : p.group.neighbours)
			p.totalConnections += g.ports.size();
		
		
		if (p.totalConnections <= 0) {
			p.totalConnections = 0;
			return;
		}

		if (p.dists.size() == p.totalConnections) {
			for (PortDist d : p.dists) {
				double dist = p.distToHome + d.dist + d.to.distToHome;
				if (!hasOtherLandRoute(p, d.to, dist))
					return;
			}
			
			for (int i = 0; i < p.dists.size(); i++) {
				ArrayListGrower<PortDist> dd = p.dists.get(i).to.dists;
				for (int k = 0; k < dd.size(); k++) {
					if (dd.get(k).to == p) {
						dd.remove(dd.get(k));
						p.dists.get(i).to.totalConnections--;
					}
				}
				
			}
			p.group.ports.remove(p);
			p.dists.clear();
			
			p.totalConnections = 0;
		}
	}
	
	private boolean hasOtherLandRoute(Port pfrom, Port pto, double maxDist) {
		Flooder f = RES.flooder();
		f.init(this);
		
		RReg from = rr.get(pfrom.coo);
		RReg to = rr.get(pto.coo);
		{
			for (int i = 0; i < from.dists.size(); i++) {
				RDistance d = from.dists.get(i);
				f.pushSmaller(d.to.reg.cx(), d.to.reg.cy(), 0);
			}
		}

		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue() > maxDist)
				break;
			
			RReg r = rr.get(t);
			if (r == to) {
				f.done();
				return true;
			}
			for (int i = 0; i < r.dists.size(); i++) {
				RDistance d = r.dists.get(i);
				f.pushSmaller(d.to.reg.cx(), d.to.reg.cy(), t.getValue()+ d.dist);
			}
		}
		
		f.done();
		return false;
		
		
		
	}
	
}
