package settlement.entity.humanoid.ai.service;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.need.NEED;
import init.need.NEEDS;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.room.service.module.RoomServiceNeed;
import settlement.stats.STATS;

class ModuleFood extends AIModule{

	private final AIDataSuspender suspender = AI.suspender();
	final RBITImp bits = new RBITImp();
	private final NEED need = NEEDS.TYPES().HUNGER;
	private final MPlans plans;
	public ModuleFood(MPlans plans) {
		
		this.plans = plans;
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
			b.clearAccess(a);
		}
		
		if (!suspender.is(d)) {
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
				
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					
					AiPlanActivation p = plans.get(a, d, b);
					if (p != null)
						return p;
				}
			}
			
			
			AiPlanActivation p = eat.activate(a, d);
			if (p != null)
				return p;
			
			suspender.suspend(d);
			
		}
		
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		suspender.update(d);
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {

		if (suspender.is(d))
			return 0;
		
		int pp = need.stat().iPrio(a.indu());
		
		if (pp > 0) {			
			bits.clearSet(RESOURCES.EDI().mask);
			if (PATH().finders.resource.normal.has(a.tc().x(), a.tc().y(), bits))
				return pp;
			for (RoomServiceNeed b : a.race().service().services(a.indu().clas(), need)) {
				if (b.accessRequest(a) && b.finder.has(a.tc())) {
					return pp;
				}
			}
		}
		return 0;
		
	}
	
	private final AIPLAN eat = new AIPLAN.PLANRES(){

		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return fetchRaw.set(a, d);
		}
		
		private final Resumer fetchRaw = new Resumer("Finding Food") {
			
			final RBITImp bits = new RBITImp();
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				bits.clearSet(RESOURCES.EDI().mask);
				if (STATS.FOOD().STARVATION.indu().get(a.indu()) <= 0) {
					bits.and(STATS.FOOD().fetchMask(a));
				}
				return AI.SUBS().walkTo.resource(a, d, bits, Integer.MAX_VALUE);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				STATS.FOOD().eat(a, 0, 0);
				need.stat().fix(a.indu());
				return eat.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
		};
		
		private final Resumer eat = new Resumer("Eating") {
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (d.resourceCarried() != null && RESOURCES.EDI().is(d.resourceCarried())) {
					FACTIONS.player().res().inc(d.resourceCarried(), RTYPE.CONSUMED, -1);
				}
				d.resourceCarriedSet(null);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.modules().needs.subs.eat.activate(a, d);
			}
		};
		


		
		
	};

}
