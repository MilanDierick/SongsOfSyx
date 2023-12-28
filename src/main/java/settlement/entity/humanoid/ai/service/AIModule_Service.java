package settlement.entity.humanoid.ai.service;

import init.need.NEED;
import init.need.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.service.arena.pit.ROOM_FIGHTPIT;
import settlement.room.service.module.RoomService;
import settlement.room.service.speaker.ROOM_SPEAKER;
import settlement.room.service.stage.ROOM_STAGE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public class AIModule_Service {

	public final LIST<AIModule> all;
	
	final EPlans eplans = new EPlans();
	final MPlans plans = new MPlans(eplans);
	final Subs subs = new Subs();

	
	public AIModule_Service() {
		
		AIModule[] all = new AIModule[NEEDS.ALL().size()];
		ArrayListGrower<AIModule> aa = new ArrayListGrower<>();
		
		all[NEEDS.TYPES().HUNGER.index()] = new ModuleFood(plans);
		all[NEEDS.TYPES().TEMPLE.index()] = new ModuleTemple();
		all[NEEDS.TYPES().SHRINE.index()] = new ModuleShrine();
		all[NEEDS.TYPES().THIRST.index()] = new ModuleDrink(plans);
		all[NEEDS.TYPES().SHOPPING.index()] = new ModuleEquipment(plans, eplans);
		all[NEEDS.TYPES().SKINNYDIP.index()] = new ModuleSkinny(plans);
		
		for (ROOM_SPEAKER ee : SETT.ROOMS().SPEAKERS)
			all[ee.service().need.index()] = new ModuleEntertain(ee.service().need, plans, 4);
		
		for (ROOM_STAGE ee : SETT.ROOMS().STAGES)
			all[ee.service().need.index()] = new ModuleEntertain(ee.service().need, plans, 8);
		
		for (ROOM_FIGHTPIT ee : SETT.ROOMS().ARENAS)
			all[ee.service().need.index()] = new ModuleEntertain(ee.service().need, plans, 12);
		
		for (NEED n : NEEDS.ALL()) {
			if (all[n.index()] == null) {
				all[n.index()] = new Module(n, plans);
			}
		}
		
		aa.add(all);
		
		this.all = aa;
		
	}
	
	public AiPlanActivation get(Humanoid a, AIManager d, RoomService service, int dist) {
		
		return plans.get(a, d, service, dist);
	}
	
	public AiPlanActivation get(Humanoid a, AIManager d, NEED need, int dist) {
		return all.get(need.index()).getPlan(a, d);
	}
	
	
}
