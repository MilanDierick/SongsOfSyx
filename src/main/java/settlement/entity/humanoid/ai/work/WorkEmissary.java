package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.*;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.stats.STATS;

final class WorkEmissary extends PlanBlueprint {

	protected WorkEmissary(AIModule_Work module, PlanBlueprint[] map) {
		
		super(module, ROOMS().EMBASSY, map);
	}
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		d.planByte1 = 0;
		
		JOBMANAGER_HASER j = (JOBMANAGER_HASER) STATS.WORK().EMPLOYED.get(a);
		
		SETT_JOB c = j.getWork().getReservableJob(a.tc());
		
		if (c == null)
			return null;
		d.planTile.set(c.jobCoo());
		c.jobReserve(null);
		work.set(a, d);
		return AI.SUBS().walkTo.coo(a, d, c.jobCoo());
		
	}
	
	final Resumer work = new Resumer(SETT.ROOMS().EMBASSY.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
	
			d.planByte1 ++;
			if (d.planByte1 == 1)
				return AI.SUBS().WORK_HANDS.activate(a, d, 60);
			
			JOBMANAGER_HASER j = (JOBMANAGER_HASER) STATS.WORK().EMPLOYED.get(a);
			j.getWork().getJob(d.planTile).jobPerform(a, null, 0);
			
			if ((TIME.days().bitsSinceStart()& 0b01) == 0) {
				return null;
			}
			
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.7)
				return null;
			
			if (AIModules.nextPrio(d) > 7) {
				return null;
			}
			
			return goOnMission.set(a, d);
			
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprint() == SETT.ROOMS().EMBASSY) {
				JOBMANAGER_HASER j = (JOBMANAGER_HASER) STATS.WORK().EMPLOYED.get(a);
				SETT_JOB c = j.getWork().getJob(d.planTile);
				if (c != null)
					c.jobReserveCancel(null);
			}
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprint() == SETT.ROOMS().EMBASSY) {
				JOBMANAGER_HASER j = (JOBMANAGER_HASER) STATS.WORK().EMPLOYED.get(a);
				SETT_JOB c = j.getWork().getJob(d.planTile);
				return c != null && c.jobReservedIs(null);
			}
			return false;
		}

	};
	
	final Resumer goOnMission = new Resumer(SETT.ROOMS().EMBASSY.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return beOnMission.set(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

	};
	
	final Resumer beOnMission = new Resumer(SETT.ROOMS().EMBASSY.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			SETT.ENTITIES().moveIntoTheTheUnknown(a);
			a.speed.magnitudeInit(0);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.9) {
				can(a, d);
				return null;
			}
			if (AIModules.nextPrio(d) > 7) {
				can(a, d);
				return null;
			}
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT.ENTITIES().returnFromTheTheUnknown(a);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

	};

	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		if (!super.shouldContinue(a, d))
			return false;
		
		return true;
	}


}