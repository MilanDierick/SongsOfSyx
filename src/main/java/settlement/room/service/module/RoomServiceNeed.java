package settlement.room.service.module;

import init.need.NEED;
import init.need.NEEDS;
import init.race.RACES;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.Json;

public abstract class RoomServiceNeed extends RoomServiceAccess {

	public final RoomServiceGroup group;
	public final NEED need;
	
	public RoomServiceNeed(RoomBlueprintIns<?> b, RoomInitData data) {
		super(b, data);
		Json json = data.data().json("SERVICE");
		need = NEEDS.MAP().get(json);
		group = data.service.add(need, this);
	}
	
	@Override
	public double totalMultiplier() {
		return 1.0/(group.need.rate.get(RACES.clP(null, null)));
	}

	public interface ROOM_SERVICE_NEED_HASER extends ROOM_SERVICE_ACCESS_HASER{
		
		@Override
		RoomServiceNeed service();

	}

}
