package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.tilemap.TFence;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class JobBuildFence extends JobBuild{

	private final TFence fence;
	private static CharSequence ¤¤desc = "¤Stops subjects and animals from wandering off where you don't want them. Does not stop enemies";
	
	static LIST<Job> make(){
		D.ts(JobBuildFence.class);
		ArrayList<Job> all = new ArrayList<>(TERRAIN().FENCES.all().size());
		for (TFence s : TERRAIN().FENCES.all()) {
			all.add(new JobBuildFence(s));
		}
		return all;
	}
	
	JobBuildFence(TFence fence) {
		super(
				fence.resource, 
				1, 
				true, 
				fence.name(), 
				¤¤desc, 
				fence.getIcon());
		needsFerClear = false;
		this.fence = fence;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		for (DIR d: DIR.ORTHO) {
			if (FLOOR().getter.is(tx, ty, d) || JOBS().getter.get(tx, ty, d) instanceof JobBuildFence)
				mask |= d.mask();
		}
		SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
	}
	
	@Override
	protected double constructionTime(Humanoid skill) {
		return 10;
	}
	
	@Override
	protected Sound constructSound() {
		return fence.clearing().sound();
	}
	
	@Override
	protected boolean construct(int tx, int ty) {
		if (fence.resource != null)
			GAME.player().res().outConstruction.inc(fence.resource, fence.resAmount);
		fence.placeFixed(tx, ty);
		return false;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return fence;
	}

}
