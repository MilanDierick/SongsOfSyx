package game.values;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.Profiler;
import game.faction.Faction;
import game.time.TIME;
import init.D;
import init.paths.PATHS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.data.DOUBLE_O;
import util.dic.DicGeo;
import util.keymap.RCollection;
import view.main.VIEW;

public final class GCOUNTS extends GameResource{
	
	private LinkedList<SAccumilator> all = new LinkedList<>();
	{
		D.gInit(this);
	}
	public final SAccumilator ENSLAVED = new SAccumilator(all, "ENSLAVED", true,  D.g("ENSLAVED", "Enslaved Population"));
	public final SAccumilator FREED_SLAVES = new SAccumilator(all, "FREED_SLAVES", true, D.g("FREED_SLAVES", "Slaves Freed"));
	public final SAccumilator TIME_PLAYED = new SAccumilator(all, "TIME_PLAYED", true, D.g("TIME_PLAYED", "Time Played"));
	public final SAccumilator TRADE_SALES = new SAccumilator(all, "TRADE_SALES", true, D.g("TRADE_SALES", "Denari from sales"));
	public final SAccumilator TRADE_PURCHASES = new SAccumilator(all, "TRADE_PURCHASES", true, D.g("TRADE_PURCHASES", "Denari from purchases"));
	public final SAccumilator ENEMIES_KILLED = new SAccumilator(all, "ENEMIES_KILLED", true, D.g("ENEMIES_KILLED", "Enemies killed"));
	public final SAccumilator RIOTS = new SAccumilator(all, "RIOTS", true, D.g("RIOTS", "Riots"));
	public final SAccumilator CRAFTED = new SAccumilator(all, "CRAFTED", true, D.g("CRAFTED", "Goods Crafted"));
	public final SAccumilator INVASIONS = new SAccumilator(all, "INVASIONS", true, D.g("INVASIONS", "Invasions"));
	public final SAccumilator EXECUTIONS = new SAccumilator(all, "EXECUTIONS", true, D.g("EXECUTIONS", "Executions"));
	public final SAccumilator TUNNELS = new SAccumilator(all, "TUNNELS", true, D.g("TUNNELS", "Tunnels dug"));
	public final SAccumilator ROOMS_BUILT = new SAccumilator(all, "ROOMS_BUILT", false, D.g("ENSLAVED", "Rooms built"));
	public final SAccumilator SUBJECTS = new SAccumilator(all, "SUBJECTS", false, D.g("SUBJECTS", "Population"));
	public final SAccumilator BATTLES_WON = new SAccumilator(all, "BATTLES_WON", false, D.g("BATTLES_WON", "Battles Won"));
	public final SAccumilator BATTLES_LOST = new SAccumilator(all, "BATTLES_LOST", false, D.g("BATTLES_LOST", "Battles Lost"));
	public final SAccumilator INVASIONS_WON = new SAccumilator(all, "INVASIONS_WON", false, D.g("INVASIONS_WON", "Invasions Won"));
	public final SAccumilator INVASIONS_LOST = new SAccumilator(all, "INVASIONS_LOST", false, D.g("INVASIONS_LOST", "Invasions Lost"));
	public final SAccumilator ROYALTIES_KILLED = new SAccumilator(all, "ROYALTIES_KILLED", false, D.g("ROYALTIES_KILLED", "Royalties assassinated"));
	public final SAccumilator CURED = new SAccumilator(all, "HOSPITAL_CURED", false, D.g("HOSPITAL_CURED", "Cured"));
	
	public final LIST<SAccumilator> ALL = new ArrayList<>(all);;
	public final RCollection<SAccumilator> MAP;
	
	private final static String filename = "StatsDoNotCheat";
	
	private static CharSequence ¤¤allTime = "¤all time";
	
	static {
		D.ts(GCOUNTS.class);
	}
	
	public GCOUNTS(){ 
		
		all = null;
		
		KeyMap<SAccumilator> m = new KeyMap<>();
		for (SAccumilator s : ALL)
			m.put(s.key, s);
		
		MAP = new RCollection<SAccumilator>("STATISTIC", m) {

			@Override
			public SAccumilator getAt(int index) {
				return ALL.get(index);
			}

			@Override
			public LIST<SAccumilator> all() {
				return ALL;
			}
		};
		
		read();
		
		GVALUES.FACTION.push("WORLD_REGIONS", DicGeo.¤¤Regions, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				return t.realm().regions();
			}
			
		}, false);
		
		
	}

	
	private void read() {
		try {
			Json json = new Json(PATHS.local().PROFILE.get(filename));
			for (SAccumilator s : ALL) {
				s.old = 0;
				s.hi = 0;
				if (json.has(s.key)) {
					s.old = json.i(s.key);	
				}
				if (json.has(s.key+"_HIGH"))
					s.hi = s.old = json.i(s.key+"_HIGH");	
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			for (SAccumilator s : ALL) {
				s.old = 0;
			}
			try {
				JsonE j = new JsonE();
				for (SAccumilator s : ALL) {
					j.add(s.key, 0);
				}
				if (!PATHS.local().PROFILE.exists(filename))
					PATHS.local().PROFILE.create(filename);
				j.save(PATHS.local().PROFILE.get(filename));
			}catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	
	public void flush() {
		if (!GAME.achieving())
			return;
		
		try {
			JsonE j = new JsonE();
			for (SAccumilator s : ALL) {
				j.add(s.key, CLAMP.i(s.allTime(), 0, Integer.MAX_VALUE));
				j.add(s.key+"_HIGH", CLAMP.i(s.allTimeHigh(), 0, Integer.MAX_VALUE));
			}
			if (!PATHS.local().PROFILE.exists(filename))
				PATHS.local().PROFILE.create(filename);
			j.save(PATHS.local().PROFILE.get(filename));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Json getJson() {
		if (PATHS.local().PROFILE.exists(filename))
			return new Json(PATHS.local().PROFILE.get(filename));
		return null;
	}

	@Override
	protected void save(FilePutter file) {
		file.i(ALL.size());
		for (SAccumilator s : ALL) {
			s.save(file);
		}
		flush();
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		int am = file.i();
		if (am != ALL.size()) {
			for (SAccumilator s : ALL) {
				s.value = 0;
			}
			for (int i = 0; i < am; i++)
				file.i();
		}else {
			for (SAccumilator s : ALL) {
				s.load(file);
			}
		}
		
		
		
	}

	@Override
	protected void update(float ds, Profiler prof) {
		TIME_PLAYED.set((int) (TIME.playedGame()/60.0));
	}

	public static class SAccumilator implements INDEXED{

		private int value;
		public final String key;
		private final boolean isBattle;
		private int old;
		private int hi;
		private final int index;
		public final CharSequence name;
		
		SAccumilator(LISTE<SAccumilator> all, String key, boolean isBattle, CharSequence name){
			index = all.add(this);
			this.key = "COUNT_" + key;
			this.isBattle = isBattle;
			this.name = name;
			GVALUES.FACTION.push(this.key + "_GAME", name, new DOUBLE_O<Faction>() {
				
				@Override
				public double getD(Faction t) {
					return current();
				}
			}, false);
			GVALUES.FACTION.push(this.key + "_ALL_TIME", name + " (" + ¤¤allTime + ")", new DOUBLE_O<Faction>() {
				
				@Override
				public double getD(Faction t) {
					return allTime();
				}
			}, false);
		}
		

		
		public void inc(int delta) {
			if (isBattle || !VIEW.b().isActive()) {
				value += delta;
				value &= Integer.MAX_VALUE;
			}
		}
		
		public void set(int a) {
			if (isBattle || !VIEW.b().isActive()) {
				value = a;
				value &= Integer.MAX_VALUE;
			}
		}


		public int allTime() {
			long l = old + value;
			if (l < 0)
				return 0;
			if (l > Integer.MAX_VALUE)
				return Integer.MAX_VALUE;
			return (int) l;
		}
		
		public int allTimeHigh() {
			return Math.max(value, hi);
		}

		public int current() {
			return value;
		}

		void save(FilePutter file) {
			file.i(value);
		}

		void load(FileGetter file) throws IOException {
			value = file.i();
		}



		@Override
		public int index() {
			return index;
		}
		
	}
	
}
