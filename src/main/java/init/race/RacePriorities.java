package init.race;

import init.boostable.*;
import init.boostable.BOOST_LOOKUP.BOOSTER_LOOKUP_IMP;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;

public final class RacePriorities extends BOOSTER_LOOKUP_IMP {

	private final double skillD;
	private final double skillMin;
	
	public RacePriorities() {
		super(RACES.name());
		
		for (Race r : RACES.all())
			init(r.bonus());
		
		
		{
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
				for (Race r : RACES.all()) {
					max = Math.max(max, r.bonus().getWorkValue(p));
					min = Math.min(min, r.bonus().getWorkValue(p));
				}
			}
			
			this.skillMin = min;
			this.skillD = max-skillMin;
		}
		
		for (BBoost b : makeBoosts()) {
			
			if (b.isMul())
				new BonusMul(b);
			else
				new BonusAdd(b);
		}
		
		
	}
	
	private static class BonusAdd extends BBooster {

		protected BonusAdd(BBoost boost) {
			super(RACES.name(), boost, true, true, false);
		}

		@Override
		public double value(Induvidual i) {
			return i.race().bonus().add(boost.boostable);
		}

		@Override
		public double value(HCLASS c, Race r) {
			if (r == null) {
				double res = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r2 = RACES.all().get(ri);
					res += STATS.POP().POP.data(c).get(r2)*r2.bonus().add(boost.boostable);
				}
				double p = STATS.POP().POP.data(c).get(null);
				if (p != 0)
					return res/p;
				return 0;
			}
			return r.bonus().add(boost.boostable);
			
		}

		@Override
		public double value(Div v) {
			return v.info.race().bonus().add(boost.boostable);
		}
		
	}

	private static class BonusMul extends BBooster {

		protected BonusMul(BBoost boost) {
			super(RACES.name(), boost, true, true, false);
		}

		@Override
		public double value(Induvidual i) {
			return i.race().bonus().mul(boost.boostable);
		}

		@Override
		public double value(HCLASS c, Race r) {
			if (r == null) {
				double res = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r2 = RACES.all().get(ri);
					res += STATS.POP().POP.data(c).get(r2)*r2.bonus().mul(boost.boostable);
				}
				double p = STATS.POP().POP.data(c).get(null);
				if (p != 0)
					return res/p;
				return 0;
			}
			return r.bonus().mul(boost.boostable);
			
		}

		@Override
		public double value(Div v) {
			return v.info.race().bonus().mul(boost.boostable);
		}
		
	}

	public double add(BOOSTABLE b, Race t) {
		return t.bonus().add(b);
	}

	public double mul(BOOSTABLE b, Race t) {
		return t.bonus().mul(b);
	}

	public double tot(BOOSTABLE b, Race t) {
		return mul(b,t)*(1+add(b,t));
	}
	
//	public void hover(GUI_BOX box, BOOSTABLE b, Race tt) {
//		hover(box, b, mul(b, tt), add(b, tt));
//	}
	
	public double priorityCapped(Race r, RoomEmploymentSimple e) {
		double p = r.bonus().getWorkValue(e)-skillMin;
		if (p < 1) {
			return 0.5*p;
		}else if (p > 1){
			return CLAMP.d(0.5+Math.sqrt((p-1)/(skillD-1)), 0, 1);
		}
		return 0.5;
	}

}
