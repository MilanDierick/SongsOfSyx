package settlement.tilemap;

import settlement.job.Job;
import settlement.tilemap.Terrain.TerrainTile;

public interface TILE_FIXABLE {
	
	public Job fixJob(int tx, int ty);
	public TerrainTile getTerrain(int tx, int ty);
}