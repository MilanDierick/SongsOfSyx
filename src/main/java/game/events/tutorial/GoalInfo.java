package game.events.tutorial;

import snake2d.util.file.Json;

final class GoalInfo {

	public final CharSequence name;
	public final CharSequence desc;
	public final CharSequence mission;
	
	GoalInfo(Json json, String key){
		json = json.json(key);
		name = json.text("NAME");
		desc = json.text("DESC");
		mission = json.has("GOAL") ? json.text("GOAL") : null;
	}
	
}
