package settlement.room.main.job;

import static settlement.main.SETT.*;

import init.boostable.BOOSTABLE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finder.SPath;
import settlement.room.main.ROOMA;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract class StorageCrate implements RESOURCE_TILE, TILE_STORAGE{

	final static int noRes = 0x0;
	private final static Bits bRes 				= new Bits(0x000000FF);
	private final static Bits bAmount 			= new Bits(0x0000FF00);
	private final static Bits bReserved 		= new Bits(0x00FF0000);
	private final static Bits bReservedSpace 	= new Bits(0xFF000000);
	private int tx, ty;
	private final int max;
	private ROOMA room;
	
	protected StorageCrate(int max){
		this.max = max;
	}
	
	public StorageCrate get(int tx, int ty, ROOMA a) {
		if (is(tx, ty)) {
			this.tx = tx;
			this.ty = ty;
			room = a;
			return this;
		}
		return null;
	}
	
	protected abstract boolean is(int tx, int ty);
	
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
			throw new RuntimeException(storageReservable() + " " + amount);
			
		reservedSpaceSet(reservedSpace()+amount);
	}

	@Override
	public void storageUnreserve(int amount) {
		if (storageReserved() < amount)
			amount = storageReserved();
		reservedSpaceSet(storageReserved()-amount);
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
			if (findableReservedCanBe())
				PATH().finders.resource.reportAbsence(this);
			if (storageReservable() > 0)
				PATH().finders.storage.reportAbsence(this);
		}
	}
	
	public void add() {
		RESOURCE r = resource();
		if (r != null) {
			count(r.bIndex(), 1, bAmount.get(data()), bAmount.get(data()) - bReserved.get(data()), bReservedSpace.get(data()));
			if (findableReservedCanBe()) {
				PATH().finders.resource.reportPresence(this);
			}
			if (storageReservable() > 0) {
				PATH().finders.storage.reportPresence(this);
			}
		}
	}
	
	protected abstract void count(int res, int crates, int amountTot, int amountUnres, int spaceRes);

	@Override
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
		SETT.ROOMS().data.set(room, tx, ty, d);
	}
	

	
	@Override
	public int y() {
		return ty;
	}
	
	@Override
	public int x() {
		return tx;
	}
	
	@Override
	public boolean findableReservedIs() {
		return bReserved.get(data()) > 0;
	}
	
	@Override
	public boolean findableReservedCanBe() {
		return bReserved.get(data()) < bAmount.get(data());
	}
	
	@Override
	public void findableReserveCancel() {
		if (reserved() > 0)
			reservedSet(reserved()-1);
	}
	
	@Override
	public void findableReserve() {
		reservedSet(reserved()+1);
	}
	
	@Override
	public void resourcePickup() {
		findableReserveCancel();
		amountSet(amount()-1);
	}
	
	@Override
	public int reservable() {
		return StorageCrate.this.amount()-StorageCrate.this.reserved();
	}
	
	public void clear() {
		if (resource() == null)
			return;
		int am = amount();
		remove();
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
	
	@Override
	public boolean isStoring() {
		return true;
	}

	public interface STORAGE_CRATE_HASSER {
		
		public TILE_STORAGE job(COORDINATE start, SPath path);
		public TILE_STORAGE job(int tx, int ty);
		
		public boolean getsMaximum(RESOURCE res);
		
		public default boolean fetchesFromEveryone(RESOURCE res) {
			return false;
		}
		
		public BOOSTABLE carryBonus();
		
	}

}