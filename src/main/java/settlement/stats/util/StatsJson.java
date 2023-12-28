package settlement.stats.util;

import java.util.Comparator;
import java.util.LinkedList;

import game.GAME;
import settlement.stats.STATS;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;

public abstract class StatsJson {

	private static boolean hasErrored = false;
	
	public StatsJson(Json json){
		this("STATS", json);
	}
	
	public StatsJson(String masterkey, Json json){
		if (json.has(masterkey))
			json = json.json(masterkey);
		
		for (String k : json.keys()) {
			
			if (STATS.COLLECTION(k) != null) {
				StatCollection c = STATS.COLLECTION(k);
				for (STAT s : c.all())
					if (s.key() != null)
						doWithTheJson(s, json, k);
				
			}else if (STATS.STAT(k) != null && STATS.STAT(k).key() != null) {
				doWithTheJson(STATS.STAT(k), json, k);
			}else if (STATS.MULTIPLIERS().MAP.tryGet(k) != null) {
				StatMultiplier m = STATS.MULTIPLIERS().MAP.tryGet(k);
				doWithMultiplier(m, json, k);
			}else {
				handleFault(json, k);
			}
			
		}
		
	}
	
	public abstract void doWithMultiplier(StatMultiplier m, Json j, String key);
	
	public abstract void doWithTheJson(STAT s, Json j, String key);
	
	public void handleFault(Json j, String key) {
		
		String p = "No stat named: " + key + "  " + j.path() + " line: " + j.line(key);
		if (!hasErrored) {
			p += System.lineSeparator() + "Available:" + System.lineSeparator();
			p += available();
			GAME.Warn(p);
			hasErrored = true;
		}else {
			LOG.ln(p);
		}
		
	}
	
	public String available() {
		String k = "";
		LinkedList<String> ss = new LinkedList<>();
		for (StatCollection c : STATS.COLLECTIONS())
			ss.add(c.key);
		for (StatMultiplier c : STATS.MULTIPLIERS().all())
			ss.add(c.key);
		for (STAT c : STATS.all())
			if (c.key() != null)
				ss.add(c.key());
		ArrayList<String> as = new ArrayList<String>(ss);
		as.sort(new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String s : as)
			k += s + System.lineSeparator();
		return k;
		
	}
	
	
}
