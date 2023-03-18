package settlement.room.food.orchard;

import static settlement.main.SETT.*;

import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.job.*;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

final class Instance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER, ANIMAL_ROOM_RUINER {

	private static final long serialVersionUID = 1L;
	private final long[] produceData;
	private int fertility;
	private double skill;
	private double skillPrev;
	private int skillI;
	public final float base;
	
	private final JobIterator jobmanager = new JobIterator(this) {
		private static final long serialVersionUID = 1L;

		@Override
		protected SETT_JOB init(int tx, int ty) {
			OTile t = blueprintI().tile(tx, ty);
			if (t != null)
				return t.job();
			return null;
		}
	};

	Instance(ROOM_ORCHARD p, TmpArea area, RoomInit init) {
		super(p, area, init);
		double t = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				int fer = (int) (SETT.FERTILITY().target.get(c)*OTile.BFER.mask);
				SETT.ROOMS().data.set(this, c, OTile.BFER.set(SETT.ROOMS().data.get(c), fer));
				fertility += fer;
				if (p.tile.init(c.x(), c.y(), this))
					t ++;
			}
			
		}
		base = (float) (t/ROOM_ORCHARD.TILES_PER_WORKER);
		int jobs = (int) Math.ceil(base);
		employees().maxSet((int) (jobs*1.25));
		employees().neededSet((int) jobs);
		produceData = p.productionData.makeData();
		activate();
		
		
	}

	
	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {

		blueprintI().productionData.updateRoom(this);
		
		if (day) {
			jobmanager.searchAgain();
			
			if (blueprintI().time.isDeadDay()) {
				skillPrev = skill();
				skill = 0;
				skillI = 0;
			}
			
		}
		
	}

	@Override
	public TmpArea remove(int tx, int ty, boolean scatter, Object obj, boolean forced) {
		if (scatter) {
			for (COORDINATE c : body()) {
				if (is(c)) {
					OTile t = blueprintI().tile.getM(c.x(), c.y());
					if (t != null)
						t.chop();
				}
				
			}
		}
		return super.remove(tx, ty, scatter, obj, forced);
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		int d = SETT.ROOMS().fData.spriteData2.get(it.tile());
		if (d != 0) {
			blueprintI().constructor.sEdge.render(r, shadowBatch, d, it, 0, false);
		}
		
		return super.render(r, shadowBatch, it);
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
	public ROOM_ORCHARD blueprintI() {
		return (ROOM_ORCHARD) blueprint();
	}

	@Override
	public boolean acceptsWork() {
		return true;
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
	
	public void incSkill(double skill) {
		this.skill += skill;
		this.skillI ++;
	}
	
	public void changeTo(ROOM_ORCHARD f) {
		ConstructionInit init = new ConstructionInit(0, f.constructor, null, 0, makeState(mX(), mY()));
		TmpArea a = remove(mX(), mY(), false, this, true);
		
		ROOMS().construction.createClean(a, init);
		
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		
		OTile t = blueprintI().tile(tx, ty);
		if (t != null)
			t.updateDay();
		
		
		int ofer = (int) OTile.BFER.get(SETT.ROOMS().data.get(tx, ty)); 
		int fer = (int) (SETT.FERTILITY().target.get(tx, ty)*OTile.BFER.mask);
		if (ofer != fer) {
			fertility -= ofer;
			fertility += fer;
			SETT.ROOMS().data.set(this,tx, ty, OTile.BFER.set(SETT.ROOMS().data.get(tx, ty), fer));
		}
			
	}
	
	public double fertility() {
		return (double)fertility / (area()*OTile.BFER.mask);
	}

	public double skill() {
		if (skillI == 0)
			return skillPrev;
		return skill/skillI;
	}
	
	public double skillPrev() {
		return skillPrev;
	}


	public boolean event() {
		boolean ff = false;
		for (COORDINATE c : body()) {
			if (is(c) && RND.rBoolean()) {
				OTile t = blueprintI().tile.getM(c.x(), c.y());
				if (t != null)
					ff |= t.kill();
			}
			
		}
		return ff;
	}
	
}
