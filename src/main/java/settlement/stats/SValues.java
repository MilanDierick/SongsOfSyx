package settlement.stats;

import game.faction.Faction;
import game.values.GVALUES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.stats.colls.StatsReligion.StatReligion;
import settlement.stats.standing.STANDINGS;
import settlement.stats.stat.STAT;
import util.data.DOUBLE_O;
import util.dic.DicMisc;
import util.dic.DicRes;

final class SValues {

	SValues(){
		for (STAT s : STATS.all()) {
			if (s.key() != null && s.indu() != null) {
				GVALUES.INDU.push(s.key() + "_F", s.info().name, s.indu());
				GVALUES.INDU.pushI(s.key() + "_I", s.info().name, s.indu());
			}
			if (s.key() != null) {

				String k = s.key();
				if (s.info().isInt()) {
					GVALUES.FACTION.push(k, s.info().name, new DOUBLE_O<Faction>() {

						@Override
						public double getD(Faction t) {
							return s.data(HCLASS.CITIZEN).getD(null) * s.data(HCLASS.CITIZEN).max(null);
						}

					}, false);
				} else {
					GVALUES.FACTION.push(k, s.info().name, new DOUBLE_O<Faction>() {

						@Override
						public double getD(Faction t) {
							return s.data(HCLASS.CITIZEN).getD(null);
						}

					}, true);

				}

			}
		}

		GVALUES.FACTION.push("POPULATION", DicMisc.¤¤Population, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction o) {
				return STATS.POP().POP.data(null).get(null);
			}

		}, false);
		
		GVALUES.FACTION.push("CREDITS", DicRes.¤¤Currs, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction o) {
				return (int)o.credits().getD();
			}

		}, false);
		
		for (HCLASS cl : HCLASS.ALL) {
			String k = "POPULTAION_" + cl.key;
			GVALUES.FACTION.push(k, cl.names, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					return STATS.POP().POP.data(cl).get(null);
				}

			}, false);
		}
		
		for (Race r : RACES.all()) {
			String k = "POPULATION_" + r.key + "_";
			for (HCLASS cl : HCLASS.ALL) {
				if (!cl.player)
					continue;
				GVALUES.FACTION.push(k + cl.key + "_F", cl.names + ": " + r.info.names, new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction o) {
						double div = STATS.POP().POP.data(null).get(null);
						if (div == 0)
							return 0;
						return STATS.POP().POP.data(cl).get(r)/div;
						
					}

				}, true);
				GVALUES.FACTION.push(k + cl.key + "_I", cl.names + ": " + r.info.names, new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction o) {
						return STATS.POP().POP.data(cl).get(r);
					}

				}, false);
			}
		}
		
		for (HTYPE t : HTYPE.ALL()) {
			String k = "POPULTAION_" + t.key;
			GVALUES.FACTION.push(k + "_I", t.names, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					return STATS.POP().pop(t);
				}

			}, false);
			GVALUES.FACTION.push(k + "_F", t.names, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					double div = STATS.POP().POP.data(null).get(null);
					if (div == 0)
						return 0;
					return STATS.POP().pop(t) / div;
				}

			}, true);
		}
		
		for (StatReligion r : STATS.RELIGION().ALL) {
			GVALUES.FACTION.push(STATS.RELIGION().key + "_" + r.religion.key + "_F", r.religion.info.name, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					return r.followers.data(HCLASS.CITIZEN).getD(null);
				}
				
			}, true);
			GVALUES.FACTION.push(STATS.RELIGION().key + "_" + r.religion.key + "_I", r.religion.info.name, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					return r.followers.data(HCLASS.CITIZEN).get(null);
				}
				
			}, false);
			
		}
		
		for (HCLASS cl : HCLASS.ALL) {
			if (cl.player) {
				GVALUES.FACTION.push("LOYALTY_" + cl.key, DicMisc.¤¤Happiness + ": " + cl.names, new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction t) {
						return STANDINGS.get(cl).current();
					}
					
				}, true);
				
				
			}
		}
	}
	
}
