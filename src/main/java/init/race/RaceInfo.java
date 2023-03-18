package init.race;

import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;

public final class RaceInfo extends INFO {

	public final String namePosessive;
	public final String desc_long;
	public final String initialChallenge;
	public final String[] pros;
	public final String[] cons;
	
	
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
	}

	
}
