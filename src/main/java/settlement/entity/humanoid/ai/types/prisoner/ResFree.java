package settlement.entity.humanoid.ai.types.prisoner;

import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES.Resumer;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.CAUSE_ARRIVE;
import util.dic.DicMisc;

class ResFree {
	
	public static Resumer make(AIPLAN.PLANRES res) {
		return res .new Resumer(DicMisc.¤¤Free) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				SETT.ROOMS().PRISON.unregisterPrisoner(AI.modules().coo(d));
				AI.modules().coo(d).set(-1, -1);
				PrisonerData.self.reportedPunish.set(d, 1);
				
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				a.HTypeSet(HTYPE.SUBJECT, null, CAUSE_ARRIVE.PAROLE);
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
	}
	
}
