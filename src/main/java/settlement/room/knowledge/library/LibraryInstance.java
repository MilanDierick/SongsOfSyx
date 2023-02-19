package settlement.room.knowledge.library;

import static settlement.main.SETT.*;

import settlement.main.RenderData;
import settlement.misc.job.*;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

final class LibraryInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER{

	private static final long serialVersionUID = 1L;
	final Jobs jobs;
	private long[] pdata;

	protected LibraryInstance(ROOM_LIBRARY blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		
		employees().neededSet((int) Math.ceil(jobs.size()));
		employees().maxSet(jobs.size());

		jobs.randomize();
		pdata = blueprint.industry.makeData();
		activate();
		
	}
	
	
	void performJob(double d) {
		
		blueprintI().data.perform(d);
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i) {
		i.lit();
		return super.render(r, shadowBatch, i);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		blueprintI().industry.updateRoom(this);
		jobs.searchAgain();
	}

	@Override
	protected void activateAction() {
		blueprintI().data.incStations(jobs.size());
	}

	@Override
	protected void deactivateAction() {
		blueprintI().data.incStations(-jobs.size());
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().job.get(c.x(), c.y()) != null)
				blueprintI().job.dispose();
		}
	}
	
	@Override
	public ROOM_LIBRARY blueprintI() {
		return (ROOM_LIBRARY) blueprint();
	}

	@Override
	public long[] productionData() {
		return pdata;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}
	
	static class Jobs extends JobPositions<LibraryInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(LibraryInstance ins) {
			super(ins);
		}
		
		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().job.get(tx, ty);
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			if (ROOMS().fData.tile.is(tx, ty, ins.blueprintI().constructor.ww)) {
				ROOMS().data.set(ins, tx, ty, Job.bit.mask);
			}else {
				ROOMS().data.set(ins, tx, ty, Job.ran.set(0, 1 + RND.rInt(Job.ran.mask)));
			}
			
			return ins.blueprintI().job.get(tx, ty) != null;	
			
		}
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}

}
