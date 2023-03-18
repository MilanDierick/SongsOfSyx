package world.map.terrain;

import static world.World.*;

import java.io.IOException;

import init.resources.Minable;
import init.resources.RESOURCES;
import settlement.main.RenderData;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.sets.Bitsmap1D;
import world.World.WorldResource;

public class WorldMinerals extends WorldResource implements MAP_OBJECTE<Minable>{

	private final Bitsmap1D ids = new Bitsmap1D(-1, 5, TAREA());
	
	
	public WorldMinerals() throws IOException{

		
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		ids.load(f);
		
	}

	@Override
	protected void save(FilePutter f){
		ids.save(f);
	}
	
	@Override
	protected void clear() {
		ids.clear();
	}
	
	@Override
	protected void update(float ds) {
		
	}
	
	public void render(Renderer r, RenderData data, double season){
		//render the mine here
	}
	
	@Override
	public Minable get(int tx, int ty){
		if (!IN_BOUNDS(tx, ty))
			return null;
		return get(tx+ty*TWIDTH());
	}
	
	@Override
	public Minable get(int tile) {
		int i = ids.get(tile)-1;
		if (i >= 0)
			return RESOURCES.minables().getAt(i);
		return null;
	}

	@Override
	public void set(int tile, Minable object) {
		if (object == null)
			ids.set(tile, 0);
		else {
			ids.set(tile, object.index()+1);
		}
		
	}

	@Override
	public void set(int tx, int ty, Minable object) {
		if (IN_BOUNDS(tx, ty))
			set(tx+ty*TWIDTH(), object);
	}
	
}
