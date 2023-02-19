package settlement.room.home.chamber;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RES_AMOUNT;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.job.SETT_JOB;
import settlement.path.AVAILABILITY;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

final class Work implements SETT_JOB{
	
	private static final int NEEDS = 0;
	private static final int RESERVED = 1;
	private int data;
	private final Coo coo = new Coo();
	private ChamberInstance ins;
	private final ROOM_CHAMBER blue;
	
	Work(ROOM_CHAMBER blue) {
		this.blue = blue;
	}
	
	Work get(int tx, int ty) {
		if (blue.is(tx, ty)) {
			int data = ROOMS().data.get(tx, ty);
			if (PATH().availability.get(tx, ty)== AVAILABILITY.ROOM) {
				this.data = data;
				this.coo.set(tx, ty);
				this.ins = blue.get(tx, ty);
				
				return this;
			}
		}
		return null;
	}
	
	void clear() {
		ROOMS().data.set(ins, coo, 0);
	}
	
	private void save() {
		
		int old = ROOMS().data.get(coo);
		
		if (old != data) {
			ROOMS().data.set(ins, coo, data);
		}
	}
	
	private int state() {
		return data & 0b01111;
	}
	
	private void stateSet(int state) {
		data &= ~0b01111;
		data |= state;
		save();
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
		return ins.blueprintI().employment().sound();
	}
	
	int i = 0;
	
	@Override
	public long jobResourceBitToFetch() {
		i++;
		if ((i & 1) == 0 && !ins.fetching && ins.occupant() != null) {
			long m = 0;
			int i = 0;
			for (RES_AMOUNT am : ins.occupant().race().home().clas(HCLASS.NOBLE).resources()) {
				if (STATS.HOME().shouldFetch(ins.occupant(), i++) && ins.jobs.resourceReachable(am.resource()))
					m |= am.resource().bit;
			}
			return m;
		}
		return 0;
	}
	
	
	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return state() == RESERVED;
	}
	
	@Override
	public void jobReserveCancel(RESOURCE r) {
		if (jobReservedIs(r)) {
			if (r != null)
				ins.fetching = false;
			stateSet(NEEDS);
		}
	}
	
	@Override
	public boolean jobReserveCanBe() {
		return state() == NEEDS || state() == 2;
	}
	
	@Override
	public void jobReserve(RESOURCE r) {
		if (!jobReserveCanBe())
			throw new RuntimeException();
		stateSet(RESERVED);
		if (r != null)
			ins.fetching = true;
	}
	
	@Override
	public double jobPerformTime(Humanoid skill) {
		return 30;
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE res, int ram) {
		if (!jobReservedIs(res))
			throw new RuntimeException();
		stateSet(NEEDS);
		
		if (res != null) {
			if (ins.occupant() != null) {
				STATS.HOME().fetchResource(ins.occupant(), res);
			}
			ins.fetching = false;
		}
		
		return null;
	}
	
	@Override
	public CharSequence jobName() {
		return ins.blueprintI().employment().verb;
	}
	
	@Override
	public COORDINATE jobCoo() {
		return coo;
	}


	
}
