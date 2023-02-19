package game.statistics;

import game.GAME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HCLASS;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomsJson;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;

final class RData {
	
	final int AMOUNT;
	final boolean isAllTime;
	final boolean isAllTimeHi;
	final boolean isInt;
	final boolean isAnti;
	final RESOURCE resource;
	final HCLASS clas;
	final Race race;
	final LIST<RoomBlueprintImp> rooms;
	
	RData(Json json){
		AMOUNT = json.i("AMOUNT");
		isInt = json.bool("IS_INT", false);
		isAnti = json.bool("IS_MAX", false);
		if (json.has("CLASS"))
			clas = HCLASS.MAP.getByKeyWarn(json.value("CLASS"), json);
		else
			clas = null;
		if (json.has("RACE"))
			race = RACES.map().getByKeyWarn(json.value("RACE"), json);
		else
			race = null;
		
		if (json.has("SPAN")) {
			switch(json.value("SPAN")) {
			case "GAME": isAllTime = false; isAllTimeHi = false; break;
			case "ALL_TIME": isAllTime = true; isAllTimeHi = false; break;
			case "ALL_TIME_HIGH": isAllTimeHi = true; isAllTime = false; break;
			default: isAllTime = false; isAllTimeHi = false; 
				GAME.Warn(json.errorGet("Available SPAN: GAME,ALL_TIME,ALL_TIME_HIGH ", "SPAN"));
				break;
			}
		}else {
			isAllTime = false;
			isAllTimeHi = false;
		}
		
		if (json.has("ROOM")) {
			rooms = RoomsJson.get(json.value("ROOM"), json);
		}else {
			rooms = null;
		}
		
		if (json.has("RESOURCE")) {
			resource = RESOURCES.map().getByKeyWarn(json.value("RESOURCE"), json);
		}else
			resource = null;
	}
	
}