package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface TileGetter<T> {

	/**
	 * 
	 * @param tile
	 * @return
	 */
	public default boolean is(int tile, T value) {
		return get(tile) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public default boolean is(int tx, int ty, T value) {
		return get(tx,ty) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default boolean is(int tx, int ty, DIR d, T value) {
		return get(tx, ty, d) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default boolean is(COORDINATE c, T value) {
		return get(c) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default boolean is(COORDINATE c, DIR d, T value) {
		return get(c,d) == value;
	}

	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public default boolean has(int tile) {
		return get(tile) != null;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public default boolean has(int tx, int ty) {
		return SETT.IN_BOUNDS(tx, ty) && has(tx+ty*SETT.TWIDTH);
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default boolean has(int tx, int ty, DIR d) {
		return has(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default boolean has(COORDINATE c) {
		return has(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default boolean has(COORDINATE c, DIR d) {
		return has(c.x()+d.x(), c.y()+d.y());
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public T get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public default T get(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return get(tx +ty*SETT.TWIDTH);
		return null;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default T get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default T get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default T get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
	
}
