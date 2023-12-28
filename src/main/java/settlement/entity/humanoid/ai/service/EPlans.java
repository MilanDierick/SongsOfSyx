package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.ai.main.AIPLAN;

class EPlans {

	public final AIPLAN drunk = new EPlanDrunk();
	public final AIPLAN shop = new EPlanEquip();
}
