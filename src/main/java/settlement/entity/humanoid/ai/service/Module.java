package settlement.entity.humanoid.ai.service;

import init.need.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.service.module.RoomServiceNeed;
import util.data.INT_O.INT_OE;

class Module extends AIModule{

	public final NEED need;
	public final MPlans plans;
	protected final INT_OE<AIManager> timeout = AI.data().new DataBit();
	
	public Module(NEED need, MPlans plans) {
		this.need = need;
		this.plans = plans;
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		boolean timeOut = timeout.get(d) > 0;
				
//		for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//			if (b.stats().mulAction() != null && b.stats().mulAction().markIs(a)) {
//				AiPlanActivation p = plans.get(a, d, b);
//				if (p != null) {
//					b.stats().mulAction().consume(a);
//					return p;
//				}else {
//					b.stats().mulAction().mark(a, false);
//				}
//			}
//		}
		
		for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
			if (b.accessRequest(a) && b.finder.has(a.tc())) {
				AiPlanActivation p = plans.get(a, d, b);
				if (p != null) {
					need.stat().fix(a.indu());
					clear(a, d, b);
					return p;
					
				}
			}
		}
		if (timeOut) {
			clear(a, d, null);
			timeout.set(d, 0);
			need.stat().fix(a.indu());
		}else {
			timeout.set(d, 1);
		}
			
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		int pp = need.stat().iPrio(a.indu())-timeout.get(d);
		
		if (pp > 0) {
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//				if (b.stats().mulAction() != null && b.stats().mulAction().markIs(a)) {
//					return pp;
//				}
				if (b.accessRequest(a)) {
					return pp;
				}else {
					b.clearAccess(a);
				}
			}

		}
		
		return 0;
	}
	
	private void clear(Humanoid a, AIManager d, RoomServiceNeed dontClear) {
		for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
			if (b != dontClear) {
				b.clearAccess(a);
			}
		}
	}

	static abstract class ModuleBackup extends Module{

		private final AIPLAN backup;
		
		public ModuleBackup(NEED need, MPlans plans, AIPLAN backup) {
			super(need, plans);
			this.backup = backup;
		}
		
		@Override
		public AiPlanActivation getPlan(Humanoid a, AIManager d) {
			
			boolean timeOut = timeout.get(d) > 0;
			
			if (timeOut) {
				timeout.set(d, 0);
				need.stat().fix(a.indu());
				clear(a, d, null);
				return backup.activate(a, d);
			}
			
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					AiPlanActivation p = plans.get(a, d, b);
					if (p != null) {
						clear(a, d, b);
						return p;
						
					}
				}
			}
			if (timeOut) {
				clear(a, d, null);
				timeout.set(d, 0);
				need.stat().fix(a.indu());
			}else {
				timeout.set(d, 1);
			}
				
			return null;
		}

		protected abstract boolean canTryBackup(Humanoid a, AIManager d);
		
		@Override
		protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getPriority(Humanoid a, AIManager d) {
			int pp = need.stat().iPrio(a.indu())-timeout.get(d);
			
			if (pp > 0) {
				for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
//					if (b.stats().mulAction() != null && b.stats().mulAction().markIs(a)) {
//						return pp;
//					}
					if (b.accessRequest(a) && b.finder.has(a.tc())) {
						return pp;
					}
				}
				if (canTryBackup(a, d)) {
					return pp;
				}
			}
			
			return 0;
		}
		
		private void clear(Humanoid a, AIManager d, RoomServiceNeed dontClear) {
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
				if (b != dontClear) {
					b.clearAccess(a);
				}
			}
		}

	}
	
}
