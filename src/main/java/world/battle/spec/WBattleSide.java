package world.battle.spec;

import init.config.Config;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;

public class WBattleSide {

	public final Coo coo = new Coo();
	public int men;
	public int losses;
	public int lossesRetreat;
	public final ArrayList<WBattleUnit> units = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	public int artilleryPieces;
	public double powerBalance;

}
