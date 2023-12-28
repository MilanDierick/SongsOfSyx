package world.map.terrain;

import static world.WORLD.*;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.misc.CLAMP;
import world.WORLD.WorldResource;

public final class WorldFertility extends WorldResource{

	private final byte[] m = new byte[TAREA()];
	private final double fullI = 1.0/255;
	
	public WorldFertility(){
		
	}

	@Override
	protected void save(FilePutter file) {
		file.bs(m);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		file.bs(m);
	}

	
	
	final MAP_DOUBLEE setter = new MAP_DOUBLEE() {
		
		@Override
		public double get(int tile) {
			return (m[tile]&0x0FF)*fullI;
		}

		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return 0;
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			m[tile] = (byte) (255*CLAMP.d(value, 0, 1));
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH(), value);
			}
			return this;
		}
	};
	public final MAP_DOUBLE map = setter;

	
}
