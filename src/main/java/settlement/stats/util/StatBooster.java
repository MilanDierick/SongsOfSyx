package settlement.stats.util;

import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.faction.npc.ruler.Royalty;
import init.race.POP_CL;
import init.race.Race;
import settlement.army.Div;
import settlement.stats.Induvidual;
import settlement.stats.stat.STAT;
import world.regions.Region;

public interface StatBooster extends BValue {

	
	@Override
	public default double vGet(Race race) {
		return 0;
	}
	
	@Override
	public default double vGet(Royalty roy) {
		return 0;
	}
	
	@Override
	public default double vGet(Faction f) {
		return 0;
	}
	
	@Override
	public default double vGet(Region reg) {
		return 0;
	}
	
	@Override
	public default double vGet(POP_CL reg) {
		return vGet(reg, 0);
	}
	
	@Override
	public default boolean has(Class<? extends BOOSTABLE_O> b) {
		return b == Induvidual.class || b == POP_CL.class || b == Div.class || b == NPCBonus.class;
	}

	public static class StatBoosterStat implements StatBooster{
		
		private final STAT stat;
		private final boolean npc;
		
		public StatBoosterStat(STAT stat, boolean hasNPC){
			this.stat = stat;
			this.npc = hasNPC;
		}
		
		@Override
		public double vGet(Div div) {
			return stat.div().getD(div);
		}
		
		@Override
		public double vGet(Induvidual indu) {
			return stat.indu().getD(indu);
		}

		@Override
		public double vGet(POP_CL reg) {
			return stat.data(reg.cl).getD(reg.race);
		}
		
		@Override
		public double vGet(POP_CL reg, int daysBack) {
			return stat.data(reg.cl).getD(reg.race, daysBack);
		}

		@Override
		public double vGet(NPCBonus bonus) {
			if (npc)
				return bonus.get(stat.index());
			return 0;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			if (!npc && b == NPCBonus.class)
				return false;
			return StatBooster.super.has(b);
		}
		
	}
	
	

}