package world.map.pathing;

import static world.WORLD.*;

import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.*;
import world.WORLD;
import world.map.pathing.WGenPorts.*;
import world.regions.Region;

final class WGenPortNeighs {

	private final WGenUtil u;
	private final LIST<PortGroup> groups;
	private final WGenPorts pmap;
	
	private Bitmap2D network = new Bitmap2D(WORLD.TBOUNDS(), false);
	public final Polymap polly = new Polymap(TBOUNDS(), 12, 1);
	
	public WGenPortNeighs(WGenUtil u, WGenPorts pmap) {
		this.pmap = pmap;
		this.u = u;
	
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (polly.isEdge(c.x(), c.y()) && !network.is(c)) {
				Flooder f = RES.flooder();
				f.pushSloppy(c, 0);
				while(RES.flooder().hasMore()) {
					PathTile t = f.pollSmallest();
					boolean e = polly.isEdge.is(t.x(), t.y());
					if (e) {
						network.set(t, true);
					}
					
					for (DIR d : DIR.ALL) {
						if (WORLD.WATER().isBig.is(t, d)) {
							
							PathTile res = f.pushSmaller(t, d, t.getValue()+d.tileDistance());
							if (res != null && ((!e && polly.isEdge.is(t, d)) || pmap.get(t, d) != null)) {
								while(res != null) {
									if (network.is(res))
										break;
									network.set(res, true);
									res = res.getParent();
								}
							}
							
						}
					}
				}
				
				
				
			}
		}
		
		LinkedList<Port> ports = pmap.allports;
		LinkedList<PortGroup> groups = new LinkedList<>();
		
		int[] oceanPorts = new int[u.wmapMax];
		
		for (Port p : ports) {
			oceanPorts[u.wmap.get(p.coo)]++;
		}
		
		for (Port p : ports) {
			p.group = null;
		}

		for (Port p : ports) {
			if (p.group == null) {
				PortGroup g = new PortGroup(groups.size());
				groups.add(g);
				createGroup(p, g);
			}
		}
	
		this.groups = new ArrayList<PortGroup>(groups);
		
		
		IntChecker cgroups = new IntChecker(groups.size());
		
		for (PortGroup g : groups) {
			
			if (g.ports.size() < oceanPorts[u.wmap.get(g.ports.get(0).coo)]) {
				findOtherGroups(g, cgroups);
				u.astep.exe();
			}
		}
	}

	
	private void findOtherGroups(PortGroup group, IntChecker cgroups) {
		Flooder f = RES.flooder();
		f.init(this);
		f.pushSloppy(group.ports.rnd().coo, 0);
		
		cgroups.init();
		for (PortGroup n : group.neighbours) {
			cgroups.isSetAndSet(n.id);
		}
		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			Port p = pmap.ports.get(t);
			if (p != null && p.group != null && p.group != group) {
				if(!cgroups.isSetAndSet(p.group.id)) {
					group.neighbours.add(groups.get(p.group.id));
					groups.get(p.group.id).neighbours.add(group);
					connect(t, p);
				}
			}
		
			
			
			for (DIR d : DIR.ALL) {

				if (WORLD.WATER().isBig.is(t, d) && WTRAV.can(t.x(), t.y(), d, false)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					
					if (!network.is(dx, dy))
						continue;
					double v = cost(dx, dy);
					
					f.pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
					
				}
			}

		}
		f.done();
		
	}
	
	public double cost(int dx, int dy) {
		double v = 1;
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
			if (!u.tmpRoute.is(dx, dy))
				v*= 3;
		}
		
		return v;
	}

	private void createGroup(Port start, PortGroup group) {
		
		Flooder f = RES.flooder();
		f.init(this);
		f.pushSloppy(start.coo, 0);
		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (t.getValue() > 32)
				break;
			
			Port p = pmap.ports.get(t);
			if (p.group != null)
				continue;
			p.group = group;
			group.ports.add(p);
			
			for (PortDist d : p.dists) {
				f.pushSmaller(d.to.coo, t.getValue()+d.dist);
			}
		}
		f.done();
		
		
	}


	private void connect(PathTile tile, Port from) {
		
		PathTile t = tile;
		u.connectWay(t);
		double cost = 0;
		while (t.getParent() != null) {
			cost += WPATHING.getTerrainCost(t.x(), t.y())*DIR.get(t, t.getParent()).tileDistance();
			t = t.getParent();
		}
		Port to = pmap.ports.get(t);
		
		for (PortDist d : to.dists) {
			if (d.to == from)
				return;
		}
			
		
		from.push(to, cost);
		to.push(from, cost);
		
	}

	

	

}
