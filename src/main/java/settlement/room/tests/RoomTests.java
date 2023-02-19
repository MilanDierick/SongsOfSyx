package settlement.room.tests;

import settlement.room.main.ROOMS;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import view.sett.IDebugPanelSett;

public final class RoomTests {

	public RoomTests(ROOMS r) {
		for (ROOM_ARTILLERY a : r.ARTILLERY) {
			IDebugPanelSett.add(new ArtilleryTest(a.eplacer));
		}
	}
	
}
