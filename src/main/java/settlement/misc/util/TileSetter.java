package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface TileSetter<T> extends TileGetter<T>{

	/**
	 * 
	 * @param tile
	 * @return
	 */
	public void set(int tile, T value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public default void set(int tx, int ty, T value) {
		if (SETT.IN_BOUNDS(tx, ty))
			set(tx +ty*SETT.TWIDTH, value);
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default void set(int tx, int ty, DIR d, T value) {
		set(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default void set(COORDINATE c, T value) {
		set(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default void set(COORDINATE c, DIR d, T value) {
		set(c.x()+d.x(), c.y()+d.y(), value);
	}
	
	public default void setAll(T value) {
		for (int i = 0; i < SETT.TAREA; i++) {
			set(i, value);
		}
	}
	
	
}
