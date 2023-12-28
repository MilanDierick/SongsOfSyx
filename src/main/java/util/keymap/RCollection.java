package util.keymap;

import java.util.Iterator;
import java.util.Set;

import game.GAME;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.*;

public abstract class RCollection<T> implements COLLECTION<T>, KEY_COLLECTION<T>, Iterable<T>{
	
	public final String key;
	protected final KeyMap<T> map; 
	
	public RCollection(String key) {
		this(key, new KeyMap<>());
	}
	
	public RCollection(String key, KeyMap<T> map) {
		this.key = key;
		this.map = map;
	}
	
	@Override
	public abstract LIST<T> all();

	
	@Override
	public T tryGet(String value) {
		if (map.containsKey(value)) {
			return map.get(value);
		}
		return null;
	}
	
	@Override
	public String key() {
		return key;
	}
	
	@Override
	public Iterator<T> iterator() {
		return all().iterator();
	}
	
	@Override
	public Set<String> available() {
		return map.keys();
	}
	
	private boolean hasErr = false;
	
	public abstract class KJson {
		
		public KJson(Json json) {
			this(key, json);
		}
		
		public KJson(String key, Json json) {
			
			if (json.has(key)) {
				json = json.json(key);
				for (String s : json.keys()) {
					
					if (s.equals(WILDCARD)) {
						for (int i = 0; i < all().size(); i++) {
							T t = all().get(i);
							process(t, json, s, true);
						}
					}else if (map.containsKey(s)) {
						process(map.get(s), json, s, false);
					}else{
						String p = "No " + key + " named " + s + " " + json.path() + " line: " + json.line(s);
						if (!hasErr) {
							p += System.lineSeparator() + "Available:" + System.lineSeparator();
							p += map.keysString();
							GAME.Warn(p);
							hasErr = true;
						}else {
							LOG.ln(p);
						}
					}
					
					
				}
				
			}
		}
		
		protected abstract void process(T s, Json j, String key, boolean isWeak);
		
	}

}
