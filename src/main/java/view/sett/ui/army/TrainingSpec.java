package view.sett.ui.army;

import java.util.Arrays;

import game.GAME;
import init.config.Config;
import settlement.army.Div;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import world.WORLD;

final class TrainingSpec {

	private  int upI = -1;
	
	private final int[] needsTraining = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	private final int[] training = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	
	private final EntityIterator.Humans iter = new EntityIterator.Humans() {
		
		@Override
		protected boolean processAndShouldBreakH(Humanoid h, int ie) {
			if (h.indu().clas().player)
				count(h);
			return false;
			
		}
		
		private void count(Humanoid h) {
			Div div = STATS.BATTLE().DIV.get(h);
			if (div != null) {
				if (h.indu().hType() == HTYPE.RECRUIT) {
					training[div.indexArmy()] ++;
					needsTraining[div.indexArmy()] ++;
				}else {
					for (ROOM_M_TRAINER<?> tra : ROOM_M_TRAINER.ALL()) {
						if (tra.training().shouldTrain(h.indu(), div.info.trainingD(tra).getD(), false)) {
							needsTraining[div.indexArmy()] ++;
							return;
						}
						
					}
				}
			}else {
				div = STATS.BATTLE().RECRUIT.get(h);
				if (div != null) {
					if (h.indu().hType() == HTYPE.RECRUIT) {
						training[div.indexArmy()] ++;
						needsTraining[div.indexArmy()] ++;
					}
				}
			}
			
		}
		
	};
	
	private void init() {
//		if (GAME.updateI() == upI)
//			return;
//		Arrays.fill(needsTraining, 0);
//		Arrays.fill(training, 0);
//		iter.iterate();
//		upI = GAME.updateI();
//		
		if (GAME.updateI() == upI)
			return;
		//int mul = ROOM_M_TRAINER.ALL().size();
		for (int di = 0; di < SETT.ARMIES().player().divisions().size(); di++) {
			Div d = SETT.ARMIES().player().divisions().get(di);
			
			needsTraining[di] = d.info.men()-STATS.BATTLE().DIV.stat().div().get(d)-STATS.BATTLE().RECRUIT.stat().div().get(d);
			if (WORLD.ARMIES().cityDivs().attachedArmy(d) != null) {
				needsTraining[di] -= WORLD.ARMIES().cityDivs().get(d).men();
			}
		}
		
		Arrays.fill(training, 0);
		iter.iterate();
		upI = GAME.updateI();
	}
	
	public int training(Div div) {
		init();
		return training[div.indexArmy()];
	}
	
	public int needsTraining(Div div) {
		init();
		return needsTraining[div.indexArmy()];
	}
	
}
