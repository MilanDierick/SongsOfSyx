package init.settings;

import java.io.File;

import game.VERSION;
import init.C;
import init.D;
import init.paths.PATHS;
import init.paths.PATHS.PATHS_BASE;
import launcher.LSettings;
import launcher.LSettings.LSettingInt;
import snake2d.*;
import snake2d.Displays.DisplayMode;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.info.INFO;

public final class S {

	private static S s;

	public static S get() {
		if (s == null) {
			s = new S();
		}
		return s;
	}

	private final ArrayListResize<Setting> all = new ArrayListResize<>(128, 128);
	private final LSettings settings = new LSettings();
	public final boolean developer;
	public final boolean debug;

	public final Setting shadows;
	public final Setting particles;
	public final Setting graphics;
	public final Setting gore;
	public final Setting volumeMusic;
	public final Setting volumeSound;
	public final Setting muteUnfocused;
	public final Setting brightness;
	public final Setting autoSaveInterval;
	public final Setting autoSaveFiles;
	public final Setting scroll;
	public final Setting lightCycle;
	public final Setting uilightCycle;

	private S() {
		S.s = this;

		D.gInit(this);

		final CharSequence[] samount = new CharSequence[] { DicMisc.¤¤off, D.g("some"), D.g("lots") };

		developer = settings.developer.get() == 1; 
		debug = settings.debug.get() == 1;
		shadows = get(settings.shadows, D.g("Shadow"),
				D.g("ShadowD", "The amount of shadows in-game. Can decrease performance."), samount);

		particles = get(settings.particles, D.g("Particles"),
				D.g("ParticlesD", "The amount of particles in-game. Can decrease performance."), samount);

		gore = get(settings.gore, D.g("Gore"), D.g("GoreD", "The amount of gore in-game. Can decrease performance."),
				samount);

		volumeMusic = new SettingPerc(all, settings.volumeMusic, D.g("Music"), D.g("MusicD", "The music volume"));

		volumeSound = new SettingPerc(all, settings.volumeSound, D.g("Sound"), D.g("SoundD", "The sound effects volume"));

		graphics = get(settings.detail, D.g("Graphics"), D.g("GraphicsD", "The amount of detail when rendering the map."),
				new CharSequence[] { DicMisc.¤¤Low, DicMisc.¤¤High});
		
		muteUnfocused = new Setting(all, settings.focusMute, D.g("Mute-Un-focused"),
				D.g("Mute-UnfocusedD", "Will mute the game when game is un-focused")) {
			@Override
			public int max() {
				return 1;
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public void getValue(Str str) {
				str.clear().add(get() == 0 ? DicMisc.¤¤off : DicMisc.¤¤on);
			}
		};
		
		brightness = new SettingPerc(all, settings.brightness, D.g("Brightness"),
				D.g("BrightnessD", "The brightness of the game."));

		lightCycle = get(settings.lightCycle, D.g("Day-cycle"), D.g("Day-cycleD", "Simulates the day and night cycle visually."),
				new CharSequence[] { DicMisc.¤¤off, DicMisc.¤¤on});
		uilightCycle = get(settings.uiLightCycle, D.g("UI-Day-cycle"), D.g("UI-Day-cycleD", "Simulates the day and night cycle visually for the UI."),
				new CharSequence[] { DicMisc.¤¤off, DicMisc.¤¤on});
		CharSequence min = D.g("{0} min");

		autoSaveInterval = new Setting(all, settings.autoSaveInterval, D.g("Auto-Saving"),
				D.g("Auto-SavingD", "How often the game will auto-save.")) {
			@Override
			public int max() {
				return 10;
			}

			@Override
			public void getValue(Str str) {
				if (get() == 0)
					str.clear().add(DicMisc.¤¤off);
				else
					str.clear().add(min).insert(0, 1 + 2 * (max() - get()));
			}
		};

		autoSaveFiles = new Setting(all, settings.autoSaveFiles, D.g("Auto-Save-Files"),
				D.g("Auto-Save-FilesD", "How many auto save files that will be kept.")) {
			@Override
			public int max() {
				return 10;
			}

			@Override
			public int min() {
				return 1;
			}

			@Override
			public void getValue(Str str) {
				str.clear().add(get());
			}
		};
		
		scroll = new Setting(all, settings.edgeScroll, D.g("Edge-Scrolling"),
				D.g("Edge-ScrollingD", "Scroll maps by touching the edges.")) {
			@Override
			public int max() {
				return 1;
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public void getValue(Str str) {
				str.clear().add(get() == 0 ? DicMisc.¤¤off : DicMisc.¤¤on);
			}
		};

		all.trim();
	}

	public LIST<Setting> all() {
		return all;
	}

	public boolean isNewVersion() {
		return settings.version.get() != VERSION.VERSION;
	}
	
	private Setting get(LSettingInt s, CharSequence name, CharSequence desc,
			CharSequence[] values) {
		return new Setting(all, s, name, desc) {

			@Override
			public int max() {
				return values.length - 1;
			}

			@Override
			public void getValue(Str str) {
				str.clear().add(values[get()]);
			}
		};
	}

	public void print() {
		LOG.ln("SETTINGS");
		for (Setting s : all) {
			Str.TMP.clear();
			s.getValue(Str.TMP);
			LOG.ln("   " + s.s.key + ": " + Str.TMP);
		}
	}
	
	public void revert() {
		for (Setting s : all) {
			s.set(s.s.defaultValue);
		}
	}

	public void applyRuntimeConfigs() {
		CORE.getSoundCore().ajustGain(volumeSound.getD(), volumeMusic.getD(), muteUnfocused.get() == 1);
	}

	public static abstract class Setting extends INFO implements INTE {

		protected final LSettingInt s;
		
		Setting(LISTE<Setting> all, LSettingInt s , CharSequence name, CharSequence desc) {
			super(name, desc);
			this.s = s;
			all.add(this);
		}

		@Override
		public int get() {
			return CLAMP.i(s.get(), min(), max());
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public void set(int t) {
			s.set(t);
		}

		public abstract void getValue(Str str);

	}

	private static class SettingPerc extends Setting {

		SettingPerc(LISTE<Setting> all, LSettingInt s, CharSequence name, CharSequence desc) {
			super(all, s, name, desc);
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return 20;
		}
		
		@Override
		public void set(int t) {
			double d = t/5.0;
			d = CLAMP.d(d, min(), max());
			s.set(s.max()*t/max());
			//super.set(t);
		}

		@Override
		public int get() {
			return (int) (s.getD()*max());
		}
		
		@Override
		public void getValue(Str str) {
			str.clear().add(get() * 5).add('%');
		}

	}

	public SETTINGS make() {
		return new SETTINGS() {

			@Override
			public String getWindowName() {
				return C.NAME;
			}

			@Override
			public boolean getVSynchEnabled() {
				return settings.vsync.get() == 1;
			}

			@Override
			public String getScreenshotFolder() {
				return ""+PATHS.local().SCREENSHOT.get() + File.separator;
			}

			@Override
			public int getRenderMode() {
				return  settings.shading.get();
			}

			@Override
			public int getPointSize() {
				return C.SCALE;
			}

			@Override
			public int getNativeWidth() {

				if (display() == null) {
					LOG.ln(settings.screenMode.get());
					LOG.ln(settings.fullScreenDisplay.get());
					LOG.ln(settings.monitor.get());
					
					for (int i = 0; i < Displays.monitors(); i++) {
						LOG.ln("M " + i + " " + Displays.current(i));
						for (int s = 0; s < Displays.available(i).size(); s++) {
							LOG.ln("- " + s + " " + Displays.available(i).get(s));
						}
					}
					
				}
				
				int w = nWidth();
				int h = nHeight();

				if (h < C.MIN_HEIGHT)
					h = C.MIN_HEIGHT;
				if (w < C.MIN_WIDTH)
					w = C.MIN_WIDTH;

				double a = w * h;
				double d = Math.pow(C.MAX_SCREEN_AREA / a, 0.5);

				
				
				if (a > C.MAX_SCREEN_AREA) {
					w = (int) (w * d);
					h = (int) (h * d);
					if (h < C.MIN_HEIGHT) {
						h = C.MIN_HEIGHT;
						w = C.MAX_SCREEN_AREA / h;
					}
				}
				
				
				
				w &= ~1;
				C.init(w, C.HEIGHT());
				return w;
			}

			@Override
			public int getNativeHeight() {

				int w = nWidth();
				int h = nHeight();

				if (h < C.MIN_HEIGHT)
					h = C.MIN_HEIGHT;
				if (w < C.MIN_WIDTH)
					w = C.MIN_WIDTH;

				double a = w * h;
				double d = Math.pow(C.MAX_SCREEN_AREA / a, 0.5);
				
				if (a > C.MAX_SCREEN_AREA) {
					w = (int) (w * d);
					h = (int) (h * d);
					if (h < C.MIN_HEIGHT) {
						h = C.MIN_HEIGHT;
						w = C.MAX_SCREEN_AREA / h;
					}
				}
				h &= ~1;
				C.init(C.WIDTH(), h);
				return h;
			}
			
			private int nWidth() {
				if (settings.screenMode.get() == LSettings.screenModeWindowed)
					return (int) Math.ceil(Displays.current(monitor()).width * settings.windowWidth.getD());
				else if (settings.screenMode.get() == LSettings.screenModeBorderLess)
					return (int) Math.ceil(Displays.current(monitor()).width / (1.0 + settings.windowBorderLessScale.getD()));
				
				return display().width;
			}
			
			private int nHeight() {
				if (settings.screenMode.get() == LSettings.screenModeWindowed)
					return (int) Math.ceil(Displays.current(monitor()).height * settings.windowHeight.getD());
				else if (settings.screenMode.get() == LSettings.screenModeBorderLess)
					return (int) Math.ceil(Displays.current(monitor()).height / (1.0 + settings.windowBorderLessScale.getD()));
				return display().height;
			}

			@Override
			public boolean getLinearFiltering() {
				return settings.linear.get() == 1;
			}

			@Override
			public String getIconFolder() {
				return PATHS_BASE.ICON_FOLDER;
			}

			@Override
			public boolean getFitToScreen() {
				return settings.screenMode.get() == LSettings.screenModeBorderLess || settings.screenMode.get() == LSettings.screenModeWindowed || (settings.windowWidth.getD() == 1 && settings.windowHeight.getD() == 1);
			}

			@Override
			public DisplayMode display() {
				if (settings.screenMode.get() == LSettings.screenModeFull) {
					if (settings.fullScreenDisplay.get() == -1) {
						DisplayMode d = Displays.current(monitor());
						return new DisplayMode(d.width, d.height, d.refresh, true);
					}
					return Displays.available(monitor()).get(settings.fullScreenDisplay.get());
				}

				if (settings.screenMode.get() == LSettings.screenModeBorderLess) {
					return Displays.current(monitor());
				}
				int width = (int) (Displays.current(monitor()).width * settings.windowWidth.getD());
				int height = (int) (Displays.current(monitor()).height * settings.windowHeight.getD());
				return new DisplayMode(width, height, Displays.current(monitor()).refresh, false);
			}

			@Override
			public boolean decoratedWindow() {
				return settings.decorated.get() == 1 && settings.screenMode.get() == LSettings.screenModeWindowed;
			}

			@Override
			public boolean debugMode() {
				return debug;
			}

			@Override
			public int monitor() {
				int m = settings.monitor.get();
				return CLAMP.i(m, 0, Displays.monitors());
			}

			@Override
			public String openALDevice() {
				return settings.audiodevice.get();
			}

		};
	}

}
