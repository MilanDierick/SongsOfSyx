package settlement.path.finder;

import static settlement.main.SETT.*;

import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.components.*;
import settlement.path.components.SCompFinder.SCompPatherExister;
import settlement.room.main.Room;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

public final class SFinderResourceStore {

	private Coo result = new Coo();

	SFinderResourceStore() {
		new TestPath("storage", null) {
			@Override
			protected void place(int sx, int sy, SPath p) {
				TILE_STORAGE j = find(sx, sy, Integer.MAX_VALUE);
				if (j != null) {
					LOG.ln(j.x() + " " + j.y());
					p.request(sx,  sy, j);
				}else {
					LOG.ln("Nay");
				}
			}
		};
		
	}
	
	void update(float ds) {
		
		
	}
	
	long resMask = 0;
	long storeMask = 0;
	
	private FindableDatas d() {
		return SETT.PATH().comps.data;
	}
	
	private final SCompPatherExister wierd = new SCompPatherExister() {
		
		
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			storeMask |= d().storage.bits(c);
			resMask |= d().resScattered.bits(c);
			return (storeMask & resMask) != 0;
		}
		
		@Override
		public void init(SComponentLevel l) {
			resMask = 0;
			storeMask = 0;
		}
	};
	
	private SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return (resMask & d().storage.bits(c)) != 0;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null) {
				TILE_STORAGE result = r.storage(tx, ty);
				if (result != null && result.storageReservable() > 0 && result.resource() != null && (result.resource().bit & resMask) != 0) {
					SFinderResourceStore.this.result.set(result);
					return true;
				}
			}
			return false;
		}
	};
	

	
	/**
	 * 
	 * @param sx
	 * @param sy
	 * @param maxDistance
	 * @param path
	 * @return null if nothing was found. Else a job. If job resource == null, then the path is not set. Otherwise it is set.
	 */
	public TILE_STORAGE find(int sx, int sy, int maxDistance) {
		
		if (maxDistance == Integer.MAX_VALUE) {
			resMask = d().resScattered.bits(sx, sy);
			LOG.bits(resMask);
			
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance);
			if (c != null) {
				return ROOMS().map.get(result).storage(result.x(), result.y());
			}
			return null;
		}
		
		if (SETT.PATH().comps.pather.exists(sx, sy, wierd, maxDistance)) {
			if (SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance) != null)
				return ROOMS().map.get(result).storage(result.x(), result.y());
		}
		return null;
	
	}
	
	public boolean hasStoreJob(int sx, int sy) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s == null)
			return false;
		long m = d().resScattered.bits(s);
		m &= d().storage.bits(s);
		return m != 0l;
	}


}
