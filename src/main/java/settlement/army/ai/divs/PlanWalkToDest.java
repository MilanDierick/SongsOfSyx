package settlement.army.ai.divs;

import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import snake2d.util.sets.ArrayList;

final class PlanWalkToDest extends PlanWalkAbs{

	
	public PlanWalkToDest(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
	}
	
	@Override
	void init() {

		setWalkToDest();

	}
	
	@Override
	void update(int upI, int gamemillis) {
		
		
		state(m).update(upI, gamemillis);
		

	}

	@Override
	void finished() {
		task.stop();
		
		m.order.task.set(task);
		
	}
	



}
