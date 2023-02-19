package settlement.path.finder;

import static settlement.main.SETT.*;

import game.GAME;
import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.components.FindableDataRes;
import settlement.path.components.SComponent;
import settlement.room.main.Room;
import settlement.room.main.throne.THRONE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.map.MAP_OBJECT;

public final class SFinderResourceStorage {

	private long mask;
	private RESOURCE result;

	SFinderResourceStorage() {

	}
	
	public void reportPresence(TILE_STORAGE r) {
		s().reportPresence(r.x(), r.y(), r.resource());
	}
	
	public void reportAbsence(TILE_STORAGE r) {
		s().reportAbsence(r.x(), r.y(), r.resource());
	}
	
	public boolean has(int sx, int sy, RESOURCE r) {
		return s().has(sx, sy, r.bit);
	}
	
	public boolean has(RESOURCE r) {
		return has(THRONE.coo().x(),THRONE.coo().y(), r);
	}
	
	
	
	public boolean has(int sx, int sy, long mask) {
		return s().has(sx, sy, mask);
	}

	public long hasMask(int sx, int sy) {
		return s().bits(sx, sy);
	}
	
	private final FindableDataRes s() {
		return PATH().comps.data.storage;
	}
	
	/**
	 * 
	 * @param start
	 * @param r
	 * @param path
	 * @return true and a set path if found
	 */
	public boolean reserve(COORDINATE start, RESOURCE r, SPath path, int maxdistance) {
		return reserve(start, r.bit, path, maxdistance) == r;
	}
	
	public RESOURCE reserve(COORDINATE start, long resMask, SPath path, int maxdistance) {
		
		if (has(start.x(), start.y(), resMask)) {
			mask = resMask;
			
			if (path.request(start.x(), start.y(), fin, maxdistance)) {
				reserve(path.destX(), path.destY());
				return result;
			}
		}
		return null;
	}
	
	private final SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return s().has(c, mask);
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			Room res = ROOMS().map.get(tx, ty);
			if (res != null) {
				TILE_STORAGE s = res.storage(tx, ty);
				if (s != null && s.resource() != null && s.storageReservable() > 0 && (s.resource().bit & mask) != 0) {
					result = s.resource();
					return true;
				}
			}
			return false;
		}
	};

	public COORDINATE reserve(COORDINATE start, RESOURCE r, int maxdistance) {
		return reserve(start.x(), start.y(), r, maxdistance);
	}
	
	public COORDINATE reserve(int sx, int sy, RESOURCE r, int maxdistance) {
		mask = r.bit;
		COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, maxdistance);
		if (c != null) {
			reserve(c.x(), c.y());
			return c;
		}
		return SETT.PATH().finders.finder().findDest(sx, sy, fin, maxdistance);
	}
	
	public boolean isReservedAndAvailable(COORDINATE reserved, RESOURCE r) {
		return isReservedAndAvailable(reserved.x(), reserved.y(), r.bIndex());
	}
	
	public boolean isReservedAndAvailable(int x, int y, short r) {
		Room res = ROOMS().map.get(x, y);
		if (res != null) {
			TILE_STORAGE s = res.storage(x, y);
			return s != null && s.storageReserved() > 0 && s.resource() != null && s.resource().bIndex() == r;
		}
		return false;
	}
	
	public boolean isReservedAndAvailable(COORDINATE reserved, byte r) {
		return isReservedAndAvailable(reserved.x(), reserved.y(), r);
	}
	
	public void deposit(COORDINATE reserved, RESOURCE r) {
		deposit(reserved.x(), reserved.y(), r.bIndex());
	}
	
	public void deposit(int x, int y, short r) {
		Room res = ROOMS().map.get(x, y);
		if (res != null) {
			TILE_STORAGE s = res.storage(x, y);
			if (s != null && s.storageReserved() > 0 && s.resource().bIndex() == r) {
				s.storageDeposit(1);
			}
		}else {
			GAME.Notify("no resource to pick up at: " + x + " " + y);
		}
	}

	/**
	 * 
	 * @param reserved
	 *            - the tile you have reserved
	 */
	public void cancelReservation(COORDINATE reserved, byte resourceIndex) {
		cancelReservation(reserved.x(), reserved.y(), resourceIndex);
	}
	
	public void cancelReservation(int x, int y, short resourceIndex) {
		Room res = ROOMS().map.get(x, y);
		if (res != null) {
			TILE_STORAGE s = res.storage(x, y);
			if (s != null && s.storageReserved() > 0 && s.resource().bIndex() == resourceIndex) {
				s.storageUnreserve(1);
			}
		}
	}
	
	private void reserve(int tx, int ty) {
		Room res = ROOMS().map.get(tx, ty);
		if (res != null) {
			TILE_STORAGE s = res.storage(tx, ty);
			if (s != null && s.storageReservable() > 0 && (s.resource().bit & result.bit) != 0) {
				s.storageReserve(1);
				return;
			}
		}
		throw new RuntimeException();
	}
	
	public MAP_OBJECT<TILE_STORAGE> getter = new MAP_OBJECT<TILE_STORAGE>() {

		@Override
		public TILE_STORAGE get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public TILE_STORAGE get(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null)
				return r.storage(tx, ty);
			return null;
		}
		
	};

}
