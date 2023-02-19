package settlement.job;

import static settlement.main.SETT.*;

import init.D;
import init.sprite.SPRITES;
import settlement.job.StateManager.State;
import snake2d.util.datatypes.AREA;
import view.tool.*;

final class PlacerActivate extends PlacableMulti{

	private static CharSequence ¤¤name = "¤Activate Job";
	private static CharSequence ¤¤desc = "¤Activates suspended jobs.";
	static {
		D.ts(PlacerActivate.class);
	}
	
	public PlacerActivate() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.job_awake);
	}
	
	static void place(int tx, int ty) {
		int i = tx +ty*TWIDTH;
		if (JOBS().getter.has(i)) {
			JOBS().state.activate(i, JOBS().getter.get(i));
		}
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null && !j.jobToggleIs())
			j.jobToggle(true);
	}
	
	@Override
	public PLACABLE getUndo() {
		return JOBS().tool_dormant;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		ROOM_JOBBER j = ROOM_JOBBER.get(fromX, fromY);
		return j != null && j.is(toX, toY);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		int i = tx +ty*TWIDTH;
		if (ROOMS().map.is(i)) {
			ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
			if (j != null)
				return null;
		}else if (JOBS().getter.has(i)) {
			if (JOBS().state.is(i, State.DORMANT))
				return null;
		}
		return "";
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		int i = tx +ty*TWIDTH;
		if (JOBS().getter.has(i)) {
			JOBS().state.activate(i, JOBS().getter.get(i));
		}
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null && !j.jobToggleIs())
			j.jobToggle(true);
	}
	

}
