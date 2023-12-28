package settlement.maintenance;

import static settlement.main.SETT.*;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import init.race.RACES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class MRoom {
	
	static final Bit negative = 	new Bit(	0x0080000000);
	private final Bits jobsPlaced = new Bits(	0x007F000000);
	private final Bits degrade = 	new Bits(	0x0000FFFFFF);
	static final Bits degradeReal = new Bits(	0x0000F00000);
	private final int degradePerDay = 			0x0000020000-1;
	private final Data data;
	
	
	MRoom(Data data){
		this.data = data;
	}
	
	public boolean updateRoom(int tile, int tx, int ty) {
		if (SETT.ROOMS().map.is(tile)) {
			update(tile, tx, ty);
			return true;
		}
		return false;
	}

	private void update(int tile, int tx, int ty) {

		Room room = ROOMS().map.get(tile);
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null)
			return;

		int data = deg.getData();

		int am = getTileAmount(tx, ty, room);
		data = add(data, am);
		deg.setData(data);
		place(tile, tx, ty);
	}
	
	private int add(int data, int am) {
		int a = get(data);
		a += am;
		if (a < 0) {
			data = negative.set(data);
			a = -a;
		}else {
			data = negative.clear(data);
		}
		a &= degrade.mask;
		data = degrade.set(data, a);
		return data;
	}
	
	private int get(int data) {
		int a = degrade.get(data);
		if (negative.is(data))
			a = -a;
		return a;
	}
	
	private void place(int tile, int tx, int ty) {

		Room room = ROOMS().map.get(tile);
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		int data = deg.getData();
		
		if (isBlocked(tx, ty)) {
			return;
		}
		
		if (!room.constructor().blue().reqs.passes(FACTIONS.player()))
			return;

		if (this.data.setter.is(tx, ty)) {
			return;
		}

		if (jobsPlaced.isMaximum(data))
			return;

		if (jobsNeeded(tx, ty, room) <= 0)
			return;

		data = jobsPlaced.inc(data, 1);
		deg.setData(data);

		this.data.setter.set(tx, ty);
		setResource(deg, tx, ty);
	}
	
	void setResource(ROOM_DEGRADER deg, int tx, int ty) {
		
		double am = 0;
		for (int ri = 0; ri < deg.resSize(); ri++) {
			am += deg.resAmount(ri);
		}
		if (am < 6*deg.roomArea()*RND.rFloat()) {
			return;
		}
		
		double lim = am*RND.rFloat();
		am = 0;
		for (int ri = 0; ri < deg.resSize(); ri++) {
			am += deg.resAmount(ri);
			if (am >= lim && deg.resAmount(ri) > 0) {
				data.resourceSetter.set(tx, ty, ri+1);
			}
		}
	}
	
	boolean validate(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		if (room != null) {
			ROOM_DEGRADER deg = room.degrader(tx, ty);
			if (deg == null || isBlocked(tx, ty)) {
				data.setter.clear(tx, ty);
			}	
			return true;
		}
		return false;
	}

	public int getTileAmount(int tx, int ty, Room room) {
		double v = 1.0 + (1.0 - room.isolation(tx, ty)) * 2;
		double b = CLAMP.d(1.0/BOOSTABLES.CIVICS().MAINTENANCE.get(RACES.clP(null, null)), 0, 1000);;
		v*= room.degrader(tx, ty).degRate();
		
		v *= degradePerDay;
		v /= room.area(tx, ty);
		v*= b;
		v *= (1.0 - 0.5 * room.getDegrade(tx, ty));
		
		
		return (int) Math.ceil(v);
	}

	public int jobValue(int tx, int ty, Room room) {
		double v = degrade.mask;
		v/= room.area(tx, ty);
		return (int) Math.ceil(v);
	}

	public void maintain(int tx, int ty) {
		
		Room room = ROOMS().map.get(tx, ty);

		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null) {
			GAME.Notify("MAINTENANCE" + tx + " " + ty);
			return;
		}
		
		int d = deg.getData();
		d = add(d, -jobValue(tx, ty, room));
		if (jobsNeeded(tx, ty, room) <= 1) {
			data.setter.clear(tx, ty);
			d = jobsPlaced.inc(d, -1);
		}
		
		deg.setData(d);
		
	}

	private int jobsNeeded(int tx, int ty, Room room) {
		int am = get(room.degrader(tx, ty).getData());
		if (am <= 0)
			return 0;
		double t = Math.ceil(degrade.mask);
		int deg = degrade.mask - am;
		t -= deg;
		int j = (int) Math.ceil(t / jobValue(tx, ty, room));
		return j - jobsPlaced.get(room.degrader(tx, ty).getData());
	}

	private boolean isBlocked(int tx, int ty) {
		for (DIR d : DIR.ORTHO)
			if (IN_BOUNDS(tx, ty, d) && PATH().availability.get(tx, ty, d).player <= AVAILABILITY.ROOM.player && PATH().availability.get(tx, ty, d).player > 0)
				return false;
		return true;

	}

	public boolean vandalise(short tx, short ty) {
		Room room = ROOMS().map.get(tx, ty);
		if (room == null)
			return false;
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null)
			return true;

		int data = deg.getData();
		data = add(data, degradeReal.inc(data, 2));
		deg.setData(data);
		place(tx+ty*SETT.TWIDTH, tx, ty);
		return true;
	}

	public void initRoom(Room room, int rx, int ry) {
		ROOM_DEGRADER deg = room.degrader(rx, ry);
		if (deg == null)
			return;
		int data = deg.getData();
		data = jobsPlaced.set(data, 0);
		deg.setData(data);
	}

}