package settlement.room.health.physician;

import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FSERVICE;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomBits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

final class Service {

	private final Coo coo = new Coo();
	
	
	private final RoomBits s_worked = new RoomBits(coo, 		0b0000_0000_0001);
	private final Bit s_reservable = new Bit(coo, 				0b0000_0000_0010);
	private final Bit s_reserved = new Bit(coo, 				0b0000_0000_0100);
	
	
	private Instance ins;
	private static final Service self = new Service();
	
	private static ROOM_PHYSICIAN b() {
		return SETT.ROOMS().PHYSICIAN;
	}
	
	static FSERVICE getS(int tx, int ty) {
		self.ins = b().get(tx, ty);
		if (self.ins != null && SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.BIT_SERVICE) {
			self.coo.set(tx, ty);
			return self.service;
		}
		return null;
	}
	
	static void dispose(Instance ins, int tx, int ty) {
		if (getS(tx, ty) != null)
			self.s_worked.set(ins, 0);
	}
	
	static SETT_JOB getJ(int tx, int ty) {
		self.ins = b().get(tx, ty);
		if (self.ins != null && SETT.ROOMS().fData.tileData.get(tx, ty) != 0) {
			self.coo.set(tx, ty);
			return self.jo;
		}
		return null;
	}

	private final FSERVICE service = new FSERVICE() {
		
		@Override
		public boolean findableReservedCanBe() {
			return s_reservable.get() == 1 && s_reserved.get() == 0;
		}

		@Override
		public void findableReserve() {
			ins.jobs.searchAgain();
			s_reserved.set(ins, 1);
		}

		@Override
		public boolean findableReservedIs() {
			return s_reserved.get() == 1;
		}

		@Override
		public void findableReserveCancel() {
			s_reserved.set(ins, 0);
		}

		@Override
		public int x() {
			return coo.x();
		}

		@Override
		public int y() {
			return coo.y();
		}

		@Override
		public void startUsing() {
			
		}
		
		@Override
		public void consume() {
			s_reservable.set(ins, 0);
			s_reserved.set(ins, 0);
		}
	};
	
	private final SETT_JOB jo = new SETT_JOB() {
		
		@Override
		public boolean jobUseTool() {
			return false;
		}
		
		@Override
		public boolean jobUseHands() {
			return false;
		};
		
		@Override
		public void jobStartPerforming() {
			
		}
		
		@Override
		public Sound jobSound() {
			return null;
		}
		
		@Override
		public long jobResourceBitToFetch() {
			return 0;
		}
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return s_worked.get() == 1;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			s_worked.set(ins, 0);
		}
		
		@Override
		public boolean jobReserveCanBe() {
			return (s_reservable.get() == 0 || s_reserved.get() == 1) && !jobReservedIs(null);
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			s_worked.set(ins, 1);
		}
		
		@Override
		public double jobPerformTime(Humanoid a) {
			return 20;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
			s_worked.set(ins, 0);
			s_reservable.set(ins, 1);
			return null;
		}

		
		@Override
		public CharSequence jobName() {
			return b().employment().verb;
		}
		
		@Override
		public COORDINATE jobCoo() {
			return coo;
		}
	};
	
	private  class Bit extends RoomBits {

		public Bit(COORDINATE coo, int mask) {
			super(coo, mask);

		}
		
		@Override
		public void set(RoomInstance r, int t) {
			ins.service.report(service, ins.blueprintI().data, -1);
			super.set(r, t);
			ins.service.report(service, ins.blueprintI().data, 1);
		};
		
		
	}

}
