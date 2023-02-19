package settlement.entity.humanoid.ai.types.slave;

import game.GAME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;

public final class AIModule_Slave extends AIModule{

	private PlanUprise uprise = new PlanUprise();
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return uprise.activate(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (GAME.events().uprising.spots.shouldSignUpUpriser(a))
			return 8;
		return 0;
	}

}
