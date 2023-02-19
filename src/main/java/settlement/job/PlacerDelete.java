package settlement.job;

import static settlement.main.SETT.*;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.TGrowable;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.util.datatypes.AREA;
import view.tool.*;

final class PlacerDelete extends PlacableMulti{

	private static CharSequence ¤¤name = "Delete Jobs";
	private static CharSequence ¤¤desc = "Deletes all jobs and room plans";
	static {
		D.ts(PlacerDelete.class);
	}
	
	public PlacerDelete() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.cancel);
	}
	
	static void place(int tx, int ty) {
		
		if (JOBS().getter.has(tx, ty))
			JOBS().state.clear(tx, ty);
		
		TerrainTile t = TERRAIN().get(tx, ty);
		if (t instanceof TGrowable) {
			TGrowable b = (TGrowable) t;
			b.job.set(tx, ty, false);
		}
		
	}

	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		if (SETT.ROOMS().construction.isser.is(fromX, fromY)) {
			return SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
		}
		return false;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (SETT.ROOMS().construction.isser.is(tx, ty))
			return null;
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null)
			return PlacableMessages.¤¤JOB_MUST;
		if (!JOBS().getter.has(tx, ty)) {
			if (!SETT.TERRAIN().GROWABLES.get(0).job.is(tx, ty))
				return PlacableMessages.¤¤JOB_MUST;
		}
		return null;
	}


	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (SETT.ROOMS().construction.isser.is(tx, ty)) {
			SETT.ROOMS().map.get(tx, ty).remove(tx, ty, true, this, false).clear();
		}
		Job j = JOBS().getter.get(tx, ty);
		if (j != null)
			j.cancel(tx, ty);
		place(tx, ty);
		if (SETT.TERRAIN().GROWABLES.get(0).job.is(tx, ty))
			SETT.TERRAIN().GROWABLES.get(0).job.set(tx, ty, false);
		
	}
	
	@Override
	public boolean canBePlacedAs(PLACER_TYPE t) {
		return true;
	}
	

}
