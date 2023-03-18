package world.map.landmark;

import static world.World.*;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.sets.*;
import world.World.WorldResource;

public final class WorldLandmarks extends WorldResource {

	final static int nothing = 0;
	final static int MAX = 256-1;
	private final ArrayList<WorldLandmark> areas = new ArrayList<>(MAX);
	private final Bitsmap1D mapID = new Bitsmap1D(0, Integer.numberOfTrailingZeros(MAX+1), TAREA());
	
	
	
	public WorldLandmarks() {
		for (int i = 0; i < MAX; i++)
			areas.add(new WorldLandmark(i));
	}

	public WorldLandmark getByIndex(int index) {
		return areas.get(index);
	}
	
	public LIST<WorldLandmark> all(){
		return areas;
	}
	
	public MAP_OBJECTE<WorldLandmark> setter = new MAP_OBJECTE<WorldLandmark>() {

		@Override
		public WorldLandmark get(int tile) {
			int i = mapID.get(tile);
			if (i == nothing)
				return null;
			return areas.get(i-1);
		}

		@Override
		public WorldLandmark get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return null;
		}

		@Override
		public void set(int tile, WorldLandmark object) {
			if (object == null)
				mapID.set(tile, nothing);
			else
				mapID.set(tile, object.index()+1);
		}

		@Override
		public void set(int tx, int ty, WorldLandmark object) {
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH(), object);
			}
		}
	};
	

	@Override
	protected void save(FilePutter file) {
		for (WorldLandmark a : areas)
			if (a != null)
				a.save(file);
		mapID.save(file);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		for (WorldLandmark a : areas)
			if (a != null)
				a.load(file);
		mapID.load(file);
	}
	
	@Override
	protected void clear() {
		mapID.setAll(0);
		for (WorldLandmark a : areas)
			if (a != null)
				a.clear();
		super.clear();
	}
	


	

	
}
