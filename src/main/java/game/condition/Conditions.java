package game.condition;

import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.file.Json;
import snake2d.util.sets.LinkedList;
import util.data.BOOLEAN;

public class Conditions {

	public Conditions(Json json) {
		
		LinkedList<BOOLEAN> all = new LinkedList<>();
		
		if (json.has("STATS_INTEGER")) {
			Json j = json.json("STATS_INTEGER");
			COMPARATOR c = COMPARATOR.map.get(j);
			HCLASS cl = j.has(HCLASS.MAP.key) ? HCLASS.MAP.get(j) : null;
			Race race = j.has(RACES.map().key) ? RACES.map().get(j) : null;
			
			new StatsJson(j) {
				
				@Override
				public void doWithTheJson(StatCollection coll, STAT s, Json j, String key) {
					
					double b = j.i(key);
					all.add(new CStat(s.info().name, c, b, true, s, race, cl));
				}
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
				}
			};
			
		}
		if (json.has("STATS_PERC")) {
			Json j = json.json("STATS_INTEGER");
			COMPARATOR c = COMPARATOR.map.get(j);
			HCLASS cl = j.has(HCLASS.MAP.key) ? HCLASS.MAP.get(j) : null;
			Race race = j.has(RACES.map().key) ? RACES.map().get(j) : null;
			
			new StatsJson(j) {
				@Override
				public void doWithTheJson(StatCollection coll, STAT s, Json j, String key) {
					
					double b = j.i(key);
					all.add(new CStat(s.info().name, c, b, true, s, race, cl));
				}
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					double b = j.i(key);
					all.add(new CMul(m.name, c, b, m, race, cl));
				}
			};
			
		}
		
	}
	
	private static class CStat extends CONDITION {

		private final STAT s;
		private final Race r;
		private final HCLASS cl;
		
		public CStat(CharSequence name, COMPARATOR comp, double target, boolean isInt, STAT s, Race r, HCLASS cl) {
			super(name, comp, target, isInt);
			this.s = s;
			this.r = r;
			this.cl = cl;
		}

		@Override
		public double current() {
			if (isInt)
				return s.data(cl).get(r);
			return s.data(cl).getD(r);
		}
		
	}
	
	private static class CMul extends CONDITION {

		private final StatMultiplier s;
		private final Race r;
		private final HCLASS cl;
		
		public CMul(CharSequence name, COMPARATOR comp, double target, StatMultiplier s, Race r, HCLASS cl) {
			super(name, comp, target, false);
			this.s = s;
			this.r = r;
			this.cl = cl;
		}

		@Override
		public double current() {
			return s.multiplier(cl, r, 0);
		}
		
	}
	
}
