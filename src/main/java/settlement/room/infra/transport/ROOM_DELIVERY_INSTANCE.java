package settlement.room.infra.transport;

import init.resources.RBIT;
import settlement.misc.util.TILE_STORAGE;

public interface ROOM_DELIVERY_INSTANCE {

	public int deliverCapacity();
	public TILE_STORAGE getDeliveryCrate(RBIT okMask, int minAmount);
	
}
