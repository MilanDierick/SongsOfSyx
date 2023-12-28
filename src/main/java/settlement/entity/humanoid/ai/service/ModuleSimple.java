package settlement.entity.humanoid.ai.service;

import init.need.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;

abstract class ModuleSimple<T extends ROOM_SERVICE_HASER> extends AIModule{

	public final NEED need;
	private final INT_OE<AIManager> timeout = AI.data().new DataBit();
	
	public ModuleSimple(NEED need) {
		this.need = need;
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		boolean timeOut = timeout.get(d) > 0;
		
//		if (timeOut) {
//			need.stat().fix(a.indu());
//			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//				b.clearAccess(a);
//			}
//		}
//		

		
		for (T t : services(a, d)) {
			if (t.service().finder.has(a.tc())) {
				AiPlanActivation p = plan(a, d, t);
				if (p != null) {
					clearAll(a, d, t);
					return p;
					
				}
			}
		}
		if (timeOut) {
			clearAll(a, d, null);
			timeout.set(d, 0);
			need.stat().fix(a.indu());
		}else {
			timeout.set(d, 1);
		}
			
		return null;
	}

	public abstract LIST<T> services(Humanoid a, AIManager d);
	
	public abstract AiPlanActivation plan(Humanoid a, AIManager d, T t);
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		int pp = need.stat().iPrio(a.indu())-timeout.get(d);
		
		if (pp > 0) {
			return pp;
		}
		
		return 0;
	}
	
	private void clearAll(Humanoid a, AIManager d, T dontClear) {
		for (T t : services(a, d)) {
			if (t != dontClear) {
				clear(a, d, t);
			}
		}
	}
	
	protected abstract void clear(Humanoid a, AIManager d, T t);

}
