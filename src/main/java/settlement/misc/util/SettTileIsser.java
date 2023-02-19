package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface SettTileIsser {

	public boolean is(int tile);
	
	public default boolean is(int tx, int ty) {
		return SETT.IN_BOUNDS(tx, ty) && is(tx+ty*SETT.TWIDTH);
	}
	
	public default boolean is(int tx, int ty, DIR d) {
		return is(tx+d.x(), ty+d.y());
	}
	
	public default boolean is(COORDINATE c) {
		return is(c.x(), c.y());
	}
	
	public default boolean is(COORDINATE c, DIR d) {
		return is(c.x()+d.x(), c.y()+d.y());
	}
	
}
