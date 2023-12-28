package settlement.entity.humanoid.ai.service;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.service.MPlans.MPlan;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.food.tavern.ROOM_TAVERN;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class PlanTavern extends MPlan<ROOM_TAVERN>{

	public PlanTavern() {
		super(SETT.ROOMS().TAVERNS, false);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return eat.set(a, d);
	}
	
	Resumer eat = new Resumer("eat") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			f.startUsing();
			d.planByte1 = (byte) (STATS.FOOD().DRINK.decree().get(a));
			d.planByte2 = 0;
			return AI.modules().needs.subs.drink.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			f.consume();
			GAME.player().res().inc(RESOURCES.ALCOHOL(), RTYPE.CONSUMED, -1);
			succeed(a, d);
			
			d.planByte2 ++;
			if (d.planByte2 < d.planByte1 && f.findableReservedCanBe()) {
				f.findableReserve();
				f.startUsing();
				return AI.modules().needs.subs.drink.activate(a, d);
			}
			
			int am = d.planByte2;
			if (am >= 0)
				STATS.FOOD().DRINK.indu().set(a.indu(), am);
			
			if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
				return d.resumeOtherPlan(a, AI.modules().needs.eplans.drunk);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			return f != null && f.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			if ( f != null && f.findableReservedIs())
				f.findableReserveCancel();
		}
	};


	
}
