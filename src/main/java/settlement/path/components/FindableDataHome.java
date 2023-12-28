package settlement.path.components;

import settlement.room.home.HOMET;
import settlement.room.home.HomeSettings.HomeSetting;
import snake2d.util.sets.ArrayList;

public final class FindableDataHome {

	private final ArrayList<FindableData> all;

	FindableDataHome() {
		
		all = new ArrayList<>(HOMET.ALL().size());
		for (HOMET t : HOMET.ALL()) {
			all.add(new FindableData("home " + t.cl.name + ": " +t.race));
		}
		
	}

	void add(SComponent c, HomeSetting t) {
		for (int ti = 0; ti < HOMET.ALL().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).add(c);
			}
		}
	}
	
	void remove(SComponent c, HOMET t) {
		all.get(t.index()).remove(c);
	}

	public boolean has(SComponent c, HOMET t) {
		return all.get(t.index()).get(c) > 0;
	}
	
	public final void reportPresence(int tx, int ty, HomeSetting t) {
		for (int ti = 0; ti < HOMET.ALL().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).reportPresence(tx, ty);
			}
		}
		
		
		
	}
	
	public final void reportAbsence(int tx, int ty, HomeSetting t) {
		for (int ti = 0; ti < HOMET.ALL().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).reportAbsence(tx, ty);
			}
		}
	}

	FindableData get(HOMET t) {
		return all.get(t.index());
	}

}