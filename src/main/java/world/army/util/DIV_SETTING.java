package world.army.util;

import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;

public interface DIV_SETTING {

	public double training(StatTraining tr);
	
	
	public double equip(EquipBattle e);
	
}
