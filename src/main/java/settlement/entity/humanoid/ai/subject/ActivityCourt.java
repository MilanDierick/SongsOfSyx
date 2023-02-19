package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.*;

import init.D;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.subject.AIModule_Subject.Activity;
import settlement.misc.util.FSERVICE;
import snake2d.util.rnd.RND;

final class ActivityCourt extends Activity{

	private static CharSequence ¤¤trial = "Watching an Trial";
	
	static {
		D.ts(ActivityCourt.class);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return start.set(a, d);
	}
	

	private final Resumer start = new Resumer(¤¤trial) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.serviceInclude(a, d, ROOMS().COURT.finder, 200);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return mourn.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}
	};
	
	private final Resumer mourn = new Resumer(¤¤trial) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (4+RND.rInt(8));
			return AI.SUBS().STAND.activate(a, d);
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1--;
			if (d.planByte1 >= 0) {
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			FSERVICE s = ROOMS().COURT.finder.get(d.path.destX(), d.path.destY());
			if (s != null)
				s.consume();
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return ROOMS().COURT.is(d.path.destX(), d.path.destY());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			 FSERVICE s = ROOMS().COURT.finder.get(d.path.destX(), d.path.destY());
			 if (s != null)
				 s.findableReserveCancel();
		}
	};

}
