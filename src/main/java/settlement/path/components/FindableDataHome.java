package settlement.path.components;

import settlement.room.home.HOME_TYPE;
import snake2d.util.sets.ArrayList;

public final class FindableDataHome {

	private final ArrayList<FindableData> all;

	FindableDataHome() {
		
		all = new ArrayList<>(HOME_TYPE.ALL().size());
		for (HOME_TYPE t : HOME_TYPE.ALL()) {
			all.add(new FindableData("home " + t.name));
		}
		
	}

	void add(SComponent c, HOME_TYPE t) {
		all.get(t.index()).add(c);
	}
	
	void remove(SComponent c, HOME_TYPE t) {
		all.get(t.index()).remove(c);
	}

	public boolean has(SComponent c, HOME_TYPE t) {
		return all.get(t.index()).get(c) > 0;
	}
	
	public final void reportPresence(int tx, int ty, HOME_TYPE t) {
		all.get(t.index()).reportPresence(tx, ty);
		
	}
	
	public final void reportAbsence(int tx, int ty, HOME_TYPE t) {
		all.get(t.index()).reportAbsence(tx, ty);
	}

	FindableData get(HOME_TYPE t) {
		return all.get(t.index());
	}

}