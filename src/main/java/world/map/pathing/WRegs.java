package world.map.pathing;

import game.GAME;
import game.faction.Faction;
import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.sets.*;
import util.data.BOOLEANO;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;

public class WRegs{
	
	public static int WORLD_STATE = 0;
	private final RDist[] regs = new RDist[WREGIONS.MAX];
	private int upI = GAME.updateI();
	private int lastState = 0;
	private WTREATY lastTreaty = null;
	private BOOLEANO<Region> lastSelector = null;
	private int lx,ly;
	private final ArrayList<RDist> li = new ArrayList<WRegs.RDist>(WREGIONS.MAX);
	
	private static ArrayCooShort cootmps = new ArrayCooShort(128);
	
	public static final BOOLEANO<Region> s_all = new BOOLEANO<Region>() {

		@Override
		public boolean is(Region t) {
			return true;
		}
		
	};
	
	public WRegs(){
		for (int i = 0; i < regs.length; i++)
			regs[i] = new RDist();
	}
	
	public LIST<RDist> capitols(Faction f){
		return all(f.capitolRegion(), WTREATY.FACTIONS(f), WRegSel.CAPITOLS(f));
	}
	
	public LIST<RDist> all(Region home, WTREATY trav, WRegSel selector) {
		return all(home.cx(), home.cy(), trav, selector);
	}
	
	public LIST<RDist> all(int tx, int ty, WTREATY trav, WRegSel selector) {
		if (lastState == WORLD_STATE && lx == tx && ly == ty && lastTreaty == trav && lastSelector == selector && upI == GAME.updateI())
			return li;
		
		lastState = WORLD_STATE;
		lastTreaty = trav;
		lastSelector = selector;
		lx = tx;
		ly = ty;
		upI = GAME.updateI();
		
		
		li.clearSloppy();
		if (!pushInitial(tx, ty))
			return li;
		
		int id = 0;
		
		Flooder f = RES.flooder();
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			WComp c = WORLD.PATH().COMPS.get(t);
			boolean w = WORLD.WATER().isBig.is(t) || t.getValue2() == 1;
			Region r = WORLD.REGIONS().map.cTile.get(t);
			
			if (r != null && selector.is(r)) {
				RDist rr = regs[id++];
				rr.reg = r;
				rr.distance = (int) t.getValue();
				rr.water = w;
				li.add(rr);
			}
			
			for (int i = 0; i < c.edges(); i++) {
				WComp to = c.edge(i);
				if (trav.can(c.x, c.y, to.x, to.y, t.getValue() + c.dist(i))) {
					if (RES.flooder().pushSmaller(to.x, to.y, t.getValue() + c.dist(i)) != null) {
						f.setValue2(to.x, to.y, w ? 1 : 0);
					}
				}
			}
		}
		f.done();
		return li;
	}
	
	private boolean pushInitial(int tx, int ty) {
		
		if (!WORLD.PATH().route.is(tx, ty))
			return false;
		
		Flooder f = RES.flooder();
		f.init(this);
		
		WComp start = WORLD.PATH().COMPS.get(tx, ty);
		if (start != null) {
			f.pushSloppy(tx, ty, 0);
			return true;
		}
		
		cootmps.set(0);
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			WComp c = WORLD.PATH().COMPS.get(t);
			if (c != null) {
				t.setValue2(t.getValue());
				cootmps.get().set(t);
				cootmps.inc();
				continue;
			}
			WCompsPather.push(t, t.getValue(), WORLD.PATH().COMPS);
		}
		
		f.done();
		
		if (cootmps.getI() == 0)
			return false;
		
		f.init(this);
		while(cootmps.getI() > 0) {
			cootmps.dec();
			f.pushSloppy(cootmps.get(), f.getValue2(cootmps.get().x(), cootmps.get().y()));
		}
		return true;
		
		
	}
	
	public RDist single(Region home, WTREATY trav, WRegSel selector) {
		return single(home.cx(), home.cy(), trav, selector);
	}
	
	public RDist single(int tx, int ty, WTREATY trav, WRegSel selector) {
		
		if (!WORLD.PATH().route.is(tx, ty))
			return null;
		
		if (!pushInitial(tx, ty))
			return null;
		lastState = -1;
		
		Flooder f = RES.flooder();

		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			WComp c = WORLD.PATH().COMPS.get(t);
			boolean w = WORLD.WATER().isBig.is(t) || t.getValue2() == 1;
			Region r = WORLD.REGIONS().map.cTile.get(t);
			if (r != null && selector.is(r)) {
				RDist rr = regs[0];
				rr.reg = r;
				rr.distance = (int) t.getValue();
				rr.water = w;
				f.done();
				return rr;
			}
			
			for (int i = 0; i < c.edges(); i++) {
				WComp to = c.edge(i);
				if (trav.can(c.x, c.y, to.x, to.y, t.getValue() + c.dist(i))) {
					if (RES.flooder().pushSmaller(to.x, to.y, t.getValue() + c.dist(i)) != null) {
						f.setValue2(to.x, to.y, w ? 1 : 0);
					}
				}
			}
		}
		f.done();
		return null;
	}
	
	public static final class RDist {
		
		public Region reg;
		public int distance;
		public boolean water;
		
		private RDist() {
			
		}
		
	}

	
}
