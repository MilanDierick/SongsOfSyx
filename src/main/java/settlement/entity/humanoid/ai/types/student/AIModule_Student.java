package settlement.entity.humanoid.ai.types.student;

import java.util.Arrays;
import java.util.Comparator;

import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;

public final class AIModule_Student extends AIModule{

	private final Plan plan = new Plan(this);
	private final ROOM_UNIVERSITY[] best = new ROOM_UNIVERSITY[SETT.ROOMS().SCHOOLS.size()];
	
	{
		for (ROOM_UNIVERSITY s : SETT.ROOMS().UNIVERSITIES)
			best[s.typeIndex()] = s;
		Arrays.sort(best, new Comparator<ROOM_UNIVERSITY>() {

			@Override
			public int compare(ROOM_UNIVERSITY o1, ROOM_UNIVERSITY o2) {
				if (o1.learningSpeed > o2.learningSpeed)
					return 1;
				return -1;
			}
		
		});
	}
	
	public AIModule_Student(){
		
		
		
	}
	
	public boolean tryInit(Humanoid h, AIManager d) {
		return BOOSTABLES.RATES().LEARNING_SKILL.get(h) > 0 && getFirstUni(h, d) != null;
	}
	
	public static boolean shouldContinue(Humanoid h, AIManager d) {
		
		if (BOOSTABLES.RATES().LEARNING_SKILL.get(h) <= 0)
			return false;
		if (STATS.WORK().EMPLOYED.get(h) == null || !(STATS.WORK().EMPLOYED.get(h).blueprintI() instanceof ROOM_UNIVERSITY))
			return false;
		if (!checkUni(h, d, (ROOM_UNIVERSITY) STATS.WORK().EMPLOYED.get(h).blueprintI()))
			return false;
		if (STATS.WORK().EMPLOYED.get(h).employees().isOverstaffed())
			return false;
		return true;
	}
	
	private ROOM_UNIVERSITY getFirstUni(Humanoid h, AIManager d) {
		for (ROOM_UNIVERSITY u : best) {
			if (u.emp.employable() > 0 && checkUni(h, d, u)) {
				return u;
			}
		}
		return null;
	}
	
	static boolean checkUni(Humanoid h, AIManager d, ROOM_UNIVERSITY u) {
		return u.emp.employable() >= 0 && u.limit.getD(h.race()) > STATS.EDUCATION().TOTAL().getD(h.indu());
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return plan.activate(a, d);
	}

	@Override
	protected void init(Humanoid a, AIManager d) {
		ROOM_UNIVERSITY u = getFirstUni(a, d);
		u.emp.employ(a);
	}

	private static double lls = 1/16.0;
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		RoomInstance in = STATS.WORK().EMPLOYED.get(a);
		if (in != null && in.blueprintI() instanceof ROOM_UNIVERSITY) {
			ROOM_UNIVERSITY u = (ROOM_UNIVERSITY) in.blueprintI();
			double ls = u.learningSpeed(in);
			STATS.EDUCATION().educate(a.indu(), ls*lls);
		}
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (!shouldContinue(a, d))
			return 0;
		return ((ROOM_UNIVERSITY) STATS.WORK().EMPLOYED.get(a).blueprintI()).isTime.is() ? 4 : 0;
	}

}
