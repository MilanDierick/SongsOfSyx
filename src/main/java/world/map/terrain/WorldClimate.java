package world.map.terrain;

import static world.WORLD.*;

import java.io.IOException;

import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitsmap1D;
import world.WORLD.WorldResource;

public final class WorldClimate extends WorldResource{

	private final Bitsmap1D map = new Bitsmap1D(0, CLIMATES.ALL().size(), TAREA());
	private final Bitsmap1D offmap = new Bitsmap1D(0, 3, TAREA());
	
	public WorldClimate(){
		
	}
	
	final MAP_OBJECTE<CLIMATE> setter = new MAP_OBJECTE<CLIMATE>() {

		@Override
		public CLIMATE get(int tile) {
			return CLIMATES.ALL().get(map.get(tile));
		}

		@Override
		public CLIMATE get(int tx, int ty) {
			
			return get(tx+ty*TWIDTH());
		}

		@Override
		public void set(int tile, CLIMATE object) {
			map.set(tile, object.index());
		}

		@Override
		public void set(int tx, int ty, CLIMATE object) {
			if (IN_BOUNDS(tx, ty))
				set(tx+ty*TWIDTH(), object);
		}

		
	};
	
	final MAP_DOUBLEE offset = new MAP_DOUBLEE() {
		
		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return offmap.get(tile) -3;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty))
				return set(tx+ty*TWIDTH(), value);
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			int v = (int) (value*4);
			v = CLAMP.i(v, -3, 4);
			v += 3;
			offmap.set(tile, v);
			return this;
		}
	};
	
	
	public final MAP_OBJECT<CLIMATE> getter = setter;

	@Override
	protected void save(FilePutter saveFile) {
		map.save(saveFile);
		offmap.save(saveFile);
	}
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		map.load(saveFile);
		offmap.load(saveFile);
	}
	
}
