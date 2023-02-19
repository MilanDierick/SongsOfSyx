package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract class TileIntGetter {

	private final int width,height,outof;
	
	public TileIntGetter(int maxWidth, int maxHeight, int outof) {
		this.width = maxWidth;
		this.height = maxHeight;
		this.outof = outof;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public boolean is(int tile, int value) {
		return get(tile) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public boolean is(int tx, int ty, int value) {
		return get(tx,ty) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public boolean is(int tx, int ty, DIR d, int value) {
		return get(tx, ty, d) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public boolean is(COORDINATE c, int value) {
		return get(c) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public boolean is(COORDINATE c, DIR d, int value) {
		return get(c,d) == value;
	}
	
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public abstract int get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public int get(int tx, int ty) {
		if (tx < 0 || ty < 0 || tx >= width || ty >= height)
			return outof;
		return get(tx +ty*SETT.TWIDTH);
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public int get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public int get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public int get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
	
}
