package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.resources.RESOURCE;
import init.sound.SoundSettlement.Sound;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.RenderData.RenderIterator;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.tool.PlacableMulti;

abstract class JobClear extends Job{

	private final Placer placer;
	private final CharSequence names;
	
	JobClear(CharSequence name, CharSequence desc, CharSequence verb, ICON.MEDIUM icon) {
		super(name, icon);
		this.placer = new Placer(this, desc) {
			private final String jobs = "Jobs: ";
			@Override
			public void placeInfo(GBox b, int okTiles, AREA a) {
				super.placeInfo(b, okTiles, a);
				if (okTiles > 0) {
					VIEW.hoverBox().add(VIEW.hoverBox().text().add(jobs).add(okTiles));
				}
			}
		};
		names = verb;
	}

//	@Override
//	public long jobResourceBitToFetch() {
//		return 0;
//	}

	@Override
	public int resAmount() {
		return 0;
	}
	
	@Override
	public double jobPerformTime(Humanoid skill) {
		return 30;
	}

	@Override
	public void jobStartPerforming() {
	
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
		TerrainTile t = TERRAIN().get(coo);
		if (!t.clearing().can()) {
			PlacerDelete.place(coo.x(), coo.y());
			return null;
		}
			
		for (int i = 0; i < 3; i++) {
			
			t.clearing().clear1(coo.x(), coo.y());
			if (t != TERRAIN().get(coo) || TERRAIN().NADA.is(coo)) {
				PlacerDelete.place(coo.x(), coo.y());
				if (t.clearing().resource() != null)
					GAME.player().res().inProduced.inc(t.clearing().resource(), 1);
				return t.clearing().resource();
			}
		}
		if (t.clearing().resource() != null)
			GAME.player().res().inProduced.inc(t.clearing().resource(), 1);
		JOBS().state.set(State.RESERVABLE, this);
		return t.clearing().resource();
		
	}

	@Override
	public CharSequence jobName() {
		return names;
	}

	@Override
	public boolean jobUseTool() {
		return true;
	}

	@Override
	public Sound jobSound() {
		return TERRAIN().get(coo).clearing().sound();
	}

	@Override
	void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {
		
		
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		SPRITES.cons().ICO.clear.render(r, x, y);
	}

	@Override
	void init(int tx, int ty) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean becomesSolidNext() {
		return false;
	}


	@Override
	public PlacableMulti placer() {
		return placer;
	}
	
	@Override
	public RESOURCE resourceCurrentlyNeeded() {
		return null;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().NADA;
	}

}
