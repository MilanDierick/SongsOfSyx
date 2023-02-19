package settlement.room.military.supply;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.thing.halfEntity.caravan.CaravanPickup;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;

final class Crate implements TILE_STORAGE{

	final static int noRes = 0x0;
	private final static Bits bRes 				= new Bits(0x000000FF);
	private final static Bits bAmount 			= new Bits(0x0000FF00);
	private final static Bits bReserved 		= new Bits(0x00FF0000);
	private final static Bits bReservedSpace 	= new Bits(0xFF000000);
	private int tx, ty;
	private final int max;
	
	public final Pick pickup = new Pick();
	
	protected final ROOM_SUPPLY b;
	SupplyInstance ins;
	
	protected Crate(ROOM_SUPPLY b){
		this.max = Constructor.STORAGE;
		this.b = b;
	}
	
	public Crate get(int tx, int ty) {
		if (is(tx, ty)) {
			this.tx = tx;
			this.ty = ty;
			return this;
		}
		return null;
	}
	
	protected boolean is(int tx, int ty) {
		if (b.is(tx, ty)) {
			ins = b.getter.get(tx, ty);
			if (SETT.ROOMS().fData.tileData.is(tx, ty, 1)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void storageDeposit(int amount) {
		if (resource() == null || amount() + amount > max)
			throw new RuntimeException(resource() + " " + amount() + " " + amount + " " + max);
		reservedSpaceSet(reservedSpace()-amount);
		amountSet(amount()+amount);
	}

	@Override
	public int storageReserved() {
		if (resource() == null)
			return 0;
		return bReservedSpace.get(data());
	}

	@Override
	public int storageReservable() {
		if (resource() == null)
			return 0;
		
		return max - (amount() + storageReserved());
	}

	@Override
	public void storageReserve(int amount) {
		if (storageReservable() < amount)
			throw new RuntimeException();
			
		reservedSpaceSet(reservedSpace()+amount);
	}

	@Override
	public void storageUnreserve(int amount) {
		if (storageReserved() < amount)
			amount = storageReserved();
		reservedSpaceSet(reservedSpace()-amount);
	}
	
	@Override
	public RESOURCE resource() {
		int i = bRes.get(data());
		if (i == noRes)
			return null;
		return RESOURCES.ALL().get(i-1);
	}
	
	public static RESOURCE resource(int data) {
		int i = bRes.get(data);
		if (i == noRes)
			return null;
		return RESOURCES.ALL().get(i-1);
	}
	
	public static int amount(int data) {
		return bAmount.get(data);
	}
	
	public void resourceSet(RESOURCE res) {
		if (resource() != null) {
			throw new RuntimeException();
		}
		save(bRes.set(data(), res.bIndex()+1));
		add();
	}
	
	public void remove() {
		RESOURCE r = resource();
		if (r != null) {
			count(r.bIndex(), -1, -bAmount.get(data()), -(bAmount.get(data()) - bReserved.get(data())), -bReservedSpace.get(data()));
//			if (storageReservable() > 0)
//				PATH().finders.storage.reportAbsence(this);
		}
		
	}
	
	public void add() {
		RESOURCE r = resource();
		if (r != null) {
			count(r.bIndex(), 1, bAmount.get(data()), bAmount.get(data()) - bReserved.get(data()), bReservedSpace.get(data()));
//			if (storageReservable() > 0)
//				PATH().finders.storage.reportPresence(this);
		}
	}
	
	protected void count(int res, int crates, int amountTot, int amountUnres, int spaceRes) {
		ins.addCrate(res, crates, amountTot, amountUnres, spaceRes);
		b.tally.count(RESOURCES.ALL().get(res), amountTot, amountTot-amountUnres);
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
	
	private void reservedSpaceSet(int r) {
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
	

	
	@Override
	public int y() {
		return ty;
	}
	
	@Override
	public int x() {
		return tx;
	}
	

	
	public void clear() {
		if (resource() == null)
			return;
		remove();
		int am = pickup.reservable();
		if (am > 0) {
			for (DIR dd : DIR.ORTHO) {
				if (!PATH().solidity.is(this, dd)) {
					SETT.THINGS().resources.create(x()+dd.x(), y()+dd.y(), resource(), am);
					break;
				}
			}
		}
		
		save(bRes.set(0, noRes));
	}
	
	public void dispose(){
		RESOURCE res = resource();
		if (res == null)
			return;
		int am = amount();
		remove();
		if (am > 0)
			SETT.THINGS().resources.create(tx, ty, res, am);
		
		save(bRes.set(0, noRes));
	}
	
	public final class Pick implements CaravanPickup {
		
		private Pick() {
			
			
		}
		
		@Override
		public int reserved() {
			return bReserved.get(data());
		}
		
		@Override
		public int reservable() {
			return Crate.this.amount()-reserved();
		}
	
		@Override
		public void reserve(int i) {
			reservedSet(CLAMP.i(reserved()+i, 0, amount()));
		}
		
		@Override
		public void pickup(int i) {
			reserve(-i);
			amountSet(CLAMP.i(amount()-i, reserved(), amount()));
		}

		@Override
		public int x() {
			return tx;
		}

		@Override
		public int y() {
			return ty;
		}


		
	}

}
