package settlement.room.law.court;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import snake2d.util.datatypes.Coo;

public final class Service implements FSERVICE{

	private final Coo coo = new Coo();
	private CourtInstance ins;
	private static final Service self = new Service();
	private int data;
	
	
	static Service init(int tx, int ty) {
		
		CourtInstance ins = SETT.ROOMS().COURT.get(tx, ty);
		if (ins == null)
			return null;
		
		if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.codeSpectator) {
			self.ins = ins;
			self.coo.set(tx, ty);
			self.data = SETT.ROOMS().data.get(tx, ty);
			return self;
		}
		return null;
	}

	@Override
	public boolean findableReservedCanBe() {
		return !findableReservedIs();
	}

	@Override
	public void findableReserve() {
		data = 0;
		save();
	}

	@Override
	public boolean findableReservedIs() {
		return data == 0;
	}

	@Override
	public void findableReserveCancel() {
		data = 1;
		save();
	}
	
	private void save() {
		int n = data;
		data = SETT.ROOMS().data.get(coo);
		if ( n == data)
			return;
		if (findableReservedCanBe()) {
			ins.blueprintI().finder.report(this, -1);
		}
		data = n;
		if (findableReservedCanBe()) {
			ins.blueprintI().finder.report(this, 1);
		}
		SETT.ROOMS().data.set(ins, coo, data);
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
	public void consume() {
		findableReserveCancel();
	}
	
	void activate() {
		findableReserveCancel();
	}

	void deactivate() {
		findableReserve();
	}

	
}
