package game.battle;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import init.paths.PATHS;
import snake2d.Errors;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.LIST;
import world.World;
import world.army.WARMYD;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class Util {

	private Util() {
		
	}
	
	public static void save(String name) {
		if (PATHS.local().SAVE.exists(name))
			PATHS.local().SAVE.delete(name);
		try {
			RES.saver().save(name);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Errors.DataError(name, PATHS.local().SAVE.get(name));
		}
		
	}
	
	public static boolean isHostileTile(WArmy a, COORDINATE c) {
		return isHostileTile(a, c.x(), c.y());
		
	}
	
	public static boolean isHostileTile(WArmy a, int tx, int ty) {
		
		if (regionCanAttack(tx, ty, a.faction()))
			return true;
		
		LIST<WEntity> es = World.ENTITIES().fillTiles(
				tx-WArmy.reinforceTiles*2, 
				tx+WArmy.reinforceTiles*2, 
				ty-WArmy.reinforceTiles*2, 
				ty+WArmy.reinforceTiles*2);
		
		for (WEntity e : es) {
			if (e == a)
				continue;
			if (!(e instanceof WArmy))
				continue;
			
			WArmy a2 = (WArmy) e;
			if (enemy(a, a2)) {
				if (Math.abs(tx-e.ctx()) + Math.abs(ty-e.cty()) <= WArmy.reinforceTiles)
					return true;
			}
		}
		return false;
		
	}
	
	public static boolean ally(Faction a, Faction b) {
		return FACTIONS.rel().ally(a, b);
	}
	
	public static boolean enemy(Faction a, Faction b) {
		return FACTIONS.rel().enemy(a, b);
	}
	
	public static boolean ally(WArmy a, WArmy b) {
		if (WARMYD.men(null).get(a) == 0 || WARMYD.men(null).get(b) == 0)
			return false;
		return ally(a.faction(), b.faction());
	}
	
	public static boolean enemy(WArmy a, WArmy b) {
		if (WARMYD.men(null).get(a) == 0 || WARMYD.men(null).get(b) == 0)
			return false;
		return enemy(a.faction(), b.faction());
	}
	
	public static boolean reinforces(WArmy a, int tx, int ty) {
		return reinforces(a, tx, ty, WArmy.reinforceTiles);
	}
	
	public static boolean reinforces(WArmy a, int tx, int ty, int tiles) {
		return Math.abs(a.ctx()-tx) + Math.abs(a.cty()-ty) <= tiles;
	}
	
	public static boolean regionWillAttack(WArmy a) {
		return regionCanAttack(a) && (a.region() != null && a.region().faction() != FACTIONS.player()) && REGIOND.MILITARY().power.get(a.region())*0.75 > WARMYD.quality().get(a);
	}
	
	
	public static boolean regionCanAttack(WArmy a) {
		return regionCanAttack(a.ctx(), a.cty(), a.faction());
	}
	
	public static boolean regionCanAttack(int tx, int ty, Faction a) {
		
		Region r = World.REGIONS().getter.get(tx, ty);
		
		if (r == null)
			return false;
		if (REGIOND.MILITARY().power.get(r) == 0)
			return false;
		if (!Util.enemy(a, r.faction()))
			return false;
		if (COORDINATE.tileDistance(r.cx(), r.cy(), tx, ty) > Region.attackRange)
			return false;
		return true;
	}
	
}
