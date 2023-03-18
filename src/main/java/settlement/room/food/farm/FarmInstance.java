package settlement.room.food.farm;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.job.*;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class FarmInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER,ANIMAL_ROOM_RUINER {

	private static final long serialVersionUID = 1L;
	private final long[] produceData;
	final Tile.IData tData = new Tile.IData(this);
	
	private final JobIterator jobmanager = new JobIterator(this) {
		private static final long serialVersionUID = 1L;

		@Override
		protected SETT_JOB init(int tx, int ty) {
			return blueprintI().tile(tx, ty).job();
		}
	};

	FarmInstance(ROOM_FARM p, TmpArea area, RoomInit init) {
		super(p, area, init);
		for (COORDINATE c : body()) {
			if (is(c)) {
//				ROOMS().data.set(c, 0);
//				blueprintI().data.planted.set(c, false);
				p.tile(c.x(), c.y()).init(c, this);
			}
			
		}
		double w = Math.ceil(p.constructor.workers.get(this));
		employees().maxSet((int) ((w+1)*1.25));
		employees().neededSet((int) w);
		produceData = p.productionData.makeData();
		activate();
		
		
	}

	
	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {

		blueprintI().productionData.updateRoom(this);
		
		if (day) {
			jobmanager.searchAgain();
			tData.updateDay();
		}
		
	}

	@Override
	protected boolean renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		blueprintI().tile(i.tx(), i.ty()).renderTill(r, shadowBatch, i);
		return false;
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		blueprintI().tile(it.tx(), it.ty()).render(r, shadowBatch, it);
		return false;
	}
	
	public RESOURCE getCrop() {
		return blueprintI().crop.resource;
	}

	@Override
	public boolean canBeGraced(int tx, int ty) {
		return blueprintI().tile(tx, ty).destroyTileCan();
	}

	@Override
	public void grace(int tx, int ty) {
		blueprintI().tile(tx, ty).destroyTile();
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
	protected void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public JOB_MANAGER getWork() {
		return jobmanager;
	}

	@Override
	public ROOM_FARM blueprintI() {
		return (ROOM_FARM) blueprint();
	}

	@Override
	public boolean acceptsWork() {
		return true;
	}

	@Override
	protected AVAILABILITY getAvailability(int tile) {
		return AVAILABILITY.ROOM;
	}

	@Override
	public void destroyTile(int tx, int ty) {
		if (destroyTileCan(tx, ty)) {
			blueprintI().tile(tx, ty).destroyTile();
		}
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		return blueprintI().tile(tx, ty).destroyTileCan();
	}

	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		return null;
	}

	@Override
	public long[] productionData() {
		return produceData;
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
	
	public void changeTo(ROOM_FARM f) {
		ConstructionInit init = new ConstructionInit(0, f.constructor, null, 0, makeState(mX(), mY()));
		TmpArea a = remove(mX(), mY(), false, this, true);
		
		ROOMS().construction.createClean(a, init);
		
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		blueprintI().tile(tx, ty).updateDay();
	}
	
	@Override
	public void reportWorkSuccess(boolean success) {
		if (blueprintI().time.dayI() != blueprintI().time.dayOffWork)
			super.reportWorkSuccess(success);
	}

}
