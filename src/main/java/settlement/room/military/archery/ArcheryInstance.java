package settlement.room.military.archery;

import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.misc.job.*;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import settlement.room.military.barracks.ROOM_BATTLE_TRAINER;
import settlement.stats.STATS;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class ArcheryInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_BATTLE_TRAINER{

	private static final long serialVersionUID = 1L;
	private final JobIterator jobs;
	
	ArcheryInstance(ROOM_ARCHERY b, TmpArea area, RoomInit init) {
		super(b, area, init);
		int am = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				ArcheryThing t = b.thing.init(c.x(), c.y());
				
				if (t != null) {
					am++;
				}
			}
		}
		jobs = new Jobs(this);
		employees().maxSet(am);
		employees().neededSet(am);
		
		activate();
	}

	private static class Jobs extends JobIterator {
		public Jobs(RoomInstance ins) {
			super(ins);
			// TODO Auto-generated constructor stub
		}

		private static final long serialVersionUID = 1L;

		@Override
		protected SETT_JOB init(int tx, int ty) {
			return SETT.ROOMS().ARCHERY.thing.init(tx, ty);
		}
	}
	
	@Override
	public ROOM_ARCHERY blueprintI() {
		return (ROOM_ARCHERY) blueprint();
	}

	@Override
	protected void activateAction() {
		
	}

	@Override
	protected void deactivateAction() {
		
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		jobs.searchAgain();
	}
	
	@Override
	protected void dispose() {
		
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		i.lit();
		return super.render(r, shadowBatch, i);
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	public void train(Humanoid a, double delta) {
		double d = IndustryUtil.calcProductionRate(1, a, blueprintI().rate, BOOSTABLES.RATES().TRAINING, this)*blueprintI().RATEI;
		if (STATS.BATTLE().TRAINING_MELEE.indu().get(a.indu()) < 1) {
			STATS.BATTLE().TRAINING_MELEE.inc(a.indu(), d);
		}else {
			STATS.BATTLE().TRAINING_ARCHERY.inc(a.indu(), d);
		}

		
		
	}

	
}
