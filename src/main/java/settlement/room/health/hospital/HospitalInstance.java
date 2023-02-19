package settlement.room.health.hospital;

import settlement.main.RenderData;
import settlement.misc.job.*;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class HospitalInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER, ROOM_SERVICER{


	private static final long serialVersionUID = 1L;

	final Jobs jobs;
	private final long[] pData;
	boolean auto = true;
	private final RoomServiceInstance service;
	
	
	protected HospitalInstance(ROOM_HOSPITAL blue, TmpArea area, RoomInit init) {
		super(blue, area, init);
		
		jobs = new Jobs(this);
		
		int j = 0;
		for (COORDINATE c : body()) {
			if (is(c) && Bed.job(c.x(), c.y()) != null)
				j++;
		}
		service = new RoomServiceInstance(j, blue.service());
		
		
		employees().maxSet(j);
		employees().neededSet((int)Math.ceil(blue.constructor.workers.get(this)));
		pData = blue.consumtion.makeData();
		activate();
	}
	

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		if (day)
			service.updateDay();
		jobs.searchAgain();
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}

	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c) && Bed.service(c.x(), c.y()) != null)
				Bed.service(c.x(), c.y()).findableReserve();
		}
		service.dispose(blueprintI().service);
	}

	@Override
	public ROOM_HOSPITAL blueprintI() {
		return (ROOM_HOSPITAL) blueprint();
	}

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public long[] productionData() {
		return pData;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}
	
	@Override
	public int industryI() {
		return 0;
	}
	
	static class Jobs extends JobIterator {

		private static final long serialVersionUID = 1L;

		public Jobs(HospitalInstance ins) {
			super(ins);
		}

		@Override
		protected SETT_JOB init(int tx, int ty) {
			return Bed.job(tx, ty);
		}
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}


	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 1);
	}



}
