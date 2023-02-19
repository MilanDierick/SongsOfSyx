package game.statistics;

import game.statistics.GCOUNTS.SAccumilator;
import init.D;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.*;
import settlement.stats.standing.STANDINGS;
import util.dic.DicMisc;

abstract class GRequirementImp extends G_REQ {
	
	private final String key;
	protected final boolean isAnti;
	protected final boolean isInt;
	protected final int target;
	protected final CharSequence name;
	
	private static CharSequence ¤¤AllTime = "¤(all time)";
	private static CharSequence ¤¤AllTimeH = "¤(all time high)";
	
	static {
		D.ts(GRequirementImp.class);
	}
	public GRequirementImp(String key, RData data, CharSequence name) {
		this.key = key + data.clas + data.race + data.resource;
		this.isAnti = data.isAnti;
		this.isInt = data.isInt;
		this.target = data.AMOUNT;
		this.name = name;
	}

	@Override
	public boolean isInt() {
		return isInt;
	}

	@Override
	public CharSequence name() {
		return name;
	}

	@Override
	public int target() {
		return target;
	}


	@Override
	public boolean isAnti() {
		return isAnti;
	}

	@Override
	protected String key() {
		return key;
	}

	static final class GRequirementStat extends GRequirementImp {
		
		private final STAT stat;
		private final HCLASS cl;
		private final Race race;
		
		public GRequirementStat(StatCollection coll, STAT stat, RData data) {
			super("STAT" + coll.key + stat.key(), data, coll.info.name + ": " + stat.info().name + nameGet(data));
			this.stat = stat;
			this.race = data.race;
			this.cl = data.clas;
			
		}
		
		private static String nameGet(RData data) {
			HCLASS cl = data.clas;
			Race race = data.race;
			if (cl != null && data.race != null) {
				return " (" + cl.names + "-" + race.info.names + ")";
			}else if (cl != null) {
				return " (" + cl.names + ")";
			}else if (race != null) {
				return " (" + race.info.names + ")";
			}else {
				return "";
			}
		}

		@Override
		public int value() {
			return isInt ? stat.data(cl).get(race) : (int)(stat.data(cl).getD(race)*100);
		}
		
	}
	
	static final class GRequirementEmp extends GRequirementImp {
		
		private final RoomBlueprintImp r;
		
		public GRequirementEmp(RoomBlueprintImp r, RData data) {
			super("EMPLOYED" + r.key, data, r.employment().title);
			this.r = r;
		}
		

		@Override
		public int value() {
			if (isInt) {
				return r.employment().employed();
			}
			return 100*r.employment().employed()/STATS.WORK().workforce();

		}
		
	}
	
	static final class GRequirementStatistics extends GRequirementImp {
		
		private final SAccumilator stat;

		private final boolean isAllTime;
		private final boolean isAllTimeHi;
		
		public GRequirementStatistics(SAccumilator stat, RData data) {
			super("COUNT"+stat.key, data, stat.name + nameGet(data));
			this.stat = stat;
			this.isAllTime = data.isAllTime;
			this.isAllTimeHi = !isAllTime && data.isAllTimeHi;
			
		}

		private static String nameGet(RData data) {
			if (data.isAllTime)
				return " " + ¤¤AllTime;
			else if (data.isAllTimeHi)
				return " " + ¤¤AllTimeH;
			return "";
		}
		
		@Override
		public boolean isInt() {
			return true;
		}

		@Override
		public int value() {
			if (isAllTime)
				return stat.allTime();
			else if(isAllTimeHi)
				return stat.allTimeHigh();
			return stat.current();
		}

	}
	
	static final class GRequirementPop extends GRequirementImp {

		private final Race race;
		private final HCLASS cl;
		
		public GRequirementPop(RData data) {
			super("POPULATION", data, nameGet(data));
			this.race = data.race;
			this.cl = data.clas;
		}

		private static CharSequence nameGet(RData data) {
			HCLASS cl = data.clas;
			Race race = data.race;
			if (cl != null && race != null) {
				return DicMisc.¤¤Population + " (" + cl.names + "-" + race.info.names + ")";
			}else if (cl != null) {
				return DicMisc.¤¤Population + " (" + cl.names + ")";
			}else if (race != null) {
				return DicMisc.¤¤Population + " (" + race.info.names + ")";
			}else {
				return DicMisc.¤¤Population;
			}
		}
		
		@Override
		public boolean isInt() {
			return true;
		}

		@Override
		public int value() {
			return STATS.POP().POP.data(cl).get(race);
		}
		
	}
	
	static final class GHappiness extends GRequirementImp {

		private final HCLASS cl;
		
		public GHappiness(RData data) {
			super("HAPPINESS", data, DicMisc.¤¤Happiness + " (" + (data.clas == null ? HCLASS.CITIZEN.name : data.clas.name) + ")");
			this.cl = data.clas == null ? HCLASS.CITIZEN : data.clas;
		}
		
		@Override
		public boolean isInt() {
			return false;
		}

		@Override
		public int value() {
			return (int) Math.ceil(STANDINGS.get(cl).current()*100);
		}
		
	}
	
	static final class GEmployed extends GRequirementImp {

		private final HCLASS cl;
		
		public GEmployed(RData data) {
			super("EMPLOYED", data, DicMisc.¤¤Happiness + " (" + (data.clas == null ? HCLASS.CITIZEN.name : data.clas.name) + ")");
			this.cl = data.clas == null ? HCLASS.CITIZEN : data.clas;
		}
		
		@Override
		public boolean isInt() {
			return false;
		}

		@Override
		public int value() {
			return (int) Math.ceil(STANDINGS.get(cl).current()*100);
		}
		
	}
}