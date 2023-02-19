package settlement.room.infra.transport;

import settlement.misc.util.TILE_STORAGE;

public interface ROOM_DELIVERY_INSTANCE {

	public int deliverCapacity();
	public TILE_STORAGE getDeliveryCrate(long okMask, int minAmount);
	
}
