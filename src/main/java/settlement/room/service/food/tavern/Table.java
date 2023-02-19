package settlement.room.service.food.tavern;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sound.SoundSettlement;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FSERVICE;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.misc.CLAMP;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.data.INT.INTE;

final class Table implements FSERVICE{


	private final Coo coo = new Coo();
	private TavernInstance ins;
	private int data;
	private final ROOM_TAVERN b;
	
	Table(ROOM_TAVERN b){
		this.b = b;
	}
	
	public BOOLEAN_MUTABLE jobReserved = new BOOLEAN_MUTABLE() {
		
		private final Bit bit = new Bit(0b0000_0000_0000_0000_0000_0000_0001);
		
		@Override
		public boolean is() {
			return bit.is(data);
		}
		
		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			data = bit.set(data, b);
			save();
			return this;
		}
	};
	
	public BOOLEAN_MUTABLE serviceReserved = new BOOLEAN_MUTABLE() {
		
		private final Bit bit = new Bit(0b0000_0000_0000_0000_0000_0000_0010);
		
		@Override
		public boolean is() {
			return bit.is(data);
		}
		
		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			data = bit.set(data, b);
			save();
			return this;
		}
	};
	
	public BOOLEAN_MUTABLE serviceUsed = new BOOLEAN_MUTABLE() {
		
		private final Bit bit = new Bit(0b0000_0000_0000_0000_0000_0000_0100);
		
		@Override
		public boolean is() {
			return bit.is(data);
		}
		
		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			data = bit.set(data, b);
			SETT.ROOMS().data.set(ins, coo, data);
			emptyAmount.set(0);
			return this;
		}
	};
	
	public INTE drinkAmount = new INTE() {
		
		private final Bits bits = new Bits(0b0000_0000_0000_0000_0000_1111_0000);
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 6;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}
		
		@Override
		public void set(int t) {
			data = bits.set(data, CLAMP.i(t, 0, max()));
			save();
		}
	};
	
	public INTE emptyAmount = new INTE() {
		
		private final Bits bits = new Bits(0b0000_0000_0000_0000_1111_0000_0000);
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 6;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}
		
		@Override
		public void set(int t) {
			data = bits.set(data, CLAMP.i(t, 0, max()));
			SETT.ROOMS().data.set(ins, coo, data);
		}
	};
	
	
	Table get(int tx, int ty) {
		if (b.is(tx, ty) && ROOMS().fData.sprite.is(tx, ty, b.constructor.sTable)) {
			data = ROOMS().data.get(tx, ty);
			ins = b.get(tx, ty);
			coo.set(tx, ty);
			return this;
		}
		return null;
	}
	
	private void save() {
		int old = data;
		data = ROOMS().data.get(coo);
		if (old == data)
			return;
		if (findableReservedCanBe())
			ins.service.report(this, b.serviceData, -1);
		data = old;
		if (findableReservedCanBe())
			ins.service.report(this, b.serviceData, 1);
		
		ROOMS().data.set(ins, coo, data);
		if (job.jobReserveCanBe())
			ins.jobs.searchAgain();
	}
	
	@Override
	public boolean findableReservedIs() {
		return serviceReserved.is();
	}
	
	@Override
	public boolean findableReservedCanBe() {
		return !serviceReserved.is() && drinkAmount.get() > 0;
	}

	@Override
	public void findableReserve() {
		if (!findableReservedCanBe())
			throw new RuntimeException();
		serviceReserved.set(true);
	}

	@Override
	public void findableReserveCancel() {
		serviceReserved.set(false);
	}
	
	@Override
	public void consume() {
		serviceUsed.set(false);
		drinkAmount.inc(-1);
		emptyAmount.inc(1);
		serviceReserved.set(false);
	}
	
	@Override
	public void startUsing() {
		serviceUsed.set(true);
	}
	
	public void dispose() {
		if (findableReservedCanBe())
			serviceReserved.set(true);
		int am = drinkAmount.get();
		if (am > 0) {
			SETT.THINGS().resources.create(coo.x(), coo.y(), RESOURCES.ALCOHOL(), am);
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

	final SETT_JOB job = new SETT_JOB() {
		
		@Override
		public void jobReserve(RESOURCE r) {
			jobReserved.set(true);			
		}

		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return jobReserved.is();
		}

		@Override
		public void jobReserveCancel(RESOURCE r) {
			jobReserved.set(false);
		}

		@Override
		public long jobResourceBitToFetch() {
			return RESOURCES.ALCOHOL().bit;
		}
		
		@Override
		public int jobResourcesNeeded() {
			return drinkAmount.max() - drinkAmount.get();
		};
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 5;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE res, int ram) {
			if (!jobReservedIs(res))
				throw new RuntimeException();
			jobReserved.set(false);
			drinkAmount.inc(ram);
			emptyAmount.inc(-ram);
			return null;
		}

		@Override
		public COORDINATE jobCoo() {
			return coo;
		}

		@Override
		public CharSequence jobName() {
			return b.employment().verb;
		}

		@Override
		public void jobStartPerforming() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean jobUseTool() {
			return false;
		}

		@Override
		public SoundSettlement.Sound jobSound() {
			return b.employment().sound();
		}

		@Override
		public boolean jobReserveCanBe() {
			return !jobReserved.is() && !serviceReserved.is() && drinkAmount.get() < 4;
		}
	};
	
	int amountFull(int data) {
		this.data = data;
		return drinkAmount.get();
	}
	
	int amountEmpty(int data) {
		this.data = data;
		return emptyAmount.get();
	}
	
	boolean used(int data) {
		this.data = data;
		return serviceUsed.is();
	}
	

	
	
	
}
