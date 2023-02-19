package world.map.regions;

import static world.World.*;

import init.C;
import snake2d.util.datatypes.Rec;
import view.tool.PlacableMessages;
import world.World;


public final class CapitolPlacablity{


	public static final int TILE_DIM = 3; 

	private final static Rec bounds = new Rec();
	
	public static CharSequence whole(int tileX1, int tileY1){
		
		Region r = null;
		
		if (b(tileX1+1, tileY1+1))
			return PlacableMessages.¤¤TERRAIN_BLOCK;
		
		boolean oneClear = false;
		
		for (int y = tileY1 ; y < tileY1 + TILE_DIM; y++){
			for (int x = tileX1 ; x < tileX1 + TILE_DIM; x++){
				if (tile(x, y) != null)
					return tile(x, y);
				oneClear |= !b(x, y);
				Region r2 = World.REGIONS().setter.get(x, y);
				if (r2 != null) {
					if (r == null)
						r = r2;
					if (r2 != r)
						return PlacableMessages.¤¤SAME_REGION;
				}
				
			
			}
		}
		
		if (r == null)
			return PlacableMessages.¤¤REGION;
		
		if (r.isWater())
			return PlacableMessages.¤¤TERRAIN_BLOCK;
		
		if (!oneClear)
			return PlacableMessages.¤¤ONE_CLEAR_TILE;
		return null;
	}
	
	public static CharSequence tile(int tx, int ty){
		
		if (tx < 1 || ty < 1 || tx >= TWIDTH()-1 || ty >= THEIGHT()-1)
			return PlacableMessages.¤¤IN_MAP;
		
		bounds.moveX1Y1(tx*C.TILE_SIZE, ty*C.TILE_SIZE);
		bounds.setDim(C.TILE_SIZE);
		
		return null;
	}
	
	public static boolean b(int tx, int ty) {
		if (WATER().has.is(tx, ty)) {
			if (WATER().RIVER.is(tx, ty))
				return false;
			return true;
		}
		
		if (World.camps().map.is(tx, ty))
			return true;
		
		return MOUNTAIN().heighter.get(tx, ty) > 0;
	}
	
}
