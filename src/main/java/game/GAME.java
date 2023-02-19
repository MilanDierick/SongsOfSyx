package game;

import java.io.IOException;

import game.battle.BATTLE;
import game.events.EVENTS;
import game.faction.FACTIONS;
import game.faction.player.Player;
import game.nobility.NOBILITIES;
import game.statistics.GCOUNTS;
import game.time.Intervals;
import game.time.TIME;
import game.tourism.TOURISM;
import init.D;
import init.RES;
import init.boostable.BOOSTABLES;
import init.paths.PATHS;
import init.race.RACES;
import init.settings.S;
import init.tech.TECHS;
import script.ScriptEngine;
import script.ScriptLoad;
import settlement.main.SETT;
import snake2d.*;
import snake2d.CORE.GlJob;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.spritecomposer.Initer;
import view.main.VIEW;
import world.World;
import world.army.WARMYD;

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
		private final World world;

		private final FACTIONS factions;
		private final EVENTS events;
		private final ScriptEngine script;
		private final NOBILITIES nobilities;
		private final BATTLE battle;
		private final GCOUNTS counts;
		
		private int updateI = 0;
		private final GameSpec spec;
		
		Game(GameSpec spec, LIST<ScriptLoad> scripts) throws IOException{
			CORE.disposeClient();
			GameDisposable.disposeAll();
			this.spec = spec;
			game = this;
			for (ScriptLoad l : scripts)
				l.script.initBeforeGameCreated();
			
			GAME_LOAD_FIXER.all.clear();
			
			
			new RES(spec.race);
			CORE.checkIn();
			new TIME();
			CORE.checkIn();
			
			intervals = new Intervals();
			CORE.checkIn();
			
			
			settlement = new SETT();
			CORE.checkIn();
			new WARMYD();
			BOOSTABLES.finishUp(null);
			new TECHS();
			
			CORE.checkIn();
			counts = new GCOUNTS();
			CORE.checkIn();
			world = new World(spec.worldW, spec.worldH);
			CORE.checkIn();
			

			script = new ScriptEngine();
			CORE.checkIn();
			
			RACES.expand();
			CORE.checkIn();
			events = new EVENTS();
			new TOURISM();
			CORE.checkIn();
			nobilities = new NOBILITIES();
			factions = new FACTIONS(RACES.all().get(spec.race), spec.boosts);
			CORE.checkIn();
			battle = new BATTLE();
			
			SPEED.clear();
			CORE.checkIn();
			game.script.set(scripts);
			
		}
		

		
	}
	
	private static Game game;
	public final static GameSpeed SPEED = new GameSpeed(); 
	private final static AutoSaver saver = new AutoSaver();
	
	private static void create(GameSpec spec, LIST<ScriptLoad> scripts) {
		new GlJob() {
			@Override
			public void doJob() {
				
				texture = new Initer() {
					
					@Override
					public void createAssets() throws IOException {
						CORE.getSoundCore().stopAllSounds();
						CORE.getSoundCore().disposeSounds();
						CORE.checkIn();
						new Game(spec, scripts);
						CORE.checkIn();
						
						
					}
				}.get("game", PATHS.textureSize(), SETT.THEIGHT);
			}
		}.perform();
	}
	
	GAME(GameConRandom random){
		LOG.ln("NEW GAME " + "Game version: " + VERSION.VERSION_STRING);
		
		create(new GameSpec(random), random.scripts);
		game.factions.generate(random);
		game.world.generate(random);
		game.events.generate(random);

	}
	
	GAME(FileGetter fg, String script) throws IOException{
		
		
		GameSpec s = new GameSpec(fg);
		
		LIST<ScriptLoad> scripts = new ArrayList<>();
		if (VERSION.versionMajor(s.version) > 63 || (VERSION.versionMajor(s.version) == 63 && VERSION.versionMinor(s.version) >= 30))
			scripts = ScriptLoad.load(fg);
		create(s, new ArrayList<ScriptLoad>(scripts));
		
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
		
		if (script != null)
			game.script.set(new ArrayList<ScriptLoad>(ScriptLoad.get(script)));
		
		for (GAME_LOAD_FIXER f : GAME_LOAD_FIXER.all) {
			f.fix();
		}

	}
	
	
	static void save(FilePutter fp) throws IOException{
		
		
		game.spec.save(fp);
		
		ScriptLoad.save(game.script.makeCurrent(), fp);
		
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
		
		if (game.battle.poll()) {
			RES.sound().update(0, ds, ds);
			SPEED.tmpPause();
			return;
		}
		
		float ods = ds;
		
		float max = (float) (1.0/16);
		
		double speed = SPEED.update(slowDown);
		
		
		if (speed == 0) {
			up(0);
		}else {
			ds *= speed;
			saver.autosave(ds);
			int fulls = (int) (ds/max);
			float last = ds - fulls*max;
			
			for (int i = 0; i < fulls; i++){
				up(max);
			}
			
			if (last > 0) {
				up(last);
			}
		}
		RES.sound().update(speed, ods, ds);
		
		
	}
	
	private static void up(float max) {
		for (GameResource s : GameResource.all) {
			if (s.isBattle|| !VIEW.b().isActive())
				s.update(max);
		}
		game.updateI++;
	}
	
	public static void afterTick() {
		for (GameResource s : GameResource.all)
			s.afterTick();
	}
	
	public static World world(){
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
	
	public static BATTLE battle() {
		return game.battle;
	}
	
	public static void saveReset() {
		saver.clear();
	}

	public static void saveNew() {
		saver.saveNew();
	}
	
	
	public static GCOUNTS stats() {
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
		protected abstract void update(float ds);
		
	}
	
	static class GameSpec{

		public final int race;
		public final int worldW;
		public final int worldH;
		public final int version;
		public final KeyMap<Double> boosts;
		
		GameSpec(GameConRandom random){
			race = (int)random.race.getValue();
			worldW = (int)random.size.getValue();
			worldH = (int)random.size.getValue();
			version = VERSION.VERSION;
			boosts = random.BOOSTS;
		}
		
		GameSpec(FileGetter file) throws IOException{
			race = file.i();
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
			file.i(race);
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
		if (!a) {
			GAME.Notify("here");
		}
		achieving = a;
	}
	
}
