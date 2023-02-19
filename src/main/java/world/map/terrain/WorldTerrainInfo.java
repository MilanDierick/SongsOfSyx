package world.map.terrain;

import java.io.IOException;
import java.util.Arrays;

import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import world.World;
import world.map.regions.CapitolPlacablity;

public final class WorldTerrainInfo implements SAVABLE{

	private double[] terrain = new double[TERRAINS.ALL().size()];
	private double fertility;
	public int tx;
	public int ty;
	
	public WorldTerrainInfo() {
		
	}
	
	@Override
	public void clear() {
		Arrays.fill(terrain, 0);
		fertility = 0;
	}
	
	public void initCity(int x1, int y1) {
		clear();
		
		for (int y = 0; y < CapitolPlacablity.TILE_DIM; y++) {
			for (int x = 0; x < CapitolPlacablity.TILE_DIM; x++) {
				int tx = x + x1;
				int ty = y + y1;
				add(tx, ty);
			}
		}
		tx = x1+1;
		ty = y1+1;
		double d = CapitolPlacablity.TILE_DIM*CapitolPlacablity.TILE_DIM;
		for (int i = 0; i < terrain.length; i++)
			terrain[i]/=d;
		fertility /= d;
	}
	
	public void add(int tx, int ty) {
		fertility += World.GROUND().getFertility(tx, ty);
		double f = 0;
		f += World.FOREST().add(this, tx, ty);
		f += World.MOUNTAIN().add(this, tx, ty);
		f += World.WATER().add(this, tx, ty);
		
		add(TERRAINS.NONE(), CLAMP.d(1.0-f, 0, 1));
	}
	
	public void add(TERRAIN t, double v) {
		terrain[t.index()] += v;
	}
	
	public double get(TERRAIN t) {
		return terrain[t.index()];
	}
	
	public double fertility() {
		return fertility;
	}
	
	public void addFertility(double v) {
		fertility += v;
	}

	@Override
	public void save(FilePutter file) {
		file.ds(terrain);
		file.d(fertility);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ds(terrain);
		fertility = file.d();
	}

	public void divide(double d) {
		for (int i = 0; i < terrain.length; i++)
			terrain[i]/=d;
		fertility/=d;
	}
	
}
