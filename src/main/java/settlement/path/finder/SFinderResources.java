package settlement.path.finder;

import static settlement.main.SETT.*;

import init.resources.*;
import init.resources.RBIT.RBITImp;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.path.components.SComponent;
import settlement.room.main.Room;
import settlement.room.main.throne.THRONE;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.ACTION;
import view.sett.IDebugPanelSett;

public final class SFinderResources {

	public final Normal normal = new Normal();
	public final Scattered scattered = new Scattered();

	private final RBITImp bscattered = new RBITImp();
	private final RBITImp bstored = new RBITImp();
	private final RBITImp bfetch = new RBITImp();
	
	SFinderResources() {
		
		IDebugPanelSett.add("Unreserve everything", new ACTION() {
			
			@Override
			public void exe() {
				for (COORDINATE c : new Rec(SETT.TILE_BOUNDS)) {
					
					
					
					while(unres(c))
						;
					
					
					
					Room room = ROOMS().map.get(c.x(), c.y());
					if (room != null) {
						RESOURCE_TILE res = room.resourceTile(c.x(), c.y());
						while (res != null && res.findableReservedIs() && res.resource() != null) {
							res.findableReserveCancel();
						}
					}
					
					
				}
			}
			
			private boolean unres(COORDINATE c) {
				for (Thing t : THINGS().get(c.x(), c.y())) {
					if (t instanceof ScatteredResource) {
						ScatteredResource sc = ((ScatteredResource) t);
						if (sc.findableReservedIs() && sc.resource() != null) {
							sc.findableReserveCancel();
							return true;
						}
					}
				}
				return false;
			}
		});
	}
	
	public boolean has(int sx, int sy, RBIT scattered, RBIT stored, RBIT fetch) {
		return PATH().comps.data.resScattered.has(sx, sy, scattered) || 
				PATH().comps.data.resCrate.has(sx, sy, stored) || 
				PATH().comps.data.resCrateGet.has(sx, sy, fetch);
	}
	
	public RESOURCE find(RBIT scattered, RBIT stored, RBIT fetch, COORDINATE start, SPath path, int maxdistance) {
		return find(scattered, stored, fetch, start.x(), start.y(), path, maxdistance);
	}
	
	public RESOURCE find(RBIT scattered, RBIT stored, RBIT fetch, int sx, int sy, SPath path, int maxdistance) {

		if (has(sx, sy, scattered, stored, fetch)) {
			this.bscattered.clearSet(scattered);
			this.bstored.clearSet(stored);
			this.bfetch.clearSet(fetch);
			if (path.request(sx, sy, finder, maxdistance)) {
				RESOURCE_TILE t = reservable(scattered, stored, fetch, path.destX(), path.destY());
				t.findableReserve();
				return t.resource();
			}
			
		}
		
		return null;
	}
	
	private SFINDER finder = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return PATH().comps.data.resScattered.has(c, bscattered)
					|| PATH().comps.data.resCrate.has(c, bstored)
					|| PATH().comps.data.resCrateGet.has(c, bfetch);
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			return reservable(bscattered, bstored, bfetch, tx, ty) != null;
		}
	};

	private final RBITImp tmp = new RBITImp();
	
	private RESOURCE_TILE reservable(RBIT scattered, RBIT stored, RBIT fetch, int tx, int ty) {
		
		tmp.clear();
		tmp.or(scattered).or(stored).or(fetch);
		
		ScatteredResource sc = THINGS().resources.getReservable(tx, ty,tmp);
		if (sc != null && sc.findableReservedCanBe()) {
			return sc;
		}
		
		Room room = ROOMS().map.get(tx, ty);
		if (room == null)
			return null;
		RESOURCE_TILE res = room.resourceTile(tx, ty);
		if (res == null)
			return null;
		RESOURCE r = res.resource();
		if (r == null)
			return null;
		if (!res.findableReservedCanBe())
			return null;
		if (res.isfetching()) {
			if (fetch.has(r))
				return res;
			return null;
		}
		if (res.isStoring()) {
			if (stored.has(r))
				return res;
			return null;
		}
		if (scattered.has(r))
			return res;
		return null;
	}
	
	private RESOURCE_TILE reserved(RESOURCE resource, int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		if (room != null) {
			RESOURCE_TILE res = room.resourceTile(tx, ty);
			if (res != null && res.resource() == resource && res.findableReservedIs()) {
				return res;
			}
		}
		for (Thing t : THINGS().get(tx, ty)) {
			if (t instanceof ScatteredResource) {
				ScatteredResource sc = ((ScatteredResource) t);
				if(sc.findableReservedIs() && sc.resource() == resource) {
					return sc;
				}
			}
		}
		
		return null;
	}
	
	public int reserveExtra(boolean stored, boolean fetch, RESOURCE r, int tx, int ty, int amount) {
		RBIT sc = r.bit;
		RBIT st = stored ? r.bit : RBIT.NONE;
		RBIT fe = fetch ? r.bit : RBIT.NONE;

		int am = 0;
		while(am < amount) {
			RESOURCE_TILE t = reservable(sc, st, fe, tx, ty);
			if (t == null)
				return am;
			while(am < amount && t.findableReservedCanBe()) {
				t.findableReserve();
				am ++;
			}
		}
		return am;
	}
	
	public boolean isReservedAndAvailable(RESOURCE r, int x, int y) {
		return reserved(r, x, y) != null;
	}
	
	public final int pickup(RESOURCE r, int tx, int ty, int amount) {
		int am = 0;
		while(am < amount) {
			RESOURCE_TILE t = reserved(r, tx, ty);
			if (t == null)
				return am;
			while(am < amount && t.findableReservedIs()) {
				t.resourcePickup();
				am ++;
			}
		}
		return am;
	}
	
	public final int pickupD(RESOURCE r, int tx, int ty, int amount) {
		int am = 0;
		while(am < amount) {
			RESOURCE_TILE t = reserved(r, tx, ty);
			if (t == null)
				return am;
			while(am < amount && t.findableReservedIs()) {
				t.resourcePickup();
				am ++;
			}
		}
		return am;
	}
	
	public final void unreserve(RESOURCE r, int tx, int ty, int amount) {
		while(amount > 0) {
			RESOURCE_TILE t = reserved(r, tx, ty);
			if (t == null)
				return;
			while(amount > 0 && t.findableReservedIs()) {
				t.findableReserveCancel();
				amount --;
			}
		}
	}
	
	public void reportPresence(RESOURCE_TILE r) {
		if (r.isfetching()) {
			PATH().comps.data.resCrateGet.reportPresence(r.x(), r.y(), r.resource());
		}else if(r.isStoring()) {
			PATH().comps.data.resCrate.reportPresence(r.x(), r.y(), r.resource());
		}else {
			PATH().comps.data.resScattered.reportPresence(r.x(), r.y(), r.resource());
		}
		
	}
	
	public void reportAbsence(RESOURCE_TILE r) {
		if (r.isfetching()) {
			PATH().comps.data.resCrateGet.reportAbsence(r.x(), r.y(), r.resource());
		}else if(r.isStoring()) {
			PATH().comps.data.resCrate.reportAbsence(r.x(), r.y(), r.resource());
		}else {
			PATH().comps.data.resScattered.reportAbsence(r.x(), r.y(), r.resource());
		}
	}

	
	public class Normal {
		
		private Normal() {
			
		}
		
		public boolean has(int sx, int sy, RESOURCE r) {
			return has(sx, sy, r.bit);
		}
		
		public boolean has(int sx, int sy, ResGroup group) {
			return has(sx, sy, group.mask);
		}
		
		public boolean has(int sx, int sy, RBIT mask) {
			return SFinderResources.this.has(sx, sy, mask, mask, mask);
		}
		
		public boolean reserve(COORDINATE start, RESOURCE r, SPath path, int maxdistance) {
			return reserve(start, r.bit, path, maxdistance) != null;
		}
		
		public RESOURCE reserve(COORDINATE start, RBIT mask, SPath path, int maxdistance) {
			return SFinderResources.this.find(mask, mask, mask, start, path, maxdistance);
		}

		public int reserveExtra(RESOURCE r, int x, int y, int amount) {
			return SFinderResources.this.reserveExtra(true, true, r, x, y, amount);
		}

		public boolean has(RESOURCE r) {
			return has(THRONE.coo().x(),THRONE.coo().y(), r);
		}

		

		
	}
	

	
	public class Scattered {
		
		private Scattered() {
			
		}
		
		public boolean has(RESOURCE r) {
			return has(THRONE.coo().x(),THRONE.coo().y(), r);
		}
		
		public boolean has(int sx, int sy, RESOURCE r) {
			return has(sx, sy, r.bit);
		}
		
		public boolean has(int sx, int sy, RBIT mask) {
			return SFinderResources.this.has(sx, sy, mask, RBIT.NONE, RBIT.NONE);
		}
		
		public boolean reserve(COORDINATE start, RESOURCE r, SPath path, int maxdistance) {
			return reserve(start, r.bit, path, maxdistance) != null;
		}
		
		public RESOURCE reserve(COORDINATE start, RBIT resMask, SPath path, int maxdistance) {
			return reserve(start.x(), start.y(), resMask, path, maxdistance);
		}
		
		public RESOURCE reserve(int sx, int sy, RBIT resMask, SPath path, int maxdistance) {
			return SFinderResources.this.find(resMask, RBIT.NONE, RBIT.NONE, sx, sy, path, maxdistance);
		}

		public int reserveExtra(RESOURCE r, int x, int y, int amount) {
			return SFinderResources.this.reserveExtra(false, false, r, x, y, amount);
		}
		
	}



}
