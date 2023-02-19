package settlement.room.service.hearth;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.job.ROOM_JOBBER;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import util.rendering.ShadowBatch;

final class HearthInstance extends RoomInstance implements ROOM_SERVICER, ROOM_JOBBER, ROOM_PRODUCER{

	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	byte wood = 0;
	byte used = 0;
	byte inactive = 0;
	private final long[] pData;
	private short jx,jy;
	
	protected HearthInstance(ROOM_HEARTH b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		int am = 0;
		for (COORDINATE c : body()) {
			if (is(c) && SETT.ROOMS().fData.tileData.is(c, Constructor.codeService)){
				am++;
			}
		}
		service = new RoomServiceInstance(am, blueprintI().data);
		inactive = (byte) am;
		boolean job = false;
		for (COORDINATE c : body()) {
			if (is(c) && SETT.ROOMS().fData.tileData.is(c, Constructor.codeFire)){
				if (!job) {
					jobSet(c.x(), c.y(), true, RESOURCES.WOOD());
					jx = (short) c.x();
					jy = (short) c.y();
				}
				SETT.LIGHTS().fire(c.x(), c.y(), 0);
				SETT.LIGHTS().hide(c.x(), c.y(), true);
				job = true;
			}
		}
		pData = b.indus.get(0).makeData();
		activate();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
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
		if (day)
			service.updateDay();
		blueprintI().indus.get(0).updateRoom(this);
	}
	
	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c)) {
				SETT.JOBS().clearer.set(c);
				Hearth t = blueprintI().bed.get(c.x(), c.y());
				if (t != null)
					t.dispose();
			}
		}
		service.dispose(blueprintI().data);
		
	}
	
	@Override
	public ROOM_HEARTH blueprintI() {
		return (ROOM_HEARTH) blueprint();
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 1);
	}

	@Override
	public void jobFinsih(int tx, int ty, RESOURCE r, int ram) {
		blueprintI().industries().get(0).ins().get(0).inc(this, ram);
		wood+=4*ram;
		
		if (wood < 10*4) {
			jobSet(jx, jy, true, RESOURCES.WOOD());
		}
		
		if (inactive > 0 && available() > 0) {
			for (COORDINATE c : body()) {
				if (is(c)) {
					Hearth t = blueprintI().bed.get(c.x(), c.y());
					if (t != null)
						t.makeAvailable();
					if (inactive <= 0 || available() <= 0) {
						break;
					}
				}
			}
		}
		
	}
	
	int available() {
		return wood - service.total() + inactive;
	}
	
	void use() {
		
		wood--;
		if (wood < 10*4) {
			jobSet(jx, jy, true, RESOURCES.WOOD());
		}
	}

	@Override
	public void jobToggle(boolean toggle) {
		
	}

	@Override
	public boolean jobToggleIs() {
		return true;
	}

	@Override
	public boolean needsFertilityToBeCleared(int tx, int ty) {
		return false;
	}

	@Override
	public long[] productionData() {
		return pData;
	}

	@Override
	public boolean becomesSolid(int tx, int ty) {
		return false;
	}

	@Override
	public int totalResourcesNeeded(int x, int y) {
		return CLAMP.i((10*4 - wood)/4, 4, (10*4 - wood)/4);
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
