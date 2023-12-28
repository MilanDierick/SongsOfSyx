package world.battle;

import game.faction.FACTIONS;
import init.RES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.regions.Region;

class AC_URetreat {

	public void retreat(Side side) {
		
		
		
		for (SideUnit u : side.us) {
			if (u.a() != null) {
				retreat(u.a());
			}
		}
		
	}
	
	private void retreat(WArmy a) {
		if (!a.added() || AD.men(null).get(a) == 0)
			return;
		a.stop();
		
		COORDINATE c = retreatTile(a);
		
		a.teleport(c.x(), c.y());
	}
	
	
	public boolean canRetreat(Side side) {
		for (SideUnit u : side.us)
			if (u.a() != null) {
				COORDINATE c = retreatTile(u.a());
				if (c == null)
					return false;
				if (u.a().getHostileArmy() != null) {
					
					
					if (c == null || u.a().getHostileArmy(c.x(), c.y()) != null)
						return false;
				}else if (retreatValue(u.a(), c) != 0){
					return false;
				}
			}
		return true;
	}
	
	private COORDINATE retreatTile(WArmy a) {
		
		double bestV = Double.MAX_VALUE;
		final int maxTiles = 5;
		Flooder f = RES.flooder();
		f.init(f);
		COORDINATE best = f.pushSloppy(a.ctx(), a.cty(), 0);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue() > maxTiles)
				break;
			
			double vv = retreatValue(a, t) + t.getValue();
		
			if (vv < bestV) {
				best = t;
				bestV = vv;
			}
			
			int mr = WORLD.PATH().dirMap().get(t);
			for (DIR d : DIR.ALL) {
				if ((d.bit & mr) != 0 && canRetreatTile(a, t.x(), t.y(), t.x()+d.x(), t.y()+d.y())) {
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
				
			}
			
		}
		f.done();
		return best;
		
	}
	
	private boolean canRetreatTile(WArmy a, int fx, int fy, int tx, int ty) {
		Region reg = WORLD.REGIONS().map.get(fx, fy);
		if (reg == null || (reg.cx() == tx && reg.cy() == ty))
			return true;
		
		reg = WORLD.REGIONS().map.get(tx, ty);
		if (reg == null)
			return true;
		
		if (FACTIONS.DIP().war.is(a.faction(), reg.faction())) {
			return !WORLD.REGIONS().map.centre.is(tx, ty);
		}
		return true;
	}
	
	private double retreatValue(WArmy a, COORDINATE coo) {
		
		if (a.getHostileArmy(coo.x(), coo.y()) != null)
			return 10000;
		
		Region reg = WORLD.REGIONS().map.get(coo.x(), coo.y());
		
		if (reg == null)
			return 100;
		
		if (FACTIONS.DIP().war.is(reg.faction(), a.faction()))
			return 1000-10*COORDINATE.tileDistance(coo.x(), coo.y(), reg.cx(), reg.cy());
		
		if (reg.faction() != a.faction())
			return 100;
		
		return 0;
		
	}
	
}
