package world.army.util;

import java.io.IOException;
import java.util.Arrays;

import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.*;

public class DivSpec implements SAVABLE, DIV_SETTING {

	public double[] equip = new double[STATS.EQUIP().BATTLE_ALL().size()];
	public double[] training = new double[STATS.BATTLE().TRAINING_ALL.size()];

	@Override
	public void save(FilePutter file) {
		file.ds(equip);
		file.ds(training);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ds(equip);
		file.ds(training);
	}

	@Override
	public void clear() {
		Arrays.fill(equip, 0);
		Arrays.fill(training, 0);
	}
	
	public DivSpec copy(DIV_SETTING other) {
		for (int i = 0; i < equip.length; i++) {
			equip[i] = other.equip(STATS.EQUIP().BATTLE_ALL().get(i));
		}
		for (int i = 0; i < training.length; i++) {
			training[i] = other.training(STATS.BATTLE().TRAINING_ALL.get(i));
		}
		return this;
	}
	
	public DivSpec copy(DIV_SETTING other, double e, double t) {

		for (int i = 0; i < equip.length; i++) {
			equip[i] = other.equip(STATS.EQUIP().BATTLE_ALL().get(i))*e;
		}
		for (int i = 0; i < training.length; i++) {
			training[i] = other.training(STATS.BATTLE().TRAINING_ALL.get(i))*t;
		}
		return this;
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
