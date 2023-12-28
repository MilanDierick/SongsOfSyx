package settlement.army;

import static settlement.main.SETT.*;

import init.C;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.AREA;
import snake2d.util.map.*;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TargetMap {
	
	TargetMap(){
		
		IDebugPanelSett.add(new PlacableMulti("break something") {
			
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				breakIt(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return attackable.is(tx, ty, SETT.ARMIES().player()) || attackable.is(tx, ty, SETT.ARMIES().enemy()) ? null : E;
			}
		});
		
	}
	
	public final MAP_OBJECT<Army> army = new MAP_OBJECT<Army>() {

		@Override
		public Army get(int tile) {
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null) {
				if (r instanceof ArtilleryInstance) {
					return ((ArtilleryInstance)r).army();
				}
				return SETT.ARMIES().player();
			}
			
			return SETT.PATH().availability.get(tile).player < 0 ? SETT.ARMIES().player() : null;
		}

		@Override
		public Army get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return null;
			return get(tx+ty*SETT.TWIDTH);
		}
	};
	
	public final MAP_OBJECT_ISSER<Army> attackable = new MAP_OBJECT_ISSER<Army>() {
		
		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*SETT.TWIDTH, value);
	
		}
		
		@Override
		public boolean is(int tile, Army value) {
			
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null) {
				if (r instanceof ArtilleryInstance) {
					return ((ArtilleryInstance)r).army() != value;
				}
				return SETT.ARMIES().player() != value;
			}
			
			if (value == SETT.ARMIES().player()) {
				if (SETT.TERRAIN().get(tile).clearing().isStructure())
					return false;
			}
			
			if (SETT.PATH().availability.get(tile).isSolid(value) && SETT.TERRAIN().get(tile).clearing().canDestroy(tile%SETT.TWIDTH, tile/SETT.TWIDTH))
				return true;
			
			return false;
			
		}
	};
	
	public MAP_DOUBLE strength = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty)) {
				return 0;
			}
			return get(tx+ty*SETT.TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			RoomBlueprint p = SETT.ROOMS().map.blueprint.get(tile);
			if (p != null) {
				return p.strength(tile);
			}
			return SETT.TERRAIN().get(tile).clearing().strength();
		}
	};
	
	public void breakIt(int x, int y) {
		Room r = ROOMS().map.get(x, y);
		if (r != null && r.destroyTileCan(x, y)) {
			THINGS().gore.debris((x<<C.T_SCROLL)+C.TILE_SIZEH, (y<<C.T_SCROLL) + C.TILE_SIZEH, 0, 0);
			r.destroyTile(x, y);
			return;
		}
		
		TerrainTile b = SETT.TERRAIN().get(x, y);
		if (b.clearing().canDestroy(x, y)) {
			THINGS().gore.debris((x<<C.T_SCROLL)+C.TILE_SIZEH, (y<<C.T_SCROLL) + C.TILE_SIZEH, 0, 0);
			b.clearing().destroy(x, y);
			return;
		}
	}
	
}
