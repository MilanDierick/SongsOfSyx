package game.faction.player;

import java.io.IOException;

import game.GAME;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.values.*;
import init.D;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.process.Proccesser;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.data.DOUBLE_O;
import util.info.INFO;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.interrupter.IDebugPanel;
import world.regions.Region;

public final class PTitles {

	private final LIST<PTitle> titles;
	private int newAmount = 0;
	public final INFO info;
	public final BoostSpecs boosters;
	private final BoostCompound<PTitle> bos;
	private static CharSequence ¤¤name = "Titles";
	private static CharSequence ¤¤desc = "Titles are unlocked by various achievements. At the start of each game, you may choose 5 of these unlocked titles to be associated with your name and boost your kingdom in various ways.";
	
	static {
		D.ts(PTitles.class);
	}
	
	
	PTitles() throws IOException{
		
		
		info = new INFO(¤¤name, ¤¤desc);
		
		PATH data = PATHS.INIT().getFolder("player").getFolder("titles");
		PATH text = PATHS.TEXT().getFolder("player").getFolder("titles");
		IconMaker mm = new IconMaker();
		String[] ss = data.getFiles();
		ArrayList<PTitle> all = new ArrayList<>(ss.length);
		for (String s : ss) {
			Json j = new Json(data.get(s));
			Json t = new Json(text.get(s));
			new PTitle(s, all, j, t, mm);
		}
		this.titles = all;
		
		{
			KeyMap<PTitle> map = new KeyMap<>();
			for (PTitle t : all) {
				map.put(t.key, t);
			}
			try {
				Json j = new Json(PATHS.local().PROFILE.get("Titles"));
				String[] sss = j.values("UNLOCKED");
				for (String s : sss) {
					if (map.containsKey(s)) {
						map.get(s).unlocked = true;
					}
						
				}
			}catch(Exception e) {
				GAME.Notify("resetting");
				saveUnlocked();
			}
		}
		
		boosters = new BoostSpecs(¤¤name, UI.icons().s.chevron(DIR.N), true);
		bos = new BoostCompound<PTitle>(boosters, titles) {
			
			double npc = CLAMP.d(titles.size()/5.0, 0, 1);
			
			@Override
			protected double getValue(PTitle t) {
				return t.selected ? 1 : 0;
			}
			
			@Override
			protected BoostSpecs bos(PTitle t) {
				return t.boosters;
			}
			
			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return npc*super.get(bo, f, isMul);
			}
		};
		
		IDebugPanel.add("STEAM ACHIEVE", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles")) {
					Json old = new Json(PATHS.local().PROFILE.get("Titles"));
					String[] sss = old.values("UNLOCKED");
//					Proccesser.exec(SteamAchieve.class, new String[] {}, sss, new String[] {});
					LOG.err("Steam integration is not available.");
				}
			}
		});
		IDebugPanel.add("STEAM ACHIEVE_ALL", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles")) {
					String[] ss = new String[all().size()];
					for (int i = 0; i< ss.length; i++) {
						ss[i] = all().get(i).key;
					}
					LOG.err("Steam integration is not available.");
				}
			}
		});
		IDebugPanel.add("STEAM ACHIEVE_CLEAR", new ACTION() {
			
			@Override
			public void exe() {
				if ((PATHS.isSteam() || PATHS.isDevelop()) && PATHS.local().PROFILE.exists("Titles")) {
					LOG.err("Steam integration is not available.");
				}
			}
		});
		
	}

	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(titles.size());
			for (PTitle t : titles) {
				file.bool(t.isNew);
				file.bool(t.selected);
			}
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			
			for (PTitle t : titles) {
				t.isNew = false;
				t.selected = false;
			}
			
			int am = file.i();
			for (int i = 0; i < am; i++) {
				boolean n = file.bool();
				boolean s = file.bool();
				if (i < titles.size()) {
					PTitle t = titles.get(i);
					t.isNew = n;
					t.selected = s;
				}
				
				
			}
			bos.clearChache();
		}
		
		@Override
		public void clear() {
			for (PTitle t : titles) {
				t.isNew = false;
				t.selected = false;
			}
			bos.clearChache();
		}
	};
	
	double ddd = 0;
	
	void update(double ds) {
		ddd += ds;
		if (ddd < 1)
			return;
		if (!GAME.achieving())
			return;
		ddd-= 1;
		boolean newonlocks = false;
		for (PTitle t : titles) {
			if (!t.unlocked && t.unlockable()) {
				t.isNew = true;
				t.unlocked = true;
				newonlocks = true;
			}
		}
		if (newonlocks)
			saveUnlocked();
	}
	
	public int selected() {
		int am = 0;
		for (PTitle t : titles) {
			if (t.selected)
				am++;
		}
		return am;
	}
	
	public int unlocked() {
		int am = 0;
		for (PTitle t : titles) {
			if (t.unlocked)
				am++;
		}
		return am;
	}
	
	public boolean hasNew() {
		return newAmount > 0;
	}
	
	private void saveUnlocked() {
		try {
			KeyMap<String> map = new KeyMap<>();
			JsonE to = new JsonE();
			if (PATHS.local().PROFILE.exists("Titles")) {
				Json old = new Json(PATHS.local().PROFILE.get("Titles"));
				String[] sss = old.values("UNLOCKED");
				for (String s : sss)
					map.put(s, s);
			}else {
				PATHS.local().PROFILE.create("Titles");
			}
			
			for (PTitle t : titles) {
				if (t.unlocked)
					map.put(t.key, t.key);
			}
			LIST<String> vv = map.all();
			String[] res = new String[vv.size()];
			for (int i = 0; i < vv.size(); i++) {
				res[i] = vv.get(i);
			}
			to.add("UNLOCKED", res);
			to.save(PATHS.local().PROFILE.get("Titles"));
		}catch(Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static void achieve() {
		if (PATHS.isSteam() && PATHS.local().PROFILE.exists("Titles")) {
			Json old = new Json(PATHS.local().PROFILE.get("Titles"));
			String[] sss = old.values("UNLOCKED");
			LOG.err("Steam integration is not available.");
		}
	}
	
	
	public LIST<PTitle> all(){
		return titles;
	}
	
	public static final class PTitle extends INFO implements INDEXED{

		private final int index;
		public final Lockers lockers;
		public final Lockable<Faction> lockable;
		public final BoostSpecs boosters;
		public final SPRITE icon;
		
		private final String key;
		private boolean selected;
		private boolean isNew;
		private boolean unlocked;
		
		
		PTitle(String key, LISTE<PTitle> all, Json jdata, Json jtext, IconMaker iconM) throws IOException{
			super(jtext);
			this.key = key;
			index = all.add(this);
			
			lockable = GVALUES.FACTION.LOCK.push();
			lockable.push(jdata);
			icon = iconM.get(jdata);
			lockers = new Lockers(¤¤name + ": " + name, UI.icons().s.chevron(DIR.N));
			lockers.add(GVALUES.FACTION, jdata, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					if (t == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.INDU, jdata, new DOUBLE_O<Induvidual>() {

				@Override
				public double getD(Induvidual t) {
					if (t.faction() == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			lockers.add(GVALUES.REGION, jdata, new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					if (t.faction() == FACTIONS.player()) {
						return selected ? 1 : 0;
					}
					return 1;
				}
			
			});
			
			boosters = new BoostSpecs(name, UI.icons().s.chevron(DIR.N), false);
			boosters.push(jdata, null);
		}

		@Override
		public int index() {
			return index;
		}
		
		public void select(boolean s) {
			if (s == selected)
				return;
			selected = s;
			FACTIONS.player().titles.bos.clearChache();
		}
		
		public boolean selected() {
			return selected;
		}
		
		private boolean unlockable() {
			return lockable.passes(FACTIONS.player());
		}
		
		public boolean unlocked() {
			return unlocked || unlockable();
		}
		
		public boolean isNew() {
			return isNew;
		}
		
		public void consumeNew() {
			isNew = false;
		}

		
	}
	
	private static class IconMaker {
		
		private final int WW = 5;
		private int txs = 6;
		private int tys = 4;
		
		IconMaker() throws IOException{
			new ComposerThings.IInit(PATHS.SPRITE().getFolder("ui").get("Titles"), 540, 190) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
					int hi = c.getSource().height/(tys*8+6);
					s.full2.init(0, 0, WW, hi, txs, tys, d.s8);
				}
				
			};
		}
		
		public SPRITE get(Json json) throws IOException {
			
			int ii = json.i("ICON_I");
			
			TILE_SHEET s = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full2.setVar(ii);
					s.full2.paste(true);
					return d.s8.saveGui();
				}
			}.get();
			
			return new SPRITE.Imp(txs*8, tys*8) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					int w = (X2-X1)/txs;
					int h = (Y2-Y1)/tys;
					int y = Y1;
					int i = 0;
					for (int dy = 0; dy < tys; dy++) {
						int x = X1;
						for (int dx = 0; dx < txs; dx++) {
							s.render(r, i, x, x+w, y, y+h);
							x += w;
							i++;
							
						}
						y+= h;
					}
					
				}
			};
		}
		
	}
	
}
