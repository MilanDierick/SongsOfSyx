package settlement.entity.humanoid.ai.crime;

import init.D;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.law.PRISONER_TYPE.CRIME;

final class Vandalism extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "¤Vandalizing";
	
	static{
		D.ts(Vandalism.class);
	}
	
	final AIModule_Crime m;
	
	public Vandalism(AIModule_Crime m) {
		this.m = m;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return go.set(a, d);
	}

	private final Resumer go = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (SETT.MAINTENANCE().finder().reserve(a.tc(), d.path, 100)) {
				SETT.MAINTENANCE().finder().getReserved(d.path.destX(), d.path.destY()).findableReserveCancel();
				return AI.SUBS().walkTo.path(a, d);
			}else if (SETT.MAINTENANCE().maintainable(a.tc().x(), a.tc().y())) {
				return AI.SUBS().walkTo.coo(a, d, a.tc().x(), a.tc().y());
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return next.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private final Resumer next = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.box, 2.5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			m.commitCrime(a, d, true, CRIME.VANDALISM);
			SETT.MAINTENANCE().vandalise(d.path.destX(), d.path.destY());
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.NOTIFY_CRIME)
			return false;
		return super.event(a, d, e);
	}
	
}
