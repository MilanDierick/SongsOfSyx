package settlement.room.military.supply;

import static settlement.main.SETT.*;

import game.GAME;
import game.boosting.Boostable;
import init.resources.*;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finder.SPath;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.StorageCrate;
import settlement.room.main.util.RoomInit;
import snake2d.util.datatypes.COORDINATE;

final class SupplyInstance extends RoomInstance implements StorageCrate.STORAGE_CRATE_HASSER{

	private static final long serialVersionUID = 1L;

	short resI = -1;
	boolean fetch = true;
	boolean unavailable = false;
	private int amount;
	private int spaceRes;
	private int reserved;
	short fill = 10;
	boolean auto = true;

	SupplyInstance(ROOM_SUPPLY blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		ROOMS().data.set(this, mX(), mY(), 0);
		employees().maxSet((int) (blueprint.constructor.workers.get(this)));
		employees().neededSet((int) Math.ceil(employees().max()*0.5));

		activate();
	}

	void addCrate(int res, int crates, int amountTot, int amountUnres, int spaceRes) {
		this.amount += amountTot;
		this.spaceRes += spaceRes;
		if (this.spaceRes < 0)
			this.spaceRes = 0;
		reserved += amountTot - amountUnres;
		if (this.reserved < 0)
			this.reserved = 0;
	}

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub

	}

	public void setResource(RESOURCE res) {
		resI = -1;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
				blueprintI().crate.clear();
			}
		}
		if (res != null) {
			for (COORDINATE c : body()) {
				if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
					blueprintI().crate.resourceSet(res);
				}
			}
			resI = (byte) res.index();
		}
		
	}
	
	public int amount() {
		return amount;
	}
	
	public int reserved() {
		return reserved;
	}
	
	public RESOURCE resource() {
		if (resI == -1)
			return null;
		return RESOURCES.ALL().get(resI);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {

		unavailable = false;
	}

	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
				blueprintI().crate.dispose();
			}
		}
	}

	@Override
	public ROOM_SUPPLY blueprintI() {
		return ROOMS().SUPPLY;
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		return true;
	}

	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		return null;
	}
	
	public boolean fetch() {
		return fetch;
	}
	
	public void fetch(boolean f) {
		if (f != fetch) {
			
			for (COORDINATE c : body()) {
				if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
					blueprintI().crate.remove();
				}
			}
			fetch = f;
			for (COORDINATE c : body()) {
				if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
					blueprintI().crate.add();
				}
			}
		}
	}

	@Override
	public TILE_STORAGE job(COORDINATE start, SPath path) {
		
		
		if (resource() == null)
			return null;
		if (unavailable)
			return null;
		
		if (amount + spaceRes >= fill*blueprintI().constructor.storage.get(this)/10)
			return null;
		
		RESOURCE r;
		
		if (fetch) {
			r = SETT.PATH().finders.resource.normal.reserve(start, resource().bit, path, Integer.MAX_VALUE);
		}else
			r = SETT.PATH().finders.resource.find(resource().bit, resource().bit, fetch ? resource().bit : RBIT.NONE, start, path, Integer.MAX_VALUE);
		if (r == null) {
			unavailable = true;
			return null;
		}
		
		
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
				if (blueprintI().crate.storageReservable() > 0)
					return blueprintI().crate;
			}
		}
		
		PATH().finders.resource.unreserve(r, path.destX(), path.destY(), 1);
		
		GAME.Notify("weird! " + " " + amount + " " + spaceRes);
		unavailable = true;
		
		return null;
	}
	
	@Override
	public TILE_STORAGE job(int tx, int ty) {
		if (is(tx, ty))
			return blueprintI().crate.get(tx, ty);
		return null;
	}

	@Override
	public boolean getsMaximum(RESOURCE res) {
		return true;
	}
	
	@Override
	public boolean fetchesFromEveryone(RESOURCE res) {
		return fetch;
	}

	@Override
	public Boostable carryBonus() {
		return SETT.ROOMS().STOCKPILE.bonus();
	}
	
//	@Override
//	public TILE_STORAGE storage(int tx, int ty) {
//		return blueprintI().crate.get(tx, ty);
//	}

}
