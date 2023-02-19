package util.keymap;

import java.util.Iterator;
import java.util.Set;

import snake2d.util.sets.*;

public abstract class RCollection<T extends INDEXED> implements COLLECTION<T>, KEY_COLLECTION<T>, Iterable<T>{
	
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

}
