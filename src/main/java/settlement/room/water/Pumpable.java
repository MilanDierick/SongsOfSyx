package settlement.room.water;

import settlement.main.SETT;
import snake2d.util.datatypes.DIR;

interface Pumpable {

	public void drain(int tx, int ty);
	public void pump(int tx, int ty, DIR d);
	public int dirmask(int tx, int ty);
	public int radius();
	
	public default void reportChange(int tx, int ty, int radius) {
		SETT.ROOMS().WATER.updater.reportChange(tx, ty, radius);
	}
}
