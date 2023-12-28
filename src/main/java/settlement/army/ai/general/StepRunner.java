package settlement.army.ai.general;

import settlement.army.DivMorale;
import settlement.army.ai.general.MDivs.MDiv;
import snake2d.util.sets.LIST;

class StepRunner {

	void run(LIST<MDiv> all) {
		
		double aveDist = 0;
		int am = 0;
		for (MDiv d : all) {
			if (d.isDeployed) {
				aveDist += d.distance;
				am++;
			}
		}
		
		if (am == 0)
			return;
		
		aveDist /= am;
		
		for (MDiv d : all) {
			if (d.isDeployed) {
				boolean run = false;
				if (d.distance - 32 > aveDist)
					run = true;
				double proj = DivMorale.PROJECTILES.getD(d.div);
				if (proj > d.projectiles)
					run = true;
				d.projectiles = proj;
				d.div.settings.running = run;
			}
		}
		
	}
	
}
