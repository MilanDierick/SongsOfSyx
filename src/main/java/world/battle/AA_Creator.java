package world.battle;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.regions.Region;
import world.regions.data.RD;

class AA_Creator {


	private final AB_Initer initer;
	private final SideUnitFactory f;
	
	public AA_Creator(SideUnitFactory f, PUnitFactory uf) {
		this.f = f;
		initer = new AB_Initer(uf);
	}
	
	public boolean battle(WArmy a) {
		
		if (a == null || AD.men(null).get(a) <= 0)
			return false;
		
		WArmy b = a.getHostileArmy();
		if (b == null || AD.men(null).get(b) <= 0)
			return false;
		
		f.clear();
		f.A.add(a, true);
		f.B.add(b, true);
		
		enemyGarrison(f.B, a);
		enemyGarrison(f.A, b);

		fillAllies(f.A, a.ctx(), a.cty(), f.B);
		fillAllies(f.B, a.ctx(), a.cty(), f.A);
		initer.handle(f.A, f.B);
		return true;
	}
//	
//	public void battle(WArmy a, WArmy b) {
//		
//		f.clear();
//		f.A.add(a, true);
//		f.B.add(b, true);
//		
//		enemyGarrison(f.B, a);
//		enemyGarrison(f.A, b);
//
//		fillAllies(f.A, a.ctx(), a.cty(), f.B);
//		fillAllies(f.B, a.ctx(), a.cty(), f.A);
//		initer.handle(f.A, f.B);
//	}
	

	
	public void siege(WArmy a, Region besiged, double besigedTimer) {
		if (a == null)
			return;
		
		f.clear();
		
		f.A.add(a, false);
		f.B.add(besiged, true);
		
		fillAllies(f.A, a.ctx(), a.cty(), f.B);
		fillAllies(f.B, a.ctx(), a.cty(), f.A);
		
		initer.handle(f.A, f.B, besiged, besigedTimer);
	}
	
	public boolean regAttack(Region reg, WArmy a) {
		
		if (a == null || AD.men(null).get(a) <= 0)
			return false;
		
		if (reg == null)
			return false;
		if (!FACTIONS.DIP().war.is(a.faction(), reg.faction()))
			return false;
		if( RD.MILITARY().garrison.get(reg) <= 0)
			return false;
		if (WORLD.BATTLES().besiged(reg) && !a.besieging(reg))
			return false;
		
		f.clear();
		f.A.add(reg, false);
		f.B.add(a, true);

		fillAllies(f.A, a.ctx(), a.cty(), f.B);
		
		fillAllies(f.B, a.ctx(), a.cty(), f.A, true);
		return initer.handle(f.A, f.B);
	}
	
	private static void enemyGarrison(Side enemy, WArmy a) {
		Region reg = WORLD.REGIONS().map.get(a.ctx(), a.cty());
		if (reg == null)
			return;
		if (!FACTIONS.DIP().war.is(reg.faction(), a.faction()))
			return;
		
		if (RD.MILITARY().garrison.get(reg) == 0 || RD.MILITARY().divisions(reg).size() == 0)
			return;
		if (WORLD.BATTLES().besiged(reg) && !a.besieging(reg))
			return;
		enemy.add(reg, false);
	}

	private void fillAllies(Side enemy, int cx, int cy, Side sideToFill) {
		fillAllies(enemy, cx, cy, sideToFill, false);
	}
	
	private void fillAllies(Side enemy, int cx, int cy, Side sideToFill, boolean itIsARegionAskingForHElp) {
		
		RES.flooder().init(AA_Creator.class);
		RES.flooder().pushSloppy(cx, cy, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (t.getValue() > WArmy.reinforceTiles)
				break;
			
			for (WArmy ar : WORLD.ENTITIES().armies.fillTile(t.x(), t.y())) {
				if (isEnemy(enemy, ar.faction())) {
					if (itIsARegionAskingForHElp) {
						if (ar.hasBeenAskedforRegionAssistance)
							continue;
						ar.hasBeenAskedforRegionAssistance = true;
					}
					sideToFill.add(ar, false);
				}
			}
			
			int dm = WORLD.PATH().dirMap().get(t);
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if ((dm & d.bit) != 0)
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		RES.flooder().done();
	}
	
	private boolean isEnemy(Side side, Faction test) {
		for (SideUnit u : side.us) {
			if (!FACTIONS.DIP().war.is(u.faction(), test))
				return false;
		}
		return true;
	}

	
}
