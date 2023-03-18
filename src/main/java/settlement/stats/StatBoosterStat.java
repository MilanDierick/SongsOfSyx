package settlement.stats;

import init.boostable.BBoost;
import init.boostable.BBooster;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;

public final class StatBoosterStat extends BBooster.BBoosterImp {

	private final STAT stat;
	private final double mul;

	public StatBoosterStat(CharSequence name, STAT stat, BBoost boost, double mul) {
		super(name, boost, true, true, false);
		this.stat = stat;
		stat.boosts.add(this);
		this.mul = mul;
	}
	
	public StatBoosterStat(STAT stat, BBoost boost, double mul) {
		this(stat.info().name, stat, boost, mul);
	}
	
	public StatBoosterStat(CharSequence name, STAT stat, BBoost boost) {
		this(name, stat, boost, 1);
	}
	
	public StatBoosterStat(STAT stat, BBoost boost) {
		this(stat.info().name, stat, boost);
	}

	@Override
	public double pvalue(Induvidual v) {
		return stat.indu().getD(v)*mul;
	}

	@Override
	public double pvalue(HCLASS c, Race r) {
		return stat.data(c).getD(r)*mul;
	}

	@Override
	public double pvalue(Div v) {
		return stat.div().getD(v)*mul;
	}


}