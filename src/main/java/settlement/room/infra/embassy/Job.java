package settlement.room.infra.embassy;

import static settlement.main.SETT.*;

import init.resources.RBIT;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.util.RoomBits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

class Job implements SETT_JOB{

	private final Coo coo = new Coo();
	private final RoomBits reserved = new RoomBits(coo, 0b001);
	private EmbassyInstance ins;
	static final double time = 45;
	
	private final ROOM_EMBASSY b;
	Job(ROOM_EMBASSY b){
		this.b = b;
	}
	
	SETT_JOB get(int tx, int ty) {
		ins = b.get(tx, ty);
		if (ins == null)
			return null;
		if (ROOMS().fData.tileData.get(tx, ty) == Constructor.IWORK) {
			coo.set(tx, ty);
			return this;
		}
		return null;
		
	}

	@Override
	public void jobReserve(RESOURCE r) {
		if (reserved.get() == 1) {
			throw new RuntimeException();
		}
		reserved.set(ins, 1);
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return reserved.get() == 1;
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		reserved.set(ins, 0);
	}
	
	@Override
	public boolean jobReserveCanBe() {
		return !jobReservedIs(null);
	}

	@Override
	public RBIT jobResourceBitToFetch() {
		return null;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return time;
	}

	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		reserved.set(ins, 0);
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
	public boolean jobUseTool() {
		return false;
	}

	@Override
	public Sound jobSound() {
		return b.employment().sound();
	}

	@Override
	public void jobStartPerforming() {
		// TODO Auto-generated method stub
		
	}
	
	
}
