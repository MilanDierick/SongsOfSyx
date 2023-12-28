package settlement.room.infra.admin;

import static settlement.main.SETT.*;

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
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class AdminInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER{

	private static final long serialVersionUID = 1L;
	final Jobs jobs;
	final float efficiency;
	private long[] pdata;
	short paper = 0;

	protected AdminInstance(ROOM_ADMIN blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		
		efficiency = (float) blueprintI().constructor.efficiency.get(this);
		employees().neededSet((int) Math.ceil(jobs.size()/2.0));
		employees().maxSet(jobs.size());

		jobs.randomize();
		pdata = blueprint.industry.makeData();
		activate();
		paper = 0;
		blueprintI().data.incStations(jobs.size());
		
	}
	
	void performJob(double d) {
		
		blueprintI().data.inc(d);
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
			if (is(c) && blueprintI().job.get(c.x(), c.y()) != null)
				blueprintI().job.dispose();
		}
	}
	
	@Override
	public ROOM_ADMIN blueprintI() {
		return (ROOM_ADMIN) blueprint();
	}

	@Override
	public long[] productionData() {
		return pdata;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}
	
	static class Jobs extends JobPositions<AdminInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(AdminInstance ins) {
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
