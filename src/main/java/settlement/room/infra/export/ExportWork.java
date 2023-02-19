package settlement.room.infra.export;

import init.resources.RESOURCE;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;

public class ExportWork extends Interractor{
	
	private ExportInstance i;
	
	ExportWork (ROOM_EXPORT b){
		super(b);
	}
	
	ExportWork init(ExportInstance i) {
		this.i = i;
		return this;
	}

	public COORDINATE getReservableSpot(int sx, int sy, RESOURCE res) {
		return super.getReservableSpot(i, sx, sy, res);
	}
	


	@Override
	public int reserved(RESOURCE res, COORDINATE c) {
		Crate crate = b.crate(c.x(), c.y());
		if (crate == null || crate.resource() != res)
			return 0;
		return crate.reservedSpace();
	}
	
	@Override
	public int reservable(RESOURCE res, COORDINATE c) {
		Crate crate = b.crate(c.x(), c.y());
		if (crate == null || crate.resource() != res)
			return 0;
		return  ExportInstance.crateMax - (crate.amount()+crate.reservedSpace());
	}
	
	public void reserve(RESOURCE res, int amount) {
		if (res == i.resource()) {
			i.spaceReserved += amount;
			if (i.spaceReserved < 0)
				i.spaceReserved = 0;
		}
		
	}
	
	public int reserved(RESOURCE res) {
		return i.spaceReserved;
	}
	
	public int reservable(RESOURCE res) {
		
		double tot = SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
		double am = SETT.ROOMS().STOCKPILE.tally().amountReservable(res);
		
		if (b.tally.exportWhenUnder.getD(res) == 1) {
			am = 128;
		}else {
			double exp = (double)b.tally.exportWhenUnder.get(res)/(b.tally.exportWhenUnder.max(null)-1);
			double to = tot*(1-exp);
			am = am - to;
		}
		if (am > 0) {
			int a = i.crates*ExportInstance.crateMax -(i.amount + i.spaceReserved);
			return CLAMP.i(a, 0, (int)am);
		}
		return 0;
	}

	@Override
	public void finish(RESOURCE res, COORDINATE c, int amount) {
		Crate crate = b.crate(c.x(), c.y());
		crate.amountSet(crate.amount()+amount);
		crate.reservedSpaceSet(crate.reservedSpace()-amount);
	}


	@Override
	public void reserve(RESOURCE res, COORDINATE c, int amount) {
		Crate crate = b.crate(c.x(), c.y());
		crate.reservedSpaceSet(crate.reservedSpace()+amount);
	}
	

}
