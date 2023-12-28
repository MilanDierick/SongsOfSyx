package settlement.entity.humanoid.ai.service;

import init.race.RACES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.service.MPlans.MPlan;
import settlement.main.SETT;
import settlement.room.service.market.ROOM_MARKET;
import settlement.stats.equip.WearableResource;
import snake2d.util.rnd.RND;

final class PlanMarket extends MPlan<ROOM_MARKET>{

	private final AIPLAN ee;
	
	public PlanMarket(EPlans eplans) {
		super(SETT.ROOMS().MARKET, false);
		ee = eplans.shop;
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	final Resumer first = new Resumer("") {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			get(a, d).findableReserveCancel();
			succeed(a, d);
			d.planByte1 = (byte) (5 + RND.rInt(5));
			return AI.SUBS().STAND.activateRndDir(a, d, 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return shop.set(a, d);
	
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	final Resumer shop = new Resumer("") {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateRndDir(a, d, 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			ROOM_MARKET m = blue(d);
			if (m == null || !m.is(a.tc()))
				return d.resumeOtherPlan(a, ee);
			
			boolean bought = false;
			
			for (WearableResource e : RACES.res().all(a.indu().popCL())) {
				int needed = e.needed(a.indu());
				if (needed > 0) {
					int am = m.buy(RACES.res().get(e.resource(a.indu())), needed, a.tc().x(), a.tc().y());
					if (am > 0) {
						bought = true;
						e.wearOut(a.indu());
						e.inc(a.indu(), am);
					}
				}
			}
			
			d.planByte1--;
			if (bought || d.planByte1 > 0) {
				return walk.set(a, d);
			}
			return d.resumeOtherPlan(a, ee);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	final Resumer walk = new Resumer("") {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
		
			ROOM_MARKET m = blue(d);
			if (m.is(a.tc()) && d.planByte1 > 0) {
				AISubActivation s = AI.SUBS().walkTo.room(a, d, m.getter.get(a.tc()));
				if (s != null) {
					return s;
				}
			}
			return d.resumeOtherPlan(a, ee);
		
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return shop.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};


	
}
