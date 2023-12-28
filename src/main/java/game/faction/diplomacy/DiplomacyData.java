package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.sets.Bitmap1D;

class DiplomacyData extends Bitmap1D{

	DiplomacyData() {
		super(FACTIONS.MAX*FACTIONS.MAX/2, false);
	}
	
	public boolean get(Faction a, Faction b) {
		return get(index(a, b));
	}
	
	public void set(Faction a, Faction b, boolean value) {
		set(index(a, b), value);
	}
	
	private int index(Faction a, Faction b) {
		if (b.index() > a.index()) {
			Faction c = a;
			a = b;
			b = c;
		}
		return a.index()+b.index()*FACTIONS.MAX;
	}
	
	
}