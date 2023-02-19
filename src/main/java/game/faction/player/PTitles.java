package game.faction.player;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PLocks.PLocker;
import game.statistics.G_REQ;
import init.D;
import init.boostable.*;
import init.boostable.BOOSTER.BOOSTER_IMP_DATA;
import init.boostable.BOOSTER_COLLECTION.BOOSTER_COLLECTION_IMP;
import init.boostable.BOOSTER_COLLECTION.SIMPLE;
import init.paths.PATH;
import init.paths.PATHS;
import init.tech.Unlocks;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.tilemap.Floors.Floor;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.info.INFO;

public final class PTitles {

	private final LIST<PTitle> titles;
	private int newAmount = 0;
	public final INFO info;
	private final Boost boost;
	public final SIMPLE BOOSTER;
	private static CharSequence ¤¤Unlocks = "¤Unlocks with title:";
	private static CharSequence ¤¤name = "Titles";
	private static CharSequence ¤¤desc = "Titles are unlocked by various achievements. At the start of each game, you may choose 5 of these unlocked titles to be associated with your name and boost your kingdom in various ways.";
	
	static {
		D.ts(PTitles.class);
	}
	
	
	PTitles(){
		
		
		info = new INFO(¤¤name, ¤¤desc);
		
		PATH data = PATHS.INIT().getFolder("player").getFolder("titles");
		PATH text = PATHS.TEXT().getFolder("player").getFolder("titles");
		String[] ss = data.getFiles();
		ArrayList<PTitle> all = new ArrayList<>(ss.length);
		for (String s : ss) {
			Json j = new Json(data.get(s));
			Json t = new Json(text.get(s));
			new PTitle(s, all, j, t);
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
		
		boost = new Boost(all);
		
		BOOSTER = boost;
		
	}
	
	public final PLocker locker = new PLocker(¤¤Unlocks) {

		
		@Override
		public CharSequence unlockText(Floor f) {
			s.clear();
			for (PTitle t : all()) {
				if (t.selected || t.unlock == null)
					continue;
				for (Floor ff : t.unlock.unlocksRoads()) {
					if (f == ff) {
						s.add(t.name);
						s.NL();
					}
				}
			}
			return s;
		}
		
		@Override
		public CharSequence unlockText(Industry f) {
			s.clear();
			for (PTitle t : all()) {
				if (t.selected || t.unlock == null)
					continue;
				for (Industry ff : t.unlock.unlocksIndustry()) {
					if (f == ff) {
						s.add(t.name);
						s.NL();
					}
				}
			}
			return s;
		}
		
		@Override
		public CharSequence unlockText(RoomBlueprint f) {
			s.clear();
			for (PTitle t : all()) {
				if (t.selected || t.unlock == null)
					continue;
				for (RoomBlueprintImp ff : t.unlock.roomsUnlocks()) {
					if (f == ff) {
						s.add(t.name);
						s.NL();
					}
				}
			}
			return s;
		}

		@Override
		public int lockedUpgrades(RoomBlueprint b) {
			int am = 0;
			for (PTitle t : all()) {
				if (t.selected || t.unlock == null)
					continue;
				for (RoomBlueprintImp bb : t.unlock.unlocksUpgrades()) {
					if (b == bb) {
						am++;
					}
				}
			}
			return am;
		}

		@Override
		public CharSequence unlockTextUpgrade(RoomBlueprint b) {
			s.clear();
			for (PTitle t : all()) {
				if (t.selected || t.unlock == null)
					continue;
				for (RoomBlueprintImp bb : t.unlock.unlocksUpgrades()) {
					if (b == bb) {
						s.add(t.name);
						s.NL();
					}
				}
			}
			return s;
		}
	};
	
	private class Boost extends BOOSTER_COLLECTION_IMP implements SIMPLE {

		private final double[] add = new double[BOOSTABLES.all().size()];
		private final double[] mul = new double[BOOSTABLES.all().size()];
		
		protected Boost(LIST<PTitle> titles) {
			super(info.name);
			for (PTitle t : titles)
				init(t.boost);
			setBonuses();
		}

		@Override
		public double add(BOOSTABLE b) {
			return add[b.index()];
		}

		@Override
		public double mul(BOOSTABLE b) {
			return mul[b.index()];
		}
		
		private void setBonuses() {
			Arrays.fill(add, 0);
			Arrays.fill(mul, 1);
			
			for (PTitle t : titles) {
				if (t.selected()) {
					for (BBoost b : t.boost.boosts()) {
						if (b.isMul())
							mul[b.boost.index()] *= b.value();
						else
							add[b.boost.index()] += b.value();
					}
				}
			}
		}
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
			boost.setBonuses();
		}
		
		@Override
		public void clear() {
			for (PTitle t : titles) {
				t.isNew = false;
				t.selected = false;
			}
			boost.setBonuses();
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

	
	public LIST<PTitle> all(){
		return titles;
	}
	
	public static final class PTitle extends INFO implements INDEXED{

		private final int index;
		public final LIST<G_REQ> reqs;
		private final String key;
		private boolean selected;
		private boolean isNew;
		private boolean unlocked;
		public final BOOSTER_IMP_DATA boost;
		public final Unlocks unlock;
		
		
		PTitle(String key, LISTE<PTitle> all, Json jdata, Json jtext){
			super(jtext);
			this.key = key;
			index = all.add(this);
			boost = new BOOSTER_IMP_DATA(name, jdata);
			reqs = G_REQ.READ(jdata);
			if (jdata.has("UNLOCKS")) {
				unlock = new Unlocks(name, jdata.json("UNLOCKS"));
			}else {
				unlock = null;
			}
			
		}

		@Override
		public int index() {
			return index;
		}
		
		public void select(boolean s) {
			if (s == selected)
				return;
			selected = s;
			FACTIONS.player().titles.boost.setBonuses();
		}
		
		public boolean selected() {
			return selected;
		}
		
		private boolean unlockable() {
			for (G_REQ r : reqs) {
				if (!r.isFulfilled())
					return false;
				
			}
			
			return true;
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
	
}
