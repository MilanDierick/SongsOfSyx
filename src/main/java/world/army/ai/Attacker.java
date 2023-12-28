package world.army.ai;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.*;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.RD;

final class Attacker {

	private final Bitmap1D check = new Bitmap1D(WREGIONS.MAX, false);

	public void attack(Faction f, ArrayList<WArmy> armies) {
		if (armies.size() == 0)
			return;
		
		check.clear();
		
		double enemyPower = 0;
		
		for (Faction e : FACTIONS.DIP().war.getEnemies(f)) {
			enemyPower += AD.power().get(e);
		}
		
		double power = 0;
		
		for (WArmy a : armies)
			power += AD.power().get(a);
		
		
		
		power -= enemyPower;
		
		if (War.logging) {
			War.log(f, ""+power);
		}
		
		while(power > 0 && armies.size() > 0) {
			WArmy a = armies.removeLast();
			power -= AD.power().get(a);
			attack(a);
		}
		
		while(armies.size() > 0) {
			WArmy a = armies.removeLast();
			guard(a);
		}
	}
	
	
	private void guard(WArmy a) {
		if (a.region() == a.faction().capitolRegion())
			return;
		if (a.state() == WArmyState.moving && WORLD.REGIONS().map.get(a.path().destX(), a.path().destY()) == a.faction().capitolRegion())
			return;
		COORDINATE c = WORLD.PATH().rnd(a.faction().capitolRegion());
		if (c != null)
			a.setDestination(c.x(), c.y());
		
	}


	private void attack(WArmy a) {
		if (a.region() != null && a.region().faction() == a.faction() && AD.supplies().health(a) < 1) {
			a.stop();
			if (War.logging) {
				War.log(a, "stop");
			}
			return;
		}
		
		LIST<RDist> ds = WORLD.PATH().tmpRegs.all(a.faction().capitolRegion(), WTREATY.NEIGHBOURS(a.faction().capitolRegion()), WRegSel.ENEMYFACTION(a.faction()));
		
		Region best = null;
		double bestValue = 0;
		double pow = AD.power().get(a);
		
		for (RDist d : ds) {
			
			double v = pow/(RD.MILITARY().power.getD(d.reg)+100);
			if (v > 1.0) {
				v/= d.distance;
				if (v > bestValue) {
					best = d.reg;
				}
			}
			
		}
		
		if (War.logging) {
			War.log(a, " " + best);
		}
		
		if (best != null) {
			
			if (a.state() != WArmyState.besieging || a.region() != best)
				a.besiege(best);
		}else {
			guard(a);
		}
	}


}
