package world.army.util;

import game.faction.Faction;
import init.race.Race;

public interface DIV_STATS extends DIV_SETTING{

	public int men();
	public Race race();
	public double experience();
	public Faction faction();
	
	
}
