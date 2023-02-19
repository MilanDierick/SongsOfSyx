package launcher;

import game.VERSION;
import init.C;
import init.paths.PATHS;
import snake2d.Displays;
import snake2d.Displays.DisplayMode;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.misc.CLAMP;
import snake2d.util.process.Proccesser;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT.INTE;

public class LSettings {

	private final ArrayList<LSetting> all = new ArrayList<>(100);
	public final LSetting otherJVM = new LSetting("JVM", 0, 1);
	public final LSetting debug = new LSetting("DEBUG", 0, 1);
	public final LSetting developer = new LSetting("DEVELOPER", 0, 1);
	public final LSetting rpc = new LSetting("RPC", PATHS.isSteam() && !Proccesser.isMac() ? 1 : 0, 1);
	public final LSetting linear = new LSetting("LINEAR", 1, 1);
	public final LSetting shading = new LSetting("SHADING", 1, 1);
	public final LSetting vsync = new LSetting("VSYNC", 0, 1);
	public final LSetting easy = new LSetting("EASY_FONT", 0, 1);
	public final LSetting version = new LSetting("VERSION", -1, Integer.MAX_VALUE);
	public final LSetting monitor = new LSetting("MONITOR", 0, Integer.MAX_VALUE) {
		
		@Override
		public int max() {
			return Displays.monitors()-1;
		};
	};
	
	public final LSetting screenMode = new LSetting("SCREEN_MODE", 0, 2);
	
	public final static int screenModeBorderLess = 0;
	public final static int screenModeFull = 1;
	public final static int screenModeWindowed = 2;
	public final LSetting fullScreenDisplay = new LSetting("FULL_DISPLAY", -1, Integer.MAX_VALUE) {
		
		@Override
		public int max() {
			LIST<DisplayMode> dis = Displays.available(monitor.get());
			if (dis == null || dis.size() == 0)
				return 0;
			return dis.size()-1;
		};
		
	};
	public final LSetting windowWidth = new LSetting("WINDOW_WIDTH", 15, 20) {
		@Override
		public int min() {
			return (int) (max()*(double)C.MIN_WIDTH/Displays.current(monitor.get()).width);
		};
	};
	public final LSetting windowHeight = new LSetting("WIDOW_HEIGHT", 15, 20) {
		@Override
		public int min() {
			return (int) (max()*(double)C.MIN_HEIGHT/Displays.current(monitor.get()).height);
		};
	};
	public final LSetting decorated = new LSetting("WINDOW_DECORATE", 1, 1);
	public final LSetting fill = new LSetting("WINDOW_FILL", 0, 1);
	
	public final LSetting shadows = new LSetting("SHADOWS", 2, 2);
	public final LSetting particles = new LSetting("PARICLES", 2, 2);
	public final LSetting gore = new LSetting("GORE", 2, 2);
	public final LSetting volumeMusic = new LSetting("VOLUME_MUSIC", 50, 100);
	public final LSetting volumeSound = new LSetting("VOLUME_SOUND", 60, 100);
	public final LSetting focusMute = new LSetting("FOCUS_MUTE", 0, 1);
	public final LSetting nightGamma = new LSetting("NIGHT_GAMMA", 10, 100);
	public final LSetting autoSaveInterval = new LSetting("AUTO_SAVE_TIME", 9, 10);
	public final LSetting autoSaveFiles = new LSetting("AUTO_SAVE_FILES", 5, 10);
	public final LSetting edgeScroll = new LSetting("EDGE_SCROLL", 0, 1);
	public final LSetting detail = new LSetting("GRAPHIC_DETAIL", 1, 1);
	public final LSetting lightCycle = new LSetting("LIGHT_CYCLE", 1, 1);
	public final LSetting uiLightCycle = new LSetting("UI_LIGHT_CYCLE", 1, 1);
	public String alternateJVM = "";
	private String[] mods = new String[0];
	public String lang = "";
	public String audiodevice = "";
	
	public void setDefault() {
		for (LSetting s : all) {
			s.v = s.defaultValue;
		}
		alternateJVM = "";
		mods = new String[0];
		lang = "";
	}
	
	public void setMods(String[] mods) {
		this.mods = mods;
	}
	
	public String[] mods() {
		return mods;
	}
	
	public LSettings() {
		try {
			Json json = new Json(PATHS.local().SETTINGS.get("LauncherSettings"));
			for (LSetting s : all) {
				if (json.has(s.key))
					s.v = json.i(s.key);
				else
					s.v = s.defaultValue;
			}
			alternateJVM = json.text("PATH_JAVA");
			setMods(json.values("MODS"));
			lang = json.text("LANGUAGE");
			if (json.has("OPENAL")) {
				audiodevice = json.text("OPENAL");
				if (audiodevice.equalsIgnoreCase("null"))
					audiodevice = null;
			}else
				audiodevice = "";
			if (lang.equals("null"))
				lang = "";
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			setDefault();
			save();
		}
	}
	
	boolean check() {
		
		return false;
		
	}
	
	public void save() {
		try {
			JsonE json = new JsonE();
			version.v = VERSION.VERSION;
			for (LSetting s : all) {
				json.add(s.key, s.v);
			}
			
			json.addString("PATH_JAVA", alternateJVM);
			json.add("MODS", mods);
			
			json.addString("LANGUAGE", lang == null ? "" : lang);
			json.addString("OPENAL", audiodevice == null ? "null" : audiodevice);
			
			json.save(PATHS.local().SETTINGS.create("LauncherSettings"));
			
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			setDefault();
		}
	}
	
	public class LSetting implements INTE {

		public final String key;
		protected int v;
		public final int defaultValue;
		private final int max;
		
		private LSetting(String key, int defaultValue, int max){
			this.key = key;
			this.defaultValue = defaultValue;
			this.max = max;
			all.add(this);
		}
		
		@Override
		public int get() {
			return CLAMP.i(v, 0, max());
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return max;
		}

		@Override
		public void set(int t) {
			v = CLAMP.i(t, 0, max());
			save();
		}
		
		
	}
	
}

