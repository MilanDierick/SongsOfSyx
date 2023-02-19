package settlement.job;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.sound.SOUND;
import init.sound.SoundSettlement.Sound;
import settlement.entity.humanoid.Humanoid;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.util.sprite.SPRITE;
import util.rendering.ShadowBatch;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

abstract class JobBuild extends Job{

	private enum PSTATE {
		
		CLEAR_TERRAIN("Clearing terrain"),
		CLEAR_VEG("Clearing vegetation"),
		REMOVING("Removing obstacles"),
		FETCHING("Getting materials"),
		CONSTRUCTING("Constructing");
		
		final String name;
		
		private PSTATE(String name) {
			this.name = name;
		}
		
	}
	
	private PSTATE state;

	private final boolean solid;
	final Placer placer;
	protected boolean needsFerClear = true;
	protected final RESOURCE res;
	protected final int resAmount;
	
	JobBuild(RESOURCE res, int resAmount, boolean solid, CharSequence name, CharSequence desc, SPRITE icon) {
		super(name, icon);
		this.res = res;
		if (res == null)
			resAmount = 0;
		this.resAmount = resAmount;
		this.solid = solid;
		placer = new Placer(this, res, resAmount, desc);
	}
	
	@Override
	void init(int tx, int ty) {
		
		JOBS().progress.set(tx+ty*TWIDTH, 0);
		JOBS().wantsRes.set(tx+ty*TWIDTH, false);
		if (res != null) {
			for (Thing t : THINGS().get(tx, ty)) {
				if (t instanceof ScatteredResource) {
					ScatteredResource tt = (ScatteredResource) t;
					if (tt.resource() == res) {
						int a = tt.amount()-tt.amountReserved();
						if (a>= resAmount) {
							tt.removeUnreserved(resAmount);
							JOBS().progress.set(tx+ty*TWIDTH, resAmount);
							break;
						}else if (a > 0){
							JOBS().progress.set(tx+ty*TWIDTH, a);
							tt.removeUnreserved(a);
							break;
						}
					}
				}
			}
		}
		
		JOBS().wantsRes.set(tx+ty*TWIDTH, getStateP(tx, ty) == PSTATE.FETCHING);
	}
	
	@Override
	public RESOURCE res() {
		return res;
	}
	
	@Override
	public RESOURCE resourceCurrentlyNeeded() {
		if (state == PSTATE.FETCHING)
			return res;
		return null;
	}
	
	@Override
	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		
		if (super.problem(tx, ty, overwrite) != null)
			return super.problem(tx, ty, overwrite);
		if (TERRAIN().get(tx, ty).clearing().isStructure())
			return PlacableMessages.¤¤STRUCTURE_BLOCK;
		if (PATH().solidity.is(tx, ty))
			return PlacableMessages.¤¤SOLID_BLOCK;
		TerrainTile t = TERRAIN().get(tx, ty);
		if (becomesSolid() && !SETT.TERRAIN().MOUNTAIN.isMountain(tx, ty)) {
			return null;
		}else if (t.clearing().isStructure())
			return PlacableMessages.¤¤STRUCTURE_BLOCK;
		if (t.clearing().needs() && !t.clearing().can())
			return PlacableMessages.¤¤MISC;
		return null;
	}
	
	private PSTATE getState(int tx, int ty) {
		PSTATE s = getStateP(tx, ty);
		if (s == PSTATE.FETCHING) {
			JOBS().wantsRes.set(tx+ty*TWIDTH, true);
		}
		if (resNeeds(tx, ty) && JOBS().wantsRes.get(tx+ty*TWIDTH)) {
			s = PSTATE.FETCHING;
		}
		return s;
	}
	
	private PSTATE getStateP(int tx, int ty) {
		if (needsFerClear && terrainNeedsClear(tx, ty))
			return PSTATE.CLEAR_TERRAIN;
		else if (needsFerClear && GRASS().current.get(tx, ty) > 0)
			return PSTATE.CLEAR_VEG;
		else if (resNeeds(tx, ty))
			return PSTATE.FETCHING;
		else if (solid && THINGS().resources.has(tx, ty, -1l))
			return PSTATE.REMOVING;
		else
			return PSTATE.CONSTRUCTING;
	}
	
	@Override
	protected Job get(int tx, int ty) {
		state = getState(tx, ty);
		super.get(tx, ty);
		return this;
	}
	
	boolean terrainNeedsClear(int tx, int ty) {
		return TERRAIN().get(tx, ty).clearing().needs() && !TERRAIN().get(tx, ty).clearing().isStructure();
	}
	
	boolean resNeeds(int tx, int ty) {
		return res != null && JOBS().progress.get(tx+ty*TWIDTH) < resAmount;
	}
	
	@Override
	public int jobResourcesNeeded() {
		if (res != null)
			return resAmount - JOBS().progress.get(tile); 
		return 0;
	}
	
	@Override
	public void jobStartPerforming() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public double jobPerformTime(Humanoid skill) {
		
		
		switch(state) {
		case CLEAR_TERRAIN:
			TerrainTile t = TERRAIN().get(coo);
			if (t.clearing().isEasilyCleared())
				return 7;
			return 15;
		case CLEAR_VEG:
			return 2.0;
		case REMOVING:
			return 0;
		case FETCHING:
			return 0;
		case CONSTRUCTING:
			return constructionTime(skill);
		}
		throw new RuntimeException();
	}

	protected abstract double constructionTime(Humanoid skill);
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
		
		if (!jobReservedIs(r)) {
			throw new RuntimeException();
		}
		RESOURCE res = null;
		switch(state) {
		case CLEAR_TERRAIN:
			TerrainTile t = TERRAIN().get(tile);
			res = t.clearing().resource();
			for (int i = 0; i < 5; i++) {
				t.clearing().clear1(coo.x(), coo.y());
				if (t != TERRAIN().get(coo))
					break;
			}
			break;
		case CLEAR_VEG:
			GRASS().currentI.increment(coo.x(), coo.y(), -4);
			break;
		case REMOVING:
			ScatteredResource ress = THINGS().resources.get(coo.x(), coo.y());
			if (ress == null)
				break;
			if (ress.findableReservedCanBe()) {
				ress.findableReserve();
				ress.resourcePickup();
			}else {
				ress.resourcePickup();
			}
			res = ress.resource();
			break;
		case FETCHING:
			JOBS().progress.set(coo.x()+coo.y()*TWIDTH, JOBS().progress.get(coo.x()+coo.y()*TWIDTH)+rAm);
			break;
		case CONSTRUCTING:
			if (!construct(coo.x(), coo.y())) {
				PlacerDelete.place(coo.x(), coo.y());
				return res;
			}
			break;
		}
//		PlacerDelete.place(coo.x(), coo.y());
//		Placer.place(coo.x(), coo.y(), this);
		
		get(coo.x(), coo.y());
		jobReserveCancel(r);
		return res;
		
		
		
	}
	
	@Override
	boolean becomesSolidNext() {
		return solid && state == PSTATE.CONSTRUCTING;
	}
	
	@Override
	public boolean becomesSolid() {
		return solid;
	}
	
	protected abstract boolean construct(int tx, int ty);

	@Override
	public String jobName() {
		return state.name;
	}

	@Override
	public boolean jobUseTool() {
		return true;
	}

	@Override
	public Sound jobSound() {
		switch(state) {
		case CLEAR_TERRAIN:
			return TERRAIN().get(coo).clearing().sound();
		case CLEAR_VEG:
			return SOUND.sett().action.dig;
		case REMOVING:
			return null;
		case FETCHING:
			return null;
		case CONSTRUCTING:
			return constructSound();
		}
		throw new RuntimeException();
	}
	
	protected abstract Sound constructSound();

	
	@Override
	protected void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {
		if (state > 0) {
			res.renderLaying(r, i.x(), i.y(), i.ran(), state);
			shadowBatch.setHeight(1).setDistance2Ground(0);
			res.renderLaying(shadowBatch, i.x(), i.y(), i.ran(), state);
		}
	}
	
	@Override
	void cancel(int tx, int ty) {
		if (JOBS().progress.get(tx + ty*TWIDTH) > 0)
			THINGS().resources.create(tx, ty, res, JOBS().progress.get(tx + ty*TWIDTH));
	}
	
	@Override
	public PlacableMulti placer() {
		return placer;
	}
	
	@Override
	public int resAmount() {
		return resAmount;
	}

}
