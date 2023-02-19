package settlement.room.main.util;

import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import util.data.INT;

public class RoomBits implements INT{

	private final Bits bits;
	private final COORDINATE coo;
	
	public RoomBits(COORDINATE coo, int mask){
		bits = new Bits(mask);
		this.coo = coo;
	}
	
	public RoomBits(COORDINATE coo, Bits bits){
		this.bits = bits;
		this.coo = coo;
	}
	
	@Override
	public int get() {
		return bits.get(SETT.ROOMS().data.get(coo));
	}

	@Override
	public int min() {
		return 0;
	}

	@Override
	public int max() {
		return bits.mask;
	}

	protected void remove() {
		
	}
	
	protected void add() {
		
	}
	
	public void set(RoomInstance r, int t) {
		remove();
		int d = SETT.ROOMS().data.get(coo);
		d = bits.set(d, t);
		SETT.ROOMS().data.set(r, coo, d);
		add();
	}
	
	public void inc(RoomInstance r, int i) {
		set(r, CLAMP.i(get()+i, 0, bits.mask));
	}

}
