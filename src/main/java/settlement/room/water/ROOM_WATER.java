package settlement.room.water;

import java.io.IOException;

import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.util.RoomInitData;
import snake2d.util.map.MAP_OBJECT;

public class ROOM_WATER {

	public final Pump pump;
	public final Canal canal;
	public final Drain drain;
	final WSprite sprite;
	final Updater updater = new Updater(this);
	
	public ROOM_WATER(RoomInitData init, RoomCategorySub cat) throws IOException{
		pump = new Pump(init, cat);
		canal = new Canal(init, cat);
		drain = new Drain(init, cat);
		sprite = new WSprite(this, init);
	}
	
	final MAP_OBJECT<Pumpable> pumpable = new MAP_OBJECT<Pumpable>() {

		@Override
		public Pumpable get(int tile) {
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null && r instanceof Pumpable)
				return (Pumpable) r;
			return null;
		}

		@Override
		public Pumpable get(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r instanceof Pumpable)
				return (Pumpable) r;
			return null;
		}
		
	};
	
}
