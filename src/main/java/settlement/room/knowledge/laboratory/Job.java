package settlement.room.knowledge.laboratory;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.IndustryUtil;
import snake2d.util.bit.Bit;
import snake2d.util.datatypes.*;

class Job implements SETT_JOB{

	private final static Bit reserved = new Bit	(0b0000000000010);
	final static Bit used = 	new Bit			(0b0000000000100);
	
	private int data;
	private final Coo coo = new Coo();
	private LaboratoryInstance ins;
	static final double time = 45;
	
	private final ROOM_LABORATORY b;
	
	Job(ROOM_LABORATORY b){
		this.b = b;
	}
	
	SETT_JOB get(int tx, int ty) {
		ins = b.get(tx, ty);
		if (ins == null)
			return null;
		data = ROOMS().data.get(tx, ty);
		if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.WORK) {
			coo.set(tx, ty);
			return this;
		}
		return null;
		
	}
	
	boolean used(int tx, int ty) {
		ins = b.get(tx, ty);
		if (ins == null)
			return false;
		data = ROOMS().data.get(tx, ty);
		return used.is(data);
		
	}
	
	@Override
	public void jobReserve(RESOURCE r) {
		if (reserved.is(data)) {
			throw new RuntimeException();
		}
		data = reserved.set(data);
		save();
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return reserved.is(data);
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		data = reserved.clear(data);
		data = used.clear(data);
		save();
	}

	@Override
	public boolean jobReserveCanBe() {
		return !jobReservedIs(null);
	}

	@Override
	public long jobResourceBitToFetch() {
		return 0;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return time;
	}

	@Override
	public void jobStartPerforming() {
		data = used.set(data, true);
		save();
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		
	
		double d = IndustryUtil.calcProductionRate(1, skill, null, b.bonus, ins);
		
		ins.performJob(d);
		jobReserveCancel(null);
		return null;
	}

	@Override
	public COORDINATE jobCoo() {
		return coo;
	}
	
	@Override
	public DIR jobStandDir() {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (ins.is(coo, DIR.ORTHO.get(di)) && SETT.ROOMS().fData.sprite.is(coo, DIR.ORTHO.get(di), b.constructor.schair))
				return DIR.ORTHO.get(di);
		}
		return null;
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
	public boolean jobUseHands() {
		return false;
	}

	@Override
	public Sound jobSound() {
		return b.employment().sound();
	}
	
	private void save() {
		ROOMS().data.set(ins, coo, data);
	}
	


	
	
}
