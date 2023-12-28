package game.battle;

import init.config.Config;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import world.army.WDivGeneration;

public abstract class PlayerBattleSpec{
	
	public SpecSide player = new SpecSide();
	public SpecSide enemy = new SpecSide();
	
	public static class SpecSide {
		
		public final Coo wCoo = new Coo();
		public int artillery;
		public double moraleBase;
		public ArrayList<WDivGeneration> divs = new ArrayList<WDivGeneration>(Config.BATTLE.DIVISIONS_PER_ARMY);
	}
	
	public abstract void conclude(boolean timer, boolean retreat);
	
	public abstract void finish();
}