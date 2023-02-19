package settlement.room.service.food.tavern;

import settlement.main.RenderData;
import settlement.misc.job.*;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class TavernInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_SERVICER{
	
	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	final Jobs jobs;
	boolean auto = true;

	protected TavernInstance(ROOM_TAVERN b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		jobs = new Jobs(this);
		
		service = new RoomServiceInstance(jobs.size(), blueprintI().serviceData);
		
		employees().maxSet((int)Math.ceil(jobs.size()/2.0));
		employees().neededSet((int)Math.ceil(jobs.size()/4.0));
		activate();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i) {
		i.lit(); 
		return super.render(r, shadowBatch, i);
		
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		if (day)
			service.updateDay();
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
		for (int i = 0; i < jobs.size(); i++) {
			COORDINATE c = jobs.get(i);
			blueprintI().table.get(c.x(), c.y()).dispose();
		}
		service.dispose(blueprintI().serviceData);
	}

	@Override
	public ROOM_TAVERN blueprintI() {
		return (ROOM_TAVERN) blueprint();
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, blueprintI().constructor.coziness.get(this));
	}
	
	static class Jobs extends JobPositions<TavernInstance> {

		private static final long serialVersionUID = 1L;

		public Jobs(TavernInstance ins) {
			super(ins);
		}

		@Override
		protected SETT_JOB get(int tx, int ty) {
			Table t = ins.blueprintI().table.get(tx, ty);
			if (t == null)
				return null;
			return t.job;
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().table.get(tx, ty) != null;
			
		}
	}
	

}
