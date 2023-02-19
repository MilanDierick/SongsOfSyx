package game.battle;

import snake2d.util.file.SAVABLE;

interface Poller extends SAVABLE{

	abstract Prompt poll();
	
}
