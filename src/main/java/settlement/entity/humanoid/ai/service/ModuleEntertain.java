package settlement.entity.humanoid.ai.service;

import game.time.TIME;
import init.need.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.service.module.RoomServiceNeed;
import util.data.INT_O.INT_OE;

final class ModuleEntertain extends Module{

	private int eventDay;
	protected final INT_OE<AIManager> eventT = AI.data().new DataBit();
	
	public ModuleEntertain(NEED need, MPlans plans, int eventDay) {
		super(need, plans);
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if ((TIME.days().bitsSinceStart() & 0x0F) == eventDay) {
			eventT.set(d, 1);
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					AiPlanActivation p = plans.get(a, d, b);
					if (p != null) {
						need.stat().fix(a.indu());
						return p;
						
					}
				}
			}
			return null;
		}else {
			return super.getPlan(a, d);
		}
		
	}
	
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if ((TIME.days().bitsSinceStart() & 0x0F) == eventDay) {
			if (eventT.get(d) == 1)
				return 0;
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//				if (b.stats().mulAction() != null && b.stats().mulAction().markIs(a)) {
//					return pp;
//				}
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					return 2;
				}
			}
			eventT.set(d, 1);
			return 0;
		}else {
			eventT.set(d, 0);
		}
		
		
		return super.getPriority(a, d);
	}
	
}
