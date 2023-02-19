package settlement.job;

import static settlement.main.SETT.*;

import game.GameDisposable;
import init.D;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.job.StateManager.State;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

public abstract class Job implements SETT_JOB{
	
	public final static COLOR CDORMANT = COLOR.WHITE65;
	public final static COLOR CACTIVE = new ColorShifting(new ColorImp(0, 0, 128), new ColorImp(0, 50, 128));
	
	public static boolean overwrite;
	
	static final byte NOTHING = (byte) 0x07F;
	static final ArrayList<Job> all = new ArrayList<Job>(NOTHING-1);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				all.clear();
				overwrite = false;
			}
		};
	}
	
	final byte index = (byte) all.add(this);
	final Coo coo = new Coo();
	int tile;
	final CharSequence name;
	final SPRITE icon;
	
	Job(CharSequence name,SPRITE icon){
		this.name = name;
		this.icon = icon;
	}
	
	protected Job get(int tx, int ty) {
		coo.set(tx, ty);
		tile = tx+ty*TWIDTH;
		return this;
	}

	public final int tile() {
		return tile;
	}
	
	/**
	 * called when player cancels jobs.
	 * @param tx
	 * @param ty
	 */
	void cancel(int tx, int ty) {
		
	}
	
	@Override
	public final void jobReserve(RESOURCE r) {
		if (!jobReserveCanBe()) {
			throw new RuntimeException();
		}
		if (r != resourceCurrentlyNeeded())
			throw new RuntimeException();
		JOBS().state.set(State.RESERVED, this);
	}

	@Override
	public final boolean jobReservedIs(RESOURCE r) {
		return JOBS().state.is(coo, State.RESERVED) && r == resourceCurrentlyNeeded();
	}

	@Override
	public final void jobReserveCancel(RESOURCE r) {
		JOBS().state.set(State.RESERVABLE, this);
	}

	@Override
	public final boolean jobReserveCanBe() {
		return JOBS().state.get(coo) == State.RESERVABLE;
	}

	@Override
	public COORDINATE jobCoo() {
		return coo;
	}

	abstract void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state);

	abstract void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty);
	
	abstract void init(int tx, int ty); 
	abstract boolean becomesSolidNext();
	
	public abstract RESOURCE resourceCurrentlyNeeded();
	
	public abstract int resAmount();
	
	public RESOURCE res() {
		return null;
	}
	
	@Override
	public final long jobResourceBitToFetch() {
		if (resourceCurrentlyNeeded() != null)
			return resourceCurrentlyNeeded().bit;
		return 0;
	}
	
	@Override
	public int jobResourcesNeeded() {
		return 1;
	}
	

	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		if (becomesSolid()) {
			if (SETT.PLACA().willBlock.is(tx, ty)) {
				return PlacableMessages.¤¤BLOCK_WILL;
			}
		}
		if (!overwrite) {
			if (JOBS().getter.has(tx, ty)) {
				return PlacableMessages.¤¤JOB_BLOCK;
			}
		}
		
		return lockText();
		
	}
	
	public CharSequence lockText() {
		return null;
	}
	
	public abstract PlacableMulti placer();

	public boolean isConstruction() {
		return false;
	}
	
	
	private static CharSequence ¤¤claimed = "Is Claimed";
	private static CharSequence ¤¤claimedNot = "Is Unclaimed";
	private static CharSequence ¤¤resources = "¤This job needs a resource that is unobtainable in your city before it can be performed";
	private static CharSequence ¤¤dormant = "¤Job is inactive and needs to be manually activated before it will be performed.";
	private static CharSequence ¤¤blocked = "¤An adjacent job is blocking this job and must be performed prior to this.";
	private static CharSequence ¤¤unreachable = "¤Job is unreachable from the throne and will not be performed.";
	
	
	static{D.t(Job.class);}
	
	public void hover(GBox  box) {
		if (icon != SPRITES.icons().m.questionmark) {
			box.add(icon);
			box.text(name);
			box.NL(8);
		}
		
		State state = JOBS().state.get(tile);
		
		if (res() != null) {
			int am = resAmount();
			int n = am-jobResourcesNeeded();
			box.setResource(res(), n, am);
			box.NL();
		}
		
		if (state == State.RESERVED) {
			box.add(box.text().normalify2().add(¤¤claimed));
			box.NL();
		}else {
			box.add(box.text().normalify2().add(¤¤claimedNot));
			box.NL();
		}
		
		box.NL(8);
		if (!PATH().reachability.is(coo)) {
			box.add(box.text().errorify().add(¤¤unreachable));
			box.NL();
		}
		if (state == State.DORMANT) {
			box.add(box.text().errorify().add(¤¤dormant));
			box.NL();
		}
		if (state == State.BLOCKED) {
			box.add(box.text().errorify().add(¤¤blocked));
			box.NL();
		}
		
		RESOURCE res = resourceCurrentlyNeeded();
		if (res != null && state != State.RESERVED && !PATH().finders.resource.normal.has(res)) {
			box.add(box.text().errorify().add(¤¤resources));
			box.NL();
		}
		
//		if (STATS.WORK().workforce()-SETT.ROOMS().employment.NEEDED.get()+SETT.ROOMS().BUILDER.employment().current().get(null) < PATH().finders.job.totalJobsEstimate()/8) {
//			box.add(box.text().errorify().add(¤¤workers));
//			box.NL();
//		}

	}
	
	public boolean becomesSolid() {
		return false;
	}

	public boolean needsRipe() {
		return false;
	}

	public void doSomethingExtraRender() {
		
	}
	
	public abstract TerrainTile becomes(int tx, int ty);

	
}
