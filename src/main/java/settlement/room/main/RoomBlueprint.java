package settlement.room.main;

import static settlement.main.SETT.*;

import game.GameDisposable;
import init.C;
import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.ROOMS.RoomResource;
import settlement.thing.pointlight.LOS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public abstract class RoomBlueprint extends RoomResource implements MAP_OBJECT<Room>, INDEXED{

	private final int index;
	static ArrayListResize<RoomBlueprint> ALL = new ArrayListResize<>(10, 512);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				ALL.clear();
			}
		};
	}
	
	protected RoomBlueprint() {
		index = ALL.add(this);
	}
	

	
	@Override
	public Room get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH);
		return null;
	}

	@Override
	public Room get(int tile) {
		Room r = ROOMS().map.get(tile);
		if (r != null && r.blueprint() == this)
			return r;
		return null;
	}
	
	@Override
	public final int index() {
		return index;
	}

	public abstract SFinderFindable service(int tx, int ty);

	
	public abstract COLOR miniC(int tx, int ty);

	public abstract COLOR miniCPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern);
	
	public boolean makesDudesDirty() {
		return false;
	}

	public void appendView(LISTE<UIRoomModule> mm) {
		
	}
	
	public double strength(int tile) {
		return 400*C.TILE_SIZE;
	}

	public LOS LOS(int tx, int ty) {
		return SETT.TILE_MAP().LOS(tx, ty);
	}
	
	public RoomEmploymentSimple employment() {
		return null;
	}


	
}
