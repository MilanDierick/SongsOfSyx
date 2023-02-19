package settlement.room.service.module;

import settlement.entity.humanoid.Humanoid;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.STATS;
import settlement.stats.StatsService.StatService;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;

public abstract class RoomServiceDataAccess extends RoomServiceDataSimple implements INDEXED{

	private final int index;
	public final StandingDef standingDef;
	public boolean usesAccess = true;

	public RoomServiceDataAccess(RoomBlueprintIns<?> b, RoomInitData data) {
		super(b, data);
		
		Json json = data.data().json("SERVICE");
		standingDef = new StandingDef(json.json("STANDING"));
		index = data.addService(this);
		
		
	}

	
	public final void reportAccess(Humanoid a, COORDINATE c) {
		reportAccess(a, c.x(), c.y());
	}
	
	public final void reportContent(Humanoid a, ROOM_SERVICER service) {
		stats().setAccess(a, true, service.quality(), 1);
	}
	
	public final void reportAccess(Humanoid a, int tx, int ty) {
		ROOM_SERVICER r = (ROOM_SERVICER) room.get(tx, ty);
		if (r == null)
			return;
		stats().setAccess(a, true, r.quality(), stats().proximity(a));
	}
	
	public final void reportDistance(Humanoid a) {
		stats().setProximity(a, CLAMP.d(1.0-(finder.getDistance()-radius/2.0)/radius, 0, 1));
	}

	public final void clearAccess(Humanoid a) {
		stats().setAccess(a, false, 0, 0);
	}
	
	public final StatService stats() {
		return STATS.SERVICE().get(this);
	}
	
	@Override
	public final int index() {
		return index;
	}
	
	public interface ROOM_SERVICE_ACCESS_HASER extends ROOM_SERVICE_HASER{
		
		@Override
		RoomServiceDataAccess service();
		
	}

	public boolean accessRequest(Humanoid a) {
		return stats().accessRequest(a);
	}
}
