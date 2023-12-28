package settlement.entity.humanoid.ai.service;

import init.D;
import init.need.NEEDS;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.equip.WearableResource;

class ModuleEquipment extends Module.ModuleBackup{

	private static CharSequence ¤¤sName = "Returning equipment";

	static {
		D.ts(ModuleEquipment.class);
	}

	public ModuleEquipment(MPlans plans, EPlans eplans) {
		super(NEEDS.TYPES().SHOPPING, plans, eplans.shop);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		
		
		if (hasReturn(a)) {
			return ret.activate(a, d);
		}
		
		return super.getPlan(a, d);
	}


	@Override
	protected boolean canTryBackup(Humanoid a, AIManager d) {
		return true;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (hasReturn(a))
			return 5;
		return super.getPriority(a, d);
	}

	
	
	private boolean hasReturn(Humanoid a) {
		Induvidual i = a.indu();
		for (WearableResource e : STATS.EQUIP().allE()) {
			if (e.needed(i) < 0)
				return true;
		}
		for (WearableResource e : STATS.HOME().getTmp(i)) {
			if (e.needed(i) < 0) {
				return true;
			}
		}
		return false;
	}
	
	private final AIPLAN ret = new AIPLAN.PLANRES() {
		
		private final RBITImp bits = new RBITImp();
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			
			Induvidual i = a.indu();
			bits.clear();
			for (WearableResource e : STATS.EQUIP().allE()) {
				if (e.needed(a.indu()) < 0) {
					bits.or(e.resource(i));
				}
					
			}
			for (WearableResource e : STATS.HOME().getTmp(i)) {
				if (e.needed(i) < 0) {
					bits.or(e.resource(i));
				}
			}		
			
			if (bits.isClear())
				return null;
			
			RESOURCE res = SETT.PATH().finders.storage.reserve(a.tc(), bits, d.path, 200);
			
			if (res == null) {
				dump(a, d);
				return null;
			}else {
				d.planByte1 = res.bIndex();
				remOne(a, d, res);
				return walk.set(a, d);
				
			}
			
		}
		
		final Resumer walk = new Resumer(¤¤sName) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.depositInited(a, d, RESOURCES.ALL().get(d.planByte1));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (AIModules.current(d).moduleCanContinue(a, d)) {
					return init(a, d);
				}
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
				//Dump
				
			}
		};
		
		private void remOne(Humanoid a, AIManager d, RESOURCE res) {
			for (WearableResource e : STATS.EQUIP().allE()) {
				if (e.needed(a.indu()) < 0 && e.resource(a.indu()) == res) {
					e.inc(a.indu(), -1);	
					return;
				}
			}
			for (WearableResource e : STATS.HOME().getTmp(a.indu())) {
				if (e.needed(a.indu()) < 0 && e.resource(a.indu()) == res) {
					e.inc(a.indu(), -1);
					return;
				}
			}	
		}
		
		private void dump(Humanoid a, AIManager d) {
			Induvidual i = a.indu();
			
			for (WearableResource e : STATS.EQUIP().allE()) {
				int toDump = -e.needed(a.indu());
				if (toDump > 0) {
					e.inc(i, -toDump);
					SETT.THINGS().resources.create(a.physics.tileC(), e.resource(i), toDump);
					
				}
			}
			
			STATS.HOME().dump(a);
			
		}
	};


}
