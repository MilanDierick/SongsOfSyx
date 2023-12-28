package settlement.room.service.module;

import init.need.NEED;
import settlement.stats.colls.StatsNeeds.StatNeed;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public final class RoomServiceGroup {

	private final ArrayListGrower<RoomServiceNeed> all = new ArrayListGrower<>();
	public final NEED need;

	RoomServiceGroup(NEED rate) {
		this.need = rate;
	}
	
	void attach(RoomServiceNeed s) {
		all.add(s);
	}
	
	public LIST<RoomServiceNeed> all(){
		return all;
	}
	
	public StatNeed stat() {
		return need.stat();
	}
}
