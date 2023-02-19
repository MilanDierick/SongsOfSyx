package settlement.entity.humanoid.ai.types.slave;

import game.GAME;
import init.D;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE;
import snake2d.util.datatypes.COORDINATE;

final class PlanUprise extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "¤Minding own business!";
	
	
	static {
		D.ts(PlanUprise.class);
	}
	
	
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return steal.set(a, d);
	}
	
	private final Resumer steal = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			int i = GAME.events().uprising.spots.signUpUpriserPositionByte(a);
			if (i < 0)
				return null;
			d.planByte3 = (byte) i;
			
			return res(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (d.resourceCarried() != null) {
				for (EQUIPPABLE e : STATS.EQUIP().allE()) {
					if (e.resource() == d.resourceCarried()) {
						e.stat().indu().inc(a.indu(), 1);
					}
				}
				d.resourceCarriedSet(null);
			}

			long res = 0;
			for (EQUIPPABLE e : STATS.EQUIP().military_all()) {
				if (e.stat().indu().getD(a.indu()) < 0.3)
					res |= e.resource().bit;
			}
			
			if (res != 0) {
				AISubActivation s = AI.SUBS().walkTo.resource(a, d, res);
				if (s != null) {
					return s;
				}
			}
			return path.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return GAME.events().uprising.spots.confirmUpriser(d.planByte3);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			GAME.events().uprising.spots.cancelUpriser(a, d.planByte3, false);
		}
	};
	
	private final Resumer path = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			COORDINATE c = GAME.events().uprising.spots.getUpriserTile(d.planByte3);
			AISubActivation s = AI.SUBS().walkTo.around(a, d, c.x(), c.y(), 0, 20);
			if (s != null)
				return s;
			can(a, d);
			return null;
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return wait.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return GAME.events().uprising.spots.confirmUpriser(d.planByte3);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			GAME.events().uprising.spots.cancelUpriser(a, d.planByte3, false);
		}
	};
	
	private final Resumer wait = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			GAME.events().uprising.spots.reportUpriserInPosition(d.planByte3);
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return GAME.events().uprising.spots.confirmUpriser(d.planByte3);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			GAME.events().uprising.spots.cancelUpriser(a, d.planByte3, true);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.IS_SLAVE_READY_FOR_UPRISING)
				return d.planByte3;
			return super.poll(a, d, e);
		}
	};

}
