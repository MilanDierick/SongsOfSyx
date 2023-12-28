package settlement.entity.humanoid.ai.types.recruit;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.main.RoomInstance;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.room.military.training.archery.ROOM_ARCHERY;
import settlement.room.military.training.barracks.ROOM_BARRACKS;
import settlement.stats.STATS;

public final class AIModule_Recruit extends AIModule{

	private final PlanBarracks plan = new PlanBarracks(this);
	private final PlanRange range = new PlanRange(this);
	
	public AIModule_Recruit(){
		
		
		
	}
	
	public boolean canBecome(Humanoid h, AIManager d) {
		return ARMIES().info.updateAndGetEmployment(h, null) != null;
		
	}
	
	public void debugBecome(Humanoid a, AIManager d) {
		
		
	}
	
	public void debugRemove(Humanoid a, AIManager d) {
		
		
	}
	
	public boolean setEmploy(Humanoid a, AIManager d) {
		
		ROOM_M_TRAINER<?> current = current(a);
		
		ROOM_M_TRAINER<?> tar = ARMIES().info.updateAndGetEmployment(a, current);
		
		if (tar == null) {
			STATS.WORK().EMPLOYED.set(a, null);
			return false;
		}
		
		if (current != tar) {
			tar.emp.employ(a);
			return true;
		}
		return true;
		
	}
	
	public boolean shouldRemain(Humanoid a, AIManager d) {
		
		ROOM_M_TRAINER<?> current = current(a);
		
		ROOM_M_TRAINER<?> tar = ARMIES().info.updateAndGetEmployment(a, current);
		
		if (tar == null || tar != current) {
			return false;
		}
		return true;
		
	}
	
	public ROOM_M_TRAINER<?> current(Humanoid a){
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins != null && ins.blueprintI() instanceof ROOM_M_TRAINER<?>)
			return (ROOM_M_TRAINER<?>) ins.blueprintI();
		return null;
	}
	
	boolean planShouldContinue(Humanoid a, AIManager d) {
		
		return shouldRemain(a, d) && moduleCanContinue(a, d) && !STATS.WORK().EMPLOYED.get(a).employees().isOverstaffed();
		
	}
	
	private boolean reinit(Humanoid a, AIManager d) {
		STATS.WORK().EMPLOYED.set(a, null);
		setEmploy(a, d);
		return STATS.WORK().EMPLOYED.get(a) != null;
	}
	
	@Override
	protected void init(Humanoid a, AIManager d) {
		if (!setEmploy(a, d)) {	
			throw new RuntimeException();
		}
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (!planShouldContinue(a, d))
			reinit(a, d);
			
		RoomInstance w = STATS.WORK().EMPLOYED.get(a);
		
		if (w == null)
			return null;
		
		if (w.blueprintI() instanceof ROOM_BARRACKS) {
			return plan.activate(a, d);
		}else if (w.blueprintI() instanceof ROOM_ARCHERY)
			return range.activate(a, d);
		else
			throw new RuntimeException("No logic for: " + w.blueprintI());
	}

	private final double trainingD = 1.0/HumanoidResource.updatesPerDay;
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
		RoomInstance r = STATS.WORK().EMPLOYED.get(a);
		
		if (r != null && r.blueprint() instanceof ROOM_M_TRAINER<?>) {
			((ROOM_M_TRAINER<?>) r.blueprint()).train(a, r, trainingD);
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) < 1 ? 4 : 0;
	}



}
