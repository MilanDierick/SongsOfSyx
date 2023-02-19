package settlement.room.military.barracks;

import static settlement.main.SETT.*;

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
import settlement.stats.STATS;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class BarracksInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_BATTLE_TRAINER{

	private static final long serialVersionUID = 1L;
	private final JobIterator jobs;
	
	BarracksInstance(ROOM_BARRACKS b, TmpArea area, RoomInit init) {
		super(b, area, init);
		int am = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				BarracksThing t = b.thing.init(c.x(), c.y());
				if (t != null)
					am++;
			}
		}
		
		employees().maxSet(am);
		employees().neededSet(am);
		
		jobs = new Jobs(this);
		
		
		
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
			return SETT.ROOMS().BARRACKS.thing.init(tx, ty);
		}
	}
	
	@Override
	public ROOM_BARRACKS blueprintI() {
		return ROOMS().BARRACKS;
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
		double d = IndustryUtil.calcProductionRate(1, a, blueprintI().rate, BOOSTABLES.RATES().TRAINING, this);
		STATS.BATTLE().TRAINING_MELEE.inc(a.indu(), blueprintI().RATEI*d);
	}
	
	
}
