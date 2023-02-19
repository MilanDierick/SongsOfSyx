package init.race;

import init.boostable.BOOSTABLE;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.boostable.BOOSTER_COLLECTION.OBJECT;
import settlement.main.SETT;
import settlement.room.main.RoomEmploymentSimple;
import snake2d.util.misc.CLAMP;

public final class RacePriorities extends BOOSTER_COLLECTION_IMP implements OBJECT<Race> {

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
	}

	@Override
	public double add(BOOSTABLE b, Race t) {
		return t.bonus().add(b);
	}

	@Override
	public double mul(BOOSTABLE b, Race t) {
		return t.bonus().mul(b);
	}

//	public double priority(Race r, RoomEmploymentSimple e) {
//		double p = r.bonus().getWorkValue(e)-skillMin;
//		return p*skillI;
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
