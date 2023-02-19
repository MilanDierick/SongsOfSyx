package settlement.room.infra.export;

import static settlement.main.SETT.*;

import game.GAME;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.thing.halfEntity.caravan.Caravan;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

public class ExportFetcher extends Interractor{

	private final ExportTally tally;
	private boolean debugged = false;
	private int rI = 0;
	private final double tick = (double)TIME.secondsPerDay/(RESOURCES.ALL().size()*20);
	
	
	ExportFetcher(ROOM_EXPORT b, ExportTally tally) {
		super(b);
		this.tally = tally;
	}

	protected void update(float ds) {

		if (SETT.ENTRY().isClosed())
			return;
		
		tally.timer += ds;
		
		if (tally.timer < tick)
			return;
		
		tally.timer-= tick;
		
		RESOURCE r = RESOURCES.ALL().get(rI++);
		rI %= RESOURCES.ALL().size();
		
		int am = tally.promised.get(r)-tally.attempting.get(r);
		
		
		
		if (am > 0) {
			if (am > Caravan.MAX_LOAD)
				am = Caravan.MAX_LOAD;
			if (SETT.HALFENTS().caravans.createFetcher(r, am)) {
				
			}else {
				COORDINATE coo = getReservableSpot(-1, -1, r);
				if (coo == null ) {
				
					if (!debugged) {
						LOG.ln(r.name + " " + am);
						tally.debug();
						debugged = true;
					}
					tally.promised.set(r, 0);
				}else {
					int a = CLAMP.i(reservable(r, coo), 0, am);
					reserve(r, coo, a);
					finish(r, coo, a);
					tally.promised.inc(r, -a);
				}

			}
		}
		
	}
	
	public COORDINATE getReservableSpot(int sx, int sy, RESOURCE res) {
		if (tally.promised.get(res) == 0) {
			return null;
		}
		ExportInstance ins = ROOMS().EXPORT.get(sx, sy);
		
		if (ins == null || reservable(ins, res) <= 0) {
			ins = null;
			ROOM_EXPORT room = ROOMS().EXPORT;
			if (room.all().size() == 0)
				return null;
			int r = RND.rInt(ROOMS().EXPORT.all().size());
			for (int i = 0; i < room.all().size(); i++) {
				ExportInstance ins2 = room.all().get((i+r)%room.all().size());
				if (reservable(ins2, res) > 0) {
					ins = ins2;
					break;
				}
			}
		}
		
		if (ins != null) {
			COORDINATE c = super.getReservableSpot(ins, sx, sy, res);
			if (c == null) {
				GAME.Notify(ins.mX() + " " + ins.mY() + " " + reservable(ins, res));
			}
			return c;
		}
		
		return null;
	}
	
	private int reservable(ExportInstance ins, RESOURCE res) {
		if (res == ins.resource())
			return ins.amount - ins.amountReserved;
		return 0;
	}
	
	@Override
	public int reserved(RESOURCE res, COORDINATE c) {
		Crate crate = b.crate(c.x(), c.y());
		if (crate == null || crate.resource() != res)
			return 0;
		return crate.reserved();
	}
	
	@Override
	public int reservable(RESOURCE res, COORDINATE c) {
		Crate crate = b.crate(c.x(), c.y());
		if (crate == null || crate.resource() != res)
			return 0;
		return crate.amount()-crate.reserved();
	}

	@Override
	public void reserve(RESOURCE res, COORDINATE c, int amount) {
		if (reservable(res, c) < amount)
			throw new RuntimeException();
		Crate crate = b.crate(c.x(), c.y());
		crate.reservedSet(crate.reserved()+amount);
	}
	
	@Override
	public void finish(RESOURCE res, COORDINATE c, int amount) {
		if (amount > reserved(res, c))
			throw new RuntimeException();
		if (amount > 0) {
			Crate crate = b.crate(c.x(), c.y());
			crate.reservedSet(crate.reserved()-amount);
			crate.amountSet(crate.amount()-amount);
			tally.promised.inc(res, -amount);
		}
	}
	
	public void initCaravan(RESOURCE r, int attempting) {
		tally.attempting.inc(r, attempting);
	}
	
	public void cancel(RESOURCE r, int attempting) {
		tally.attempting.inc(r, -attempting);
	}

	void vacate(int tx, int ty, RESOURCE res, int amount) {
		int op = tally.promised.get(res) - tally.amount.get(res);
		
		if (op > 0) {
			int p = CLAMP.i(amount, 0, op);
			tally.promised.inc(res, -p);
			amount -= p;
		}
		
		if (amount > 0) {
			SETT.THINGS().resources.create(tx, ty, res, amount);
		}
	}


	
	
}
