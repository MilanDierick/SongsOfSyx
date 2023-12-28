package settlement.room.spirit.temple;

import java.io.IOException;

import init.religion.Religion;
import init.religion.Religions;
import settlement.room.main.ROOMS;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.util.RoomInitData;
import settlement.room.main.util.RoomsCreator;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import snake2d.util.sets.*;

public final class ROOM_TEMPLES {

	public final LIST<ROOM_TEMPLE> ALL;
	
	public final LIST<ROOM_SHRINE> SHRINES;
	public final LIST<LIST<ROOM_TEMPLE>> perRel;
	public final LIST<LIST<ROOM_SHRINE>> perRelShrine;
	
	public ROOM_TEMPLES(ROOMS rooms, RoomInitData init) throws IOException{
		ALL = new RoomsCreator<ROOM_TEMPLE>(init, "TEMPLE",
				rooms.CATS.SER_REL) {

			@Override
			public ROOM_TEMPLE create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
				return new ROOM_TEMPLE(index, data, key, cat);
			}

		}.all();
		
		SHRINES = new RoomsCreator<ROOM_SHRINE>(init, "SHRINE",
				rooms.CATS.SER_REL) {

			@Override
			public ROOM_SHRINE create(String key, RoomInitData data, RoomCategorySub cat, int index) throws IOException {
				return new ROOM_SHRINE(key, index, init, cat);
			}

		}.all();
		{
			ArrayList<LIST<ROOM_TEMPLE>> tt = new ArrayList<>(Religions.ALL().size());
			
			for (Religion rel : Religions.ALL()) {
				ArrayListGrower<ROOM_TEMPLE> res = new ArrayListGrower<>();
				for (ROOM_TEMPLE t : ALL) {
					if (t.religion == rel)
						res.add(t);
				}
				tt.add(res);
			}
			this.perRel = tt;
		}
		{
			ArrayList<LIST<ROOM_SHRINE>> tt = new ArrayList<>(Religions.ALL().size());
			
			for (Religion rel : Religions.ALL()) {
				ArrayListGrower<ROOM_SHRINE> res = new ArrayListGrower<>();
				for (ROOM_SHRINE t : SHRINES) {
					if (t.religion == rel)
						res.add(t);
				}
				tt.add(res);
			}
			
			
			this.perRelShrine = tt;
		}
	}
	
	public LIST<ROOM_TEMPLE> temples(Religion rel){
		return perRel.get(rel.index());
	}
	
	public LIST<ROOM_SHRINE> shrines(Religion rel){
		return perRelShrine.get(rel.index());
	}
	
}
