package settlement.room.infra.export;

import init.resources.RESOURCE;
import settlement.main.SETT;
import snake2d.util.bit.Bits;

final class Crate {

	private final static Bits bAmount 			= new Bits(0x0000FF00);
	private final static Bits bReserved 		= new Bits(0x00FF0000);
	private final static Bits bReservedSpace 	= new Bits(0xFF000000);
	protected final ROOM_EXPORT b;
	int tx, ty;
	ExportInstance ins;
	
	Crate(ROOM_EXPORT b){
		this.b = b;
	}
	
	Crate get(int tx, int ty) {
		if (b.is(tx, ty)) {
			ins = b.getter.get(tx, ty);
			if (b.constructor.isCrate(tx, ty)) {
				this.tx = tx;
				this.ty = ty;
				return this;
			}
		}
		return null;
	}

	public RESOURCE resource() {
		return ins.resource();
	}
	
	public static int amount(int data) {
		return bAmount.get(data);
	}
	
	private void remove() {
		RESOURCE r = resource();
		if (r != null) {
			b.tally.inc(r, -ins.amount, -ExportInstance.crateMax*(ins.crates));
			ins.amount -= amount();
			ins.amountReserved -= reserved();
			ins.spaceReserved -= reservedSpace();
//			ins.workmask &= r.bit;
//			if (ins.amount[r.index()] + ins.amountReserved[r.index()] < ExportInstance.crateMax)
//				ins.workmask |= r.bit;
			b.tally.inc(r, ins.amount, ExportInstance.crateMax*(ins.crates));
		}
	}
	
	private void add() {
		RESOURCE r = resource();
		if (r != null) {
			b.tally.inc(r, -ins.amount, -ExportInstance.crateMax*(ins.crates));
			ins.amount += amount();
			ins.amountReserved += reserved();
			ins.spaceReserved += reservedSpace();
//			ins.workmask &= r.bit;
//			if (ins.amount[r.index()] + ins.amountReserved[r.index()] < ExportInstance.crateMax)
//				ins.workmask |= r.bit;
			b.tally.inc(r, ins.amount, ExportInstance.crateMax*(ins.crates));
		}
	}
	
	public int amount() {
		return bAmount.get(data());
	}
	
	public void amountSet(int am) {
		remove();
		int d = bAmount.set(data(), am);
		save(d);
		add();
	}
	
	public int reserved() {
		return bReserved.get(data());
	}
	
	public void reservedSet(int r) {
		remove();
		int d = bReserved.set(data(), r);
		save(d);
		add();
	}
	
	public int reservedSpace() {
		return bReservedSpace.get(data());
	}
	
	public void reservedSpaceSet(int r) {
		remove();
		int d = bReservedSpace.set(data(), r);
		save(d);
		add();
	}
	
	private int data() {
		return SETT.ROOMS().data.get(tx, ty);
	}
	
	private void save(int d) {
		SETT.ROOMS().data.set(ins, tx, ty, d);
	}
	
	void clear() {
		if (resource() == null)
			return;
		remove();
		save(0);
		
	}
	
}
