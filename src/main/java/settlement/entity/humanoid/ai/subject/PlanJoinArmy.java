package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.*;

import init.D;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
import world.World;

class PlanJoinArmy extends AIPLAN.PLANRES{

	{
		D.t(this);
	}
	
	public int getPriority(Humanoid a) {
		
		boolean training = a.indu().hType() == HTYPE.RECRUIT;
		
		if (!SETT.ARMIES().info.updateDiv(a, training)) {
			return 0;
		}
		
		if (World.ARMIES().cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a)) == null) {
			return 0;
		}
		
		if (!SETT.PATH().entryPoints.hasAny())
			return 0;
		
		if (SETT.ARMIES().info.shouldTrain(a, false))
			return 0;
		
		return 10;
		
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
			World.ARMIES().cityDivs().add(a, STATS.BATTLE().DIV.get(a));
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return STATS.BATTLE().DIV.get(a) != null && World.ARMIES().cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a)) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
}
