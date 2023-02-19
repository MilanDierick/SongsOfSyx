package settlement.room.infra.hauler;

import static settlement.main.SETT.*;

import game.GAME;
import init.boostable.BOOSTABLE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finder.SPath;
import settlement.room.infra.transport.ROOM_DELIVERY_INSTANCE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.StorageCrate;
import settlement.room.main.util.RoomInit;
import snake2d.util.datatypes.COORDINATE;

final class HaulerInstance extends RoomInstance implements StorageCrate.STORAGE_CRATE_HASSER, ROOM_DELIVERY_INSTANCE{

	private static final long serialVersionUID = 1L;

	byte resourceI = -1;
	boolean fetch = true;
	boolean unavailable = false;
	short amount;
	final byte crates;
	private short spaceRes;
	boolean auto = true;

	HaulerInstance(ROOM_HAULER blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		ROOMS().data.set(this, mX(), mY(), 0);
		employees().maxSet(body().width()*5);
		employees().neededSet(body().width());
		int cr = 0;
		for (COORDINATE c : body()) {
			if (is(c) && blueprint.crate.is(c.x(), c.y()))
				cr ++;
		}
		crates = (byte) cr;
		activate();
	}

	void addCrate(int res, int crates, int amountTot, int amountUnres, int spaceRes) {
		this.amount += amountTot;
		this.spaceRes += spaceRes;
		if (this.spaceRes < 0)
			this.spaceRes = 0;
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
		resourceI = -1;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
				blueprintI().crate.clear();
			}
		}
		if (res != null) {
			for (COORDINATE c : body()) {
				if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
					blueprintI().crate.resourceSet(res);
				}
			}
			resourceI = res.bIndex();
		}
		
	}
	
	public RESOURCE resource() {
		if (resourceI == -1)
			return null;
		return RESOURCES.ALL().get(resourceI);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {

		unavailable = false;
	}

	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
				blueprintI().crate.dispose();
			}
		}
	}

	@Override
	public ROOM_HAULER blueprintI() {
		return ROOMS().HAULER;
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
				if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
					blueprintI().crate.remove();
				}
			}
			fetch = f;
			for (COORDINATE c : body()) {
				if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
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
		if (amount + spaceRes >= crates*Crate.size)
			return null;
		RESOURCE r = SETT.PATH().finders.resource.find(resource().bit, fetch ? resource().bit : 0, 0, start, path, Integer.MAX_VALUE);
		if (r == null) {
			unavailable = true;
			return null;
		}
		
		int bx = -1;
		int by = -1;
		int b = 0;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
				if (blueprintI().crate.storageReservable() > b) {
					b = blueprintI().crate.storageReservable();
					bx = c.x();
					by = c.y();
				}
					
			}
		}
		
		if (b > 0) {
			return blueprintI().crate.get(bx, by, this);
		}
		
		GAME.Notify("weird! " + " " + amount + " " + spaceRes);
		PATH().finders.resource.unreserve(r, path.destX(), path.destY(), 1);
		unavailable = true;
		
		return null;
	}

	@Override
	public boolean getsMaximum(RESOURCE res) {
		return fetch;
	}
	
	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return blueprintI().crate.get(tx, ty, this);
	}
	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return blueprintI().crate.get(tx, ty, this);
	}

	@Override
	public TILE_STORAGE job(int tx, int ty) {
		if (is(tx, ty))
			return blueprintI().crate.get(tx, ty, this);
		return null;
	}

	@Override
	public int deliverCapacity() {
		return Crate.size;
	}

	@Override
	public TILE_STORAGE getDeliveryCrate(long okMask, int minAmount) {
		if (resource() == null)
			return null;
		if ((okMask & resource().bit) == 0)
			return null;
		if (Crate.size*crates - amount - spaceRes < minAmount)
			return null;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y(), this) != null) {
				if (blueprintI().crate.storageReservable() >= minAmount)
					return blueprintI().crate;
			}
		}
		return null;
	}

	@Override
	public BOOSTABLE carryBonus() {
		return SETT.ROOMS().STOCKPILE.bonus;
	}

}
