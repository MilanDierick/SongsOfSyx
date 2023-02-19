package init.race;

import init.D;
import init.boostable.*;
import init.boostable.BOOSTER.BOOSTER_IMP_DATA;
import settlement.main.SETT;
import settlement.room.main.*;
import snake2d.util.file.Json;

public final class RaceBoosts extends BOOSTER_IMP_DATA {

	private final double[] priorities = new double[SETT.ROOMS().employment.ALLS().size()];
	public static CharSequence ¤¤name = "¤Race Work Skill";
	public static CharSequence ¤¤desc = "¤Species have different talents in different professions. The higher the work skill, the more production.";
	
	
	static {
		D.ts(RaceBoosts.class);
	}
	
	RaceBoosts(Race race, Json json){
		super(race.info.name, json);
	
		boolean[] emps = new boolean[SETT.ROOMS().employment.ALLS().size()];
		
		if (json.has("BONUS_MUL")) {
			json = json.json("BONUS_MUL");
			new RoomsJson("ROOM", json) {
				
				@Override
				public void doWithTheJson(RoomBlueprintImp room, Json j, String key) {
					if (room.employment() != null) {
						emps[room.employment().eindex()] = true;
					}	
				}
			};
		}
		
		
		
		
//		if (json.has("BONUS_MUL")) {
//			
//			new RoomsJson("BONUS_MUL", json) {
//				
//				@Override
//				public void doWithTheJson(RoomBlueprintImp room, Json j, String key) {
//					if (room.employment() != null) {
//						emps[room.employment().eindex()] = true;
//					}	
//				}
//			};
//		}
		
		for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
			for (BOOSTABLERoom bo : BOOSTABLES.ROOMS().boosts(p.blueprint())) {
				if (bo.defRace != 1 && !emps[bo.room.employment().eindex()])
					mul[bo.index()] = bo.defRace;
			}
		}
		
		for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
			double d = 0;
			if (BOOSTABLES.ROOMS().boosts(p.blueprint()).size() == 0) {
				d = 1.0;
			}else {
				for (BOOSTABLE bo : BOOSTABLES.ROOMS().boosts(p.blueprint())) {
					d += mul(bo)*(1+add(bo));
				}
				d /= BOOSTABLES.ROOMS().boosts(p.blueprint()).size();
			}
			
			if (d < 0)
				d = 0;
			
			priorities[p.eindex()] = d;
		}

	}
//
//	
//	public double getBasePriority(RoomEmploymentSimple e) {
//		return CLAMP.d((priorities[e.eindex()]-RACES.prios.workskillMin)/RACES.prios.workskillDelta, 0, 1);
//	}
//	

	double getWorkValue(RoomEmploymentSimple e) {
		return priorities[e.eindex()];
	}
	
	

	
	
}
