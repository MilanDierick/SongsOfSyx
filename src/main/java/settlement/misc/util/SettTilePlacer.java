package settlement.misc.util;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface SettTilePlacer {

	public boolean place(int tile);
	
	public default boolean place(int tx, int ty) {
		return SETT.IN_BOUNDS(tx, ty) && place(tx+ty*SETT.TWIDTH);
	}
	
	public default boolean place(int tx, int ty, DIR d) {
		return place(tx+d.x(), ty+d.y());
	}
	
	public default boolean place(COORDINATE c) {
		return place(c.x(), c.y());
	}
	
	public default boolean place(COORDINATE c, DIR d) {
		return place(c.x()+d.x(), c.y()+d.y());
	}
	
	public boolean isPlacable(int tx, int ty);
	
	public default boolean isPlacable(int tx, int ty, DIR d) {
		return isPlacable(tx+d.x(), ty+d.y());
	}
	
	public default boolean isPlacable(COORDINATE c) {
		return isPlacable(c.x(), c.y());
	}
	
	public default boolean isPlacable(COORDINATE c, DIR d) {
		return isPlacable(c.x()+d.x(), c.y()+d.y());
	}
	
}
