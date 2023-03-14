package init.paths;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import init.C;
import init.paths.ModInfo.ModInfoException;
import snake2d.Errors;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

/**
 * A collection of all the paths in the game
 * 
 * @author mail__000
 *
 */
public class PATHS {

	static PATHS i;
	static final String s = FileSystems.getDefault().getSeparator();
	
	
	final LIST<Path> paths;
	private final LIST<ModInfo> mods;
	final int modHash;
	final int textureSize;
	
	static PATHS_LOCAL local;
	private final PATHS_MISC misc;

	private final PATHS_BASE BASE;
	private final PATH INIT;
	private final PATH INIT_SETTLEMENT;
	private final PATH INIT_WORLD;
	private final PATH CONFIG;
	private final PATHS_SOUND SOUND;
	private final PATH TEXT;
	private final PATH TEXT_MISC;
	private final PATH TEXT_NAMES;
	private final PATH TEXT_SETTLEMENT;
	private final ResFolder RACE;
	private final PATH TEXT_WORLD;
	private final PATH TEXT_CONFIG;
	private final PATH DICTIONARY;
	private final PATH SPRITE;
	private final PATH SPRITE_SETTLEMENT;
	private final PATH SPRITE_SETTLEMENT_MAP;
	private final PATH SPRITE_WORLD;
	private final PATH SPRITE_WORLD_MAP;
	private final PATH SPRITE_GAME;
	private final PATH SPRITE_UI;
	private final Script SCRIPT;


	
	private PATHS(String[] mm, String lang, boolean easy) {
		

		
		Path root = FileSystems.getDefault().getPath("");
		Path base = Util.checkHard(root, "base");
		Path res = null;
		if (!Files.exists(root.resolve("zipdata"))) {
			res = getFromZip("data");
		}else{
			res = Util.checkHard(root, "zipdata");
		}
		
		
		
		
		i = this;
		LinkedList<Path> paths = new LinkedList<>();
		LinkedList<ModInfo> mods = new LinkedList<>();
		
		LOG.ln("INITING PATHS");
		
		
		int tz = 4096;
		LOG.ln("MODS");
		String sss = "";
		for (String m : mm) {
			
			try {
				ModInfo i = new ModInfo(m);
				mods.add(i);
				sss += i.name + i.majorVersion;
				tz = Math.max(i.TEXTURE_CACHE_SIZE, tz);
				LOG.ln(i.name + " " + i.majorVersion + " " + m);
				paths.add(i.getModFolder());
			} catch (ModInfoException e) {
				LOG.err("Shitty mod: " + mm);
			}
			
		}
		modHash = sss.hashCode();
		textureSize = tz;
		LOG.ln("hash: " + modHash);
		LOG.ln("texture cache: " + tz);
		
		BASE = new PATHS_BASE(root, base, res);
		
		
		
		
		if (lang != null){
			Path zip = getFromZip("locale");
			PATH path = new Normal(zip, s, false);
			PATH p = path.getFolder("langs");
			
			if (p.exists(lang)) {
				p = p.getFolder(lang);
				Json j = new Json(p.get("_Info.txt"));
				String fi = j.text("CHARSET");
				int tzz = j.has("TEXTURE_CACHE_SIZE") ? j.i("TEXTURE_CACHE_SIZE") : 0;
				if (tzz > tz)
					tzz = tz;
				Path pFont = path.getFolder("chars").getFolder(""+fi).get();
				Path pLang = p.get();
				
				paths.add(pLang);
				paths.add(pFont);
			}
			
			
		}
		
		if (lang == null && easy) {
			paths.add(BASE.MODS.getFolder("easy").get());
		}
		
		Path data = Util.checkHard(res, "data");

		paths.add(data);
		
		
		
		this.paths = new ArrayList<>(paths);
		this.mods = new ArrayList<>(mods);
		LOG.ln("PATHS");
		for (Path p : paths)
			LOG.ln(p.toAbsolutePath());
		LOG.ln();
		
		misc = new PATHS_MISC();
		
		PATH A = new SemiMod("assets", s);
		
		INIT = A.getFolder("init", ".txt");
		CONFIG = INIT.getFolder("config");
		INIT_SETTLEMENT = INIT.getFolder("settlement");
		INIT_WORLD = INIT.getFolder("world");
		SOUND = new PATHS_SOUND(); 
		
		TEXT = A.getFolder("text", ".txt");
		TEXT_MISC = TEXT.getFolder("misc");
		TEXT_CONFIG = TEXT.getFolder("config");
		DICTIONARY = TEXT.getFolder("dictionary");
		TEXT_NAMES = TEXT.getFolder("names");
		TEXT_SETTLEMENT = TEXT.getFolder("settlement");
		
		TEXT_WORLD = TEXT.getFolder("world");
		SPRITE = A.getFolder("sprite", ".png");
		SPRITE_SETTLEMENT = SPRITE.getFolder("settlement");
		SPRITE_SETTLEMENT_MAP = SPRITE_SETTLEMENT.getFolder("map");
		SPRITE_WORLD = SPRITE.getFolder("world");
		SPRITE_WORLD_MAP = SPRITE_WORLD.getFolder("map");
		SPRITE_UI = SPRITE.getFolder("ui");
		SPRITE_GAME = SPRITE.getFolder("game");
		
		SCRIPT = new Script();

		RACE = new ResFolder("race", true);
		
	}
	
	public static boolean inited() {
		return i != null;
	}
	
	private static Path getFromZip(String file) {
		Path root = FileSystems.getDefault().getPath("");
		Path base = Util.checkHard(root, "base");
		
		Path zip = Util.checkHard(base, file + ".zip");
		Map<String, String> env = new HashMap<>();
		env.put("read", "true");
		URI uri = zip.toUri();
		String path = "jar:" + uri;
		try {
			Path res = FileSystems.newFileSystem(URI.create(path), env).getRootDirectories().iterator().next();
			Util.checkHard(res, "");
			return res;
		} catch (Exception e) {
			System.err.println("Game resources are corrupted. Reinstall the game.");
			e.printStackTrace();
			Util.abort(""+zip);
		}
		return null;
	}
	
	public static String getSavePath(Path pp) {
		
		
		String path = new File(""+pp.toAbsolutePath()).getAbsolutePath();
		String sd = s+"assets"+FileSystems.getDefault().getSeparator();
		if (path.contains(sd)) {
			return path.substring(path.lastIndexOf(sd)+sd.length(), path.length());
		}
		throw new RuntimeException(path + " " + "no 'assets'" + " " +  FileSystems.getDefault().getSeparator() + " " + File.pathSeparator);
	}

	public static void init(String[] mods, String lang, boolean easy) {
		new PATHS(mods, lang, easy);
	}

	public static int textureSize() {
		return i.textureSize;
	}

	public static PATHS_LOCAL local() {
		if (local == null)
			local = new PATHS_LOCAL();
		return local;
	}
	
	public static PATHS_BASE BASE() {
		return i.BASE;
	}
	
	public static PATHS_MISC MISC() {
		return i.misc;
	}
	
	public static PATH INIT() {
		return i.INIT;
	}
	
	public static PATH INIT_SETTLEMENT() {
		return i.INIT_SETTLEMENT;
	}
	
	
	public static PATH INIT_WORLD() {
		return i.INIT_WORLD;
	}
	
	public static PATH SPRITE_WORLD() {
		return i.SPRITE_WORLD;
	}
	
	public static PATH SPRITE_WORLD_MAP() {
		return i.SPRITE_WORLD_MAP;
	}
	
	public static PATH TEXT_WORLD() {
		return i.TEXT_WORLD;
	}
	
	
	public static PATH CONFIG() {
		return i.CONFIG;
	}
	
	
	public static PATH TEXT_CONFIG() {
		return i.TEXT_CONFIG;
	}
	
	public static PATHS_SOUND SOUND() {
		return i.SOUND;
	}
	
	public static PATH TEXT() {
		return i.TEXT;
	}
	
	public static PATH TEXT_MISC() {
		return i.TEXT_MISC;
	}
	
	public static PATH TEXT_SETTLEMENT() {
		return i.TEXT_SETTLEMENT;
	}
	
	public static ResFolder RACE() {
		return i.RACE;
	}

	
	public static PATH SPRITE() {
		return i.SPRITE;
	}
	
	public static PATH SPRITE_UI() {
		return i.SPRITE_UI;
	}
	
	public static int modHash() {
		return i.modHash;
	}
	
	
	public static PATH SPRITE_SETTLEMENT() {
		return i.SPRITE_SETTLEMENT;
	}
	
	public static PATH SPRITE_SETTLEMENT_MAP() {
		return i.SPRITE_SETTLEMENT_MAP;
	}
	
	
	public static PATH SPRITE_GAME() {
		return i.SPRITE_GAME;
	}
	
	public static PATH DICTIONARY() {
		return i.DICTIONARY;
	}
	
	public static PATH NAMES() {
		return i.TEXT_NAMES;
	}
	
	public static PATH CACHE_DATA() {
		return PATHS.local.CACHE_DATA;
	}
	
	public static PATH CACHE_SCRIPT() {
		return PATHS.local.CACHE_SCRIPT;
	}
	
	public static PATH CACHE_TEXTURE() {
		return PATHS.local.CACHE_TEXTURE;
	}
	
	public static Script SCRIPT() {
		return i.SCRIPT;
	}
	
	public static final class PATHS_SOUND {
		
		private final PATH sound = new SemiMod("assets" + s + "sound", s);
		public final PATH action = sound.getFolder("action", ".wav");
		public final PATH ambience = sound.getFolder("ambience", ".ogg");
		public final PATH animal = sound.getFolder("animal", ".wav");
		public final PATH gui = sound.getFolder("gui", ".wav");
		public final PATH music = sound.getFolder("music", ".ogg");
		
		PATHS_SOUND(){
			
		}
		
	}
	
	public static final class PATHS_BASE {

		//public final PATH MUSIC;
		//public final PATH TEXTURE;
		//public final PATH SOUND;
		public final PATH DATA;
		public final PATH LAUNCHER;
//		public final PATH LANG_TEXT;
//		public final PATH LANG_FONT;
		public final PATH MODS;

		
		public static final String FOLDER = new File("").getAbsolutePath() + File.separator + "base" + File.separator;
		public static final String ICON_FOLDER = FOLDER + "icons" + File.separator;
		public static final String PRELOADER = FOLDER + "PreLoader.png";
		
		
		
		PATHS_BASE(Path root, Path base, Path res) {
			
			PATH ROOT = new Normal(res.resolve("base"), s, false);
			DATA = ROOT.getFolder("data", ".txt", false);
			LAUNCHER = ROOT.getFolder("launcher", ".png", false);
			MODS = new Normal(res.resolve("mods"), s, false);

		}
		
		public static PATH langs() {
			Path zip = getFromZip("locale");
			PATH path = new Normal(zip, s, false);
			return path.getFolder("langs", ".txt");
		}
		

	}
	
	public static final class PATHS_LOCAL {

		public final PATH ROOT;
		public final PATH SETTINGS;
		public final PATH SCREENSHOT;
		public final PATH LOGS;
		public final PATH SAVE;
		public final PATH MODS;
		public final PATH PROFILE;
		final PATH CACHE_DATA;
		final PATH CACHE_SCRIPT;
		final PATH CACHE_TEXTURE;
		
		/**
		 * Containing all starting paths with the first chosed directory at 0
		 */
		
		
		PATHS_LOCAL() {
			
			ROOT = new Normal(Paths.get(Util.getLocal()), s, true);

			if (!Files.isWritable(ROOT.get()))
				throw new Errors.GameError("No read/write access was granted. Try to enable administrator rights or read and write rights for: " +  ROOT.get().toAbsolutePath());
			
			SETTINGS = ROOT.getFolder("settings", ".txt", true);
			SCREENSHOT = ROOT.getFolder("screenshots", ".png", true);
			LOGS = ROOT.getFolder("logs", ".txt", true);
			PATH SAVES = ROOT.getFolder("saves", true);
			MODS = getMods(ROOT);
			PROFILE = SAVES.getFolder("profile", ".txt", true);
			SAVE = SAVES.getFolder("saves", ".save", true);
			
			PATH cache = ROOT.getFolder("cache", s, true);
			
			
			CACHE_DATA = cache.getFolder("data", ".cachedata", true);
			CACHE_TEXTURE = cache.getFolder("texture", ".png", true);
			CACHE_SCRIPT = cache.getFolder("script", ".jar", true);
		}
		
		private static PATH getMods(PATH ROOT) {
			PATH p = ROOT.getFolder("mods", true);
			Path steam = getSteamPath(); 
			if (steam != null) {
				LOG.ln("Steam mod folder found: " + steam.toAbsolutePath());
				LIST<Path> roots = new ArrayList<Path>(p.get().toAbsolutePath(), steam);
				return new SemiMod(roots, "", s);
			}
		
			LIST<Path> roots = new ArrayList<Path>(p.get());
			return new SemiMod(roots, "", s);
			
			
		}
		
		private static Path getSteamPath() {
			Path steam = Paths.get("").toAbsolutePath();
			if (isDevelop()){
				steam = Paths.get("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Songs of Syx");
				if (!Files.exists(steam))
					return null;
			}
			while(steam.getParent() != null) {
				steam = steam.getParent();
				if ((""+steam.getFileName()).equalsIgnoreCase("steamapps")) {
					
					Path t = steam.resolve("workshop").resolve("content").resolve(""+C.STEAM_ID);
					
					if (Files.exists(t) && Files.isDirectory(t)) {
						return t;
					}
					
					
				}
			}
			
			return null;
		}

	}
	
	public static boolean isDevelop() {
		Path steam = Paths.get("").toAbsolutePath();
		return (Files.exists(steam.resolve("zipdata")));
	}
	
	public static boolean isSteam() {
		Path steam = Paths.get("").toAbsolutePath();
		return (""+steam).contains("steamapps");
	}
	
	public static final class PATHS_MISC {

		public final PATH CAMPAIGNS = new ModOnly("campaigns", s, true);
		public final PATH SAVES = new ModOnly("saves", ".save", true);
		public final PATH EXAMPLES = new ModOnly("examples", ".save", true);
		public final boolean hasTutorial;
		
		PATHS_MISC() {
			hasTutorial = SAVES.exists("_Tutorial");
		}
		

	}
	
	public static final class Script {
		
		private final PATH base = new SemiMod("script", s);
		public final PATH jar = base.getFolder("jar", ".jar");
		public final PATH text = PATHS.TEXT_MISC().getFolder("script", ".txt");
		
	}
	
	public static LIST<ModInfo> currentMods(){
		return i.mods;
	}

	public static class ResFolder {
		
		public final PATH init;
		public final PATH text;
		public final PATH sprite;
		
		ResFolder(String key, boolean spirte){
			this.init = INIT().getFolder(key);
			this.text = TEXT().getFolder(key);
			this.sprite = spirte ? SPRITE().getFolder(key) : null;
		}
		
	}


}
