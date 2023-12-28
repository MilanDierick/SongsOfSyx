package settlement.room.service.module;


import init.need.NEED;
import init.need.NEEDS;
import snake2d.util.sets.*;

public final class RoomServices {

	private final ArrayList<RoomServiceGroup> perNeed = new ArrayList<>(NEEDS.ALL().size());
	final ArrayListGrower<RoomServiceNeed> needs = new ArrayListGrower<RoomServiceNeed>();
	final ArrayListGrower<RoomServiceAccess> access = new ArrayListGrower<>();
	final ArrayListGrower<RoomService> all = new ArrayListGrower<>();

	
	public RoomServices() {
		for (NEED n : NEEDS.ALL())
			perNeed.add(new RoomServiceGroup(n));
	}
	

	
	RoomServiceGroup add(NEED need, RoomServiceNeed s) {
		perNeed.get(need.index()).attach(s);
		needs.add(s);
		return perNeed.get(need.index());
	}
	
	public RoomServiceGroup get(NEED n) {
		return perNeed.get(n.index());
	}
	
	public LIST<RoomServiceNeed> needs(){
		return needs;
	}
	
	public LIST<RoomServiceGroup> groups(){
		return perNeed;
	}
	
	public LIST<RoomServiceAccess> access(){
		return access;
	}
	
	public LIST<RoomService> all(){
		return all;
	}
	
}
