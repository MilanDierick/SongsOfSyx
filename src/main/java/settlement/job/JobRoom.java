package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.sound.SOUND;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.SETT;
import settlement.thing.ThingsResources.ScatteredResource;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.tool.PlacableMulti;

class JobRoom extends Job {

	private enum PSTATE {

		CLEAR_TERRAIN("Clearing terrain"), CLEAR_VEG("Clearing vegetation"), REMOVING("Removing obstacles"), FETCHING(
				"Getting materials"), DOING("Working");

		final String name;

		private PSTATE(String name) {
			this.name = name;
		}

	}

	private PSTATE state;
	private ROOM_JOBBER r;
	private final RESOURCE res;

	JobRoom(RESOURCE res) {
		super("work", SPRITES.icons().m.questionmark);
		this.res = res;
	}

	@Override
	void init(int tx, int ty) {
		JOBS().progress.set(tx+ty*TWIDTH, 0);
		JOBS().wantsRes.set(tx+ty*TWIDTH, getState(tx, ty) == PSTATE.FETCHING);
	}

	@Override
	protected Job get(int tx, int ty) {
		state = getState(tx, ty);
		r = (ROOM_JOBBER) ROOMS().map.get(tx, ty);
		super.get(tx, ty);
		if (JOBS().wantsRes.get(tile))
			state = PSTATE.FETCHING;
		else if (state == PSTATE.FETCHING)
			state = PSTATE.CLEAR_VEG;
			
		return this;
	}
	
	private PSTATE getState(int tx, int ty) {
//		if (SETT.TERRAIN().MOUNTAIN.is(tx, ty)) {
//			System.out.println(((ROOM_JOBBER) ROOMS().map.get(tx, ty)).needsTerrainToBeCleared(tx, ty) + " " + terrainNeedsClear(tx, ty));
//			System.out.println(TERRAIN().get(tx, ty).clearing().needs());
//			System.out.println(!TERRAIN().get(tx, ty).roofIs());
//			System.out.println(TERRAIN().get(tx, ty).clearing().can());
//		}
		if (((ROOM_JOBBER) ROOMS().map.get(tx, ty)).needsTerrainToBeCleared(tx, ty) && terrainNeedsClear(tx, ty))
			return PSTATE.CLEAR_TERRAIN;
		else if (((ROOM_JOBBER) ROOMS().map.get(tx, ty)).needsFertilityToBeCleared(tx, ty) && !GRASS().current.is(tx, ty, 0))
			return PSTATE.CLEAR_VEG;
		else if (res != null)
			return PSTATE.FETCHING;
		else if (((ROOM_JOBBER) ROOMS().map.get(tx, ty)).becomesSolid(tx, ty) && THINGS().resources.has(tx, ty, RBIT.ALL))
			return PSTATE.REMOVING;
		else
			return PSTATE.DOING;
	}

	boolean terrainNeedsClear(int tx, int ty) {
		return TERRAIN().get(tx, ty).clearing().needs() && !TERRAIN().get(tx, ty).roofIs() && TERRAIN().get(tx, ty).clearing().can();
	}

//	@Override
//	public long jobResourceBitToFetch() {
//		if (state == PSTATE.FETCHING && res != null)
//			return res.bit;
//		return 0;
//	}

	@Override
	public RESOURCE resourceCurrentlyNeeded() {
		if (state == PSTATE.FETCHING)
			return res;
		return null;
	}

	@Override
	public void jobStartPerforming() {
		// TODO Auto-generated method stub

	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		switch (state) {
		case CLEAR_TERRAIN:
			if (SETT.TERRAIN().MOUNTAIN.is(coo))
				return JOBS().clearss.tunnel.jobPerformTime(skill);
			
			TerrainTile t = TERRAIN().get(coo);
			if (t.clearing().isEasilyCleared())
				return 2;
			return 20; 
		case CLEAR_VEG:
			return 2.0;
		case REMOVING:
			return 0;
		case FETCHING:
			return 0;
		case DOING:
			return 10;
		}
		throw new RuntimeException();
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {

		if (!jobReservedIs(r)) {
			throw new RuntimeException(JOBS().state.is(coo, State.RESERVED) + " " + r + " " + resourceCurrentlyNeeded());
		}

		RESOURCE res = null;
		switch (state) {
		case CLEAR_TERRAIN:
			if (SETT.TERRAIN().MOUNTAIN.is(coo)) {
				res = JOBS().clearss.tunnelPerform(coo);
			}else {
				TerrainTile t = TERRAIN().get(tile);
				res = t.clearing().clear1(coo.x(), coo.y());
			}
			
			break;
		case CLEAR_VEG:
			GRASS().current.increment(coo.x(), coo.y(), -4);
			break;
		case REMOVING:
			ScatteredResource ress = THINGS().resources.get(coo.x(), coo.y());
			if (ress == null)
				break;
			if (ress.findableReservedCanBe()) {
				ress.findableReserve();
				ress.resourcePickup();
			} else {
				ress.resourcePickup();
			}
			res = ress.resource();
			break;
		default:
			int tx = coo.x();
			int ty = coo.y();
			ROOM_JOBBER j = this.r;
			PlacerDelete.place(tx, ty);
			j.jobFinsih(tx, ty, r, ram);
			if (!SETT.JOBS().getter.has(tx, ty)) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR d = DIR.ORTHO.get(di);
					if (SETT.JOBS().getter.has(tx, ty, d))
						SETT.JOBS().state.set(SETT.JOBS().state.get(tx, ty, d), SETT.JOBS().getter.get(tx, ty, d));
				}
			}
			return null;
		}
		
		JOBS().wantsRes.set(tile, getState(coo.x(), coo.y()) == PSTATE.FETCHING);

		get(coo.x(), coo.y());
		jobReserveCancel(r);
		if (res != null)
			GAME.player().res().inc(res, RTYPE.PRODUCED, 1);
		return res;

	}

	@Override
	boolean becomesSolidNext() {
		return ((ROOM_JOBBER) ROOMS().map.get(coo.x(), coo.y())).becomesSolid(coo.x(), coo.y());
	}
	
	@Override
	public boolean becomesSolid() {
		return ((ROOM_JOBBER) ROOMS().map.get(coo.x(), coo.y())).becomesSolid(coo.x(), coo.y());
	}
	
	@Override
	public int jobResourcesNeeded() {
		return ((ROOM_JOBBER) ROOMS().map.get(coo.x(), coo.y())).totalResourcesNeeded(coo.x(), coo.y());
	}

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
		switch (state) {
		case CLEAR_TERRAIN:
			return TERRAIN().get(coo).clearing().sound();
		case CLEAR_VEG:
			return SOUND.sett().action.dig;
		case REMOVING:
			return null;
		case FETCHING:
			return null;
		case DOING:
			return SOUND.sett().action.build;
		}
		throw new RuntimeException();
	}

	@Override
	protected void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {

	}

	@Override
	public PlacableMulti placer() {
		return null;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		if (Debug.showRoom || getState(tx, ty) == PSTATE.CLEAR_TERRAIN)
			SPRITES.cons().ICO.repair.render(r, x, y);
	}
	
	@Override
	public int resAmount() {
		return res != null ? 1 :0;
	}

	@Override
	public RESOURCE res() {
		return res;
	}

	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().NADA;
	}

}