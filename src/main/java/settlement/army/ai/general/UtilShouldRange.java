package settlement.army.ai.general;

import settlement.army.ArmyMorale;
import settlement.main.SETT;
import settlement.room.main.ROOMS;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.util.sets.ArrayListResize;

final class UtilShouldRange {

	private final ArrayListResize<ArtilleryInstance> ins = new ArrayListResize<>(256, ROOMS.ROOM_MAX);

	public boolean shouldRange(Context context) {

		if (isTakingMoreCasulties()) {
			return false;
		}

		int ally = 0;
		int enemy = 0;

		ins.clear();
		for (int ai = 0; ai < SETT.ROOMS().ARTILLERY.size(); ai++) {
			SETT.ROOMS().ARTILLERY.get(ai).threadInstances(ins);
		}
		for (int ii = 0; ii < ins.size(); ii++) {
			ArtilleryInstance i = ins.get(ii);
			if (i.isFiring()) {
				if (i.army() == context.army)
					ally++;
				else
					enemy++;
			}
		}

		if (ally == 0)
			return false;
		
		double bb = (double) (ally) / (ally + enemy + 1);
		bb -= (SETT.ARMIES().enemy().men() + 1) / (SETT.ARMIES().player().men() + 1) - 1.0;
		
		// LOG.ln(SETT.ARMIES().enemy().men() + " " +
		// SETT.ARMIES().player().men() + " " + bb + " " + ally + " " + enemy);
		if (bb < 0.5) {
			return false;
		}

		return true;
	}

	private boolean isTakingMoreCasulties() {

		double aa = ArmyMorale.CASULTIES.get(SETT.ARMIES().enemy());
		double ab = ArmyMorale.CASULTIES.get(SETT.ARMIES().player());

		return aa / (SETT.ARMIES().enemy().men() + 1.0) > 0.05 && aa > ab * 0.75;
	}

}