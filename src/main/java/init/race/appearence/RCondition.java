package init.race.appearence;

import game.GAME;
import game.condition.COMPARATOR;
import settlement.stats.*;
import snake2d.util.file.Json;
import util.data.INT_O;

public final class RCondition {

	public final INT_O<Induvidual> stat;
	public final double compI;
	public final COMPARATOR comp;
	
	RCondition(Json j){
		
		INT_O<Induvidual> stat = IDummy;
		double compI = -1;
		COMPARATOR comp = COMPARATOR.GREATER;
		comp = COMPARATOR.map.get(j);
		compI = j.d("VALUE", 0, 1);
		
		j = j.json("STAT");
		if (j.keys().size() == 0)
			j.error("No stat declared. If no condition is wanted, remove the whole condition block.", "STAT");
		String sc = j.keys().get(0);
		StatCollection coll = STATS.COLLECTION(sc);
		if (coll == null) {
			j.error("No stat collection by name: " + coll, "STAT");
		}
		STAT ss = coll.MAP().tryGet(j.value(sc));
		if (ss == null) {
			GAME.Warn(j.errorGet("No stat named this " + j.keys().get(0),  j.value(sc)));
			compI = -1;
		}else {
			stat = ss.indu();
		}
		
		this.stat = stat;
		this.compI = compI;
		this.comp = comp;
		
	}
	
	private static INT_O<Induvidual> IDummy = new INT_O<Induvidual>() {

		@Override
		public int get(Induvidual t) {
			return 0;
		}

		@Override
		public int min(Induvidual t) {
			return 0;
		}

		@Override
		public int max(Induvidual t) {
			return 1;
		}
		
	};
	
}
