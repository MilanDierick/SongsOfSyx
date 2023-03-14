package settlement.stats;


import init.boostable.*;
import settlement.army.Div;
import snake2d.util.sets.*;

public final class BattleBonus{
	
	private double[] powers = new double[BOOSTABLES.all().size()];
	private double max = -1;
	private final ArrayList<BOOSTABLE> all;
	
	private final LIST<STAT> effects;
	
	BattleBonus() {
		
		LinkedList<BOOSTABLE> all = new LinkedList<>();
		power(all, BOOSTABLES.BATTLE().ARMOUR, 3.0);
		power(all, BOOSTABLES.BATTLE().BLUNT_DAMAGE, 0.25);
		power(all, BOOSTABLES.BATTLE().DEFENCE, 2.0);
		power(all, BOOSTABLES.BATTLE().MORALE, 5.0);
		power(all, BOOSTABLES.BATTLE().OFFENCE, 2.0);
		power(all, BOOSTABLES.BATTLE().PIERCE_DAMAGE, 2.0);
		power(all, BOOSTABLES.BATTLE().RANGED_SKILL, 2.0);
		power(all, BOOSTABLES.PHYSICS().MASS, 0.2);
		power(all, BOOSTABLES.PHYSICS().SPEED, 0.5);
		power(all, BOOSTABLES.PHYSICS().STAMINA, 0.25);
		
		this.all = new ArrayList<BOOSTABLE>(all);
		
		LinkedList<STAT> eff = new LinkedList<>();
		for (STAT s : STATS.all()) {
			for (BBooster b : s.boosts) {
				if (powers[b.boost.boostable.index()] != 0) {
					eff.add(s);
					break;
				}
			}
		}
		this.effects = new ArrayList<>(eff);
	}
	
	private void power(LinkedList<BOOSTABLE> all, BOOSTABLE bo, double mul) {
		powers[bo.index()] = mul;
		all.add(bo);
	}
	
	public LIST<BOOSTABLE> boosts(){
		return all;
	}
	
	public LIST<STAT> stats(){
		return effects;
	}
	
	public double boost(BOOSTABLE b) {
		return powers[b.index()];
	}
	
	public double power(Div d) {
		double dd = 0;
		for (BOOSTABLE bo : all) {
			dd += powers[bo.index]*bo.get(d);
		}
		return dd;
	}
	
	public double max() {
		if (max == -1) {
			double m = 0;
			for (BOOSTABLE bo : all) {
				m += powers[bo.index]*BOOSTABLES.player().max(bo);
			}
			this.max = m;
		}
		return max;
	}
	
	public double power(Induvidual d) {
		double dd = 0;
		for (BOOSTABLE bo : all) {
			dd += powers[bo.index]*bo.get(d);
		}
		return dd;
	}
	
}