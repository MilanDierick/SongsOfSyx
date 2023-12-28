package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import snake2d.util.rnd.RND;

final class Subs {

	public final AISUB drink = new AISUB.Simple("drinking") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 2 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			case 3 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 4 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			}
			return null;
		}

		
	};
	
	public final AISUB eat = new AISUB.Simple("eating") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 2 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			case 3 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 4 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			}
			return null;
		}

		
	};
	
	
}
