package world.army;

import init.boostable.*;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;

final class WDivBoosts {

	private double[] melee = new double[BOOSTABLES.all().size()];
	private double[] ranged  = new double[BOOSTABLES.all().size()];
	private double[] exp  = new double[BOOSTABLES.all().size()];
	
	public WDivBoosts() {
		add(STATS.BATTLE().TRAINING_MELEE, melee);
		add(STATS.BATTLE().TRAINING_ARCHERY, ranged);
		add(STATS.BATTLE().COMBAT_EXPERIENCE, exp);
	}
	
	private void add(STAT s, double[] aa) {
		for (BBooster ss : s.boosts()) {
			if (ss.boost.isMul())
				aa[ss.boost.boostable.index] *= ss.boost.value();
			else
				aa[ss.boost.boostable.index] += ss.boost.value();
		}
	}

	public double boost(WDIV d, BOOSTABLE bo) {
		double res = bo.race(d.race());
		res += melee[bo.index()]*d.training_melee();
		res += ranged[bo.index()]*d.training_ranged();
		res += exp[bo.index()]*d.experience();
		res += equip(d, bo);
		if (d.faction() != null) {
			res += d.faction().bonus().add(bo);
			res *= d.faction().bonus().mul(bo);
		}
		return res;
	}
	
	public double max(WDIV d, BOOSTABLE bo) {
		double res = bo.raceMax();
		res += Math.max(0, melee[bo.index()]);
		res += Math.max(0, ranged[bo.index()]);
		res += Math.max(0, exp[bo.index()]);
		res += equipMax(d, bo);
		if (d.faction() != null) {
			res += d.faction().bonus().maxAdd(bo);
			res *= d.faction().bonus().maxMul(bo);
		}
		return res;
	}
	
	public double equip(WDIV d, BOOSTABLE bo) {
		double mul = 1;
		double add = bo.defAdd;
		for (EQUIPPABLE_MILITARY mi : STATS.EQUIP().military_all()) {
			for (BBooster bb : mi.boosts()) {
				if (bb.boost.boostable == bo) {
					if (bb.boost.isMul())
						mul *= bb.boost.value()*d.equip(mi)/mi.max();
				}
			}
		}
		return add*mul;
	}
	
	public double equipMax(WDIV d, BOOSTABLE bo) {
		double mul = 1;
		double add = bo.defAdd;
		for (EQUIPPABLE_MILITARY mi : STATS.EQUIP().military_all()) {
			for (BBooster bb : mi.boosts()) {
				if (bb.boost.boostable == bo) {
					if (bb.boost.isMul())
						mul *= bb.boost.value();
				}
			}
		}
		return add*mul;
	}

	
	public int power(WDIV d) {
		double dd = 0;
		for (BOOSTABLE bo : BOOSTABLES.military()) {
			dd += STATS.BATTLE_BONUS().boost(bo)*boost(d, bo);
		}
		return (int) Math.ceil(dd*d.men());
	}

	
}
