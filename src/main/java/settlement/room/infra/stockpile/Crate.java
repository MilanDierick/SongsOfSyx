package settlement.room.infra.stockpile;

import settlement.room.main.job.StorageCrate;

final class Crate extends StorageCrate{

	protected final ROOM_STOCKPILE b;
	StockpileInstance ins;
	
	Crate(ROOM_STOCKPILE b) {
		super(ROOM_STOCKPILE.CRATE_MAX);
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
	public double spoilRate() {
		return 0.25;
	}
	

}
