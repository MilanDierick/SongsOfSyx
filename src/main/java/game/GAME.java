package game;

import java.io.IOException;

import game.boosting.BOOSTING;
import game.events.EVENTS;
import game.faction.FACTIONS;
import game.faction.player.Player;
import game.nobility.NOBILITIES;
import game.time.Intervals;
import game.time.TIME;
import game.tourism.TOURISM;
import game.values.GCOUNTS;
import game.values.GVALUES;
import init.D;
import init.RES;
import init.paths.PATHS;
import init.race.RACES;
import init.settings.S;
import init.tech.TECHS;
import script.ScriptEngine;
import settlement.main.SETT;
import snake2d.*;
import snake2d.CORE.GlJob;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import util.spritecomposer.Initer;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import world.WConfig;
import world.WORLD;

public class GAME{
	
	static {
		D.gInit(GAME.class);
	}

	private static TextureHolder texture;
	private final static CharSequence sLoading = D.g("loading");
	
	private static boolean achieving = true;
	
	
	
	private static class Game {
		
		private final Intervals intervals;
		private final SETT settlement;
		private final WORLD world;

		private final FACTIONS factions;
		private final EVENTS events;
		private final ScriptEngine script;
		private final NOBILITIES nobilities;
		private final GCOUNTS counts;
		
		private int updateI = 0;
		private final GameSpec spec;
		private Profiler profiler = Profiler.DUMMY;
		
		Game(GameSpec spec) throws IOException{
			CORE.disposeClient();
			GameDisposable.disposeAll();
			
			this.spec = spec;
			game = this;
			
			CORE.checkIn();
			script = new ScriptEngine();
			CORE.checkIn();
			
			GAME_LOAD_FIXER.all.clear();
			
			
			new RES();
			CORE.checkIn();
			new TIME();
			CORE.checkIn();
			
			intervals = new Intervals();
			CORE.checkIn();
			
			
			settlement = new SETT();
			CORE.checkIn();
			
			new TECHS();
			
			CORE.checkIn();
			counts = new GCOUNTS();
			CORE.checkIn();
			

			
			RACES.expand();
			world = new WORLD(spec.worldW, spec.worldH);
			CORE.checkIn();
			events = new EVENTS();
			new TOURISM();
			CORE.checkIn();
			nobilities = new NOBILITIES();
			factions = new FACTIONS(spec.boosts);
			CORE.checkIn();
			SPEED.clear();
			CORE.checkIn();
			
			GVALUES.init(null);
			BOOSTING.fin(null);
			
			
			for (GameResource r : GameResource.all) {
				r.initAfterGameIsSetUp();
			}
			
			IDebugPanel.add("Profile", new ACTION() {
				
				@Override
				public void exe() {
					if (profiler == Profiler.DUMMY)
						profiler = Profiler.LIVE;
					else
						profiler = Profiler.DUMMY;
				}
			});
			
		}
		

		
	}
	
	private static Game game;
	public final static GameSpeed SPEED = new GameSpeed(); 
	private final static AutoSaver saver = new AutoSaver();
	
	private static void create(GameSpec spec) {
		new GlJob() {
			@Override
			public void doJob() {
				
				texture = new Initer() {
					
					@Override
					public void createAssets() throws IOException {
						CORE.getSoundCore().stopAllSounds();
						CORE.getSoundCore().disposeSounds();
						CORE.checkIn();
						new Game(spec);
						CORE.checkIn();
						
						
					}
				}.get("game", PATHS.textureSize(), SETT.THEIGHT);
			}
		}.perform();
	}
	
	public GAME(GameConRandom random){
		LOG.ln("NEW GAME " + "Game version: " + VERSION.VERSION_STRING);
		create(new GameSpec(random));
		//game.world.generate(random);
		game.events.generate(random);
		
		GAME.achieve(true);
		for (Double d : random.BOOSTS.all()) {
			if (d > 0) {
				GAME.achieve(false);
				break;
			}
		}
		game.script.initAfter();

	}
	
	GAME(FileGetter fg) throws IOException{
		
		
		GameSpec s = new GameSpec(fg);
		create(s);
		
		achieving = fg.bool();
		RES.loader().init();
		RES.loader().print(sLoading);
		for(GameResource r : GameResource.all) {
			fg.check(r);
			r.load(fg);
			fg.check(r);
		}

		RES.loader().print("patience is a virtue...");
		
		SPEED.load(fg);
		saver.load(fg);
		
		game.updateI = fg.i();
		
		for (GAME_LOAD_FIXER f : GAME_LOAD_FIXER.all) {
			f.fix();
		}
	}
	
	
	static void save(FilePutter fp) throws IOException{
		
		
		game.spec.save(fp);
		
		fp.bool(achieving);
		
		for (GameResource r : GameResource.all) {
			fp.mark(r);
			r.save(fp);
			fp.mark(r);
		}
		SPEED.save(fp);
		saver.save(fp);
		fp.i(updateI());
	}
	
	
	
	public static void update(float ds, double slowDown){
		
//		if (game.battle.poll()) {
//			RES.sound().update(0, ds, ds);
//			SPEED.tmpPause();
//			return;
//		}
		game.profiler.logStart(game);
		float ods = ds;
		
		float max = (float) (1.0/16);
		
		double speed = SPEED.update(slowDown);
		
		
		if (speed == 0) {
			for (GameResource s : GameResource.all) {
				if (s.isBattle || !VIEW.b().isActive())
					s.update(0, game.profiler);
			}
			game.updateI++;
		}else {
			ds *= speed;
			saver.autosave(ds);
			int fulls = (int) (ds/max);
			float last = ds - fulls*max;
			
			for (int i = 0; i < fulls; i++){
				for (GameResource s : GameResource.all) {
					if (s.isBattle|| !VIEW.b().isActive())
						s.update(max, game.profiler);
				}
				game.updateI++;
			}
			
			if (last > 0) {
				for (GameResource s : GameResource.all) {
					if (s.isBattle || !VIEW.b().isActive())
						s.update(last, game.profiler);
				}
				game.updateI++;
			}
		}
		RES.sound().update(speed, ods, ds);
		game.profiler.logEnd(game);
		game.profiler.log();
		
	}
	
	public static void afterTick() {
		for (GameResource s : GameResource.all) {
			s.afterTick();
		}

	}
	
	public static WORLD world(){
		return game.world;
	}
	
	public static SETT s(){
		return game.settlement;
	}

	public static Intervals intervals(){
		return game.intervals;
	}
	
	public static FACTIONS factions() {
		return game.factions;
	}
	
	public static int updateI() {
		return game.updateI;
	}
	
	public static ScriptEngine script() {
		return game.script;
	}
	
	public static Player player() {
		return FACTIONS.player();
	}
	
	public static EVENTS events() {
		return game.events;
	}
	
	public static TextureHolder texture() {
		return texture;
	}
	
	public static NOBILITIES NOBLE() {
		return game.nobilities;
	}
	
//	public static BATTLEPOLL battle() {
//		return game.battle;
//	}
	
	public static void saveReset() {
		saver.clear();
	}

	public static void saveNew() {
		saver.saveNew();
	}
	
	
	public static GCOUNTS count() {
		return game.counts;
	}
	
	public static void Notify(CharSequence s) {
	    if (S.get().developer && S.get().debug) {
			SPEED.speedSet(0);
		    System.out.println();
		    System.out.println("SYX NOTIFICATION: " + s);
		    StackTraceElement[] trace =  new RuntimeException().getStackTrace();
		    int l = trace.length-1;
		    for (; l>= 0; l--) {
		    	StackTraceElement e = trace[l];
		    	if (e.getClassName() == GAME.class.getName())
		            break;
		        if (e.getClassName().startsWith("snake2d"))
		            break;
		    }
		    for (int i = 1; i <= l; i++) {
		        System.out.println("    " + trace[i]);
		    }
		    
		    System.out.println();
	    }
	}
	
	public static void Notify(Object s) {
	    Notify(""+s);
	}
	
	public static void Error(String s) {
	    if (S.get().developer || S.get().debug) {
	    	Warn(s);
	    }else {
	    	throw new RuntimeException(s);
	    }
		
	}
	
	public static void Warn(String s) {
	    if (S.get().developer || S.get().debug) {
	    	SPEED.speedSet(0);
	    	LOG.err("SYX WARNING: " + s);
		    StackTraceElement[] trace =  new RuntimeException().getStackTrace();
		    for (StackTraceElement e : trace) {
		        if (e.getClassName() == GAME.class.getName())
		            continue;
		        if (e.getClassName().startsWith("snake2d"))
		            continue;
		        LOG.err("    " + e);
		    }    
	    }
		
	}
	
	public static void WarnLight(String s) {
	    if (S.get().developer || S.get().debug) {
	    	LOG.err("SYX WARNING: " + s);
	    }
	}
	
	public static void Error(CharSequence s) {
	    Error(""+s);
		
	}
	
	
	
	
	public static int version() {
		return game.spec.version;
	}
	
	public abstract static class GameResource {
		
		protected final boolean isBattle;
		
		private static final ArrayList<GameResource> all = new ArrayList<GameResource>(16);
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		
		protected GameResource() {
			this(true);
		}

		protected GameResource(boolean isBattle) {
			all.add(this);
			this.isBattle = isBattle;
		}

		protected abstract void save(FilePutter file);
		protected abstract void load(FileGetter file) throws IOException;
		protected void afterTick() {
			
		}
		protected abstract void update(float ds, Profiler prof);
		
		protected void initAfterGameIsSetUp() {
			
		}
		
	}
	
	static class GameSpec{

		public final int worldW;
		public final int worldH;
		public final int version;
		public final KeyMap<Double> boosts;
		
		GameSpec(GameConRandom random){
			worldW = WConfig.data.WORLD_SIZE;
			worldH = WConfig.data.WORLD_SIZE;
			version = VERSION.VERSION;
			boosts = random.BOOSTS;
		}
		
		GameSpec(FileGetter file) throws IOException{
			worldW = file.i();
			worldH = file.i();
			version = file.i();
			boosts = new KeyMap<>();
			int am = file.i();
			for (int i = 0; i < am; i++) {
				boosts.put(file.chars(), file.d());
			}
		}
		
		void save(FilePutter file) {
			file.i(worldW);
			file.i(worldH);
			file.i(VERSION.VERSION);
			file.i(boosts.all().size());
			for (String k : boosts.keys()) {
				file.chars(k);
				file.d(boosts.get(k));
			}
		}
		
		
	}

	public static boolean achieving() {
		return achieving;
	}
	
	public static void achieve(boolean a) {
		achieving = a;
	}
	
	public static class Cache {
		
		int upI;
		private final int ticks;
		
		public Cache(int ticks){
			upI = -ticks;
			this.ticks = ticks;
		}
		
		public boolean shouldAndReset() {
			if (Math.abs(GAME.updateI()-upI) > ticks) {
				upI = GAME.updateI();
				return true;
			}
			return false;
		}
		
		public void reset() {
			upI = GAME.updateI()-ticks;
		}
		
	}
	
}
