package settlement.entity.humanoid.ai.health;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.stats.STATS;

public final class AIModule_Health extends AIModule{

	private final PlanDoctor doc = new PlanDoctor();
	private final PlanSick sick = new PlanSick();
	private final PlanInjured bleed = new PlanInjured();

	public AIModule_Health() {
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().HOSPITAL) {
			STATS.WORK().EMPLOYED.set(a, null);
		}
		
		if (STATS.NEEDS().disease.getter.get(a.indu()) != null) {
			return sick.activate(a, d);
		}
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return bleed.activate(a, d);
		}
		if (a.indu().clas().player)
			return doc.activate(a, d);
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		doc.update(a, d);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.NEEDS().disease.getter.get(a.indu()) != null) {
			return 10;
		}
		
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return 7;
		}
		
		if (a.indu().clas().player)
			return doc.isTime(a, d);
		return 0;
	}


	
	
}
