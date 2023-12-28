package snake2d.util.sets;

import java.util.*;

public class KeyMap<T> {

	private final HashMap<String, T> map = new HashMap<>();
	
	public void put(String key, T t) {
		map.put(key, t);
	}
	
	public void remove(String key) {
		map.remove(key);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public T get(String key) {
		return map.get(key);
	}
	
	public void debug() {
		for (String s : map.keySet())
			System.err.println(s);
	}
	
	public void expand() {
		ArrayList<String> bb = new ArrayList<String>(50);
		for (String s : map.keySet()) {
			if (s.startsWith("_")) {
				
				String key = s.substring(1, s.length());
				if (map.containsKey(key))
					continue;
				bb.add(s);
				
			}
		}
		
		for (String s : bb) {
			
			map.put(s.substring(1, s.length()), map.get(s));
		}
		
		
	}
	
	public int size() {
		return map.size();
	}
	
	public LIST<T> all(){
		return new ArrayList<T>(map.values());
	}
	
	public LIST<T> allSorted() {
		String[] keys = new String[map.values().size()];
		int i = 0;
		for (String k : keys()) {
			keys[i++] = k;
		}
		Arrays.sort(keys);
		ArrayList<T> res = new ArrayList<>(keys.length);
		for(i = 0; i < keys.length; i++)
			res.add(get(keys[i]));
		return res;
	}

	public Set<String> keys() {
		return map.keySet();
	}
	
	public LIST<String> keysSorted() {
		String[] keys = new String[map.values().size()];
		int i = 0;
		for (String k : keys()) {
			keys[i++] = k;
		}
		Arrays.sort(keys);
		ArrayList<String> res = new ArrayList<>(keys.length);
		for(i = 0; i < keys.length; i++)
			res.add(keys[i]);
		return res;
	}
	
	public String keysString() {
		String s = "";
		for (String ss : keysSorted()) {
			s += ss + System.lineSeparator();
		}
		return s;
	}
	
	public void clear() {
		map.clear();
	}
	

	
	public static class KeyMapL<T> {

		private final HashMap<String, ArrayListGrower<T>> map = new HashMap<>();
		
		public void put(String key, T t) {
			if (!containsKey(key))
				map.put(key, new ArrayListGrower<>());
			map.get(key).add(t);
		}
		
		public void remove(String key) {
			map.remove(key);
		}
		
		public boolean containsKey(String key) {
			return map.containsKey(key);
		}
		
		public LIST<T> get(String key) {
			return map.get(key);
		}
		
		public void debug() {
			for (String s : map.keySet())
				System.err.println(s);
		}
		
		public void expand() {
			ArrayList<String> bb = new ArrayList<String>(50);
			for (String s : map.keySet()) {
				if (s.startsWith("_")) {
					
					String key = s.substring(1, s.length());
					if (map.containsKey(key))
						continue;
					bb.add(s);
					
				}
			}
			
			for (String s : bb) {
				
				map.put(s.substring(1, s.length()), map.get(s));
			}
		}
		
		public LIST<T> allSorted() {
			String[] keys = new String[map.values().size()];
			int i = 0;
			for (String k : keys()) {
				keys[i++] = k;
			}
			Arrays.sort(keys);
			ArrayList<T> res = new ArrayList<>(keys.length);
			for(i = 0; i < keys.length; i++)
				res.add(get(keys[i]));
			return res;
		}

		public Set<String> keys() {
			return map.keySet();
		}
		
		public LIST<String> keysSorted() {
			String[] keys = new String[map.values().size()];
			int i = 0;
			for (String k : keys()) {
				keys[i++] = k;
			}
			Arrays.sort(keys);
			ArrayList<String> res = new ArrayList<>(keys.length);
			for(i = 0; i < keys.length; i++)
				res.add(keys[i]);
			return res;
		}
		
		public String keysString() {
			String s = "";
			for (String ss : keysSorted()) {
				s += ss + System.lineSeparator();
			}
			return s;
		}
		
		public void clear() {
			map.clear();
		}
		
	}
	
}
