package settlement.entity.humanoid.ai.types.recruit;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomInstance;
import settlement.room.military.barracks.ROOM_BARRACKS;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class PlanBarracks extends AIPLAN.PLANRES{

	private final ROOM_BARRACKS b = SETT.ROOMS().BARRACKS;
	private final AIModule_Recruit module;
	
	PlanBarracks(AIModule_Recruit module){
		this.module = module;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return inits.set(a, d);
	}
	
	private final Resumer done = new Resumer(b.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (job(a, d) != null)
				job(a, d).jobReserveCancel(null);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			a.HTypeSet(HTYPE.SUBJECT, null, null);
			return d.resumeOtherPlan(a, AI.plans().NOP);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private JOBMANAGER_HASER get(Humanoid a) {
		RoomInstance w = STATS.WORK().EMPLOYED.get(a);
		if (w == null || w.blueprintI() != SETT.ROOMS().BARRACKS)
			return null;
		return (JOBMANAGER_HASER) w;
	}
	
	private SETT_JOB job(Humanoid a, AIManager d) {
		JOBMANAGER_HASER w = get(a);
		if (w == null)
			return null;
		
		SETT_JOB j = w.getWork().getJob(d.planTile);
		if (j == null || !j.jobReservedIs(null))
			return null;
		
		if (!module.planShouldContinue(a, d)) {
			j.jobReserveCancel(null);
			return null;
		}
			
		return j;
	}
	
	final Resumer inits = new Resumer(b.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			JOBMANAGER_HASER w = get(a);
			
			if (w == null)
				return done.set(a, d);
			
			SETT_JOB j = w.getWork().getReservableJob(a.tc());
			
			if (j == null)
				return done.set(a, d);
			
			d.planTile.set(j.jobCoo());
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, j.jobCoo());
			
			if (s != null) {
				j = w.getWork().getJob(d.planTile);
				j.jobReserve(null);
				return s;
			}
			
			return done.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (job(a, d) == null)
				return null;
			return walkLast.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			if (j != null)
				j.jobReserveCancel(null);
		}
	};
	
	final Resumer walkLast = new Resumer(b.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			COORDINATE man = ROOMS().BARRACKS.faceCoo(d.planTile.x(), d.planTile.y());
			DIR dir = DIR.get(d.planTile, man);
			AISTATE s = AI.STATES().WALK2.edge(a, d, dir);
			a.speed.setDirCurrent(dir);
			return AI.SUBS().DUMMY.activate(a, d, s);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (job(a, d) == null)
				return null;
			a.speed.magnitudeTargetSet(0);
			a.speed.magnitudeInit(0);
			return fight.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			if (j != null)
				j.jobReserveCancel(null);
		}
	};
	
	final Resumer fight = new Resumer(b.employment().verb) {
		
		private final AISUB.Simple sub = new AISUB.Simple() {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (job(a, d) == null)
					return null;
				if (d.subByte == 1) {
					return AI.STATES().anima.sword_out.activate(a, d);
				}else if (d.subByte == 2){
					job(a, d).jobSound().rnd(a.body());
					return AI.STATES().anima.sword_in.activate(a, d);
				}
				
				return null;
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			j.jobStartPerforming();
			return sub.activate(a, d, AI.STATES().anima.sword.activate(a, d, 5+RND.rFloat(5)));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (job(a, d) == null)
				return null;
			if (!module.planShouldContinue(a, d)) {
				can(a, d);
				return null;
			}
			
			return sub.activate(a, d, AI.STATES().anima.sword.activate(a, d, 5+RND.rFloat(5)));
		}
		
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			if (j != null)
				j.jobReserveCancel(null);
		}
		
	};
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING)
			return 1.0;
		return super.poll(a, d, e);
	}


}