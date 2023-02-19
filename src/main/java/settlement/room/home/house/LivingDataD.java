package settlement.room.home.house;

import settlement.main.SETT;
import settlement.room.main.ROOMA;
import snake2d.util.bit.Bits;
import util.data.INT.INTE;

final class LivingDataD implements INTE{

	private ROOMA data;
	private final int tile;
	private final Bits bits;
	
	public LivingDataD(ROOMA data, int tile, int mask) {
		this.data = data;
		this.tile = tile;
		this.bits = new Bits(mask);
	}
	
	@Override
	public int get() {
		int tx = data.body().x1() + tile%data.body().width();
		int ty = data.body().y1() + tile/data.body().width();
		return bits.get(SETT.ROOMS().data.get(tx, ty));
	}

	@Override
	public int min() {
		return 0;
	}

	@Override
	public int max() {
		return bits.mask;
	}

	@Override
	public void set(int t) {
		int tx = data.body().x1() + tile%data.body().width();
		int ty = data.body().y1() + tile/data.body().width();
		int d = bits.set(SETT.ROOMS().data.get(tx, ty), t);
		SETT.ROOMS().data.set(data, tx, ty, d);
	}

}
