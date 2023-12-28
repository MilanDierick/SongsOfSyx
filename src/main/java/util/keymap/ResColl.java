package util.keymap;

import game.GAME;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

public class ResColl<T> {

	private final ArrayListGrower<T> all = new ArrayListGrower<>();
	private final KeyMap<ArrayListGrower<T>> map = new KeyMap<>();
	public final String key;
	private boolean hasComplained = false;
	public static String wildCard = "*";
	
	public ResColl(String key) {
		this.key = key;
	}
	
	public void push(T t, String key) {
		if (map.containsKey(key))
			throw new RuntimeException("Another element with this key exists " + key);
		ArrayListGrower<T> li = new ArrayListGrower<>();
		li.add(t);
		map.put(key, li);
		all.add(t);
	}
	
	public void pushComposite(T t, String key) {
		if (!map.containsKey(key))
			map.put(key, new ArrayListGrower<>());
		ArrayListGrower<T> li = map.get(key);
		li.add(li);
	}
	
	public LIST<T> get(String key){
		if (key.equals(wildCard)) {
			return all;
		}
		return map.get(key);
	}
	
	public void process(String key, Json json, RCAction<T> action) {
		if (!json.has(key))
			return;
		
		json = json.json(key);
		
		for (String k : json.keys()) {
			LIST<T> li = get(k);
			
			if (li == null) {
				if (hasComplained)
					continue;
				String err = json.errorGet("No " + this.key + " named " + k, k);
				err += " Available:";
				err += System.lineSeparator();
				err += map.keysString();
				GAME.Warn(err);
				hasComplained = true;
				continue;
			}
			for (T t : li) {
				action.doWithJson(t, json, k);
			}
		}
		
	}
	
	public void process(Json json, RCAction<T> action) {
		process(key, json, action);
	}
	
	
	
	public interface RCAction<T> {
		
		public void doWithJson(T t, Json json, String key);
		
	}
	
	
}
