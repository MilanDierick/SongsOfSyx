package world.army;

import game.faction.Faction;
import util.data.INT_O.INT_OE;
import world.entity.army.WArmy;

public final class ADPower {

	private final INT_OE<WArmy> carmy;
	private final INT_OE<Faction> cfaction;
	
	private final INT_OE<WArmy> army;
	private final INT_OE<Faction> faction;
	
	ADPower(ADInit init){
		
		carmy = init.dataA.new DataByte();
		cfaction = init.dataT.new DataBit();
		
		army = init.dataA.new DataInt();
		faction = init.dataT.new DataInt();
		
	}
	
	public int get(WArmy a) {
		if (carmy.isMax(a)) {
			carmy.set(a, 0);
			army.set(a, (int) AD.UTIL().power.get(a));
			if (a.faction() != null)
				cfaction.set(a.faction(), 1);
		}
		carmy.inc(a, 1);
		return army.get(a);
	}
	
	public int get(Faction f) {
		if (cfaction.isMax(f)) {
			
			int p = 0;
			for (int ai = 0; ai < f.armies().all().size(); ai++) {
				WArmy a = f.armies().all().get(ai);
				p += get(a);
			}
			cfaction.set(f, 0);
			if (p < 0) {
				for (int ai = 0; ai < f.armies().all().size(); ai++) {
					WArmy a = f.armies().all().get(ai);
					System.err.println(get(a) + " " + f);
				}
				faction.set(f, 0);
			}else

			faction.set(f, p);
		}
		return faction.get(f);
	}
	
	void clearCache(WArmy a) {
		carmy.set(a, carmy.max(a));
		if (a.faction() != null)
			cfaction.set(a.faction(), 1);
	}
	
}
