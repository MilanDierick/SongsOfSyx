package settlement.entity.humanoid.ai.service;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.D;
import init.need.NEEDS;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

class ModuleDrink extends Module.ModuleBackup{

	private static CharSequence ¤¤sDrink = "Having a drink";
	
	static {
		D.ts(ModuleDrink.class);
	}
	
	public ModuleDrink(MPlans plans) {
		super(NEEDS.TYPES().THIRST, plans, new Drink());
	}
	


	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {

		if (STATS.FOOD().DRINK.decree().get(a) <= 0) {
			need.stat().fix(a.indu());
			return 0;
		}
		return super.getPriority(a, d);
		
		
	}
	
	private static final RBITImp bi = new RBITImp();
	
	private static final class Drink extends AIPLAN.PLANRES {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		
		
		private final Resumer walk = new Resumer(¤¤sDrink) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				bi.clearSet(RESOURCES.DRINKS().mask).and(STATS.FOOD().fetchMask(a));
				return AI.SUBS().walkTo.resource(a, d, bi);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
	
				
				GAME.player().res().inc(d.resourceCarried(), RTYPE.CONSUMED, -1);
				NEEDS.TYPES().THIRST.stat().fix(a.indu());
				STATS.FOOD().DRINK.indu().set(a.indu(), 1);
				
				return drink.set(a, d);
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
		
		private final Resumer drink = new Resumer(¤¤sDrink) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				if (RND.rBoolean())
					return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
				d.resourceCarriedSet(null);
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.fist, RND.rFloat()*4);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
				if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
					return d.resumeOtherPlan(a, AI.modules().needs.eplans.drunk);
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
		};
	}

	@Override
	protected boolean canTryBackup(Humanoid a, AIManager d) {
		bi.clearSet(RESOURCES.DRINKS().mask).and(STATS.FOOD().fetchMask(a));
		return !bi.isClear() && SETT.PATH().finders.resource.has(a.tc().x(), a.tc().y(), bi, bi, bi);
	}


}
