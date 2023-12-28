package settlement.entity.humanoid.ai.danger;

import init.D;
import init.sound.SOUND;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

class PlanInjured extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "Bleeding out";
	private final SubPlanSeekHospital ho = new SubPlanSeekHospital(this);
	
	static {
		D.ts(PlanInjured.class);
	}
	
	public PlanInjured() {
		
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		AISubActivation s = ho.init(a, d);
		if (s != null)
			return s;
		return res.set(a, d);
	}
	
	private final Resumer res = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (RND.oneIn(10))
				SOUND.sett().action.pain.rnd(a.body());
			return AI.SUBS().LAY.activate(a, d);
			
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (!STATS.NEEDS().INJURIES.inDanger(a.indu())) {
				return null;
			}
			
			AISubActivation s = ho.init(a, d);
			if (s != null)
				return s;
		
			return set(a, d);
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
