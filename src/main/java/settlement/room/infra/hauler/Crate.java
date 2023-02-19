package settlement.room.infra.hauler;

import settlement.main.SETT;
import settlement.room.main.job.StorageCrate;

final class Crate extends StorageCrate{

	static final int size = 64;
	protected final ROOM_HAULER b;
	HaulerInstance ins;
	
	Crate(ROOM_HAULER b) {
		super(size);
		this.b = b;
	}

	@Override
	protected boolean is(int tx, int ty) {
		if (b.is(tx, ty)) {
			ins = b.getter.get(tx, ty);
			if (SETT.ROOMS().fData.tileData.is(tx, ty, 1)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void count(int res, int crates, int amountTot, int amountUnres, int spaceRes) {
		ins.addCrate(res, crates, amountTot, amountUnres, spaceRes);
	}
	
	@Override
	public boolean isfetching() {
		return ins.fetch;
	}

}
