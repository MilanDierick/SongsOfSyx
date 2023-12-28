package world.entity;

import init.C;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.*;
import world.WORLD;
import world.regions.Region;

public abstract class WEntityConstructor<T extends WEntity> {
	
	final int index;
	
	private final ArrayList<T> tmp = new ArrayList<>(64);
	
	protected WEntityConstructor(LISTE<WEntityConstructor<? extends WEntity>> all){
		index = all.add(this);
	}
	
	protected abstract T create();
	
	protected abstract void clear();
	
	@SuppressWarnings("unchecked")
	public void fill(LISTE<T> res, Region reg) {
		
		WEntity e = WORLD.ENTITIES().regFirst(reg);
		while(e != null && res.hasRoom()) {
			if (e.constructor() == this) {
				res.add((T) e);
			}
			e = e.regionNext;
		}
	}
	
	public LIST<T> fill(Region reg) {
		tmp.clearSloppy();
		fill(tmp, reg);
		return tmp;
	}
	
	public LIST<T> fillTile(int tx, int ty) {
		return fill(tx*C.TILE_SIZE, (tx+1)*C.TILE_SIZE, ty*C.TILE_SIZE, (ty+1)*C.TILE_SIZE);
	}
	
	public LIST<T> fillTiles(int tx1, int tx2, int ty1, int ty2) {
		return fill(tx1*C.TILE_SIZE, tx2*C.TILE_SIZE, ty1*C.TILE_SIZE, ty2*C.TILE_SIZE);
	}
	
	public LIST<T> fillTiles(RECTANGLE tiles) {
		return fill(tiles.x1()*C.TILE_SIZE, tiles.x2()*C.TILE_SIZE, tiles.y1()*C.TILE_SIZE, tiles.y2()*C.TILE_SIZE);
	}

	public LIST<T> fill(int x1, int x2, int y1, int y2) {
		tmp.clearSloppy();
		fill(tmp, x1, x2, y1, y2);
		return tmp;
	}
	
	@SuppressWarnings("unchecked")
	public LIST<T> fill(LISTE<T> tmp, int x1, int x2, int y1, int y2) {
		for (WEntity e : WORLD.ENTITIES().fill(x1, x2, y1, y2)) {
			if (e.constructor() == this)
				tmp.add((T)e);
		}
		return tmp;
	}
}
