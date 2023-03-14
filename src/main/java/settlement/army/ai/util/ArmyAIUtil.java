package settlement.army.ai.util;

import init.config.Config;
import settlement.main.SETT;

public final class ArmyAIUtil {
	
	public static final int MAX_AREA_DISTANCE = 3;
	
	final DivTDataStatus[] statuses = new DivTDataStatus[Config.BATTLE.DIVISIONS_PER_BATTLE];
	final DivsTileMap map = new DivsTileMap(statuses);
	final DivsQuadMap quads = new DivsQuadMap(statuses);
	final DivsSpaceMap space = new DivsSpaceMap(statuses);
	
	ArmyAIUtil() {
		for (int i = 0; i < statuses.length; i++)
			statuses[i] = new DivTDataStatus();
	}

	
	void copy() {
		for (short i = 0; i < statuses.length; i++) {
			SETT.ARMIES().division(i).order().status.set(statuses[i]);
		}
	}
	
	public static DivsTileMap map() {
		return ArmyAIUtilThread.self.current().map;
	}
	
	public static DivsQuadMap quads() {
		return ArmyAIUtilThread.self.current().quads;
	}
	
	public static DivsSpaceMap space() {
		return ArmyAIUtilThread.self.current().space;
	}
	
}
