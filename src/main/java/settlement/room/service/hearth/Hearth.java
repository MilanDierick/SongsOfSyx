package settlement.room.service.hearth;

import static settlement.main.SETT.*;

import settlement.misc.util.FSERVICE;
import snake2d.util.datatypes.Coo;

final class Hearth implements FSERVICE{


	private static final int NOWOOD = 0;
	private static final int AVAILABLE = 1;
	private static final int RESERVED = 2;
	private static final int USED = 3;
	private int data;
	private final Coo coo = new Coo();
	private HearthInstance ins;
	private final ROOM_HEARTH blue;
	
	
	Hearth(ROOM_HEARTH blue) {
		this.blue = blue;
	}
	
	Hearth get(int tx, int ty) {
		if (blue.is(tx, ty)) {
			if (ROOMS().fData.tileData.get(tx, ty) == Constructor.codeService) {
				this.data = ROOMS().data.get(tx, ty);
				this.coo.set(tx, ty);
				this.ins = blue.get(tx, ty);
				return this;
			}
		}
		return null;
	}
	
	boolean makeAvailable() {
		if (state() == NOWOOD) {
			stateSet(AVAILABLE);
			return true;
		}
		return false;
	}
	
	private void save() {
		
		int old = ROOMS().data.get(coo);
		
		if (old != data) {
			int current = data;
			data = old;
			if (state() == NOWOOD)
				ins.inactive --;
			if (state() == USED)
				ins.used --;
			ins.service.report(this, ins.blueprintI().data, -1);
			data = current;
			if (state() == NOWOOD)
				ins.inactive ++;
			if (state() == USED)
				ins.used ++;
			ROOMS().data.set(ins, coo, data);
			ins.service.report(this, ins.blueprintI().data, 1);
			
		}
	}
	
	private int state() {
		return data & 0b01111;
	}
	
	private void stateSet(int state) {
		data &= ~0b01111;
		data |= state;
		save();
	}
	
	@Override
	public void consume() {
		if (state() != USED)
			throw new RuntimeException();
		ins.use();
		if (ins.available() > 1) {
			stateSet(AVAILABLE);
		}else {
			stateSet(NOWOOD);
		}
	}
	
	@Override
	public void startUsing() {
		stateSet(USED);
	}
	
	@Override
	public int x() {
		return coo.x();
	}

	@Override
	public int y() {
		return coo.y();
	}

	@Override
	public boolean findableReservedCanBe() {
		return state() == AVAILABLE;
	}

	@Override
	public void findableReserve() {
		if (state() != AVAILABLE)
			throw new RuntimeException();
		stateSet(RESERVED);
		
	}

	@Override
	public boolean findableReservedIs() {
		return state() == RESERVED || state() == USED;
	}

	@Override
	public void findableReserveCancel() {
		if (state() == RESERVED || state() == USED)
			stateSet(AVAILABLE);
		
	}
	
	void dispose(){
		stateSet(NOWOOD);
	}


	
}
