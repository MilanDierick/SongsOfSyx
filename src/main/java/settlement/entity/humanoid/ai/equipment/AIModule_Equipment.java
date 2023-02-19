package settlement.entity.humanoid.ai.equipment;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE;

public final class AIModule_Equipment extends AIModule{



	

	private final AIData.AIDataSuspender suspender = AI.suspender();
	private static long rm;
	
	public AIModule_Equipment() {
//		new Stat("deg", "deg") {
//			
//			@Override
//			double getP(Humanoid a) {
//				// TODO Auto-generated method stub
//				return (clothes.getI(a) & 0b011111)/(double)0b011111;
//			}
//		};
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		rm = 0;
		suspender.suspend(d);
		for (EQUIPPABLE e : STATS.EQUIP().allE()) {
			
			if (e.stat().indu().get(a.indu()) < e.target(a)) {
				rm |= e.resource().bit;
			}else if(e.stat().indu().get(a.indu()) > e.target(a)) {
				return dropEquipment.activate(a, d);
			}
		}
		
		if (PATH().finders.resource.normal.has(a.tc().x(), a.tc().y(), rm)) {
			return getEquipment.activate(a, d);
		}
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		suspender.update(d);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (AIModules.current(d) == AI.modules().battle) {
			suspender.set(d, 0);
			if (AI.modules().battle.getPriority(a, d) >= 0)
				return 0;
		}
		
		if (suspender.is(d))
			return 0;
		
		if (STATS.POP().pop(HTYPE.ENEMY) > 0)
			return 0;
		
		for (EQUIPPABLE e : STATS.EQUIP().allE()) {
			if (e.stat().indu().get(a.indu()) != e.target(a)) {
				return e.stat().indu().get(a.indu()) > e.target(a) ? 5 : 2;
			}
		}
		suspender.suspend(d);
		return 0;
	}
	
	private AIPLAN getEquipment = new AIPLAN.PLANRES() {

		private final Resumer fetch = new Resumer("Getting equipment") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = AI.SUBS().walkTo.resource(a, d, rm, 500);
				if (s == null)
					suspender.suspend(d);
				return s;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				for (EQUIPPABLE e : STATS.EQUIP().allE()) {
					
					if (e.resource() == d.resourceCarried() && e.stat().indu().get(a.indu()) < e.target(a)) {
						e.set(a.indu(), e.stat().indu().get(a.indu())+1);
						d.resourceCarriedSet(null);
						break;
					}
				}
				d.resourceDrop(a);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return fetch.set(a, d);
		}
		
	};
	
	private AIPLAN dropEquipment = new AIPLAN.PLANRES() {

		private final Resumer drop = new Resumer("Dropping Equipment") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				for (EQUIPPABLE e : STATS.EQUIP().allE()) {
					
					if (e.stat().indu().get(a.indu()) > e.target(a)) {
						
						AISubActivation s = AI.SUBS().walkTo.deposit(a, d, e.resource(), 250);
						if (s != null) {
							d.resourceCarriedSet(e.resource());
							e.stat().indu().inc(a.indu(), -1);
							return s;
						}
						
						int am = e.stat().indu().get(a.indu()) - e.target(a);
						SETT.THINGS().resources.create(a.physics.tileC(), e.resource(), am);
						e.set(a.indu(), 0);
						return AI.SUBS().STAND.activateTime(a, d, 2);
					}
				}
				
				
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return drop.set(a, d);
		}

		
	};



}
