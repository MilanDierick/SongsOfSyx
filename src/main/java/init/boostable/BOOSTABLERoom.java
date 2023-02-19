package init.boostable;

import settlement.room.main.RoomBlueprintImp;
import snake2d.util.file.Json;

public final class BOOSTABLERoom extends BOOSTABLE{
	
	public final RoomBlueprintImp room;
	public double defRace;
	
	BOOSTABLERoom(RoomBlueprintImp room, Json j, CharSequence name, CharSequence desc){
		super(room.key, 1, name, desc, room.iconBig().small);
		
		defRace = j.has("BONUS") ? j.json("BONUS").dTry("RACE", 0, 1, 1) : 1;
		this.room = room;
	}
	
}