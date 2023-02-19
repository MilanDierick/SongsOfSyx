package game.faction.npc;

import game.faction.FBonus;
import init.boostable.BOOSTABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class Bonus implements FBonus{

	private static LIST<SIMPLE> dummy = new ArrayList<SIMPLE>(0);
	
	@Override
	public double add(BOOSTABLE b) {
		return 0;
	}

	@Override
	public double mul(BOOSTABLE b) {
		return 1;
	}

	@Override
	public double minAdd(BOOSTABLE b) {
		return 0;
	}

	@Override
	public double maxAdd(BOOSTABLE b) {
		return 0;
	}

	@Override
	public double minMul(BOOSTABLE b) {
		return 1;
	}

	@Override
	public double maxMul(BOOSTABLE b) {
		return 1;
	}

	@Override
	public LIST<SIMPLE> subs() {
		return dummy;
	}

}
