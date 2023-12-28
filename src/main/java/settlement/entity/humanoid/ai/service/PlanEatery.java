package settlement.entity.humanoid.ai.service;

import init.resources.ResG;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.service.MPlans.MPlan;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.STATS;

final class PlanEatery extends MPlan<ROOM_EATERY>{

	public PlanEatery() {
		super(SETT.ROOMS().EATERIES, false);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	final Resumer first = new Resumer("") {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.modules().needs.subs.eat.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			ResG edi = a.race().pref().prefAllowedFood(a);
			short da = blue(d).eat(edi, STATS.FOOD().RATIONS.decree().get(a), d.planTile.x(), d.planTile.y(), STATS.FOOD().fetchMask(a));
			STATS.FOOD().eat(a, edi, da);
			succeed(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE ss = blue(d).service().service(d.planTile.x(), d.planTile.y());
			if (ss != null)
				ss.findableReserveCancel();
		}
	};


	
}
