package util.save;

import java.io.Serializable;

import game.GAME;
import game.VERSION;
import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;

public class SaveGame implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient String filename;

	private static CharSequence ¤¤version = "¤Version miss-match! Save is made with major game version: {0}. Try downgrading the game to this version. The current game version is: ";	
	private static CharSequence ¤¤race = "¤The amount of races do not match the current configuration.";	
	private static CharSequence ¤¤room = "¤The amount of rooms do not match the current configuration.";	
	private static CharSequence ¤¤industries = "¤The amount of industries do not match the current configuration.";	
	private static CharSequence ¤¤resources = "¤The amount of resources do not match the current configuration.";	
	private static CharSequence ¤¤modOther = "¤The save can not be loaded as it was made with another mod configuration:";
	private static CharSequence ¤¤modNone = "¤The save can not be loaded as it was made with an un-modified game. Disable all mods in the launcher to load the game.";	
	private static CharSequence ¤¤script = "¤The script: {0} that the game was saved with can not be found. The game will probably load, but could behave strange.";	

	private static CharSequence ¤¤1Minute = "¤1 minute ago";
	private static CharSequence ¤¤Minutes = "¤{0} minutes ago";
	private static CharSequence ¤¤1Hour = "¤1 hour ago";
	private static CharSequence ¤¤Hours = "¤{0} hours ago";
	private static CharSequence ¤¤Yesterday = "¤yesterday";
	private static CharSequence ¤¤DaysAgo = "¤{0} days ago";
	private static CharSequence ¤¤veryLong = "¤very long ago";
	
	private static CharSequence ¤¤mod2 = "¤Try to enable the same mods, in the same order, in the launcher and reload the save.";	
	private static CharSequence ¤¤underlaying = "¤UnderLaying problem:";	
	static {
		D.ts(SaveGame.class);
	}
	
	public final ArrayList<String> mods;
	public final int modHash = PATHS.modHash();
	public final int version = VERSION.VERSION;
	public final int playSeconds = (int) TIME.playedGame();
	public final int population = STATS.POP().POP.data().get(null);
	public final String race = ""+ FACTIONS.player().race().info.name;
	public final String city = ""+FACTIONS.player().name;
	public final String[] scripts;
	public final int check = RND.rInt();
	public final String races = getResources("race");
	public final String rooms = getResources("room");
	public final String resources = getResources("resource");
	public final String industries = getResourcesArray("room", "INDUSTRY");
	
	public final long timeSaved;
	
	public SaveGame(){
		mods = new ArrayList<String>(PATHS.currentMods().size());
		for (int i = 0; i < PATHS.currentMods().size(); i++) {
			mods.add("'" + PATHS.currentMods().get(i).name + "', version: " + PATHS.currentMods().get(i).version);
		}
		
		scripts = GAME.script().currentScripts();
		
		timeSaved = System.currentTimeMillis()/(1000*60);
	}
	
	public CharSequence getSavedAgo(){

		Str s = Str.TMP.clear();
		
		long now = (System.currentTimeMillis() - timeSaved)/(1000*60*60*24);

		if (now == 0) {
			now = (System.currentTimeMillis() - timeSaved)/(1000*60*60);
			if (now == 0) {
				now = (System.currentTimeMillis() - timeSaved)/(1000*60);
				if (now == 1) {
					return ¤¤1Minute;
				}else {
					return s.add(¤¤Minutes).insert(0, (int)now);
				}
			}else if (now == 1) {
				return ¤¤1Hour;
			}else {
				return s.add(¤¤Hours).insert(0, (int)now);
			}
		}else if(now == 1) {
			return s.add(¤¤Yesterday);
		}else if (now < 500){
			return s.add(¤¤DaysAgo).insert(0, (int)now);
		}
		return ¤¤veryLong;
	}
	
	public static CharSequence problem(java.nio.file.Path path, boolean warn){
		SaveGame b;
		try {
			FileGetter fg = new FileGetter(path, true);
			b = (SaveGame) fg.object();
		}catch(Exception e) {
			return "Save is corrupted"+e;
		}
		return b.problem(warn);
	}
	
	public CharSequence problem(boolean warn) {
		
		if (VERSION.versionMajor(version) != VERSION.VERSION_MAJOR)
			return (Str.TMP.clear().add(¤¤version).insert(0, VERSION.versionMajor(version)) + " " +  + VERSION.VERSION_MAJOR);
		
		if (!races.equals(getResources("race"))) {
			return (modException(¤¤race));
		}
		if (!rooms.equals(getResources("room"))) {
			return (modException(¤¤room));
		}
		if (!resources.equals(getResources("resource"))) {
			return (modException(¤¤resources));
		}
		if (industries != null && !industries.equals(getResourcesArray("room", "INDUSTRY")))
			return (modException(¤¤industries));
		
		if (warn)
			for (String sc : scripts) {
				if (!PATHS.SCRIPT().jar.exists(sc)) {
					return (modException(Str.TMP.clear().add(¤¤script).insert(0, sc)));
				}
			}
		return null;
	}
	
	private CharSequence modException(CharSequence problem) {
		
		if (modHash != PATHS.modHash()) {
			Str s = Str.TMP;
			s.clear();
			
			if (modHash == 0) {
				s.add(¤¤modNone);
			}else {
				s.add(¤¤modOther);
				s.NL();
				s.NL();
				for (String ss : mods) {
					s.add(ss);
					s.NL();
				}
				s.NL();
				s.add(¤¤mod2);
			}
			s.NL();
			s.NL();
			s.add(¤¤underlaying);
			s.NL();
			s.add(problem);
			return s;
		}else {
			return problem;
		}
		
	}
	
	private static String getResourcesArray(String init, String key) {
		String s = "";
		PATH p = PATHS.INIT().getFolder(init);
		for (String k : p.getFiles()) {
			Json j = new Json(p.get(k));
			if (j.has(key) && j.jsonsIs(key))
				s += k + j.jsons(key).length;
		}
		return s;
	}
	
	private static String getResources(String init) {
		String s = "";
		for (String k : PATHS.INIT().getFolder(init).getFiles())
			s += k;
		return s;
	}
	
}
