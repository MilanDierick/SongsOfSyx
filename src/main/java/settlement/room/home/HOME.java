package settlement.room.home;

import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;

public interface HOME extends AREA{

	/**
	 * Only called by the stat
	 * @param h
	 */
	public HOME vacate(Humanoid h);
	
	/**
	 * Only called by the stat
	 * @param h
	 */
	public HOME occupy(Humanoid h);
	
	/**
	 * only called by stats
	 * @param h
	 */
//	public void updateEmploymentStatus(Humanoid h);
	
	public double space();
	
	public Humanoid occupant(int oi);
	
	public int occupants();
	public int occupantsMax();
	public COORDINATE service();
	public int resourceAm(int ri);
	public Race race();
	public double isolation();
	public HOME_TYPE availability();
	public HOME resUpdate();
	public HOME done();
	
	public CharSequence nameHome();
	
	public static HOME getS(int tx, int ty, Object user) {
		HOME h = get(tx, ty, user);
		if (h != null) {
			if (h.service().isSameAs(tx, ty))
				return h;
			h.done();
		}
		return null;
	}

	public static HOME get(COORDINATE c, Object user) {
		return get(c.x(), c.y(), user);
	}
	
	public static HOME get(int tx, int ty, Object user) {
		HOME h = SETT.ROOMS().HOMES.HOME.house(tx, ty, user);
		if (h != null)
			return h;
		return SETT.ROOMS().HOMES.CHAMBER.get(tx, ty);
	}



}
