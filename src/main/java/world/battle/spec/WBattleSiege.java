package world.battle.spec;

import game.faction.FACTIONS;
import world.regions.Region;

public abstract class WBattleSiege {

	public WBattleSide player;
	public WBattleSide enemy;
	public Region besiged;
	public boolean victory;
	public double fortifications;
	
	public abstract void lift();
	public abstract void auto();
	
	
	public static abstract class Result extends WBattleResult {
		public Region besiged;
		
		public abstract void occupy(double plunderAmount, int[] enslave, int[] resources);
		public abstract void abandon(double plunderAmount, int[] enslave, int[] resources);
		public abstract void puppet(double plunderAmount, int[] enslave, int[] resources);
		
		public boolean canPuppet() {
			return FACTIONS.activateAvailable() > 0;
			
		}
		
	}
	
}
