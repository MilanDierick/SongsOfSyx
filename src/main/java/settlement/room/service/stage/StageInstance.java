package settlement.room.service.stage;

import settlement.misc.job.*;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.rnd.RND;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class StageInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_SERVICER{

	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	final byte off = (byte) RND.rInt(64);
	static final short services = 20;
	private final Job job = new Job(this);
	
	protected StageInstance(ROOM_STAGE b, TmpArea area, RoomInit init) {
		super(b, area, init);

		service = new RoomServiceInstance(services*job.size(), blueprintI().data);

		employees().maxSet(job.size());
		employees().neededSet(job.size());
		activate();
		
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}

	@Override
	protected void activateAction() {
		
	}

	@Override
	protected void deactivateAction() {
		
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return job;
	}
	
	@Override
	protected void dispose() {
		blueprintI().work.dispose(body().cX(), body().cY());
	}

	@Override
	public ROOM_STAGE blueprintI() {
		return (ROOM_STAGE) blueprint();
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, ((double)employees().employed()/employees().target()));
	}
	
	private static class Job extends JobPositions<StageInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Job(StageInstance ins) {
			super(ins);
			
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().work.job(tx, ty) != null;
		}

		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().work.job(tx, ty);
		}
		
		
	}

}
