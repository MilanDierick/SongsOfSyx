package settlement.room.service.speaker;

import settlement.main.RenderData;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

final class SpeakerInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_SERVICER{

	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	final byte off = (byte) RND.rInt(64);
	static final short services = 50;
	
	protected SpeakerInstance(ROOM_SPEAKER b, TmpArea area, RoomInit init) {
		super(b, area, init);

		service = new RoomServiceInstance(services, blueprintI().data);

		employees().maxSet(1);
		employees().neededSet(1);
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
		return blueprintI().work.manager(this);
	}
	
	@Override
	protected void dispose() {
		blueprintI().work.dispose(body().cX(), body().cY());
	}

	@Override
	public ROOM_SPEAKER blueprintI() {
		return (ROOM_SPEAKER) blueprint();
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
