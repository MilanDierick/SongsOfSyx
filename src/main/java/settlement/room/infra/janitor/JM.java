package settlement.room.infra.janitor;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FINDABLE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

final class JM implements JOB_MANAGER{

	private JanitorInstance ins;
	private final ROOM_JANITOR b;
	private final Coo coo = new Coo();
	
	JM(ROOM_JANITOR b){
		this.b = b;
	}
	
	JOB_MANAGER get(JanitorInstance ins) {
		this.ins = ins;
		return this;
	}
	
	private SETT_JOB getReservableJob() {
		SETT_JOB j = get(ins.mX(), ins.mY(), ROOM_JANITOR.radius);
		
		if (j == null)
			ins.searching = false;
		return j;
	}

	@Override
	public SETT_JOB reportResourceMissing(long resourceMask, int jx, int jy) {
		ins = b.get(jx, jy);
		if (ins != null) {
			ins.resourcesMissing |= resourceMask;
			ins.resourcesFindable &= ~resourceMask;
			return get(ins.mX(), ins.mY(), ins.radius());
		}
		return null;
	}
	
	@Override
	public void reportResourceFound(long res) {

	}

	@Override
	public boolean resourceReachable(RESOURCE res) {
		
		long m = (ins.resourcesMissing & ins.resourcesNeeded);
		return (m & res.bit) == 0;
	}
	
	@Override
	public boolean resourceShouldSearch(RESOURCE res) {
		return (ins.resourcesFindable & res.bit) == 0;
	}
	
	@Override
	public SETT_JOB getReservableJob(COORDINATE prefered) {
		if (prefered == null) {
			return getReservableJob();
		}
		
		int tx = prefered.x();
		int ty = prefered.y();
		
		coo.set(tx, ty);
		FINDABLE f = SETT.MAINTENANCE().finder().getReservable(tx, ty);
		if (f != null) {
			return work;
		}
		
		if (coo.isSameAs(ins.rx, ins.ry)) {
			if ((ins.resourcesFindable & ins.resourcesNeeded & ~ins.resourcesReserved) != 0) {
				return res;
			}
		}
		
		SETT_JOB j = get(tx, ty, 30);
		if (j == null)
			return get(ins.mX(), ins.mY(), ROOM_JANITOR.radius);
		return j;
	}

	@Override
	public SETT_JOB getJob(COORDINATE c) {

		coo.set(c);
		if (SETT.MAINTENANCE().isser.is(c)) {
			return work;
		}
		
		
		if (c.isSameAs(ins.rx, ins.ry)) {
			return res;
		}
		
		return null;
	}
	
	private SETT_JOB get(int sx, int sy, int distance) {
		
		if ((ins.resourcesFindable & ins.resourcesNeeded & ~ins.resourcesReserved) != 0 && !SETT.MAINTENANCE().isser.is(ins.rx, ins.ry)) {
			coo.set(ins.rx, ins.ry);
			return res;
		}

		if (!ins.searching)
			return null;
		
		COORDINATE c = SETT.MAINTENANCE().finder().reserve(sx, sy, distance);
		
		if (c == null) {
			return null;
		}
		coo.set(c);
		work.jobReserveCancel(null);
		RESOURCE rr = SETT.MAINTENANCE().resourceNeeded(coo.x(), coo.y());

		if (rr != null && (ins.resourcesFindable & rr.bit) != 0) {
			
			if (b.res.resources.get(rr.index()).get(ins) == 0) {
				ins.resourcesNeeded |= rr.bit;				
				if ((ins.resourcesFindable & ins.resourcesNeeded & ~ins.resourcesReserved) != 0 && !SETT.MAINTENANCE().isser.is(ins.rx, ins.ry)) {
					coo.set(ins.rx, ins.ry);
					return res;
				}
			}
		}
		
		return work;
	}
	
	void update(JanitorInstance ins) {
		ins.searching = true;
		ins.resourcesFindable = -1;
	}

	private final SETT_JOB work = new Job();
	
	private class Job implements SETT_JOB {
		
		@Override
		public boolean jobUseTool() {
			return true;
		}
		
		@Override
		public void jobStartPerforming() {
			
		}
		
		@Override
		public Sound jobSound() {
			return b.employment().sound();
		}
		
		@Override
		public long jobResourceBitToFetch() {
			return 0;
		}
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return SETT.MAINTENANCE().finder().getReserved(coo.x(), coo.y()) != null;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			FINDABLE f = SETT.MAINTENANCE().finder().getReserved(coo.x(), coo.y());
			if (f != null)
				f.findableReserveCancel();
		}
		
		@Override
		public boolean jobReserveCanBe() {
			FINDABLE f = SETT.MAINTENANCE().finder().getReservable(coo.x(), coo.y());
			if (f != null)
				return f.findableReservedCanBe();
			return false;
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			FINDABLE f = SETT.MAINTENANCE().finder().getReservable(coo.x(), coo.y());
			if (f != null)
				f.findableReserve();
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			RESOURCE r = SETT.MAINTENANCE().resourceNeeded(coo.x(), coo.y());
			if (r == null || (r.bit & ins.resourcesMissing) == 0)
				return 32;
			return 32*6;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
			jobReserveCancel(r);
			r = SETT.MAINTENANCE().resourceNeeded(coo.x(), coo.y());
			
			if (r != null) {
				b.res.resources.get(r.index()).inc(ins, -1);
				if (b.res.resources.get(r.index()).get(ins) == 0) {
					ins.resourcesNeeded |= r.bit;
				}
			}
			SETT.MAINTENANCE().maintain(coo.x(), coo.y());
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
		
	}
	
	final SETT_JOB res = new SETT_JOB() {
		
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
		public long jobResourceBitToFetch() {
			return ins.resourcesNeeded;
		}
		
		@Override
		public int jobResourcesNeeded() {
			return 8;
		}; 
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return true;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			ins.resourcesReserved &= ~r.bit;
		}
		
		@Override
		public boolean jobReserveCanBe() {
			return true;
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			ins.resourcesReserved |= r.bit;
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 0;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
			SETT.MAINTENANCE().maintain(coo.x(), coo.y());
			if (r == null)
				return null;
			jobReserveCancel(r);
			ins.resourcesNeeded &= ~r.bit;
			ins.resourcesMissing &= ~r.bit;
			
			boolean view = false;
			
			for (int i = 0; i < 3; i++) {
				if (((ins.viewRes >> (i*8)) & 0x0FF) == r.index()+1) {
					view = true;
					break;
				}
			}
			
			if (!view) {
				ins.viewRes = ins.viewRes << 8;
				ins.viewRes |= r.index()+1;
			}
			
			
			b.res.resources.get(r.index()).inc(ins, ram);
			FACTIONS.player().res().outMaintenance.inc(r, ram);
			
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

	@Override
	public void resetResourceSearch() {
		ins.resourcesMissing = 0;
		ins.resourcesFindable = ~0;
	}

}
