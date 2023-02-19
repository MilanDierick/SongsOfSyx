package settlement.stats;

import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.HCLASS;
import snake2d.util.MATH;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;

public final class STANDING{

	private final StandingDef def;
	private final STAT stat;
	
	STANDING(STAT stat, Json json){
		this.stat = stat;
		if (json == null)
			def = StandingDef.NONE;
		else
			def = new StandingDef(json);
	}
	
	STANDING(STAT stat, StandingDef def){
		this.stat = stat;
		if (def == null)
			def = StandingDef.NONE;
		this.def = def;
	}
	
	public StandingDef base() {
		return def;
	}

	
	public double getDismiss(HCLASS c, Race r) {
		return getDismiss(c, r, 0);
	}
	
	public double getDismiss(HCLASS c, Race r, int daysback) {
		if (r != null)
			return definition(r).get(c).dismiss ? definition(r).get(c).max : 0;
		double m = 0;
		double p = STATS.POP().POP.data(null).get(null, daysback);
		if (p == 0) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race ra = RACES.all().get(ri);
				m += (definition(ra).get(c).dismiss ? definition(ra).get(c).max : 0);
			}
			return m/RACES.all().size();
		}
			
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race ra = RACES.all().get(ri);
			m += (definition(ra).get(c).dismiss ? definition(ra).get(c).max : 0)*STATS.POP().POP.data(c).get(ra, daysback);
		}
		return m/p;
		
	}
	
	public double max(HCLASS c, Race r) {
		return max(c, r, 0);
	}
	
	public double max(HCLASS c, Race r, int daysback) {
		if (r != null)
			return definition(r).get(c).max;
		double m = 0;
		double p = STATS.POP().POP.data(c).get(null, daysback);
		if (p == 0) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race ra = RACES.all().get(ri);
				m += (definition(ra).get(c).max);
			}
			return m/RACES.all().size();
		}
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race ra = RACES.all().get(ri);
			m += definition(ra).get(c).max*STATS.POP().POP.data(c).get(ra, daysback);
		}
		return m/p;
		
	}
	
	public double normalized(HCLASS c, Race r) {
		if (r != null)
			return r.stats().defNormalized(c, this);
		double m = 0;
		double p = STATS.POP().POP.data(c).get(null);
		if (p == 0)
			return 0;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race ra = RACES.all().get(ri);
			m += ra.stats().defNormalized(c, this)*STATS.POP().POP.data(c).get(ra);
		}
		return m/p;
		
	}
	
	public double def(HCLASS c, Race r) {
		if (r != null)
			return get(c, r, definition(r).defaultInput);
		double m = 0;
		double p = STATS.POP().POP.data(c).get(null);
		if (p == 0)
			return 0;
		for (Race ra : RACES.all()) {
			m += get(c, ra, definition(ra).defaultInput)*STATS.POP().POP.data(c).get(ra);
		}
		return m/p;
	}
	
	public double get(HCLASS c, Race r) {
		return get(c, r, stat.data(c).getD(r));
	}
	
//	static int am;
//	static double v;
	public double getHistoric(HCLASS c, Race race, int daysBack) {
//		am++;
//		
//		if (Math.abs(v-VIEW.renderSecond()) > 2) {
//			System.out.println(am);
//			v = VIEW.renderSecond();
//			am = 0;
//		}
		
		if (race == null) {
			double m = 0;
			double p = STATS.POP().POP.data(c).get(null, daysBack);
			if (p == 0) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					m += getHistoric(c, ra, daysBack);
				}
				return m/RACES.all().size();
			}
				
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race ra = RACES.all().get(ri);
				m += getHistoric(c, ra, daysBack)*STATS.POP().POP.data(c).get(ra, daysBack);
			}
			return m/p;
		}
		StandingDef def = definition(race);
		double d = stat().data(c).getD(race, daysBack);
		d = def.getMulled(d);
		d = CLAMP.d(d, 0, 1);
		if (def.inverted)
			d = 1.0-d;
		return d*def.get(c).max;
	}
	
	public double getPrev(HCLASS c, Race race, int days) {
		if (race == null) {
			double m = 0;
			double p = STATS.POP().POP.data(c).getPeriod(null, days, 0);
			if (p == 0) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					m += getPrev(c, ra, days);
				}
				return m/RACES.all().size();
			}
				
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race ra = RACES.all().get(ri);
				m += getPrev(c, ra, days)*STATS.POP().POP.data(c).getPeriod(ra, days, 0);
			}
			return m/p;
		}
		StandingDef def = definition(race);
		double d = stat().data(c).getPeriodD(race, days, 0);
		d = def.getMulled(d);
		d = CLAMP.d(d, 0, 1);
		if (def.inverted)
			d = 1.0-d;
		return d*def.get(c).max;
	}
	
	public double get(Induvidual i) {
		return get(i.hType().CLASS, i.race(), stat.indu().getD(i));
	}
	
	public double get(HCLASS c, Race race, double input) {
		if (race == null) {
			double m = 0;
			double p = STATS.POP().POP.data(c).get(null);
			if (p == 0) {
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race ra = RACES.all().get(ri);
					m += get(c, ra, input);
				}
				return m/RACES.all().size();
			}
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race ra = RACES.all().get(ri);
				m += get(c, ra, input)*STATS.POP().POP.data(c).get(ra);
			}
			return m/p;
		}
		StandingDef def = definition(race);
		double d = input;
		d = def.getMulled(d);
		d = CLAMP.d(d, 0, 1);
		if (def.inverted)
			d = 1.0-d;
		return d*def.get(c).max;
	}

	public StandingDef definition(Race race) {
		return race.stats().def(this);
	};
	
	public STAT stat() {
		return stat;
	}
	
	
	
	public static final class StandingDef {

		public final static String key = "STANDING";
		private static KeyMap<Integer> oks;
		public final boolean inverted;
		public final double mul;
		private final double expo;
		public static StandingDef NONE = new StandingDef(null, 0);
		public final double defaultInput;
		public final MATH.MPOW exp;
		public double prio = 1;
		
		private final StandingData[] data = new StandingData[HCLASS.ALL.size()];
		
		public StandingDef(Json json){
			this(json, 0);
		}
		
		public StandingDef(Json json, StandingDef def){
			check(json);
			defaultInput =  def.defaultInput;
			inverted = json.bool("INVERTED", def.inverted);
			mul = json.dTry("MULTIPLIER", 0, 10000, def.mul);
			expo = json.dTry("EXPONENT", 0.01, 10, def.expo);
			prio = (int) json.dTry("PRIO", 0, 100000, def.prio);
			boolean dismiss = json.bool("DISMISS", def.data[0].dismiss);
			for (HCLASS c : HCLASS.ALL()) {
				this.data[c.index()] = json.has(c.key) ? new StandingData(json, c) : def.data[c.index()];
				this.data[c.index()].dismiss = dismiss;
			}
			
			if (expo == 1)
				exp = null;
			else
				exp = new MATH.MPOW(expo, 64);
			
		}
		
		private static void check(Json json) {
			if (oks == null) {
				oks = new KeyMap<>();
				oks.put("INVERTED", 1);
				oks.put("MULTIPLIER", 1);
				oks.put("EXPONENT", 1);
				oks.put("PRIO", 1);
				oks.put("DISMISS", 1);
				for (HCLASS c : HCLASS.ALL()) {
					oks.put(c.key, 1);
				}
			}
			for (String k : json.keys()) {
				if (!oks.containsKey(k)) {
					String a = "";
					for (String s : oks.keysSorted())
						a += s + ", ";
					json.error(k + " is not a valid STANDING field. Valid: " + a, k);
				}
			}
		}
		
		private double getMulled(double v) {
			
			double dd =  CLAMP.d(v*mul, 0, 1);
			if (exp != null)
				return exp.pow(dd);
			return dd;
		}
		
		public StandingDef(Json json, double def){
			boolean dismiss = false;
			defaultInput = def;
			if (json == null) {
				inverted = false;
				mul = 0;
				expo = 1;
				prio = 1;
				
			}else {
				if (json.has(key))
					json = json.json(key);
				//check(json);
				mul = json.has("MULTIPLIER") ? json.d("MULTIPLIER", 0, 100000) : 1;
				expo = json.dTry("EXPONENT", 0.01, 10, 1);
				prio = (int) json.dTry("PRIO", 0, 100000, 1);
				inverted = json.has("INVERTED") && json.bool("INVERTED");
				dismiss = json.has("DISMISS") && json.bool("DISMISS");
			}
			if (json != null) {
				for (HCLASS c : HCLASS.ALL()) {
					this.data[c.index()] = new StandingData(json, c);
					this.data[c.index()].dismiss = dismiss;
				}
			}else {
				for (HCLASS c : HCLASS.ALL()) {
					this.data[c.index()] = new StandingData(0);
					this.data[c.index()].dismiss = dismiss;
				}
			}
			
			if (expo == 1)
				exp = null;
			else
				exp = new MATH.MPOW(expo, 64);
		}
		
		public class StandingData {
			public final double max;
			public final double from;
			public final double to;
			public boolean dismiss;
			
			StandingData(Json json, HCLASS clas){
				this((json == null || !json.has(clas.key)) ? 0 : json.d(clas.key, 0, 1000000));
			}
			
			StandingData(double max){
				this.max = max;
				if (inverted) {
					from = this.max;
					to = 0;
				}else {
					from = 0;
					to = this.max;
				}
			}
		}

		
		public StandingData get(HCLASS c) {
			return data[c.index()];
		}

	}


	
	
}
