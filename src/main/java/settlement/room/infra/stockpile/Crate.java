package settlement.room.infra.stockpile;

import settlement.room.main.RoomInstance;
import settlement.room.main.job.StorageCrate;

final class Crate extends StorageCrate{

	protected final ROOM_STOCKPILE b;
	StockpileInstance ins;
	
	Crate(ROOM_STOCKPILE b) {
		this.b = b;
	}

	@Override
	protected boolean is(int tx, int ty) {
		if (b.is(tx, ty)) {
			ins = b.getter.get(tx, ty);
			if (b.constructor.isCrate(tx, ty)) {
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
		return resource() != null && ins.getsMaximum(resource());
	}

	@Override
	protected int max(RoomInstance ins) {
		return (int) (b.upgrades().boost(ins.upgrade())-1);
	}

	@Override
	protected double spoilRate(RoomInstance ins) {
		return 0.5 / (1.0+ins.upgrade());
	}
	

}
