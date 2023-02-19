package settlement.room.main.copy;

import init.paths.PATHS;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.placement.UtilWallPlacability;
import settlement.tilemap.TBuilding;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.sets.*;

public final class SavedPrints {

	private final KeyMap<Integer> versions = new KeyMap<>();
	private final ArrayList<SavedPrint> all = new ArrayList<>(256);
	
	SavedPrints(ROOMS rooms){
		
		for (RoomBlueprintImp p : rooms.imps()) {
			versions.put(p.key, 4);
			
		}
		versions.put(rooms.HUNTER.key, 4);

		
		try {
			Json[] jsons = new Json(PATHS.local().PROFILE.get("SavedPrints")).jsons("BLUEPRINTS");
			for (Json j : jsons) {
				if (!all.hasRoom())
					break;
				SavedPrint p = new SavedPrint(rooms, j);
				if (p.blue != null && versions.get(p.blue.key) != null && p.version >= versions.get(p.blue.key))
					add(p);
			}
			
		}catch(Exception e) {
			e.printStackTrace(System.out);
			all.clear();
			save();
		}
		
	}
	
	private void save() {
		try {
			
			JsonE[] jsons = new JsonE[all.size()];
			for (int i = 0; i < jsons.length; i++)
				jsons[i] = all.get(i).save();
			
			JsonE json = new JsonE();
			json.add("BLUEPRINTS", jsons);
			
			if (!PATHS.local().PROFILE.exists("SavedPrints"))
				PATHS.local().PROFILE.create("SavedPrints");
			
			json.save(PATHS.local().PROFILE.get("SavedPrints"));
			
		}catch(Exception e) {
			e.printStackTrace(System.out);
			all.clear();
			save();
		}
	}
	
	public void push(RoomInstance ins) {
		if (ins.blueprintI().constructor() != null) {
			add(new SavedPrint(versions, ins));
			save();
		}
	}
	
	public void remove(SavedPrint print) {
		if (all.removeOrdered(print) != -1) {
			save();
		}
	}
	
	public void rename(SavedPrint print, CharSequence newName) {
		if (all.removeOrdered(print) != -1) {
			add(new SavedPrint(""+newName, print));
			save();
		}
	}
	
	private void add(SavedPrint p) {
		for (int i = 0; i < all.max(); i++) {
			if (all.get(i) == null || all.get(i).blue.key.compareTo(p.blue.key) > 0) {
				all.insert(i, p);
				
				return;
			}
		}
	}
	
	public boolean canAdd(RoomInstance ins) {
		if (!all.hasRoom())
			return false;
		if (ins.blueprintI().constructor() != null)
			return false;
		if (!ins.blueprintI().constructor().canBeCopied())
			return false;
		return true;
	}
	
	public LIST<SavedPrint> all(){
		return all;
	}
	
	public static class SavedPrint {
		
		public final String name;
		public final RoomBlueprintImp blue;
		public final int version;
		public final int width,height;
		public final TBuilding structure;
		private final int[] data;
		
		SavedPrint(ROOMS rr, Json json){
			
			name = json.text("NAME");
			blue = RoomsJson.getSingle(json.value("ROOM"), null);
			version = json.i("VERSION");
			width = json.i("WIDTH");
			height = json.i("HEIGHT");
			data = json.is("DATA");
			structure = SETT.TERRAIN().BUILDINGS.getByKey("STRUCTURE", json);
		}
		
		JsonE save() {
			JsonE j = new JsonE();
			j.addString("NAME", name);
			j.add("ROOM", blue.key);
			j.add("STRUCTURE", structure.key);
			j.add("VERSION", version);
			j.add("WIDTH", width);
			j.add("HEIGHT", height);
			j.add("DATA", data);
			
			return j;
		}
		
		SavedPrint(String name, SavedPrint other){
			
			this.name = name;
			blue = other.blue;
			version = other.version;
			width = other.width;
			height = other.height;
			data = other.data;
			structure = other.structure;
		}
		
		SavedPrint(KeyMap<Integer> versions, RoomInstance ins){
			name = ""+ins.name();
			blue = ins.blueprintI();
			version = versions.get(blue.key) == null ? 0 : versions.get(blue.key);
			width = ins.body().width()+2;
			height = ins.body().height()+2;
			data = new int[width*height];
			structure = ConstructionInit.findStructure(ins.mX(), ins.mY());
			
			for (int dy = 0; dy < height; dy++)
				for (int dx = 0; dx < width; dx++) {
					int di = dx + dy*width;
					int x = dx + ins.body().x1()-1;
					int y = dy + ins.body().y1()-1;
					if (ins.is(x, y)) {
						this.data[di] |= 0b1;
						if (SETT.TERRAIN().get(x, y).roofIs())
							this.data[di] |= 0b100;
						if (SETT.PATH().availability.get(x, y) != AVAILABILITY.ROOM) {
							this.data[di] |= 0b01000;
						}
						if (SETT.ROOMS().fData.isMaster.is(x, y)) {
							FurnisherItem it = SETT.ROOMS().fData.item.get(x, y);
							this.data[(dx) + (dy)*width] |= (it.index()+1)<<4;
						}
						
					}else {
						boolean is = false;
						for (DIR d : DIR.ALL) {
							if (ins.is(x, y, d)) {
								is = true;
								break;
							}
						}
						if (is) {
							if (UtilWallPlacability.openingIsReal.is(x, y))
								this.data[di] |= 0b100;
							else if (UtilWallPlacability.wallisReal.is(x, y))
								this.data[di] |= 0b010;
						}
					}
					
				}
		}
		
		public boolean isRoom(int rx, int ry) {
			return (data[rx+ry*width] & 1) != 0;
		}
		
		public boolean isWall(int rx, int ry) {
			return (data[rx+ry*width] & 0b10) != 0;
		}
		
		public boolean isRoof(int rx, int ry) {
			return (data[rx+ry*width] & 0b100) != 0;
		}
		
		public boolean isSoldid(int rx, int ry) {
			return (data[rx+ry*width] & 0b1000) != 0;
		}
		
		public FurnisherItem item(int rx, int ry, RoomBlueprintImp blue) {
			int i = (data[rx+ry*width] & 0x0FFFF0) >> 4;
			if (i > 0) { 
				return blue.constructor().item(i-1);
			}
			return null;
		}
		
	}
	
}
