package settlement.room.knowledge.university;

import settlement.main.RenderData;
import settlement.misc.job.*;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class UniversityInstance extends RoomInstance implements JOBMANAGER_HASER{

	private static final long serialVersionUID = 1L;
	final Jobs jobs;

	protected UniversityInstance(ROOM_UNIVERSITY blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		
		employees().neededSet(jobs.size());
		employees().maxSet(jobs.size());
		jobs.randomize();
		activate();
	
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i) {
		i.lit();
		return super.render(r, shadowBatch, i);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		jobs.searchAgain();
	}
	
	@Override
	protected void activateAction() {
		
	}

	@Override
	protected void deactivateAction() {
		
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c)) {
				
			}
		}
	}
	
	@Override
	public ROOM_UNIVERSITY blueprintI() {
		return (ROOM_UNIVERSITY) blueprint();
	}
	
	static class Jobs extends JobPositions<UniversityInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(UniversityInstance ins) {
			super(ins);
		}
		
		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().job.get(tx, ty);
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().job.get(tx, ty) != null;	
			
		}
	}

}
