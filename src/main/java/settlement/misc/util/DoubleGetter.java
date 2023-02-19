package settlement.misc.util;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract class DoubleGetter {
	
	private final int w;
	private final int h;
	
	public DoubleGetter(int width, int height) {
		this.w = width;
		this.h= height;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public boolean is(int tile, double value) {
		return get(tile) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public boolean is(int tx, int ty, double value) {
		return get(tx, ty) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public boolean is(int tx, int ty, DIR d, double value) {
		return get(tx, ty, d) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public boolean is(COORDINATE c, double value) {
		return get(c) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public boolean is(COORDINATE c, DIR d, double value) {
		return get(c, d) == value;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public abstract double get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public double get(int tx, int ty) {
		if (tx < 0 || tx >= w || ty < 0 || ty >= h)
			return Integer.MAX_VALUE;
		return get(tx +ty*w);
		
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public double get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public double get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public double get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
}
