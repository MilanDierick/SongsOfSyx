package settlement.room.industry.module;

import game.boosting.Boostable;
import settlement.room.industry.module.Industry.RoomBoost;
import snake2d.util.sets.LIST;

public interface IndustryRate {

	public LIST<RoomBoost> boosts();
	public Boostable bonus();
	
}
