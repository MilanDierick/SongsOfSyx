package settlement.stats.health;

import game.time.TIME;
import init.D;
import init.boostable.*;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatBoosterStat;
import snake2d.util.misc.CLAMP;
import util.dic.DicArmy;

final class Bonuses {

	Bonuses(){
		
		D.t(this);
		
		new BBooster.BBoosterSimple(D.g("EntriesIntoCity", "New arrivals to city"), new BBoost(BOOSTABLES.CIVICS().HYGINE, 0.5, true), true, false, true) {
			
			@Override
			public double pvalue() {
				return boost.start + boost.delta*CLAMP.d(STATS.POP().COUNT.newEntries(), 0, 1);
			}
		};
		
		new StatBoosterStat(STATS.ENV().UNBURRIED, new BBoost(BOOSTABLES.CIVICS().HYGINE, 0.75, true));
		new BBooster.BBoosterSimple(STATS.NEEDS().DIRTINESS.stat().info().name, new BBoost(BOOSTABLES.CIVICS().HYGINE, 0.5, true), true, false, true) {

			@Override
			public double pvalue() {
				return boost.start + boost.delta*CLAMP.d(STATS.NEEDS().DIRTINESS.stat().data().getD(null)*2.0, 0, 1);
			}
		};
		new BBooster.BBoosterSimple(DicArmy.¤¤Besiege, new BBoost(BOOSTABLES.CIVICS().HYGINE, 0.1, true), true, false, true) {
			
			final double bi = 1.0/8*TIME.secondsPerDay;
			
			@Override
			public double pvalue() {
				return boost.start + boost.delta*CLAMP.d(SETT.ENTRY().besigeTime()-TIME.secondsPerDay*bi, 0, 1);
			}
		};
	}
	
}
