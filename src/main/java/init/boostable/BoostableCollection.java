package init.boostable;

import java.util.Set;

import init.D;
import init.biomes.CLIMATES;
import init.paths.PATHS;
import init.sprite.ICON;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.info.INFO;
import util.keymap.KEY_COLLECTION;

public class BoostableCollection implements KEY_COLLECTION<BOOSTABLE>{
	
	public final String key;
	final KeyMap<BOOSTABLE> map = new KeyMap<>();
	LIST<BOOSTABLE> all = new ArrayList<>(0);
	static Json json;
	static Json text;

	public final CharSequence name;
	
	private static CharSequence ¤¤Rooms = "¤Rooms";
	private static CharSequence ¤¤Desc = "¤Production speed of: {0}";
	static{
		D.ts(BoostableCollection.class);
	}
	
	BoostableCollection(String key, CharSequence name){
		this.key = key;
		json = new Json(PATHS.CONFIG().get("BOOSTABLE")).json(key);
		text = new Json(PATHS.TEXT().getFolder("config").get("BOOSTABLE")).json(key);
		this.name = name;
	}
	
	@Override
	public BOOSTABLE tryGet(String value) {
		return map.get(value);
	}
	
	@Override
	public String key() {
		return key;
	}
	
	@Override
	public LIST<BOOSTABLE> all() {
		return all;
	}
	
	@Override
	public Set<String> available() {
		return map.keys();
	}
	
	public static final class BRooms extends BoostableCollection {
		
		public final KeyMap<LinkedList<BOOSTABLERoom>> typemap = new KeyMap<LinkedList<BOOSTABLERoom>>();
		final KeyMap<LinkedList<BOOSTABLERoom>> room2Bonus = new KeyMap<LinkedList<BOOSTABLERoom>>();
		private final LinkedList<BOOSTABLERoom> dummy = new LinkedList<>();
		private LIST<BOOSTABLERoom> rooms = new ArrayList<>();
		
		BRooms(){
			super("ROOM", ¤¤Rooms);
		}
	

		public BOOSTABLERoom pushRoom(RoomBlueprintImp room, Json j, String type) {
			String desc = "" + new Str(¤¤Desc).insert(0, room.info.names);
			return pushRoom(room, j, type, room.info.names, desc);
		}
		
		public BOOSTABLERoom pushRoom(RoomBlueprintImp room, Json j, String type, CharSequence name, CharSequence desc) {
			BOOSTABLERoom b = new BOOSTABLERoom(room, j, name, desc);
			map.put(room.key, b);
			all = all.join(b);
			rooms = rooms.join(b);
			LinkedList<BOOSTABLERoom> rooms = new LinkedList<>();

			if (type != null) {
				if (typemap.get(type) == null) {
					typemap.put(type, new LinkedList<>());
				}
				typemap.get(type).add(b);
			}
			if (room2Bonus.get(room.key) == null) {
				room2Bonus.put(room.key, new LinkedList<>());
			}
			room2Bonus.get(room.key).add(b);
			rooms.add(b);
			if (j.has("BONUS") && j.json("BONUS").has(CLIMATES.MAP().key())) {
				CLIMATES.MAP().fill(b.climates, j.json("BONUS"), 0, 2000);
			}
			
			return b;
		}
		
		public LIST<BOOSTABLERoom> rooms(){
			return rooms;
		}
		
		public LIST<BOOSTABLERoom> boosts(RoomBlueprintImp room){
			LIST<BOOSTABLERoom> l = room2Bonus.get(room.key);
			if (l == null)
				return dummy;
			return l;
		}
		
		
	}
	
	static class Collection extends BoostableCollection{
		
		
		Collection(String key, CharSequence name){
			super(key, name);
			
		}
		
		BOOSTABLE pushMisc(ICON.SMALL icon, String key) {
			INFO info = new INFO(text.json(key));
			BOOSTABLE b = new BOOSTABLE(key, json.d(key), info.name, info.desc, icon);
			map.put(key, b);
			all = all.join(b);
			return b;
		}
		
	}
}