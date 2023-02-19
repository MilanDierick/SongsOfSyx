package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.*;

import game.GAME;
import settlement.room.infra.export.ExportFetcher;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;

class TypeFetcher extends Type{

	private static CharSequence ¤¤verb = "¤fetching";
	
	TypeFetcher() {
		super(¤¤verb);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.reservedGlobally = (short) amount;
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		f.initCaravan(c.res, amount);
		fetch(c);
		return c.path.isSuccessful();
	}
	
	private boolean fetch(Caravan c) {
		c.path.clear();
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		COORDINATE coo = f.getReservableSpot(c.ctx(), c.cty(), c.res);
		if (coo == null)
			return false;
		int am = f.reservable(c.res, coo);
		if (am > c.reservedGlobally-c.amountCarried)
			am = c.reservedGlobally-c.amountCarried;
		c.reserved = (short) am;
		f.reserve(c.res, coo, am);
		c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
		c.move();
		if (!c.path.isSuccessful()) {
			GAME.Notify(c.ctx() + " " + c.cty() + "->" + coo.x() + " " + coo.y());
		}
		return c.path.isSuccessful();
	}
	
	private boolean deposit(Caravan c) {
		
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		coo.set(c.path.destX(), c.path.destY());
		int am = f.reserved(c.res, coo);
		if (am <= 0) {
			c.reserved = 0;
			return false;
		}
		int max = 1;
		if (max > am)
			max = am;
		if (max > c.reserved)
			max = c.reserved;
		f.finish(c.res, coo, max);
		c.amountCarried += max;
		c.reserved -= max;
		return true;
	}

	@Override
	public boolean update(Caravan c, float ds) {
		if (!c.returning) {
			if (c.reserved > 0) {
				deposit(c);
			}else if (c.reservedGlobally > c.amountCarried && fetch(c)) {
				
			}else if (c.amountCarried != 0 && PATH().finders.entryPoints.find(c.ctx(), c.cty(), c.path, Integer.MAX_VALUE)) {
				c.move();
				c.returning = true;
			}else {
				return false;
			}
			return true;
			
		}else {
			return false;
		}
	}

	@Override
	public void cancel(Caravan c) {
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		if (c.reserved > 0) {
			coo.set(c.path.destX(), c.path.destY());
			int am = f.reserved(c.res, coo);
			if (am > 0) {
				if (am > c.reserved)
					am = c.reserved;
				f.finish(c.res, coo, am);
			}
		}
		c.reserved = 0;
		f.cancel(c.res, c.reservedGlobally);
		c.reservedGlobally = 0;
		
	}
	
	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.reservedGlobally-c.amountCarried > 0)
			box.setResource(c.res, c.reservedGlobally-c.amountCarried);
	}
	
}
