package settlement.room.infra.transport;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.D;
import init.resources.*;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.main.util.RoomBits;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;

final class Cart {

	final static int LOAD = 255;
	

	final Storage storage = new Storage();
	final CartWork job = new CartWork();
	
	static CharSequence ¤¤organise = "¤Organizing";
	static CharSequence ¤¤Transporting = "¤Transporting";
	static {
		D.ts(Cart.class);
	}
	
	Cart(ROOM_TRANSPORT blue){
		
	}
	
	private static ROOM_TRANSPORT b() {
		return SETT.ROOMS().TRANSPORT;
	}
	
	public CartWork getJob(int tx, int ty) {
		return job.init(tx, ty);
	}
	
	public Storage getStorage(int tx, int ty) {
		return storage.init(tx, ty);
	}
	
	public Storage getStorageRel(int tx, int ty) {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (getStorage(tx+d.x(), ty+d.y()) != null) {
				if (SETT.ROOMS().map.instance.get(tx, ty).is(tx, ty, d))
					return storage;
			}
		}
		return null;
	}
	
	public boolean isReadyToGo(int tx, int ty) {
		CartWork j = getJob(tx, ty);
		if (j == null || !j.canGo())
			return false;
		Storage t = getStorageRel(tx, ty);
		if (t == null || !t.canGo())
			return false;
		return true;
	}
	
	public void reserveGo(int tx, int ty) {
		Storage t = getStorageRel(tx, ty);
		t.sreserved.set(t.sreserved.max());
		
	}
	
	public int goAmount(int tx, int ty) {
		Storage t = getStorageRel(tx, ty);
		return t.samount.get();
	}
	
	public void cancelGo(int tx, int ty) {
		
		SETT_JOB j = getJob(tx, ty);
		if (j != null) {
			Storage t = getStorageRel(tx, ty);
			t.sreserved.set(0);
		}
		
	}
	
	public boolean go(TransportInstance ins, int tx, int ty) {
		
		CartWork j = getJob(tx, ty);
		if (j != null) {
			j.waway.set(ins, 1);
			j.tending.set(ins, 0);
			j.wreserved.set(ins, 0);
			j.wlivestockCount.inc(ins, -1);
			
			Storage t = getStorageRel(tx, ty);
			t.samount.set(0);
			t.saway.set(1);
			
			
			
			return true;
		}
		return false;
	}
	
	public boolean gofinish(TransportInstance ins, int tx, int ty) {
		
		cancelGo(tx, ty);
		CartWork j = getJob(tx, ty);
		if (j != null) {
			j.waway.set(ins, 0);
			Storage t = getStorageRel(tx, ty);
			t.saway.set(0);
			t.sreserved.set(0);
			return true;
		}
		return false;
	}
	
	public void clear(int tx, int ty) {
		storage.dispose(tx, ty);
		job.dispose(tx, ty);
	}
	
	
	
	static class Storage implements TILE_STORAGE {
		
		private final Coo coo = new Coo();
		final INTE samount = 			new CData(0b0000_0000_0000_0000_0000_0001_1111_1111, LOAD);
		final INTE sreserved = 			new CData(0b0000_0000_0000_0011_1111_1110_0000_0000, LOAD);
		final INTE saway = 				new CData(0b0010_0000_0000_0000_0000_0000_0000_0000);
		
		private TransportInstance ins;
		private int data;
		
		public Storage init(int tx, int ty) {
			if (b().is(tx, ty) && SETT.ROOMS().fData.tile.is(tx, ty, b().constructor.ca)){
				coo.set(tx,ty);
				ins = b().get(tx, ty);
				data = SETT.ROOMS().data.get(coo);
				return this;
			}
			return null;
			
		}
		
		public void dispose(int tx, int ty) {
			if (init(tx, ty) != null){
				int am = samount.get();
				if (storageReservable() > 0 && resource() != null)
					SETT.PATH().finders.storage.reportAbsence(this);
				SETT.ROOMS().data.set(ins, tx, ty, 0);
				if (am > 0 && ins.resource() != null) {
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						if (SETT.PATH().reachability.is(tx, ty, DIR.ORTHO.get(di))) {
							SETT.THINGS().resources.create(tx+DIR.ORTHO.get(di).x(), ty+DIR.ORTHO.get(di).y(), ins.resource(), am);
							return;
						}
					}
					
				}
			}
		}
		
		public boolean canGo() {
			if (sreserved.get() > 0)
				return false;
			if (samount.get() < 16)
				return false;
			if (samount.get() < 32 && ins.resourceHas)
				return false;
			return true;
		}
		
		@Override
		public int y() {
			return coo.y();
		}
		
		@Override
		public int x() {
			return coo.x();
		}
		
		@Override
		public void storageUnreserve(int amount) {
			sreserved.inc(-amount);
		}
		
		@Override
		public int storageReserved() {
			return sreserved.get();
		}
		
		@Override
		public void storageReserve(int amount) {
			sreserved.inc(amount);
		}
		
		@Override
		public int storageReservable() {
			return samount.max()-samount.get()-sreserved.get();
		}
		
		@Override
		public void storageDeposit(int a) {
			samount.inc(a);
			sreserved.inc(-a);
		}
		
		@Override
		public RESOURCE resource() {
			return ins.resource();
		}
		
		class CData implements INTE {
			
			private final int max;
			private final Bits bits;
			
			CData(int bits){
				this.bits = new Bits(bits);
				max = this.bits.mask;
			}
			
			CData(int bits, int max){
				this.bits = new Bits(bits);
				this.max = max;
			}
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return max;
			}
			
			@Override
			public int get() {
				return bits.get(data);
			}
			
			@Override
			public void set(int t) {
				data = bits.set(data, CLAMP.i(t, 0, LOAD));
				save();
			}
			
		}
		
		void save() {
			int old = data;
			data = SETT.ROOMS().data.get(coo);
			if (storageReservable() > 0 && resource() != null)
				SETT.PATH().finders.storage.reportAbsence(this);
			SETT.ROOMS().data.set(ins, coo, old);
			data = old;
			if (storageReservable() > 0 && resource() != null)
				SETT.PATH().finders.storage.reportPresence(this);
		}
		
	}

	
	
	
	static class CartWork implements SETT_JOB {
		
		private final Coo coo = new Coo();
		private TransportInstance ins;
		
		final RoomBits wlivestockCount =	new RoomBits(coo, 0b0000_0000_0111_1100_0000_0000_0000_0000);
		final RoomBits wlivestock = 		new RoomBits(coo, 0b0000_0000_0100_0000_0000_0000_0000_0000);
		
		final RoomBits wreserved =	 		new RoomBits(coo, 0b0000_0000_1000_0000_0000_0000_0000_0000);
		final RoomBits tending = 			new RoomBits(coo, 0b0000_1111_0000_0000_0000_0000_0000_0000);
		final RoomBits waway = 				new RoomBits(coo, 0b0001_0000_0000_0000_0000_0000_0000_0000);
		private int time = (int) (TIME.secondsPerDay*Humanoid.WORK_PER_DAY/(tending.max()*2));
		

		
		CartWork init(int tx, int ty) {
			if (b().is(tx, ty) && SETT.ROOMS().fData.tile.is(tx, ty, b().constructor.an)){
				coo.set(tx,ty);
				ins = b().get(tx, ty);
				return this;
			}
			return null;
			
		}
		
		public void dispose(int tx, int ty) {
			if (init(tx, ty) != null){
				wreserved.set(ins, 0);
				tending.set(ins, 0);
				waway.set(ins, 0);
			}
		}
		
		
		
		@Override
		public boolean jobUseTool() {
			return false;
		}
		
		@Override
		public void jobStartPerforming() {
			
		}
		
		@Override
		public Sound jobSound() {
			return null;
		}
		
		@Override
		public RBIT jobResourceBitToFetch() {
			if (wlivestock.get() < 1)
				return RESOURCES.LIVESTOCK().bit;
			return null;
		}
		
		@Override
		public boolean longFetch() {
			return true;
		};
		
		@Override
		public int jobResourcesNeeded() {
			return 1;
		};
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return wreserved.get() != 0;
		}
		
		public boolean canGo() {
			if (waway.get() == 1)
				return false;
			if (wreserved.get() == 1)
				return false;
			if (jobResourceBitToFetch() != null)
				return false;
			return tending.get() == tending.max();
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			wreserved.set(ins, 0);
		}
		
		@Override
		public boolean jobReserveCanBe() {
			if (waway.get() == 1)
				return false;
			if (wreserved.get() == 1)
				return false;
			if (jobResourceBitToFetch() != null)
				return true;
			return tending.get() != tending.max();
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			wreserved.set(ins, 1);
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return time;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ri) {
			jobReserveCancel(r);
			
			if (r == RESOURCES.LIVESTOCK()) {
				FACTIONS.player().res().inc(r, RTYPE.CONSUMED, -ri);
				wlivestockCount.set(ins, wlivestockCount.max());
			}else {
				tending.inc(ins, 1);
			}
			
			return null;
		}
		
		@Override
		public CharSequence jobName() {
			return ¤¤organise;
		}
		
		@Override
		public COORDINATE jobCoo() {
			return coo;
		}
		
	}

	
}
