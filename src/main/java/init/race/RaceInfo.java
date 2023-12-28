package init.race;

import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import util.info.INFO;

public final class RaceInfo extends INFO {

	public final String namePosessive;
	public final String desc_long;
	public final String initialChallenge;
	public final String[] pros;
	public final String[] cons;
	public final RaceWorldInfo winfo;
	private static KeyMap<RaceWorldInfo> wi = new KeyMap<>();
	
	public final LIST<String> armyNames;
	
	
	RaceInfo(Json json, Json text){
		super(text);
		namePosessive = text.text("POSSESSIVE");
		desc_long = text.text("DESC_LONG");
		
		initialChallenge = text.text("CHALLENGE", "");
		
		pros = text.textsTry("PROS");
		cons = text.textsTry("CONS");
		
		
//		firstNames = new ArrayList<>(names.get(json.value("FIRST_NAMES"), json));
//		lastNames = new ArrayList<>(names.get(json.value("SURNAMES"), json));
//
//		firstNamesNoble = new ArrayList<>(names.get(json.value("FIRST_NAMES_NOBLE"), json));
//		lastNamesNoble = new ArrayList<>(names.get(json.value("SURNAMES_NOBLE"), json));
		armyNames = new ArrayList<>(text.texts("ARMY_NAMES", 1, 255));
		
		String f = json.value("WORLD_NAME_FILE");
		if (!wi.containsKey(f)) {
			wi.put(f, new RaceWorldInfo(f));
		}
		
		winfo = wi.get(f);
		
	}

	public final class RaceWorldInfo {
		
		public final String[] intros;
		public final String[] fNames;
		public final String[] rIntro;
		public final String[] rNames;
		
		RaceWorldInfo(String key){
			Json json = new Json(PATHS.NAMES().getFolder("world").get(key));
			intros = json.texts("INTRO", 1, 128);
			fNames = json.texts("NAMES", 1, 512);
			rIntro = json.texts("RULER_INTRO", 1, 128);
			rNames = json.texts("RULER", 1, 512);
			
		}
		
		
	}
	

	
	
}
