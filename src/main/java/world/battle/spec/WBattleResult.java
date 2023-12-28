package world.battle.spec;

import init.race.RACES;
import init.resources.RESOURCES;

public abstract class WBattleResult {
	
	public enum RTYPE {
		VICTORY,
		DEFEAT,
		RETREAT
	}
	
	public WBattleSide player;
	public WBattleSide enemy;
	public RTYPE result;
	public int[] capturedRaces = new int[RACES.all().size()];
	public int[] lostResources = new int[RESOURCES.ALL().size()];
	
	
	public abstract void accept(int[] enslave, int[] resources);
	
	

}