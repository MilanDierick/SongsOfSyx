package settlement.room.main.util;

import settlement.room.main.RoomBlueprintImp;

public final class RoomInit {

	public final double[] statsAndRes;
	public final int degrade;
	
	public RoomInit(RoomBlueprintImp b, int degrade) {
		if (b.constructor() != null)
			statsAndRes = new double[b.constructor().stats().size()+b.constructor().resources()+1];
		else
			statsAndRes = new double[0];
		this.degrade = degrade;
	}
	
}
