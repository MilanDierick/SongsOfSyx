package settlement.maintenance;

import java.io.IOException;

import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.path.AVAILABILITY;
import settlement.path.AvailabilityListener;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.Room;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class MAINTENANCE extends SettResource{
	
	final Data data = new Data();
	final MRoom room = new MRoom(data);
	final MFloor floor = new MFloor(data);
	public final MAP_BOOLEAN isser = data.setter;
	public final MAP_BOOLEANE disabled = data.disabled;
	public final PlacableMulti enablePlacer;

	public MAINTENANCE() {
		
		new AvailabilityListener() {
			
			@Override
			protected void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
				evaluate(tx, ty);
			}
		};
		
		IDebugPanelSett.add(new PlacableMulti("maintenance degrade") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				updateTileDay(tx, ty, tx+ty*SETT.TWIDTH);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		});
		
		IDebugPanelSett.add(new PlacableMulti("maintenance maintain") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				maintain(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		});
		enablePlacer = new PlacerDormant();
	}
	
	public SFinderFindable finder() {
		return data.finder;
	}
	
	public void evaluate(int tx, int ty) {
		if (data.setter.is(tx, ty)) {
			if (!maintainable(tx, ty))
				data.setter.clear(tx, ty);
				
		}
	}
	
	public boolean maintainable(int tx, int ty) {
		if (room.validate(tx, ty))
			return true;
		if (floor.validate(tx, ty))
			return true;
		return false;
	}
		
	@Override
	protected void save(FilePutter file) {
		data.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		data.load(file);
	}
	
	@Override
	protected void init(boolean loaded) {
		for (COORDINATE c : SETT.TILE_BOUNDS)
			evaluate(c.x(), c.y());
	}
	
	public void initRoomDegrade( Room room, int rx, int ry) {
		this.room.initRoom(room, rx, ry);
	}
	
	@Override
	protected void updateTileDay(int tx, int ty, int tile) {
		if (room.updateRoom(tile, tx, ty))
			return;
		floor.update(tx, ty, tile);
	}
	
	public void vandalise(short tx, short ty) {
		if (room.vandalise(tx, ty))
			return;
		floor.vandalise(tx, ty);
	}
	
	public void maintain(int tx, int ty) {
		if (SETT.ROOMS().map.is(tx, ty) && SETT.ROOMS().map.get(tx, ty).degrader(tx, ty) != null)
			room.maintain(tx, ty);
		else
			floor.maintain(tx, ty);
	}
	
	public RESOURCE resourceNeeded(int tx, int ty) {
		
		return data.resource.get(tx, ty);
		
//		if (!isser.is(tx, ty))
//			return null;
//		Room r = SETT.ROOMS().get(tx, ty);
//		if (r != null && r.degrader() != null) {
//			if (RND.oneIn(4)) {
//				return r.degrader().getRNDResource();
//			}
//		}else if (SETT.FLOOR().getter.is(tx, ty)) {
//			if (0.1 + 0.1*SETT.FLOOR().degrade.get(tx, ty)*SETT.FLOOR().getter.get(tx, ty).resAmount > RND.rFloat()*5)
//				return SETT.FLOOR().getter.get(tx, ty).resource;
//		}
//		
//		return null;
	}


	
	
}
