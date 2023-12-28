package settlement.entity.humanoid.ai.danger;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.stats.STATS;

final class AIModule_Health extends AIModule{

	private final PlanSick sick = new PlanSick();
	private final PlanInjured bleed = new PlanInjured();

	public AIModule_Health() {
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().HOSPITAL) {
			STATS.WORK().EMPLOYED.set(a, null);
		}
		
		if (STATS.NEEDS().disease.getter.get(a.indu()) != null) {
			return sick.activate(a, d);
		}
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return bleed.activate(a, d);
		}
		return null;
	}


	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.NEEDS().disease.getter.get(a.indu()) != null) {
			return 7;
		}
		
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return 7;
		}
		
		return 0;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}


	
	
}
