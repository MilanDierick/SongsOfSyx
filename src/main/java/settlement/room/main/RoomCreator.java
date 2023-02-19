package settlement.room.main;

import java.io.IOException;

import game.GameDisposable;
import settlement.room.main.util.RoomInitData;
import snake2d.util.sets.LinkedList;

public abstract class RoomCreator {

	static final LinkedList<RoomCreator> scriptRooms = new LinkedList<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				scriptRooms.clear();
			}
		};
	}
	
	public RoomCreator() {
		scriptRooms.add(this);
	}
	
	public abstract RoomBlueprint createBlueprint(RoomInitData init) throws IOException;
	
}
