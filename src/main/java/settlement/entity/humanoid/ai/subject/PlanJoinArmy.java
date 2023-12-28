package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.*;

import init.D;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import world.WORLD;

class PlanJoinArmy extends AIPLAN.PLANRES{

	{
		D.t(this);
	}
	
	public int getPriority(Humanoid a) {
		
		return SETT.ARMIES().info.shouldJoinArmy(a) ? 10 : 0;
		
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return path.set(a, d);
	}
	
	private final Resumer path = new Resumer(D.g("Leaving", "Leaving for the Army")) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entity.findExitNoEnemies(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, Integer.MAX_VALUE)) {
				
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AIManager.dead = CAUSE_LEAVE.ARMY;
			WORLD.ARMIES().cityDivs().add(a, STATS.BATTLE().DIV.get(a));
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.ARMIES().info.shouldJoinArmy(a);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
}
