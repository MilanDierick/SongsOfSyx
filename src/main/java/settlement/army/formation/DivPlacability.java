package settlement.army.formation;

import init.C;
import settlement.army.Army;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.util.datatypes.DIR;

public final class DivPlacability{

	private static final DIR[] dirIter = new DIR[] { DIR.NW, DIR.NE, DIR.SE, DIR.SW };
	
	public static boolean pixelIsBlocked(int x1, int y1, int tileSize, Army defender) {
		for (DIR d : dirIter) {
			int tx = (x1 + d.x() * (tileSize/2-2)) >> C.T_SCROLL;
			int ty = (y1 + d.y() * (tileSize/2-2)) >> C.T_SCROLL;
			if (!tileIsOK(tx, ty, defender)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean tileIsOK(int tx, int ty,  Army defender) {
		AVAILABILITY a = SETT.PATH().availability.get(tx, ty);
		if (a == null)
			return false;
		return !a.isSolid(defender);
	}
	
	public static boolean checkPixelStep(int fx, int fy, int tox, int toy, int tz,Army defender) {
		if (pixelIsBlocked(fx, fy, tz, defender))
			return false;
		if (pixelIsBlocked(tox, toy, tz, defender))
			return false;
		if (pixelIsBlocked(fx, toy, tz, defender))
			return false;
		if (pixelIsBlocked(tox, fy, tz, defender))
			return false;
		return true;
	}
	
	public static boolean checkStep(int fx, int fy, int tx, int ty, Army defender) {
		if (!tileIsOK(tx, ty, defender))
			return false;
		
		if (tx != fx || ty != fy)
			if (!tileIsOK(tx, fy, defender) || ! tileIsOK(fx, ty, defender))
				return false;
		
		return true;
	}

}
