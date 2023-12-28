package world.map.buildings;

import static world.WORLD.*;

import java.io.IOException;

import game.GAME;
import game.Profiler;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.sets.*;
import util.rendering.RenderData.RenderIterator;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WRenContext;
import world.map.buildings.camp.WorldCamp;
import world.regions.Region;
import world.regions.data.RD;

public class WorldBuildings extends WorldResource{

	private final Bitsmap1D ids = new Bitsmap1D(-1, 4, TAREA());
	private final short[] data = new short[TAREA()];
	
	private final ArrayList<WorldBuilding> all = new ArrayList<>(16);
	
	public final WorldBuildingSimple nothing = new BuildingNothing(all);
	public final WorldCamp camp = new WorldCamp(all);
	final BuildingFarm farm;
	final BuildingVillage village;
	public final WorldBuildingSprites sprites = new WorldBuildingSprites();
	
	public WorldBuildings() throws IOException {
		
		farm = new BuildingFarm(all);
		village = new BuildingVillage(all);
	}
	
	public LIST<WorldBuilding> all(){
		return all;
	}
	
	@Override
	protected void save(FilePutter file) {
		ids.save(file);
		file.ss(data);
		for (WorldBuilding b : all)
			b.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		ids.load(file);
		file.ss(data);
		for (WorldBuilding b : all)
			b.load(file);
	}
	
	@Override
	protected void clear() {
		for (WorldBuilding b : all)
			b.clear();
		ids.clear();
	}
	

	public void renderAboveGround(WRenContext con, RenderIterator it){

		{
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing)	
				b.renderOnGround(con.r, it, data[it.tile()]);
		}
		
	}
	
	

	public void renderAbove(WRenContext con, RenderIterator it){
		{
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing) {
				b.renderAbove(con.r, con.s, it, this.data[it.tile()]);
				
			}
		}
		
		
	}
	
	public void renderAboveTerrain(WRenContext con, RenderIterator it){
		if (WORLD.MINERALS().get(it.tile()) != null){
			
			Region reg = WORLD.REGIONS().map.get(it.tile());
			
			if (reg != null && RD.BUILDINGS().levelMine.get(reg)*0x0F >= (it.ran()&0x0F)) {
				int i = it.ran()&0b1;
				i*= 4;
				
				int m = (GAME.intervals().get02() + (it.ran()>>1))&0b0111;
				if (m >= 4) {
					m -= 4;
					m = 3-m;
				}
				i += m;
				sprites.mines.render(con.r, i, it.x(), it.y());
			}
			
			

		}else {
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing)	
				b.renderAboveTerrain(con.r, con.s, it, this.data[it.tile()]);
		}
	}
	
	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(this);
		camp.update(ds);
		prof.logEnd(this);
	}

	
	final MAP_OBJECTE<WorldBuilding> map = new MAP_OBJECTE<WorldBuilding>() {

		@Override
		public WorldBuilding get(int tile) {
			return all.get(ids.get(tile));
		}

		@Override
		public WorldBuilding get(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return null;
			return get(tx+ty*TWIDTH());
		}

		@Override
		public void set(int tile, WorldBuilding object) {
			set(tile%WORLD.TWIDTH(), tile/WORLD.TWIDTH(), object);
		}

		@Override
		public void set(int tx, int ty, WorldBuilding object) {
			if (!IN_BOUNDS(tx, ty))
				return;
			
			if (object == null)
				object = nothing;
			if (get(tx, ty) != null)
				get(tx, ty).unplace(tx, ty);
			
			ids.set(tx+ty*TWIDTH(), object.index());
			
		}
		

	};
	
	public MAP_OBJECT<WorldBuilding> getter(){
		return map;
	}
	
//	public final MAP_BOOLEANE hiddenMap = new MAP_BOOLEANE() {
//		
//		@Override
//		public boolean is(int tx, int ty) {
//			return is(tx+ty*TWIDTH());
//		}
//		
//		@Override
//		public boolean is(int tile) {
//			return hidden.get(tile);
//		}
//
//		@Override
//		public MAP_BOOLEANE set(int tile, boolean value) {
//			hidden.set(tile, value);
//			return this;
//		}
//
//		@Override
//		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
//			return set(tx+ty*TWIDTH(), value);
//		}
//	};

//	public void adjustCentre(int nx, int ny, int ox, int oy) {
//		centre.remove(ox, oy);
//		centre.add(nx, ny);
//	}
	
	final MAP_INTE dataM = new MAP_INTE() {
		
		@Override
		public int get(int tx, int ty) {
			if (WORLD.IN_BOUNDS(tx, ty))
				return get(tx+ty*WORLD.TWIDTH());
			return 0;
		}
		
		@Override
		public int get(int tile) {
			return data[tile];
		}
		
		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return this;
			return set(tx+ty*WORLD.TWIDTH(), value);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			data[tile] = (short) value;
			return this;
		}
	};
	
	@Override
	protected void initBeforePlay() {
		for (WorldBuilding b : all)
			b.initBeforePlay();
	}

}
