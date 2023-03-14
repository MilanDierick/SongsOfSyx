package settlement.room.infra.janitor;

import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class JanitorInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_RADIUS_INSTANCE{

	private static final long serialVersionUID = 1L;
	boolean searching;
	int viewRes = 0;
	boolean auto = true;
	final int[] resData;
	long resourcesFindable = -1;
	long resourcesMissing = 0;
	long resourcesNeeded = 0;
	long resourcesReserved = 0;

	final short rx, ry;
	
	
	protected JanitorInstance(ROOM_JANITOR b, TmpArea area, RoomInit init) {
		super(b, area, init);
		resData = new int[b.res.intsize];

		employees().maxSet((int) blueprintI().constructor.workers.get(this));
		employees().neededSet((int) Math.ceil(blueprintI().constructor.workers.get(this)/5.0));
		activate();
		int x = 0, y = 0;
		for (COORDINATE c : body()) {
			if (is(c) && SETT.ROOMS().fData.tile.is(c, b.constructor.ta)) {
				x = c.x();
				y = c.y();
			}
		}
		rx = (short) x;
		ry = (short) y;
		
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
		blueprintI().jm.update(this);

	}
	
	@Override
	public JOB_MANAGER getWork() {
		return blueprintI().jm.get(this);
	}
	
	@Override
	protected void dispose() {
		
	}

	@Override
	public ROOM_JANITOR blueprintI() {
		return (ROOM_JANITOR) blueprint();
	}

	@Override
	public int radius() {
		return ROOM_JANITOR.radius;
	}

	@Override
	public boolean searching() {
		return searching;
	}

}
