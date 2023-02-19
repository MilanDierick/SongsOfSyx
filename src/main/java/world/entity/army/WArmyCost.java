package world.entity.army;

import world.entity.WPathing.WorldPathCost;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class WArmyCost implements WorldPathCost{

	private WArmy costEntity;
	private static final WArmyCost cost = new WArmyCost();
	
	@Override
	public boolean canMove(Region a, Region b) {
		if (b == null || a == null)
			return false;
		
		if (b.faction() == null)
			return true;
		
		if (REGIOND.OWNER().rebel.is(b))
			return true;
		
		if (costEntity.faction() == null)
			return true;
		
		if (costEntity.faction() == b.faction())
			return true;
		
		return true;
		
//		if (a.faction() == null) {
//			if (FACTIONS.rel().war.get(costEntity.faction(), b.faction()) == 0)
//				return false;
//			return true;
//		}else {
//			if (FACTIONS.rel().war.get(costEntity.faction(), b.faction()) == 0)
//				return true;
//			return false;
//		}
	}
	
	private WArmyCost() {
		
	}
	
	static WorldPathCost get(WArmy e) {
		cost.costEntity = e;
		return cost;
	}
	
}
