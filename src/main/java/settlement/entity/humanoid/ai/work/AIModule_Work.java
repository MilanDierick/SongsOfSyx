package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.*;

import game.GAME;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;

public final class AIModule_Work extends AIModule{


	private final PlanBlueprint[] map = new PlanBlueprint[ROOMS().all().size()];
	public final AIDataSuspender suspender = AI.suspender();
	public final AIDataSuspender oddSearch = AI.suspender();
	
	private final AIPLAN hangArround = new PlanHangArround();
	
	final PlanOddjobber oddjobber = new PlanOddjobber();
	
	public AIModule_Work(){

		Works w = new WorkAbs.Works();
		
		for (RoomBlueprintIns<?> b : ROOMS().FARMS)
			new WorkAbs(this, b, map, w);
		new WorkAbs(this, ROOMS().WOOD_CUTTER, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().MINES)
			new WorkAbs(this,b, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().PASTURES)
			new WorkAbs(this,b, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().FISHERIES)
			new WorkAbs(this,b, map, w);
		new WorkAbs(this, ROOMS().PRISON, map, w);
		new WorkAbs(this, ROOMS().ASYLUM, map, w);
		new WorkAbs(this, ROOMS().PHYSICIAN, map, w);
		new WorkAbs(this, ROOMS().HOSPITAL, map, w);
		new WorkAbs(this, ROOMS().INN, map, w);
		new WorkWarehouse(this, map);
		new WorkDeliveryman(this, map, ROOMS().HAULER);
		new WorkDeliveryman(this, map, ROOMS().SUPPLY);
		for (GraveData.GRAVE_DATA_HOLDER h : ROOMS().GRAVES)
			new WorkGraveDigger(this, map, h);
		new WorkAbs(this, ROOMS().JANITOR, map, w);
		new WorkExporter(this, map);
		new WorkHunter(this, map);
		new WorkCannibal(this, map);
		new WorkGuard(this, map);
		new WorkExecutioner(this, map);
		new WorkJudge(this, map);
		new WorkBuilder(this, map);
		new WorkSlaver(this, map);
		new WorkTransporter(this, map, w);
		new WorkAbs(this, SETT.ROOMS().HOMES.CHAMBER, map, w);
		{
			for (RoomBlueprintIns<?> p : ROOMS().EATERIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().CANTEENS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().TAVERNS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LAVATORIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().BATHS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().WORKSHOPS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().REFINERS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LIBRARIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LABORATORIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().ADMINS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().NURSERIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().SCHOOLS)
				new WorkAbs(this, p, map, w);
			for (ROOM_TEMPLE p : ROOMS().TEMPLES)
				new WorkTemple(this, p, map);
		}

		for (RoomBlueprintIns<?> p : ROOMS().SPEAKERS)
			WorkOrator.getSpeaker(this, p, map);
		
		for (RoomBlueprintIns<?> p : ROOMS().STAGES)
			WorkOrator.getDancer(this, p, map);
	
		for (RoomBlueprint b : SETT.ROOMS().all()) {
			if ( b instanceof RoomBlueprintIns<?>) {
				RoomBlueprintIns<?> p = (RoomBlueprintIns<?>) b;
				if (p.employmentExtra() != null && map[p.index()] == null)
					new WorkAbs(this, p, map, w);
			}
		}
		
//		new RoomWorker(ROOMS().CONSTRUCTION.jobTitle, ROOMS().CONSTRUCTION);

		
	}
	
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (!validateEmployment(a, d)) {
			if (!PlanOddjobber.hasOddjob(a))
				return null;
			if (suspender.is(d))
				return null;
			if (SETT.ARMIES().enemy().men() > 0)
				return null;
			if (STATS.POP().POP.data().get(null)-STATS.WORK().workforce() > 100) {
				int dist = oddSearch.get(d);
				if (dist == oddSearch.max(d)) {
					dist = Integer.MAX_VALUE;
				}else {
					dist = (dist + 1)*200;
				}
				AiPlanActivation p = oddjobber.activateOddjobber(a, d, dist);
				if (p == null) {
					oddSearch.inc(d, 1);
					suspender.suspend(d);
				}else {
					oddSearch.set(d, 0);
				}
				return p;
			}
			
			AiPlanActivation p = oddjobber.activateOddjobber(a, d, Integer.MAX_VALUE);
			if (p == null) {
				suspender.suspend(d);
			}
			return p;
		}
		
		PlanBlueprint b = map[work(a).blueprint().index()];
		if (b == null) {
			throw new RuntimeException(""+work(a).blueprintI().info.name);
		}
		
		
		AiPlanActivation p = b.activate(a, d);
		if (p == null) {
			if (b.shouldReportWorkFailure(a, d))
				work(a).reportWorkSuccess(false);
			if (!suspender.is(d) && SETT.ARMIES().enemy().men() == 0) {
				p = oddjobber.activateHelpOut(a, d);
				if (p != null)
					return p;
				suspender.suspend(d);
			}
			
			if (p == null) {
				return hangArround.activate(a, d);
			}
		}

		work(a).reportWorkSuccess(true);
		return p;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
		suspender.update(d);
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (work(a) == null && !ROOMS().employment.hasWork(a)) {
			if (suspender.is(d))
				return 0;
			if (SETT.ARMIES().enemy().men() > 0)
				return 0;
			if (!PlanOddjobber.hasOddjob(a))
				return 0;
			
		}
		if (GAME.events().riot.onStrike(a))
			return 0;
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) < 1 ? 4 : 0;
	}
	
	private static RoomInstance work(Humanoid a) {
		return STATS.WORK().EMPLOYED.get(a.indu());
	}

	
	private boolean validateEmployment(Humanoid a, AIManager d) {
		ROOMS().employment.setWork(a);
	
		return work(a) != null && work(a).acceptsWork() && map[work(a).blueprint().index()] != null;
	}
	
	public boolean isLawEnforcement(Humanoid a, AIManager d) {
		return a.indu().hType() == HTYPE.RECRUIT || (work(a) != null && (map[work(a).blueprint().index()] instanceof WorkGuard));
	}

	public static int getTransportAmount(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		if (d.plan() instanceof WorkTransporter) {
			WorkTransporter t = (WorkTransporter) d.plan();
			return t.transportAmount(a, d);
		}
		return -1;
	}
	

}
