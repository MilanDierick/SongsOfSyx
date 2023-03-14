package world.map.regions;

import static world.World.*;

import view.tool.PlacableMessages;
import world.World;


public final class CapitolPlacablity{


	public static final int TILE_DIM = 3; 
	
	public static CharSequence terrainC(int tx, int ty) {
		return terrain(tx-TILE_DIM/2, ty-TILE_DIM/2);
	}
	
	public static CharSequence terrain(int tileX1, int tileY1) {
		
		if (tileX1 < 1 || tileY1 < 1 || tileX1+TILE_DIM >= TWIDTH()-1 || tileY1+TILE_DIM >= THEIGHT()-1)
			return PlacableMessages.¤¤IN_MAP;
		
		if (b(tileX1+1, tileY1+1))
			return PlacableMessages.¤¤TERRAIN_BLOCK;
		
		boolean oneClear = false;
		
		for (int y = tileY1 ; y < tileY1 + TILE_DIM; y++){
			for (int x = tileX1 ; x < tileX1 + TILE_DIM; x++){
				oneClear |= !b(x, y);
			}
		}
		
		if (!oneClear)
			return PlacableMessages.¤¤ONE_CLEAR_TILE;
		return null;
	}
	
	public static CharSequence region(int tileX1, int tileY1) {
		CharSequence t = terrain(tileX1, tileY1);
		if (t != null)
			return t;
		Region r = World.REGIONS().setter.get(tileX1, tileY1);
		if (r == null)
			return PlacableMessages.¤¤REGION;
		if (r.isWater())
			return PlacableMessages.¤¤TERRAIN_BLOCK;

		for (int y = tileY1-1 ; y < tileY1 + TILE_DIM+1; y++){
			for (int x = tileX1-1 ; x < tileX1 + TILE_DIM+1; x++){
				if (r != World.REGIONS().setter.get(x, y)) {
					return PlacableMessages.¤¤SAME_REGION;
				}	
			}
		}
		return null;
	}
	
	public static boolean b(int tx, int ty) {
		if (WATER().has.is(tx, ty)) {
			if (WATER().RIVER.is(tx, ty) || WATER().RIVER_SMALL.is(tx, ty))
				return false;
			return true;
		}
		
		if (World.camps().map.is(tx, ty))
			return true;
		
		return MOUNTAIN().heighter.get(tx, ty) > 0;
	}
	
	
}
