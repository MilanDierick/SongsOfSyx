package game;

import snake2d.CORE_STATE;
import snake2d.util.sets.KeyMap;
import view.main.VIEW;

public class GameConRandom implements CORE_STATE.Constructor{
	
	public final KeyMap<Double> BOOSTS = new KeyMap<>();
	
	public GameConRandom(){
		
	}
	
	@Override
	public CORE_STATE getState() {
		
		new GAME(this);
		CORE_STATE s = new VIEW();
		GAME.achieve(true);
		for (Double d : BOOSTS.all()) {
			if (d > 0) {
				GAME.achieve(false);
				break;
			}
		}
		
		return s;
	}


	
}
