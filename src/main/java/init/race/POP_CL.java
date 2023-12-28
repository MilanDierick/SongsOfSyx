package init.race;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import settlement.entity.humanoid.HCLASS;

public final class POP_CL implements BOOSTABLE_O{

	public final int index;
	public final HCLASS cl;
	public final Race race;
	private final int fi;
	
	POP_CL(int index, HCLASS cl, Race race) {
		this.index = index;
		this.cl = cl;
		this.race = race;
		fi = -1;
	}
	
	POP_CL(int index, int fi) {
		this.index = index;
		this.cl = null;
		this.race = null;
		this.fi = fi;
	}

	public FactionNPC f() {
		if (fi == -1)
			return null;
		return ((FactionNPC)FACTIONS.getByIndex(fi));
	}
	
	@Override
	public double boostableValue(Boostable bo, BValue v) {
		if (fi != -1)
			return f().bonus.get(bo.index());
		return v.vGet(this);
	}
	
	@Override
	public String toString() {
		return "POP_CL : " + cl + " " + race;
	}
	
}