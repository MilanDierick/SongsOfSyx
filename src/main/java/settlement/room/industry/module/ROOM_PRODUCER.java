package settlement.room.industry.module;

import game.faction.FACTIONS;
import settlement.entity.humanoid.Humanoid;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomInstance;

public interface ROOM_PRODUCER {
	
	long[] productionData();
	public Industry industry();
	public int industryI();
	public default void setIndustry(int i) {
		
	}
	
	public default void updateIndustryLocks() {
		Industry in = industry();
		if (!in.lockable.passes(FACTIONS.player())) {
			setIndustry(0);
		}
	}
	
	public default double productionRate(RoomInstance ins, Humanoid h, Industry in, IndustryResource oo) {
		return IndustryUtil.calcProductionRate(oo.rate, h, in, ins)*ins.employees().efficiency();
	}
}
