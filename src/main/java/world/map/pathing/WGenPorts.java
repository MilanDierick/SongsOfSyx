package world.map.pathing;

import static world.WORLD.*;

import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LinkedList;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;

final class WGenPorts implements MAP_OBJECT<WGenPorts.PReg>{

	private final WGenUtil u;
	private final IntChecker wCheck;
	private final IntChecker rCheck = new IntChecker(WREGIONS.MAX);
	private final PReg[] all = new PReg[WREGIONS.MAX];
	public final LinkedList<Port> allports = new LinkedList<>();
	private final int[] portsInArea;
	
	public WGenPorts(WGenUtil wmap) {
		
		this.u = wmap;
		portsInArea = new int[wmap.wmapMax];
		wCheck = new IntChecker(wmap.wmapMax);
		
		for (Region r : REGIONS().all())
			all[r.index()] = new PReg(r);
		
		for (PReg r : all) {
			createPorts(r);
		}
		
		for (Port p : allports)
			portsInArea[wmap.wmap.get(p.coo)]++;
		
		for (PReg r : all) {
			connect(r);
		}
	}

	private void connect(PReg r) {
		if (r.ports.size() == 0)
			return;
		
		rCheck.init();
		
		for (Port p : r.ports) {
			for (PortDist d : p.dists)
				rCheck.isSetAndSet(d.to.home.reg.index());
		}
		
		for (int i = 0; i < r.ports.size(); i++) {
			connect(r.ports.get(i));
		}
		
	}

	private void connect(Port start) {
		Flooder f = RES.flooder();
		f.init(this);
		f.pushSloppy(start.coo, 0);
		PReg home = start.home;
		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			PReg current = get(t);
			if (current == null)
				continue;
			if (rCheck.isSet(current.reg.index()))
				continue;
			if (!WORLD.WATER().isBig.is(t)) {
				continue;
			}
			if (current != home) {
				Port p = ports.get(t);
				if (p != null) {
					connectPort(t, p);
				}
			}
		
			for (DIR d : DIR.ALL) {
				
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					PReg to = get(dx, dy);
					if (to == null)
						continue;
					if (current != home && current != to)
						continue;
					double v = u.cost(dx, dy) + WTRAV.cost(t.x(), t.y(), d);
					if (WTRAV.can(t.x(), t.y(), d, true))
						v*= 0.5;
					f.pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
				}
			}

		}
		f.done();
		
		if (start.dists.size() == 0 && portsInArea[u.wmap.get(start.coo)] > 1) {
			f.init(this);
			f.pushSloppy(start.coo, 0);
			
			while (f.hasMore()) {
				PathTile t = f.pollSmallest();
				PReg current = get(t);
				if (!WORLD.WATER().isBig.is(t)) {
					continue;
				}
				if (current != null && current != home) {
					Port p = ports.get(t);
					if (p != null) {
						connectPort(t, p);
						break;
					}
				}
			
				for (DIR d : DIR.ALL) {
					
					if (WTRAV.can(t.x(), t.y(), d, false)) {
						int dx = t.x() + d.x();
						int dy = t.y() + d.y();
						PReg to = get(dx, dy);
						if (current != null && current != home && current != to) {
							continue;
						}
						double v = u.cost(dx, dy) + WTRAV.cost(t.x(), t.y(), d);
						if (WTRAV.can(t.x(), t.y(), d, true))
							v*= 0.5;
						f.pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
					}
				}

			}
			f.done();
			
			
		}
		
	}

	private int nrOfPorts(PReg home) {
		if (home.reg.info.area() == 0)
			return 0;
		
		
		wCheck.init();
		int am = 0;
		for (COORDINATE c : home.reg.info.bounds()) {
			if (home.reg.is(c) && WTRAV.HARBOUR.isPossible(c.x(), c.y(), false) && !wCheck.isSetAndSet(u.wmap.get(c))) {
				am++;
			}
		}
//		if (home.reg.faction() == FACTIONS.player())
//			return CLAMP.i(am, 0, 1);
		return am;
	}
	
	private void createPorts(PReg home) {
		
		int am = nrOfPorts(home);
		if (am == 0)
			return;
		
		wCheck.init();
		
		while (createPort(home) && am > 0) {
			am--;
		}
		
	}
	
	private boolean createPort(PReg home) {
		

		
		Flooder f = RES.flooder();
		f.init(this);
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			int dx = home.reg.info.cx() + d.x();
			int dy = home.reg.info.cy() + d.y();
			if (WTRAV.isGoodLandTile(dx, dy) && WTRAV.canLand(home.reg.info.cx(), home.reg.info.cy(), d, false)) {
				f.pushSloppy(dx, dy, 0);
				f.setValue2(dx, dy, 0);
			}
		}
		
//		f.pushSloppy(home.reg.info.cx(), home.reg.info.cy(), 0);
//		f.setValue2(home.reg.info.cx(), home.reg.info.cy(), 0);
//		
		PathTile backup = null;
		
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			PReg current = get(t);
			if (current == null || current != home)
				continue;
			
			if (WORLD.WATER().isBig.is(t) && !wCheck.isSet(u.wmap.get(t))) {
				if (!WORLD.REGIONS().map.centre.is(t)) {
					wCheck.isSetAndSet(u.wmap.get(t));
					f.done();
					makePort(home, t);
					return true;
				}else {
					backup = t;
				}
			}
			
			if (WORLD.WATER().isBig.is(t))
				continue;
		
			for (DIR d : DIR.ALL) {
				
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					double v = u.cost(dx, dy) + WTRAV.cost(t.x(), t.y(), d);
					if (!WTRAV.can(t.x(), t.y(), d, true))
						v*= 1000;
					f.pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
				}
			}

		}
		f.done();
		if (backup != null) {
			wCheck.isSetAndSet(u.wmap.get(backup));
			makePort(home, backup);
			return true;
		}
		return false;
	}
	
	private void makePort(PReg home, PathTile tile) {
		PathTile t = tile;
		WTRAV.makeRoad(tile);
		//u.connectWay(tile);
		double cost = 0;
		while (t.getParent() != null) {
			cost += WPATHING.getTerrainCost(t.x(), t.y())*DIR.get(t, t.getParent()).tileDistance();
			t = t.getParent();
		}
		Port p = new Port(tile, home, cost);
		home.ports.add(p);
		allports.add(p);
		if (ports.get(p.coo) == null)
			throw new RuntimeException(""+WORLD.WATER().get(p.coo).name());
	}

	static final class PReg {
		
		public final Region reg;
		public final ArrayListGrower<Port> ports = new ArrayListGrower<>();
		
		PReg(Region reg){
			this.reg = reg;
		}
		
		
	}
	
	static final class Port {
		
		public int totalConnections;
		public PortGroup group;
		public final COORDINATE coo;
		public final double distToHome;
		public final PReg home;
		public final ArrayListGrower<PortDist> dists = new ArrayListGrower<>();
		
		Port(COORDINATE coo, PReg home, double dist){
			this.home = home;
			this.distToHome = dist;
			this.coo = new Coo(coo);
		}
		
		public void push(Port to, double dist) {
			for (PortDist d : dists)
				if (d.to == to)
					return;
			PortDist d = new PortDist(this, to, dist);
			dists.add(d);
		}
	
	}
	
	static final class PortDist {
		
		public final Port from;
		public final Port to;
		public final double dist;
		
		PortDist(Port from, Port to, double dist){
			this.from = from;
			this.to = to;
			this.dist = dist;
		}
		
	}

	@Override
	public PReg get(int tile) {
		Region r = WORLD.REGIONS().map.get(tile);
		if (r != null)
			return all[r.index()];
		return null;
	}

	@Override
	public PReg get(int tx, int ty) {
		Region r = WORLD.REGIONS().map.get(tx, ty);
		if (r != null)
			return all[r.index()];
		return null;
	}
	
	public final MAP_OBJECT<Port> ports = new MAP_OBJECT<WGenPorts.Port>() {

		@Override
		public Port get(int tile) {
			PReg r = WGenPorts.this.get(tile);
			if (r != null && WORLD.ROADS().HARBOUR.is(tile)) {
				for (Port p : r.ports) {
					if (p.coo.x() + p.coo.y()*WORLD.TWIDTH() == tile)
						return p;
				}
			}
			return null;
		}

		@Override
		public Port get(int tx, int ty) {
			PReg r = WGenPorts.this.get(tx, ty);
			if (r != null && WORLD.ROADS().HARBOUR.is(tx, ty)) {
				for (Port p : r.ports) {
					if (p.coo.isSameAs(tx, ty))
						return p;
				}
			}
			return null;
		}
		
	};
	
	public void connectPort(PathTile tile, Port from) {
		
		PathTile t = tile;
		u.connectWay(t);
		double cost = 0;
		while (t.getParent() != null) {
			cost += WPATHING.getTerrainCost(t.x(), t.y())*DIR.get(t, t.getParent()).tileDistance();
			t = t.getParent();
		}
		Port to = ports.get(t);
		
		for (PortDist d : to.dists) {
			if (d.to == from)
				return;
		}
			
		from.push(to, cost);
		to.push(from, cost);
		
	}
	
	public static final class PortGroup {
		
		final LinkedList<Port> ports = new LinkedList<>();
		final int id;
		final LinkedList<PortGroup> neighbours = new LinkedList<>();
		
		public PortGroup(int id){
			this.id = id;
		}
		
	}

}
