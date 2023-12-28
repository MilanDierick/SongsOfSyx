package settlement.main;

import java.io.IOException;
import java.util.Arrays;

import init.C;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.resources.Minable;
import init.resources.RESOURCES;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.pathing.WTRAV;
import world.map.terrain.WorldTerrainInfo;
import world.regions.centre.WCentre;

public final class CapitolArea{

	private static int TILES = WCentre.TILE_DIM;

	private final ArrayList<COORDINATE> tiles = new ArrayList<>(TILES*TILES);
	private int arrivalTile = -1;

	private final Rec worldPixels = new Rec(TILES*C.TILE_SIZE);
	private final Rec worldTiles = new Rec(TILES);
	
	private CLIMATE climate;
	public final WorldTerrainInfo info = new WorldTerrainInfo();
	private int[] minable = new int[RESOURCES.minables().all().size()];
	
	public boolean isBattle;
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			tiles.save(file);
			file.i(arrivalTile);
			worldPixels.save(file);
			worldTiles.save(file);
			file.i(climate.index());
			file.bool(isBattle);
			file.is(minable);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			tiles.load(file);
			arrivalTile = file.i();
			worldPixels.load(file);
			worldTiles.load(file);
			climate = CLIMATES.ALL().get(file.i());
			isBattle = file.bool();
			file.is(minable);
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	void init(int worldtileX1, int worldtileY1, boolean isBattle) {

		this.isBattle = isBattle;
		worldTiles.moveX1Y1(worldtileX1, worldtileY1);
		worldPixels.moveX1Y1(worldtileX1*C.TILE_SIZE, worldtileY1*C.TILE_SIZE);
		
		tiles.clear();
		Arrays.fill(minable, 0);
		info.initCity(worldtileX1, worldtileY1);
		
		for (int y = 0; y < TILES; y++) {
			for (int x = 0; x < TILES; x++) {
				int tx = x + worldtileX1;
				int ty = y + worldtileY1;
				int i = tiles.add(new ShortCoo(worldtileX1+x, worldtileY1+y));
				
				
				if (arrivalTile == -1 && WTRAV.isGoodLandTile(tx, ty)) {
					if (x == 0 || y == 0 || x == TILES-1 || y == TILES-1)
						arrivalTile = i;
				}
				if (WORLD.MINERALS().get(tx, ty) != null)
					minable[WORLD.MINERALS().get(tx, ty).index]++;
			}
			
		}
		
		if (arrivalTile == -1) {
			//you're on an island or such.
			arrivalTile = TILES*TILES/2;
		}
		
		climate = WORLD.CLIMATE().getter.get(worldTiles.cX(), worldTiles.cY());
	}
	
	CapitolArea() {
		
		
	}
	
	public float getWatertabe() {
		double table = 0;
		for (COORDINATE c : tiles) {
			table+= WORLD.GROUND().getter.get(c).fertility();
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
	
	public int minable(Minable m) {
		return minable[m.index()];
	}
	
}
