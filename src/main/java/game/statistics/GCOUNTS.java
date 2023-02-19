package game.statistics;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.time.TIME;
import init.paths.PATHS;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.keymap.RCollection;
import view.main.VIEW;

public final class GCOUNTS extends GameResource{
	
	private Json j = new Json(PATHS.TEXT().getFolder("player").get("_STATISTICS"));	
	private LinkedList<SAccumilator> all = new LinkedList<>();
	public final SAccumilator ENSLAVED = new SAccumilator(all, "ENSLAVED", j, true);
	public final SAccumilator FREED_SLAVES = new SAccumilator(all, "FREED_SLAVES", j, true);
	public final SAccumilator TIME_PLAYED = new SAccumilator(all, "TIME_PLAYED", j, true);
	public final SAccumilator TRADE_SALES = new SAccumilator(all, "TRADE_SALES", j, true);
	public final SAccumilator TRADE_PURCHASES = new SAccumilator(all, "TRADE_PURCHASES", j, true);
	public final SAccumilator ENEMIES_KILLED = new SAccumilator(all, "ENEMIES_KILLED", j, true);
	public final SAccumilator RIOTS = new SAccumilator(all, "RIOTS", j, true);
	public final SAccumilator CRAFTED = new SAccumilator(all, "CRAFTED", j, true);
	public final SAccumilator INVASIONS = new SAccumilator(all, "INVASIONS", j, true);
	public final SAccumilator EXECUTIONS = new SAccumilator(all, "EXECUTIONS", j, true);
	public final SAccumilator TUNNELS = new SAccumilator(all, "TUNNELS", j, true);
	public final SAccumilator ROOMS_BUILT = new SAccumilator(all, "ROOMS_BUILT", j, false);
	public final SAccumilator SUBJECTS = new SAccumilator(all, "SUBJECTS", j, false);
	
	public final LIST<SAccumilator> ALL = new ArrayList<>(all);;
	public final RCollection<SAccumilator> MAP;
	
	private final static String filename = "StatsDoNotCheat";
	
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
	protected void update(float ds) {
		TIME_PLAYED.set((int) (TIME.playedGame()/60.0));
	}

	public static class SAccumilator implements INDEXED{

		private int value;
		public final String key;
		private final boolean isBattle;
		private int old;
		private int hi;
		private final int index;
		public final String name;
		
		SAccumilator(LISTE<SAccumilator> all, String key, Json text, boolean isBattle){
			index = all.add(this);
			this.key = key;
			this.isBattle = isBattle;
			this.name = text.text(key);
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
