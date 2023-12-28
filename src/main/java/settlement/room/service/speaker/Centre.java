package settlement.room.service.speaker;

import init.resources.RBIT;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FSERVICE;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

final class Centre {

	private final Bits dused = new Bits(0b0000_0000_0000_0000_0000_0000_0000_0001);
	private final Bits dreserved = new Bits(0b0000_0000_0000_0000_0000_0000_0000_0010);
	private final Bits dservices = new Bits(0b0000_0000_0000_0000_0011_1111_1111_1100);
	private SpeakerInstance ins;
	private final Coo coo = new Coo();
	private int data;
	private final ROOM_SPEAKER b;
	
	Centre(ROOM_SPEAKER b){
		this.b = b;
	}
	
	public SETT_JOB job(int tx, int ty) {
		if (init(tx, ty))
			return job;
		return null;
	}
	
	public FSERVICE service(int tx, int ty) {
		if (init(tx, ty))
			return service;
		return null;
	}
	
	private boolean init(int tx, int ty) {
		ins = b.getter.get(tx, ty);
		if (ins != null && tx == ins.body().cX() && ty == ins.body().cY()) {
			coo.set(tx, ty);
			data = SETT.ROOMS().data.get(tx, ty);
			return true;
		}
		return false;
	}
	
	private void save() {
		int ndata = data;
		data = SETT.ROOMS().data.get(coo);
		if (service.findableReservedCanBe()) { 
			ins.service.report(service, ins.blueprintI().data, -dservices.get(data));
		}
		data = ndata;
		if (service.findableReservedCanBe()) { 
			ins.service.report(service, ins.blueprintI().data, dservices.get(data));
		}
		SETT.ROOMS().data.set(ins, coo, data);
		
	}
	
	void dispose(int tx, int ty){
		if (init(tx, ty)) {
			data = dused.set(data, 0);
			save();
		}
		
	}
	
	private int services() {
		return dservices.get(data) * dused.get(data);
	}
	
	
	private final FSERVICE service = new FSERVICE(){
		
		@Override
		public void consume() {
			
		}
		
		@Override
		public int x() {
			return ins.body().cX();
		}

		@Override
		public int y() {
			return ins.body().cY();
		}

		@Override
		public boolean findableReservedCanBe() {
			return services() > 0;
		}

		@Override
		public void findableReserve() {
			if (!findableReservedCanBe()) {
				throw new RuntimeException();	
			}
			data = dservices.inc(data, -1);
			save();
		}

		@Override
		public boolean findableReservedIs() {
			return dused.get(data) == 1 && dservices.get(data) < SpeakerInstance.services;
		}

		@Override
		public void findableReserveCancel() {
			if (dused.get(data) == 1 && dservices.get(data) < SpeakerInstance.services) {
				data = dservices.inc(data, 1);
				save();
			}
		}
		
	};
	
	private final SETT_JOB job = new SETT_JOB() {
		
		@Override
		public boolean jobUseTool() {
			return false;
		}
		
		@Override
		public void jobStartPerforming() {
			data = dused.set(data, 1);
			data = dservices.set(data, SpeakerInstance.services);
			save();
		}
		
		@Override
		public Sound jobSound() {
			return null;
		}
		
		@Override
		public RBIT jobResourceBitToFetch() {
			return null;
		}
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return dreserved.get(data) == 1;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			data = dused.set(data, 0);
			data = dreserved.set(data, 0);
			data = dservices.set(data, 0);
			save();
		}
		
		@Override
		public boolean jobReserveCanBe() {
			return !jobReservedIs(null);
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			data = dreserved.set(data, 1);
			save();
		}
		
		@Override
		public double jobPerformTime(Humanoid a) {
			return 0;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
			jobReserveCancel(r);
			return null;
		}
		
		@Override
		public CharSequence jobName() {
			return b.employment().verb;
		}
		
		@Override
		public COORDINATE jobCoo() {
			return coo;
		}
	};
	
	public JOB_MANAGER manager(SpeakerInstance ins) {
		this.ins = ins;
		return manager;
	}
	
	private final JOB_MANAGER manager = new JOB_MANAGER() {
		
		@Override
		public boolean resourceReachable(RESOURCE res) {
			return true;
		}
		
		@Override
		public SETT_JOB reportResourceMissing(RBIT resourceMask, int jx, int jy) {
			return null;
		}
		
		@Override
		public SETT_JOB getReservableJob(COORDINATE c) {
			return job(ins.body().cX(), ins.body().cY());
		}
		
		@Override
		public SETT_JOB getJob(COORDINATE c) {
			return job(ins.body().cX(), ins.body().cY());
		}

		@Override
		public void reportResourceFound(RESOURCE res) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resetResourceSearch() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean resourceShouldSearch(RESOURCE res) {
			return true;
		}
	};
}
