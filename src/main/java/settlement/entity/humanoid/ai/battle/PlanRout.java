package settlement.entity.humanoid.ai.battle;

import static settlement.main.SETT.*;

import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class PlanRout extends AIPLAN.PLANRES{
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return run.set(a, d);
	}
	
	private final Resumer run = new Resumer("Routing") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
			Div div = a.division();
			if (div != null) {
				DivMorale.DESERTION.incD(div, 1);
				int di = RND.rInt(DIR.ALL.size());
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR dir = DIR.ALL.getC(di+i);
					if (!div.settings.threat(dir) && !div.settings.threat(dir.next(1)) && !div.settings.threat(dir.next(-1))) {
						a.setDivision(null);
						a.speed.turn2(dir);
						return AI.SUBS().walkTo.run_arround_crazy(a, d, 5);
					}
				}
				a.setDivision(null);
			}
			
			a.speed.turn90().turn90();
			a.speed.turnWithAngel(RND.rFloat0(20));
			return AI.SUBS().walkTo.run_arround_crazy(a, d, 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (RND.oneIn(5))
				return path.set(a, d);
			else
				return surrendered.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer path = new Resumer("Routing") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entity.findExitNoEnemies(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, Integer.MAX_VALUE)) {
				return AI.SUBS().walkTo.pathRun(a, d);
			}
			return run.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AIManager.dead = CAUSE_LEAVE.DESERTED;
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer surrendered = new Resumer("Surrendered") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return AI.SUBS().LAY.activateTime(a, d, 10);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 ++;
			if (d.planByte1 >= 32) {
				AIManager.dead = CAUSE_LEAVE.DESERTED;
				return AI.SUBS().LAY.activateTime(a, d, 10);
			}
			
			if (a.indu().army() == SETT.ARMIES().player()) {
				if (SETT.ARMIES().enemy().men() == 0) {
					return path.set(a, d);
				}
			}
			return AI.SUBS().LAY.activateTime(a, d, 10);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
	
			return 0;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return false;
		}
		
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.COLLISION_TILE && !SETT.TILE_BOUNDS.holdsPoint(e.tx, e.ty)) {
			a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
			return false;
		}
			
		return super.event(a, d, e);
	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.DEFENCE)
			return 0;
		return super.poll(a, d, e);
	}
	
}
