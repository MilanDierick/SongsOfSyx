package settlement.room.main;

import static settlement.main.SETT.*;

import java.io.IOException;

import settlement.main.SETT;
import settlement.room.main.util.RoomAreaWrapper;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.map.MAP_INT;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.IntegerStack;

public final class MapRoom implements MAP_OBJECT<Room>{

	
	public final static short NOTHING = 0;
	private final short[] roomI = new short[SETT.TAREA];

	private final IntegerStack stack = new IntegerStack(ROOMS.ROOM_MAX);
	private final Room[] rooms = new Room[ROOMS.ROOM_MAX+1];
	
	MapRoom(){
		for (int i = ROOMS.ROOM_MAX-1; i > 0; i--) {
			stack.push(i);
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.ss(roomI);
			stack.save(file);
			file.object(rooms);
			//save rooms!
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			file.ss(roomI);
			stack.load(file);
			Room[] roomsOld = (Room[]) file.object();
			for (int i = 0; i < ROOMS.ROOM_MAX; i++) {
				if (rooms[i] != null && roomsOld[i].singleton)
					continue;
				rooms[i] = roomsOld[i];
				
			}
		}
		
		@Override
		public void clear() {
			for (int i = 0; i < TAREA; i++) {
				roomI[i] = NOTHING;
			}
			for (int i = 0; i < ROOMS.ROOM_MAX; i++) {
				if (rooms[i] != null && !rooms[i].singleton) {
					stack.push(rooms[i].roomI);
					rooms[i] = null;
				}
				
			}
		}
	};
	
	Room getByIndex(int index) {
		return rooms[index];
	}
	
	public boolean thereCanBeMoreRooms() {
		return !stack.isEmpty();
	}
	
	int create(Room r) {
		if (stack.isEmpty())
			throw new RuntimeException();
		int i = stack.pop();
		if (rooms[i] != null)
			throw new RuntimeException();
		rooms[i] = r;
		return i;
	}
	
	final Room getRaw(int tx, int ty) {
		return rooms[roomI[tx+ty*TWIDTH]&0x0FFFF];	
	}
	
	void set(int tile, Room n) {
		if (get(tile) != null)
			throw new RuntimeException(tile + " " + n + " " + get(tile));
		SETT.ROOMS().fData.clean(tile);
		roomI[tile] = (short) n.roomI;
		SETT.ROOMS().pData.set(tile, 0);
	}
	
	void clear(int tile, Room old) {
		if (old != get(tile))
			throw new RuntimeException(tile + " " + old + " " + get(tile));
		SETT.ROOMS().fData.clean(tile);
		SETT.ROOMS().pData.set(tile, 0);
		roomI[tile] = NOTHING;
	}
	
	void replace(int tile, Room old, Room current) {
		if (old != get(tile))
			throw new RuntimeException((tile%TWIDTH) + " " + (tile/TWIDTH) + " " + get(tile) + " " + old + " " + current);
		roomI[tile] = (short) current.roomI;
	}
	
	TmpArea delete(Room room, int mx, int my, Object user) {
		
		TmpArea a = SETT.ROOMS().tmpArea(user);
		a.set(room, mx, my);
		if (!(room instanceof RoomSingleton) && room.blueprint() != SETT.ROOMS().THRONE) {
			if (rooms[room.roomI] == null)
				throw new RuntimeException();
			stack.push(room.roomI);
			rooms[room.roomI] = null;
		}
		
		init(a);

		return a;
		
	}
	
	public void init(AREA room) {
		tmp.set(room.body());
		for (COORDINATE c : tmp) {
			if (room.is(c)) {
				SETT.TILE_MAP().miniCUpdate(c.x(), c.y());
				PATH().availability.updateAvailability(c.x(), c.y());
				SETT.ENV().environment.setChanged(c.x(), c.y());
				PATH().availability.updateService(c.x(), c.y());
			}
		}
	}
	
	private final Rec tmp = new Rec();

	public int nrOFRooms() {
		return ROOMS.ROOM_MAX - stack.size();
	}
	
	@Override
	public Room get(int tile) {
		return rooms[roomI[tile]&0x0FFFF];
	}

	@Override
	public Room get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			return rooms[roomI[tx+ty*TWIDTH]&0x0FFFF];
		}
		return null;
	}
	
	public final MAP_OBJECT<RoomBlueprint> blueprint = new MAP_OBJECT<RoomBlueprint>() {

		@Override
		public RoomBlueprint get(int tile) {
			int i = roomI[tile];
			if (i != NOTHING) {
				Room r =  getByIndex(i);
				if (r != null)
					return r.blueprint();
			}
			return null;
		}

		@Override
		public RoomBlueprint get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx + ty * TWIDTH);
			return null;
		}

	};
	
	public final MAP_OBJECT<RoomBlueprintImp> blueprintImp = new MAP_OBJECT<RoomBlueprintImp>() {

		@Override
		public RoomBlueprintImp get(int tile) {
			int i = roomI[tile];
			if (i != NOTHING) {
				Room r =  getByIndex(i);
				if (r != null && r.constructor() != null)
					return r.constructor().blue();
			}
			return null;
		}

		@Override
		public RoomBlueprintImp get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx + ty * TWIDTH);
			return null;
		}

	};
	
	public final MAP_INT indexGetter = new MAP_INT() {

		@Override
		public int get(int tx, int ty) {
			return get(tx + ty * TWIDTH);
		}

		@Override
		public int get(int tile) {
			return roomI[tile];
		}
	};
	
	public final MAP_OBJECT<RoomInstance> instance = new MAP_OBJECT<RoomInstance>() {

		@Override
		public RoomInstance get(int tile) {
			Room r = MapRoom.this.get(tile);
			if (r instanceof RoomInstance)
				return (RoomInstance) r;
			return null;
		}

		@Override
		public RoomInstance get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx + ty * TWIDTH);
			return null;
		}

	};
	
	public final MAP_OBJECT<ROOMA> rooma = new MAP_OBJECT<ROOMA>() {

		private final RoomAreaWrapper wrap = new RoomAreaWrapper();
		
		@Override
		public ROOMA get(int tile) {
			wrap.done();
			Room r = MapRoom.this.get(tile);
			if (r != null)
				return wrap.init(r, tile%TWIDTH, tile/TWIDTH);
			return null;
		}

		@Override
		public ROOMA get(int tx, int ty) {
			wrap.done();
			if (IN_BOUNDS(tx, ty)) {
				Room r = MapRoom.this.get(tx, ty);
				if (r != null)
					return wrap.init(r, tx, ty);
			}
			return null;
		}

	};
	



}
