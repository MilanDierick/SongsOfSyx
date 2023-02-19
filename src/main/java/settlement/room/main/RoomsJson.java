package settlement.room.main;

import game.GAME;
import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.keymap.KEY_COLLECTION;

public abstract class RoomsJson {

	public static final String KEY = "ROOMS";
	
	public RoomsJson(Json json){
		this(KEY, json);
	}
	
	public RoomsJson(String masterkey, Json json){
		if (!json.has(masterkey))
			return;
		json = json.json(masterkey);
		for (String key : json.keys()) {
	
			for (RoomBlueprintImp b : get(key, json)) {
				if (b != null)
					doWithTheJson(b, json, key);
			}
		}
	}
	
	public static LIST<RoomBlueprintImp> list(Json error){
		return list(KEY, error);
	}
	
	public static LIST<RoomBlueprintImp> list(String masterkey, Json json){
		LinkedList<RoomBlueprintImp> res = new LinkedList<>();
		if (!json.has(masterkey))
			return new ArrayList<RoomBlueprintImp>(res);
		
		
		for (String k : json.values(masterkey)) {
			for (RoomBlueprintImp b : get(k, json)) {
				
				if (!res.contains(b))
					res.add(b);
			}
				
		}
		return new ArrayList<RoomBlueprintImp>(res);
	}
	
	public static LIST<RoomBlueprintImp> get(String key, Json error){
		LinkedList<RoomBlueprintImp> res = new LinkedList<>();
		if (key.equals(KEY_COLLECTION.WILDCARD)) {
			return new ArrayList<RoomBlueprintImp>(SETT.ROOMS().imps());
		}
		
		if (key.indexOf('_') < 0) {
			res.add(getType(key, error));
		}else {
			RoomBlueprintImp b = getSingle(key, error);
			if (b != null)
				res.add(b);
		}
		return new ArrayList<RoomBlueprintImp>(res);
	}

	public static LIST<RoomBlueprintImp> getType(String key, Json error) {
		
		LIST<RoomBlueprintImp> r = ROOMS.lookup.cats.get(key);
		if (r == null && error != null) {
			String av = System.lineSeparator() + "available:" + System.lineSeparator();
			for (String k : ROOMS.lookup.cats.keysSorted()) {
				av += k + System.lineSeparator();
			}
			GAME.Warn(error.errorGet("No room type named: " + key + " available types are listed as folders in " + PATHS.INIT().getFolder("room").get() + av, key));
			return new ArrayList<RoomBlueprintImp>(0);
		}
		return r;
	}
	
	public static RoomBlueprintImp getSingle(String key, Json error) {
		
		RoomBlueprintImp res = ROOMS.lookup.look.get(key);
		
		if (res == null && error != null) {
			String av = System.lineSeparator() + "available:" + System.lineSeparator();
			for (String k : ROOMS.lookup.look.keysSorted()) {
				av += k + System.lineSeparator();
			}
			GAME.Warn(error.errorGet("No room with key " + key +  " exists" + av, key));
		}
		return res;
	}
	
	public abstract void doWithTheJson(RoomBlueprintImp room, Json j, String key);
	
	
}
