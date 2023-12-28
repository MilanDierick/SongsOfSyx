package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.*;

import game.GAME;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.food.hunter2.ROOM_HUNTER;
import settlement.room.main.*;
import settlement.room.service.arena.grand.ROOM_ARENA;
import settlement.room.service.arena.pit.ROOM_FIGHTPIT;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;

public final class AIModule_Work extends AIModule{


	private final PlanBlueprint[] map = new PlanBlueprint[ROOMS().all().size()];
	
	private final AIPLAN hangArround = new PlanHangArround();
	
	final PlanOddjobber oddjobber = new PlanOddjobber();
	private final PlanFetchEquip equip = new PlanFetchEquip();
	
	public AIModule_Work(){

		Works w = new WorkAbs.Works();
		
		for (RoomBlueprintIns<?> b : ROOMS().FARMS)
			new WorkAbs(this, b, map, w);
		new WorkAbs(this, ROOMS().WOOD_CUTTER, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().MINES)
			new WorkAbs(this,b, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().PASTURES)
			new WorkAbs(this,b, map, w);
		for (ROOM_FISHERY b : ROOMS().FISHERIES)
			new WorkFisherman(this,b, map, w);
		new WorkAbs(this, ROOMS().PRISON, map, w);
		new WorkAbs(this, ROOMS().ASYLUM, map, w);
		new WorkAbs(this, ROOMS().HOSPITAL, map, w);
		new WorkAbs(this, ROOMS().INN, map, w);
		new WorkWarehouse(this, map);
		new WorkDeliveryman(this, map, ROOMS().HAULER);
		new WorkDeliveryman(this, map, ROOMS().SUPPLY);
		for (GraveData.GRAVE_DATA_HOLDER h : ROOMS().GRAVES)
			new WorkGraveDigger(this, map, h);
		new WorkAbs(this, ROOMS().JANITOR, map, w);
		new WorkExporter(this, map);
		for (ROOM_HUNTER h : SETT.ROOMS().HUNTERS)
			new WorkHunter(h, this, map);
		new WorkCannibal(this, map);
		new WorkGuard(this, map);
		new WorkExecutioner(this, map);
		new WorkJudge(this, map);
		new WorkBuilder(this, map);
		new WorkSlaver(this, map);
		new WorkTransporter(this, map, w);
		new WorkEmissary(this, map);
		new WorkAbs(this, SETT.ROOMS().HOMES.CHAMBER, map, w);
		{
			for (RoomBlueprintIns<?> p : ROOMS().PHYSICIANS)
				new WorkAbs(this, p, map, w);
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
			for (ROOM_TEMPLE p : ROOMS().TEMPLES.ALL)
				new WorkTemple(this, p, map);
		}

		for (RoomBlueprintIns<?> p : ROOMS().SPEAKERS)
			WorkOrator.getSpeaker(this, p, map);
		
		for (RoomBlueprintIns<?> p : ROOMS().STAGES)
			WorkOrator.getDancer(this, p, map);
	
		for (ROOM_FIGHTPIT b : SETT.ROOMS().ARENAS) {
			new WorkGladiator(b.work, b, this, map);
		}
		
		for (ROOM_ARENA b : SETT.ROOMS().GARENAS) {
			new WorkGladiator(b.work, b, this, map);
		}
		
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
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (!validateEmployment(a, d)) {
			if (!PlanOddjobber.hasOddjob(a, true))
				return null;
			if (SETT.ARMIES().enemy().men() > 0)
				return null;
			AiPlanActivation p = oddjobber.activateOddjobber(a, d);
			return p;
		}
		
		PlanBlueprint b = map[work(a).blueprint().index()];
		if (b == null) {
			throw new RuntimeException(""+work(a).blueprintI().info.name);
		}
		
		{
			AiPlanActivation p = equip.activate(a, d);
			if (p != null)
				return p;
		}
		
		
		AiPlanActivation p = b.activate(a, d);
		if (p == null) {
			if (PlanOddjobber.hasOddjob(a, false) && SETT.ARMIES().enemy().men() == 0) {
				p = oddjobber.activateHelpOut(a, d);
				if (p != null)
					return p;
				
			}
			
			if (p == null) {
				return hangArround.activate(a, d);
			}
		}
		return p;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		if (AIModules.current(d) == this) {
			RoomInstance w = work(a);
			if (w != null) {
				PlanBlueprint b = map[w.blueprint().index()];
				if (b.shouldReportWorkFailure(a, d)) {
					if (d.plan() == hangArround || d.plan() == oddjobber) {
						w.reportWorkSuccess(false);
					}else {
						w.reportWorkSuccess(true);
					}
				}
				
				
			}
			
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (work(a) == null && !ROOMS().employment.hasWork(a)) {
			if (SETT.ARMIES().enemy().men() > 0)
				return 0;
			if (!PlanOddjobber.hasOddjob(a, true))
				return 0;
			
		}
		if (GAME.events().riot.onStrike(a))
			return 0;
		
		return (int) (STATS.WORK().getWorkPriority(a)*4.0);
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
