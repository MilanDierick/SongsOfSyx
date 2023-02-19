package settlement.main;

import static world.World.*;

import java.io.IOException;

import init.C;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.World;
import world.map.regions.CapitolPlacablity;
import world.map.terrain.WorldTerrainInfo;

public final class CapitolArea{

	private static int TILES = CapitolPlacablity.TILE_DIM;

	private final ArrayList<COORDINATE> tiles = new ArrayList<>(TILES*TILES);
	private int arrivalTile = -1;

	private final Rec worldPixels = new Rec(TILES*C.TILE_SIZE);
	private final Rec worldTiles = new Rec(TILES);
	
	private CLIMATE climate;
	public final WorldTerrainInfo info = new WorldTerrainInfo();
	private SGenerationConfig config;
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			tiles.save(file);
			file.i(arrivalTile);
			worldPixels.save(file);
			worldTiles.save(file);
			file.i(climate.index());
			if (config == null) {
				config = new SGenerationConfig();
				config.animals = true;
				config.minables = true;
			}
			config.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			tiles.load(file);
			arrivalTile = file.i();
			worldPixels.load(file);
			worldTiles.load(file);
			climate = CLIMATES.ALL().get(file.i());
			config = new SGenerationConfig();
			config.load(file);
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	void init(int worldtileX1, int worldtileY1, SGenerationConfig config) {
		this.config = config;
		worldTiles.moveX1Y1(worldtileX1, worldtileY1);
		worldPixels.moveX1Y1(worldtileX1*C.TILE_SIZE, worldtileY1*C.TILE_SIZE);
		
		tiles.clear();
		info.initCity(worldtileX1, worldtileY1);
		
		for (int y = 0; y < TILES; y++) {
			for (int x = 0; x < TILES; x++) {
				int tx = x + worldtileX1;
				int ty = y + worldtileY1;
				int i = tiles.add(new ShortCoo(worldtileX1+x, worldtileY1+y));
				
				
				if (arrivalTile == -1 && !WATER().has.is(tx, ty) && !MOUNTAIN().is(tx, ty)) {
					if (x == 0 || y == 0 || x == TILES-1 || y == TILES-1)
						arrivalTile = i;
				}
			}
		}
		
		if (arrivalTile == -1) {
			//you're on an island or such.
			arrivalTile = TILES*TILES/2;
		}
		
		climate = World.CLIMATE().getter.get(worldTiles.cX(), worldTiles.cY());
	}
	
	CapitolArea() {
		
		
	}
	
	public float getWatertabe() {
		double table = 0;
		for (COORDINATE c : tiles) {
			table+= World.GROUND().getter.get(c).fertility();
		}
		
		table /= (double) (TILES*TILES);
		
		table = 0.05 + table*0.1;
		return (float) table;
	}
	
	public final int arrivalTile() {
		return arrivalTile;
	}
	
	public final CLIMATE climate() {
		return climate;
	}
	
	public final RECTANGLE tiles() {
		return worldTiles;
	}
	
	public final LIST<COORDINATE> ts() {
		return tiles;
	}
	
	public final RECTANGLE pixels() {
		return worldPixels;
	}
	
	public SGenerationConfig config() {
		return config;
	}
	
}
