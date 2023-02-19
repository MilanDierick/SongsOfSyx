package settlement.room.industry.module;

import game.faction.FACTIONS;

public interface ROOM_PRODUCER {
	
	long[] productionData();
	public Industry industry();
	public int industryI();
	public default void setIndustry(int i) {
		
	}
	
	public default void updateIndustryLocks() {
		Industry in = industry();
		if (FACTIONS.player().locks.unlockText(in) != null) {
			setIndustry(0);
		}
	}
}
