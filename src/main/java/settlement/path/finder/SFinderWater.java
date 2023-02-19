package settlement.path.finder;

import static settlement.main.SETT.*;

import init.RES;
import settlement.misc.util.FINDABLE;
import settlement.room.service.hygine.bath.ROOM_BATH;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public final class SFinderWater extends SFinderFindable{
	
	private int x,y;
	
	SFinderWater() {
		super("water");
		new TestPath(name, this);
	}

	private final FINDABLE service = new FINDABLE() {
		
		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}

		@Override
		public boolean findableReservedCanBe() {
			return TERRAIN().WATER.reservable(x, y);
		}

		@Override
		public void findableReserve() {
			if (TERRAIN().WATER.reservable(x, y)) {
				TERRAIN().WATER.reserve(x, y);
				report(this, -1);
			}
			
		}

		@Override
		public boolean findableReservedIs() {
			return TERRAIN().WATER.reserved(x, y);
		}

		@Override
		public void findableReserveCancel() {
			if (TERRAIN().WATER.reserved(x, y)) {
				TERRAIN().WATER.unreserve(x, y);
				report(this, 1);
			}
		}
	};
	
	public boolean findLand(COORDINATE start, SPath path, int maxDistance) {
		
		int tx = start.x();
		int ty = start.y();
		if (isLand(tx, ty)) {
			path.request(tx, ty, tx, ty, false);
			return path.isSuccessful();
		}
		
		RES.flooder().init(this);
		PathTile t = RES.flooder().pushSloppy(tx, ty, 0);
		
		
		while(RES.flooder().hasMore()) {
			t = RES.flooder().pollSmallest();
			if (isLand(t.x(), t.y())) {
				RES.flooder().done();
				path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
				return true;
			}
			if (t.getValue() > maxDistance)
				continue;
			
			for (DIR d : DIR.ALL) {
				double v = t.getValue();
				tx = t.x()+d.x();
				ty = t.y()+d.y();
				
				if (isLand(tx,ty) && PATH().coster.player.getCost(t.x(), t.y(), tx, ty) > 0) {
					if (PATH().connectivity.is(tx,ty)) {
						RES.flooder().pushSloppy(tx, ty, v+1, t);
					}else {
						RES.flooder().pushSloppy(tx, ty, v+100, t);
					}
				}else if(isWater(tx, ty)) {
					RES.flooder().pushSloppy(tx, ty, v+1, t);
				}
			}		
		}
		
		
		RES.flooder().done();
		return false;
		
	}
	
	private boolean isLand(int tx, int ty) {
		return !isWater(tx, ty) && !PATH().solidity.is(tx, ty);
	}
	
	private boolean isWater(int tx, int ty) {
		return TERRAIN().WATER.isOpen(tx, ty) || ROOM_BATH.isPool(tx, ty);
	}

	@Override
	public FINDABLE getReservable(int tx, int ty) {
		if (TERRAIN().WATER.isService(tx, ty)) {
			this.x = tx;
			this.y = ty;
			if (service.findableReservedCanBe())
				return service;
		}
		return null;
	}

	@Override
	public FINDABLE getReserved(int tx, int ty) {
		if (TERRAIN().WATER.isService(tx, ty)) {
			this.x = tx;
			this.y = ty;
			if (service.findableReservedIs())
				return service;
		}
		return null;
	}

}

