package world.map.buildings;

import static world.World.*;

import java.io.IOException;

import game.GameConRandom;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.sets.*;
import util.rendering.ShadowBatch;
import world.World;
import world.World.WorldResource;
import world.map.buildings.camp.WorldCamp;

public class WorldBuildings extends WorldResource{

	private final Bitsmap1D ids = new Bitsmap1D(-1, 4, TAREA());
	private final short[] data = new short[TAREA()];
	
	private final ArrayList<WorldBuilding> all = new ArrayList<>(16);
	
	public final WorldBuildingSimple nothing = new BuildingNothing(all);
	public final WorldBuildingSimple mine = new BuildingMine(all);
	public final WorldCamp camp = new WorldCamp(all);
	final BuildingFarm farm;
	final BuildingVillage village;
	final UrbanCentre centre;
	public final WorldRoads roads = new WorldRoads();
	public final WorldBuildingSprites sprites = new WorldBuildingSprites();
	
	
	public WorldBuildings() throws IOException {
		
		PATH p = PATHS.SPRITE().getFolder("world").getFolder("buildings");
		farm = new BuildingFarm(p, all);
		village = new BuildingVillage(p, all);
		centre = new UrbanCentre();
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
	
	public void generate(GameConRandom r) {
		for (WorldBuilding b : all)
			b.clear();
		ids.clear();
		new Generator();
	}
	
	public void renderAboveGround(SPRITE_RENDERER r, ShadowBatch s, RenderData rdata){
		
		

		RenderIterator it = rdata.onScreenTiles();
		
		
		while (it.has()) {
			
			if (World.REGIONS().isCentre.is(it.tx(), it.ty()))
				centre.renderOnGround(r, s, it);
			
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing)	
				b.renderOnGround(r, it, data[it.tile()]);
			
			roads.render(r, s, it);
			
			
			it.next();
		}
		
	}
	
	

	public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData data){
		
		
		RenderIterator it = data.onScreenTiles(0,1,0,1);
		
		while(it.has()) {
			if (World.REGIONS().isCentre.is(it.tx(), it.ty()))
				centre.renderAbove(r, s, it);
			
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing) {
				b.renderAbove(r, s, it, this.data[it.tile()]);
				
			}
			it.next();
			
		}
		
	}
	
	public void renderAboveTerrain(SPRITE_RENDERER r, ShadowBatch s, RenderData data){

		RenderIterator it = data.onScreenTiles(0,0,0,0);
		
		while(it.has()) {
			if (World.REGIONS().isCentre.is(it.tx(), it.ty()))
				centre.renderAboveTerrain(r, s, it);
			WorldBuilding b = all.get(ids.get(it.tile()));
			if (b != nothing)	
				b.renderAboveTerrain(r, s, it, this.data[it.tile()]);
			it.next();
			
		}
		
	}
	
	@Override
	protected void update(float ds) {
		camp.update(ds);
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
			set(tile%World.TWIDTH(), tile/World.TWIDTH(), object);
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
			if (World.IN_BOUNDS(tx, ty))
				return get(tx+ty*World.TWIDTH());
			return 0;
		}
		
		@Override
		public int get(int tile) {
			return data[tile];
		}
		
		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			if (!World.IN_BOUNDS(tx, ty))
				return this;
			return set(tx+ty*World.TWIDTH(), value);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			data[tile] = (short) value;
			return this;
		}
	};

}
