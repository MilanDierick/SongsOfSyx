package settlement.room.main.placement;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.VERSION;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import view.tool.PLACABLE;
import view.tool.PlacableMessages;

public class PLACEMENT {

	public final RoomPlacer placer;
	private final Instance instance;
	
	public final MAP_BOOLEAN embryo;
	
	public PLACEMENT(ROOMS m){
		
		instance = new Instance(m, factory);

		placer = new RoomPlacer(this, instance);
		embryo = instance;
	}
	
	public final RoomBlueprint factory = new RoomBlueprint() {
		
		
		
		@Override
		protected void update(float ds) {
			placer.update(ds);
		}
		
		@Override
		public SFinderRoomService service(int tx, int ty) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void save(FilePutter saveFile) {
			placer.saver.save(saveFile);
			
		}
		
		@Override
		protected void load(FileGetter saveFile) throws IOException {
			placer.saver.load(saveFile);
		}
		
		@Override
		protected void clear() {
			
			placer.saver.clear();
		}

		@Override
		public COLOR miniC(int tx, int ty) {
			return COLOR.BLUE50;
		}

		@Override
		public COLOR miniCPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
			return origional;
		}

		
	};
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(placer.structure.get().index());
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			if (VERSION.versionIsBefore(65, 28))
				;
			else
				placer.structure.set(SETT.TERRAIN().BUILDINGS.all().getC(file.i()));
			
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public boolean canReconstruct(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r.constructor() != null && r.constructor().usesArea())
			return true;
		return false;
	}
	
	public static CharSequence placable(int tx, int ty, RoomBlueprintImp blue, boolean buildOnWalls) {

		if (!SETT.IN_BOUNDS(tx, ty))
			return PlacableMessages.¤¤TERRAIN_BLOCK;
		if (ROOMS().placement.factory.is(tx, ty))
			return PLACABLE.E;
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		
		if (TERRAIN().get(tx, ty).clearing().isEasilyCleared())
			return null;
		
		if (TERRAIN().get(tx, ty).clearing().isStructure()) {
			if (!blue.constructor().removeTerrain(tx, ty))
				return null;
			if (!buildOnWalls && !TERRAIN().get(tx, ty).roofIs())
				return PlacableMessages.¤¤TERRAIN_BLOCK;
			if (blue.constructor().mustBeIndoors()) {
				if (TERRAIN().get(tx, ty).roofIs())
					return null;
				if ((TERRAIN().get(tx, ty).clearing().can() || TERRAIN().MOUNTAIN.is(tx, ty))  && buildOnWalls)
					return null;
				return PlacableMessages.¤¤TERRAIN_BLOCK;
			}else if (blue.constructor().mustBeOutdoors()){
				if (TERRAIN().get(tx, ty).clearing().can() && buildOnWalls)
					return null;
				return PlacableMessages.¤¤TERRAIN_BLOCK;
			}else {
				if (TERRAIN().get(tx, ty).roofIs())
					return null;
				if ((TERRAIN().get(tx, ty).clearing().can() || TERRAIN().MOUNTAIN.is(tx, ty))  && buildOnWalls)
					return null;
			}
			
		}else if (TERRAIN().get(tx, ty).clearing().can() && PATH().availability.get(tx, ty).player > 0)
			return null;
		return PlacableMessages.¤¤TERRAIN_BLOCK;
	}

	
}
