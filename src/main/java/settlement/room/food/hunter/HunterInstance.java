package settlement.room.food.hunter;

import static settlement.main.SETT.*;

import settlement.main.RenderData;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import util.rendering.ShadowBatch;

public final class HunterInstance extends RoomInstance implements ROOM_PRODUCER, ROOM_RADIUS_INSTANCE{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ArrayCooShort coos;
	private final long[] pData;
	private byte radiusSearchTimer;
	
	private double count = 0;
	
	HunterInstance(ROOM_HUNTER blue, TmpArea area, RoomInit init) {
		super(blue, area, init);
		pData = blue.production.makeData();
		
		int am = 0;
		
		for (COORDINATE c : body()) {
			if (is(c) && blue.job.init(c.x(), c.y(), this) != null) {
				am++;
			}
		}
		
		coos = new ArrayCooShort(am);
		am = 0;
		for (COORDINATE c : body()) {
			if (is(c) && blue.job.init(c.x(), c.y(), this) != null) {
				coos.set(am++).set(c);
			}
		}
		employees().maxSet(am);
		employees().neededSet(am);
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
		blueprintI().production.updateRoom(this);
		if (!active())
			return;
		radiusSearchTimer --;
		radiusSearchTimer = (byte) CLAMP.i(radiusSearchTimer, 0, Byte.MAX_VALUE);
	}

	@Override
	protected void dispose() {
		
		
	}

	public SETT_JOB getWork() {
		
		for (int i = 0; i < coos.size(); i++) {
			coos.inc();
			SETT_JOB j = blueprintI().job.init(coos.get().x(), coos.get().y(), this);
			if (!j.jobReservedIs(null))
				return j;
		}
		
		for (int i = 0; i < coos.size(); i++) {
			coos.inc();
			SETT_JOB j = blueprintI().job.init(coos.get().x(), coos.get().y(), this);
			j.jobReserveCancel(null);
		}
		
		for (int i = 0; i < coos.size(); i++) {
			coos.inc();
			SETT_JOB j = blueprintI().job.init(coos.get().x(), coos.get().y(), this);
			if (!j.jobReservedIs(null))
				return j;
		}
		
		return null;
	}
	
	public void resetGore(COORDINATE c) {
		blueprintI().job.reset(this, c);
	}
	
	public void gore(COORDINATE c) {
		blueprintI().job.gore(this, c);
	}
	
	public SETT_JOB getWork(COORDINATE c) {
		return blueprintI().job.init(c.x(), c.y(), this);
	}
	
	public boolean countHunt() {
		count += 0.15*RND.rFloat()*(0.25 + 0.75*(1.0-getDegrade()))*(0.25 + 0.75*blueprintI().constructor.efficiency.get(this));
		
		
		
		if (count > 1) {
			count -= 1;
			return true;
		}
		return false;
	}
	
//	@Override
//	public double getworkEffort(SKILLSET s) {
//		return blueprintI().production.getWorkEffort(s, blueprintI().constructor.efficiency.get(this), getDegrade());
//	}
	
	@Override
	public ROOM_HUNTER blueprintI() {
		return ROOMS().HUNTER;
	}
	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return null;
	}

	@Override
	public long[] productionData() {
		return pData;
	}

	@Override
	public int radius() {
		return 400;
	}

	@Override
	public boolean searching() {
		return radiusSearchTimer == 0;
	}

	public void stopSearching() {
		radiusSearchTimer = 10;
	}

	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}

}
