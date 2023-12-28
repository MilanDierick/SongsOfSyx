package settlement.army.ai.general;

import game.time.TIME;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.army.order.DivTDataTask;
import snake2d.util.sets.ArrayList;

class StepLineCharge {

	private final DivTDataTask task = new DivTDataTask();
	private double wait = 16.0;
	
	public boolean charge(Context context, ArrayList<MDiv> divs, UtilEnemyArea ee) {
		
		if (divs.size() == 0)
			return false;
		
		
		
		for (MDiv charge : divs) {
			
			if (ee.is(charge.tx, charge.ty) && charge.div.settings.isFighting()) {
				chargeAll(divs);
				return false;
			}
		
		}
		
		double am = 0;
		double tot = 0;
		
		for (MDiv charge : divs) {
			
			if (charge.lineI != -1 && charge.lineBack == 0 && charge.destX == -1) {
				tot += 1;
				if (isCharger(charge)) {
					am++;
					if (TIME.currentSecond()-charge.inPositionSince > wait)
						am+=3;
						
				}
			}
				
			
		}
		

		
		am /= tot;
		
		for (MDiv charge : divs) {

			if (!isCharger(charge))
				continue;
			
			
			if (TIME.currentSecond()-charge.inPositionSince >= wait-wait*am) {
				
				charge.inPositionSince -= wait;
				task.charge();
				charge.div.order().task.set(task);
				charge.busyUntil = TIME.currentSecond()+20;
				return true;
			}
			
		}
		
		return false;
	}
	
	public void chargeAll(ArrayList<MDiv> divs) {

		for (MDiv charge : divs) {

			if (!isCharger(charge))
				continue;

			charge.inPositionSince -= wait;
			task.charge();
			charge.div.order().task.set(task);
			charge.busyUntil = TIME.currentSecond()+20;
			
		}
	}
	
	private boolean isCharger(MDiv charge) {
		
		if (charge.lineI < 0)
			return false;
		
		if (charge.busyUntil > TIME.currentSecond())
			return false;
		
		if (charge.lineBack > 0)
			return false;
		
		if (charge.inPositionSince >= TIME.currentSecond())
			return false;
		
		if (charge.ranged && charge.div.trajectory.hasAny())
			return false;
		
		return true;
	}
	
}
