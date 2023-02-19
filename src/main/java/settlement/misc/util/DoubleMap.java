package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract class DoubleMap extends DoubleGetter{
	
	public DoubleMap() {
		super(SETT.TWIDTH, SETT.THEIGHT);
		// TODO Auto-generated constructor stub
	}

	public abstract void set(int tile, double value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public void set(int tx, int ty, double value) {
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
	public void set(int tx, int ty, DIR d, double value) {
		set(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public void set(COORDINATE c, double value) {
		set(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public void set(COORDINATE c, DIR d, double value) {
		set(c.x()+d.x(), c.y()+d.y(), value);
	}
	
	public void setAll(double value) {
		for (int i = 0; i < SETT.TAREA; i++) {
			set(i, value);
		}
	}
	

	
}
