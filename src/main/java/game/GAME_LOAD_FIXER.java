package game;

import snake2d.util.sets.ArrayList;

public abstract class GAME_LOAD_FIXER {

	static ArrayList<GAME_LOAD_FIXER> all = new ArrayList<>(20);
	
	public GAME_LOAD_FIXER(){
		all.add(this);
	}
	
	protected abstract void fix();
	
}
