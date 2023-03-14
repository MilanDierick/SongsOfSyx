package settlement.room.food.farm;

import game.time.TIME;

class Util {

	private Util() {
		
	}
	
	public static int prospect(FarmInstance ins) {
		ROOM_FARM b = ins.blueprintI();
		
		double base = base(ins);

		double fertility = b.constructor.isIndoors ? 1 : ins.tData.fertility();
		double skill = ins.tData.skill();
		double work = ins.tData.work();
		double e =  ins.blueprintI().event();
		double m = ins.blueprintI().moisture;
		return (int) (base*fertility*skill*work*e*m);
		
	}
	
	public static double base(FarmInstance ins) {
		ROOM_FARM b = ins.blueprintI();
		
		double rate = b.industries().get(0).outs().get(0).rate;
		double year = TIME.years().bitConversion(TIME.days());
		double area = ins.area()/ROOM_FARM.WORKERPERTILE;
		return (rate*year*area);
		
	}
	
	public static int prevHarvest(FarmInstance ins) {
		
		ROOM_FARM b = ins.blueprintI();
		Time t = b.time;
		if (t.dayI() < t.dayDeath)
			return (int) b.industries().get(0).outs().get(0).yearPrev.get(ins);
		else
			return (int) b.industries().get(0).outs().get(0).year.get(ins);
	}
}
