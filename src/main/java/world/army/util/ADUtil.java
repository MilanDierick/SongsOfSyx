package world.army.util;

import game.boosting.Boostable;
import world.army.AD;

public final class ADUtil {

	public final Power power = new Power();
	private final Boosts boosts = new Boosts();
	public final DivTypes types = new DivTypes();


	public ADUtil(AD ad) {
		
	}

	public double boost(DIV_STATS div, Boostable bo) {
		return boosts.get(div, bo);
	}
	
	
}
