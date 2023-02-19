package settlement.room.law.execution;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import snake2d.util.datatypes.Coo;
import snake2d.util.rnd.RND;

public final class Service implements FSERVICE{


	
	private final Coo coo = new Coo();
	private ExecutionInstance ins;
	private static final Service self = new Service();
	
	static Service init(ExecutionInstance ins, int tx, int ty) {
		
		if (SETT.ROOMS().fData.tile.get(tx, ty) != null)
			return null;
		if (!RND.oneIn(5))
			return null;
		SETT.ROOMS().data.set(ins, tx, ty, 1);
		return get(tx, ty);
	}
	
	static Service get(int tx, int ty) {
		ExecutionInstance ins = SETT.ROOMS().EXECUTION.get(tx, ty);
		if (ins == null)
			return null;
		SETT.ROOMS().fData.tile.get(tx, ty);
		if (SETT.ROOMS().fData.tile.get(tx, ty) != null)
			return null;
		if (SETT.ROOMS().data.get(tx, ty) == 0)
			return null;
		self.ins = ins;
		self.coo.set(tx, ty);
		return self;
		
	}

	@Override
	public boolean findableReservedCanBe() {
		return ins.active() && !findableReservedIs();
	}

	@Override
	public void findableReserve() {
		if (findableReservedCanBe()) {
			SETT.ROOMS().data.set(ins, coo, 2);
			if (ins.active())
				ins.blueprintI().finder.report(this, -1);
		}
	}

	@Override
	public boolean findableReservedIs() {
		return SETT.ROOMS().data.is(coo, 1);
	}

	@Override
	public void findableReserveCancel() {
		if (findableReservedIs()) {
			SETT.ROOMS().data.set(ins, coo, 1);
			if (ins.active())
				ins.blueprintI().finder.report(this, 1);
		}
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
		if (!findableReservedIs())
			ins.blueprintI().finder.report(this, 1);
	}

	void deactivate() {
		if (!findableReservedIs())
			ins.blueprintI().finder.report(this, -1);
	}

	
}
