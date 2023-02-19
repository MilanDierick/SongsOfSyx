package settlement.job;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.resources.RESOURCES;
import init.sound.SOUND;
import init.sound.SoundSettlement.Sound;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import view.tool.PlacableMessages;

final class JobBuildFillCave extends JobBuild{

	private static CharSequence ¤¤name = "¤Refill Mountain Cave";
	private static CharSequence ¤¤desc = "¤Refills dug tunnels or natural mountain caves";
	
	static {
		D.ts(JobBuildFillCave.class);
	}
	
	JobBuildFillCave() {
		super(
				RESOURCES.STONE(),
				2, true, 
				¤¤name, 
				¤¤desc, 
				SPRITES.icons().m.tunnel_fill);
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		SPRITES.cons().ICO.unclear.render(r, x, y);
	}
	
	@Override
	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		if (SETT.PLACA().willBlock.is(tx, ty)) {
			return PlacableMessages.¤¤BLOCK_WILL;
		}
		if (!overwrite) {
			if (JOBS().getter.has(tx, ty)) {
				return PlacableMessages.¤¤JOB_BLOCK;
			}
		}
		
		if (!TERRAIN().CAVE.is(tx, ty))
			return PlacableMessages.¤¤CAVE_MUST;
		return null;
	}
	
	@Override
	boolean terrainNeedsClear(int tx, int ty) {
		return false;
	}

	@Override
	protected double constructionTime(Humanoid h) {
		return 20;
	}

	@Override
	protected boolean construct(int tx, int ty) {
		GAME.player().res().outConstruction.inc(RESOURCES.STONE(), 2);
		TERRAIN().MOUNTAIN.placeFixed(tx, ty);
		
		for (DIR d : DIR.ALLC) {
			if (TERRAIN().CAVE.canFix(tx+d.x(), ty+d.y())) {
				TERRAIN().CAVE.fix(tx+d.x(), ty+d.y());
			}
		}
		return false;
	}

	@Override
	protected Sound constructSound() {
		return SOUND.sett().action.stone;
	}
	
	@Override
	public boolean becomesSolid() {
		return true;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().MOUNTAIN;
	}

}
