package settlement.entity.humanoid.ai.types.recruit;

import init.C;
import init.D;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.military.archery.ROOM_ARCHERY;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;

final class PlanRange extends AIPLAN.PLANRES{

	private static CharSequence ¤¤Training = "¤Training";
	static {
		D.ts(PlanRange.class);
	}
	private final AIModule_Recruit module;
	
	PlanRange(AIModule_Recruit module){
		this.module = module;
	}
	
	private final ROOM_ARCHERY b = SETT.ROOMS().ARCHERY;
	
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
		if (w == null || w.blueprintI() != SETT.ROOMS().ARCHERY)
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
				j = w.getWork().getJob(d.planTile);;
				j.jobReserve(null);
				return s;
			}
			
			return done.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (job(a, d) == null)
				return null;
			DIR dir = get(d).faceCoo(d.planTile.x(), d.planTile.y());
			a.speed.setDirCurrent(dir);
			a.speed.magnitudeTargetSet(0);
			a.speed.magnitudeInit(0);
			return work.set(a, d);
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
	

	
	final Resumer work = new Resumer(¤¤Training) {
		
		private final AISUB sub = new AISUB.Simple() {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte == 1) 
					return AI.STATES().STAND.activate(a, d, 10);
				if (d.subByte == 2) 
					return AI.STATES().anima.archer1.activate(a, d, 3);
				if (d.subByte == 3) {
					if (get(d) != null) {
						DIR dir = get(d).faceCoo(d.planTile.x(), d.planTile.y());
						get(d).fireArrow(a.tc().x(), a.tc().y(), a.body().cX()+dir.x()*C.TILE_SIZEH, a.body().cY()+dir.y()*C.TILE_SIZEH);
					}
					return AI.STATES().anima.archer2.activate(a, d, 3);
				}
				return null;
			}
		};
		
		
		
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (!module.planShouldContinue(a, d)) {
				can(a, d);
				return null;
			}
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (job(a, d) == null)
				return null;
			return set(a, d);
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
	
	private ROOM_ARCHERY get(AIManager d) {
		Room r = SETT.ROOMS().map.get(d.planTile);
		if (r != null && r.blueprint() instanceof ROOM_ARCHERY) {
			return (ROOM_ARCHERY) r.blueprint();
		}
		return null;
	}
	


}