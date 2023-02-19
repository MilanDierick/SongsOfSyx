package settlement.stats;

import game.GAME;
import settlement.stats.StatsMultipliers.StatMultiplier;
import snake2d.util.file.Json;
import util.keymap.KEY_COLLECTION;

public abstract class StatsJson {

	public StatsJson(Json json){
		this("STATS", json);
	}
	
	public StatsJson(String masterkey, Json json){
		if (json.has(masterkey))
			json = json.json(masterkey);
		if (json.has(STATS.MULTIPLIERS().MAP.key)) {
			Json j = json.json(STATS.MULTIPLIERS().MAP.key);
			for (String key : j.keys()) {
				if (key.equals(KEY_COLLECTION.WILDCARD)) {
					for (StatMultiplier ss : STATS.MULTIPLIERS().all()) {
						doWithMultiplier(ss, j, ss.key);
					}
					continue;
				}
				StatMultiplier m = STATS.MULTIPLIERS().MAP.tryGet(key);
				if (m == null) {
					handleFaultyMul(j, key);
				}else {
					doWithMultiplier(m, j, key);
				}
			}
		}
		
		for (String ckey : json.keys()) {
			if (ckey.equals(STATS.MULTIPLIERS().MAP.key))
				continue;
			if (ckey.equals(KEY_COLLECTION.WILDCARD)) {
				for (StatCollection col : STATS.COLLECTIONS()) {
					for (STAT ss : col.all()) {
						if (ss.key() != null)
							doWithTheJson(col, ss, json, ckey);
					}
				}
				
				continue;
			}
			StatCollection col = STATS.COLLECTION(ckey);
			if (col == null) {
				handleFaultyColl(json, ckey);
				continue;
			}
			Json j = json.json(ckey);
			for (String key : j.keys()) {
				if (key.equals(KEY_COLLECTION.WILDCARD)) {
					for (STAT ss : col.all()) {
						doWithTheJson(col, ss, j, key);
					}
					continue;
				}
				
				STAT s = col.MAP().tryGet(key);
				if (s == null) {
					handleFaultyStat(j, col, key);
				}else {
					doWithTheJson(col, s, j, key);
				}
			}
		}
	}
	
	public abstract void doWithMultiplier(StatMultiplier m, Json j, String key);
	
	public abstract void doWithTheJson(StatCollection coll, STAT s, Json j, String key);
	
	public void handleFaultyStat(Json j, StatCollection coll, String key) {
		String av = System.lineSeparator() + "Available:" + System.lineSeparator();
		for (STAT c : coll.all())
			if (c.key() != null)
				av += c.key() + System.lineSeparator();
		GAME.Warn(j.errorGet("No stat in collection: " + coll.key + " named: " + key + av, key));
	}
	
	public void handleFaultyColl(Json j, String coll) {
		String av = System.lineSeparator() + "Available:" + System.lineSeparator();
		for (StatCollection c : STATS.COLLECTIONS())
			av += c.key + System.lineSeparator();
		GAME.Warn(j.errorGet("No stat collection with key: " + coll + av, coll));
	}
	
	public void handleFaultyMul(Json j, String mul) {
		String av = System.lineSeparator() + "Available:" + System.lineSeparator();
		for (StatMultiplier c : STATS.MULTIPLIERS().all())
			av += c.key + System.lineSeparator();
		GAME.Warn(j.errorGet("No stat MULTIPLIER with key: " + mul + av, STATS.MULTIPLIERS().MAP.key));
	}
	
}
