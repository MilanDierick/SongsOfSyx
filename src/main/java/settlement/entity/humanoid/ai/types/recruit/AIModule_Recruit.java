package settlement.entity.humanoid.ai.types.recruit;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.military.barracks.ROOM_BATTLE_TRAINER;
import settlement.stats.STATS;

public final class AIModule_Recruit extends AIModule{

	private final PlanBarracks plan = new PlanBarracks(this);
	private final PlanRange range = new PlanRange(this);
	
	public AIModule_Recruit(){
		
		
		
	}
	
	public boolean canBecome(Humanoid h, AIManager d) {
		
		if (!ARMIES().info.updateAndShouldTrain(h, false)) {
			return false;
		}
		
		if (SETT.ROOMS().BARRACKS.emp.employable() > 0 && ARMIES().info.shouldBarracks(h, false)) {
			return true;
		}
		
		if (SETT.ROOMS().ARCHERY.emp.employable() > 0 && ARMIES().info.shouldArchery(h, false)) {
			return true;
		}
		
		STATS.BATTLE().RECRUIT.set(h, null);
		
		return false;
		
	}
	
	public boolean shouldRemain(Humanoid a, AIManager d) {
		
		if (!ARMIES().info.updateAndShouldTrain(a, true)) {
			return false;
		}
		
		if (STATS.WORK().EMPLOYED.get(a) == null) {
			return reinit(a, d);
		}
		
		if (STATS.WORK().EMPLOYED.get(a) == null)
			return false;
		
		if (STATS.WORK().EMPLOYED.get(a).employees().isOverstaffed()) {
			return reinit(a, d);
		}
		
		if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().BARRACKS) {
			if (!ARMIES().info.shouldBarracks(a, true))
				return reinit(a, d);
		}else if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().ARCHERY) {
			if (!ARMIES().info.shouldArchery(a, true))
				return reinit(a, d);
		}
		return true;
		
	}
	
	boolean planShouldContinue(Humanoid a, AIManager d) {
		
		if (!ARMIES().info.updateAndShouldTrain(a, true)) {
			return false;
		}
		
		if (STATS.WORK().EMPLOYED.get(a) == null) {
			return false;
		}
		
		if (STATS.WORK().EMPLOYED.get(a).employees().isOverstaffed()) {
			return false;
		}
		
		if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().BARRACKS) {
			if (!ARMIES().info.shouldBarracks(a, true))
				return false;
		}else if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().ARCHERY) {
			if (!ARMIES().info.shouldArchery(a, true))
				return false;
		}
		return moduleCanContinue(a, d);
		
	}
	
	private boolean reinit(Humanoid a, AIManager d) {
		STATS.WORK().EMPLOYED.set(a, null);
		init(a, d);
		return STATS.WORK().EMPLOYED.get(a) != null;
	}
	
	@Override
	protected void init(Humanoid a, AIManager d) {
		
		if (!ARMIES().info.updateAndShouldTrain(a, false)) {
			return;
		}
		
		if (ARMIES().info.shouldBarracks(a, false) && SETT.ROOMS().BARRACKS.emp.employ(a))
			return;
		
		if (ARMIES().info.shouldArchery(a, false) && SETT.ROOMS().ARCHERY.emp.employ(a)) {
			return;
		}
		
		STATS.BATTLE().RECRUIT.set(a, null);
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (!planShouldContinue(a, d))
			reinit(a, d);
			
		if (STATS.WORK().EMPLOYED.get(a) == null)
			return null;
		
		if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().ARCHERY) {
			if (SETT.ROOMS().BARRACKS.emp.employable() > 0 && ARMIES().info.shouldBarracks(a, true) && SETT.ROOMS().BARRACKS.emp.employ(a)) {
				return plan.activate(a, d);
			}
			return range.activate(a, d);
		
		}
		if (STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().BARRACKS)
			return plan.activate(a, d);
		
		return null;
	}

	private final double trainingD = 1.0/HumanoidResource.updatesPerDay;
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
		RoomInstance r = STATS.WORK().EMPLOYED.get(a);
		
		if (r instanceof ROOM_BATTLE_TRAINER) {
			((ROOM_BATTLE_TRAINER) r).train(a, trainingD);
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) < 1 ? 4 : 0;
	}

}
