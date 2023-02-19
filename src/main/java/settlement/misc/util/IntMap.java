package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_INTE;

public abstract class IntMap implements MAP_INTE {
	
	@Override
	public abstract MAP_INTE set(int tile, int value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	@Override
	public MAP_INTE set(int tx, int ty, int value) {
		if (SETT.IN_BOUNDS(tx, ty))
			set(tx +ty*SETT.TWIDTH, value);
		return this;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	@Override
	public MAP_INTE set(int tx, int ty, DIR d, int value) {
		set(tx+d.x(), ty+d.y(), value);
		return this;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	@Override
	public MAP_INTE set(COORDINATE c, int value) {
		set(c.x(), c.y(), value);
		return this;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	@Override
	public MAP_INTE set(COORDINATE c, DIR d, int value) {
		set(c.x()+d.x(), c.y()+d.y(), value);
		return this;
	}
	
	public void setAll(int value) {
		for (int i = 0; i < SETT.TAREA; i++) {
			set(i, value);
		}
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	@Override
	public boolean is(int tile, int value) {
		return get(tile) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	@Override
	public boolean is(int tx, int ty, int value) {
		return get(tx, ty) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	@Override
	public boolean is(int tx, int ty, DIR d, int value) {
		return get(tx, ty, d) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	@Override
	public boolean is(COORDINATE c, int value) {
		return get(c) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	@Override
	public boolean is(COORDINATE c, DIR d, int value) {
		return get(c, d) == value;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	@Override
	public abstract int get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	@Override
	public int get(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return get(tx +ty*SETT.TWIDTH);
		return Integer.MAX_VALUE;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	@Override
	public int get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	@Override
	public int get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	@Override
	public int get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
}
