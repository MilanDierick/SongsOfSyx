package settlement.entity.humanoid.ai.entertainment;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.ROOM_SPECTATOR.ROOM_SPECTATOR_HASER;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;

public class AIModule_Entertainment extends AIModule{

	private final PlanSpectator plan;
	private final AIDataBit hasGoneEvent = AI.bit();
	private final AIDataBit hasGone = AI.bit();
	private final AIDataBit cooldown = AI.bit();
	private final static int INTERVAL = 5;
	public final static double MULTIPLIER = 2 - 1.0/5.0;
	
	public AIModule_Entertainment() {
		plan = new PlanSpectator();
	}
	

	public static double multiplier() {
		return MULTIPLIER * SETT.ROOMS().ENTERTAINMENT.size();
	}
	
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		
		ROOM_SERVICE_ACCESS_HASER event = getEvent(a, d, 0);
		
		if (event != null) {
			AiPlanActivation pa = plan(a, d, event);
			if (pa != null) {
				hasGoneEvent.set(d, true);
				if (event == getActivity(a, d, 0))
					hasGone.set(d, true);
				return pa;
			}
		}
		
		ROOM_SERVICE_ACCESS_HASER activity = getRealActivity(a, d, 0);
		
		if (activity != null) {
			AiPlanActivation pa = plan(a, d, activity);
			if (pa != null) {
				hasGone.set(d, true);
				return pa;
			}
		}
		cooldown.set(d, true);
		return null;
	}
	
	private ROOM_SERVICE_ACCESS_HASER getEvent(Humanoid a, AIManager d, int off) {
		
		if (eventDay(0)) {
			int e = (TIME.days().bitCurrent()/INTERVAL)%SETT.ROOMS().ENTERTAINMENT.size();
			ROOM_SERVICE_ACCESS_HASER s = SETT.ROOMS().ENTERTAINMENT.get(e);
			if (s.service().accessRequest(a) && s.service().stats().total().standing().max(a.indu().clas(), a.indu().race()) > 0) {
				if (s instanceof ROOM_SPECTATOR_HASER && !((ROOM_SPECTATOR_HASER)s).spec().isOpenNow()) {
					return null;
				}
				return s;
			}
				
		}
		return null;
	}
	
	private ROOM_SERVICE_ACCESS_HASER getActivity(Humanoid a, AIManager d, int off) {
		int[] ris = a.race().service().entertainIs(a.indu().clas());
		if (ris.length == 0)
			return null;
		int r = (int) (a.indu().randomness() + off + TIME.days().bitsSinceStart());
		r = r >>> 1;
		ROOM_SERVICE_ACCESS_HASER s = SETT.ROOMS().ENTERTAINMENT.get(ris[r%ris.length]);
		return s;
	}
	
	private ROOM_SERVICE_ACCESS_HASER getRealActivity(Humanoid a, AIManager d, int off) {
		ROOM_SERVICE_ACCESS_HASER s = getActivity(a, d, off);
		if (s != null && s.service().accessRequest(a) && s.service().stats().total().standing().max(a.indu().clas(), a.indu().race()) > 0)
			return s;
		return null;
	}
	
	
	private int activityDay(Humanoid a, AIManager d) {
		int r = (int) (a.indu().randomness() + TIME.days().bitsSinceStart());
		return r & 0b1;
	}
	
	private boolean eventDay(int off) {
		return (TIME.days().bitsSinceStart()+off)%INTERVAL == 0;
	}
	
	private AiPlanActivation plan(Humanoid a, AIManager d, ROOM_SERVICE_ACCESS_HASER s) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins != null && ins.blueprintI() == s) {
			s.service().reportContent(a, (ROOM_SERVICER)ins);
		}
		if (s instanceof ROOM_SPECTATOR_HASER) {
			return plan.activate(a, d, ((ROOM_SPECTATOR_HASER)s).spec());
		}
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		
		if ((updateOfDay & 0b011) == 0)
			cooldown.set(d, false);
		
		if (newDay && a.race().service().entertainsAny(a.indu().clas())) {
			if (activityDay(a, d) == 0) {
				if (!hasGone.is(d)) {
					ROOM_SERVICE_ACCESS_HASER s = getActivity(a, d, -1);
					if (s != null)
						s.service().clearAccess(a);
				}
				hasGone.set(d, false);
			}
			hasGoneEvent.set(d, false);
		}
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (d.plan() == plan)
			return 1;

		if (a.race().service().ENTERTAINMENT.get(a.indu().clas().index()).size() == 0)
			return 0;
		
		if (cooldown.is(d)) {
			return 0;
		}
		
		if (hasGone.is(d)) {
			return 0;
		}
		
		if (!hasGoneEvent.is(d)  && getEvent(a, d, 0) != null ) {
			return 3;
		}
		
		if (getRealActivity(a, d, 0) != null)
			return 1 + activityDay(a, d)*4;
		return 0;
				
		
	}

}
