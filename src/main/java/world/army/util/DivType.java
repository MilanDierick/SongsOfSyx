package world.army.util;

import init.race.RACES;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.sets.LIST;

public final class DivType implements DIV_SETTING{

	public final double occurence;
	public final double[] roccurence = new double[RACES.all().size()];

	private double[] equip = new double[STATS.EQUIP().BATTLE_ALL().size()];
	private double[] training = new double[STATS.BATTLE().TRAINING_ALL.size()];
	
	public DivType(double occ, LIST<StatTraining> trs, LIST<EquipBattle> eqps) {
		
		occurence = occ;
		
		for (StatTraining tr : trs)
			training[tr.tIndex] = 1.0;
		
		for (EquipBattle tr : eqps)
			equip[tr.indexMilitary()] = 1.0;
		
	}

	@Override
	public double training(StatTraining tr) {
		return training[tr.tIndex];
	}

	@Override
	public double equip(EquipBattle e) {
		return equip[e.indexMilitary()];
	}
	
	
}
