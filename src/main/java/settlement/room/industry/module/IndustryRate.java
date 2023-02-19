package settlement.room.industry.module;

import init.boostable.BOOSTABLE;
import settlement.room.industry.module.Industry.RoomBoost;
import snake2d.util.sets.LIST;

public interface IndustryRate {

	public LIST<RoomBoost> boosts();
	public BOOSTABLE bonus();
	
}
