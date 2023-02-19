package settlement.entity.humanoid.ai.battle;

import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;

public final class AIModule_Battle extends AIModule{
	
	private final AIPLAN march = new MarchPlan();
	private final ManPlan planMan = new ManPlan();
	final SubFight fight = new SubFight();
	final MarchSoftCollision subSoft = new MarchSoftCollision();
	final PlanAttackTile tile = new PlanAttackTile();
	final AIPLAN dessert = new PlanRout();
	


	
	public AIModule_Battle() {

	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1) {
			return dessert.activate(a, d);
		}
	
		if (a.indu().hostile()) {
			AiPlanActivation p = march.activate(a, d);
			if (p == null)
				return planMan.activate(a, d);
			return p;
		}
		else {
			Div div = a.division();
			if (div != null) {
				
				AiPlanActivation p = march.activate(a, d);
				if (p != null)
					return p; 
			}
			return planMan.activate(a, d);
		}
	}

	public AISubActivation interrupt(Humanoid a, AIManager d, ENTITY h){
		d.otherEntitySet((Humanoid) h);
		return fight.activate(a, d);
	}
	
	public AISubActivation resumeReady(Humanoid a, AIManager d, ENTITY h){
		d.otherEntitySet((Humanoid) h);
		return fight.activate(a, d);
	}
	
	public boolean breakTile(Humanoid a, AIManager d, int tx, int ty) {
		if (tile.shouldattackTile(d, a, tx, ty)) {
			tile.init(d, a, tx, ty);
			d.overwrite(a, tile);
			return true;
		}
		return false;
	}
	
	public AIPLAN interrrupt(Humanoid a, AIManager d){
		
		return march;
	}

	@Override
	protected void update(Humanoid a, AIManager ds, boolean newDay, int byteDelta, int updateI) {
		
//		if (a.division() == null && !isDeserting.is(ds)) {
//			Army t = BATTLE2().player();
//			if (a.induvidual().isEnemy()) {
//				t = BATTLE2().enemy();
//			}
//			Div d = t.getEmpty();
//			if (d != null) {
//				POPSTATS().division.set(a, d);
//			}
//		}
		
	}



	
	@Override
	public int getPriority(Humanoid a, AIManager ds) {
		if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
			return 11;
		
		if (a.indu().hostile())
			return 11;
		
		Div d = a.division();
		if (d != null && d.settings.mustering() && d.deployed() > 0) {
			return 9;
		}
		
		if (planMan.shouldMan(a, ds)) {
			return 6;
		}
		
		return -1;
	}



}
