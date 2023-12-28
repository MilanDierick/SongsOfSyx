package game.values;

import game.GAME;
import game.GameDisposable;
import game.faction.Faction;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.*;
import world.regions.Region;

public class GVALUES<T> {

	public static final String KEY = "VALUE";
	public static final GVALUES<Induvidual> INDU = new GVALUES<Induvidual>("HUMAN");
	public static final GVALUES<Region> REGION = new GVALUES<Region>("REGION");
	public static final GVALUES<Faction> FACTION = new GVALUES<Faction>("FACTION");

	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				INDU.map.clear();
				REGION.map.clear();
				FACTION.map.clear();
			}
		};
	}
	
	private final KeyMap<Value<T>> map = new KeyMap<Value<T>>();
	LinkedList<ACTION> inits = new LinkedList<>();
	boolean hasSpewed = false;
	public final String key;
	public Locks LOCK = new Locks();
	
	private GVALUES (String key) {
		this.key = key;
		
		
		
	}
	
	void clear() {
		map.clear();
		inits.clear();
		hasSpewed = false;
		LOCK.clear();
	}
	
	public static void init(GAME game) {
		INDU.init();
		REGION.init();
		FACTION.init();
	}
	
	private void init() {
		for (ACTION a : inits)
			a.exe();
		inits.clear();
		LOCK.init();
	}
	
	
	
	public void push(String key, Value<T> value) {
		
		if (map.containsKey(key))
			throw new RuntimeException("Another value has the same key: " + key);
		map.put(key, value);
		
	}
	
	public void push(String key, CharSequence name, DOUBLE_O<T> value, boolean isPercentage) {
		
		Value<T> v = new Value<T>(name, value, isPercentage, false);
		push(key, v);
		
	}
	
	public void push(String key, CharSequence name, DOUBLE_O<T> value, boolean isPercentage, boolean isBool) {
		
		Value<T> v = new Value<T>(name, value, isPercentage, isBool);
		push(key, v);
		
	}
	
	public void push(String key, CharSequence name, BOOLEANO<T> value) {
		
		DOUBLE_O<T> v = new DOUBLE_O<T>() {

			@Override
			public double getD(T t) {
				return value.is(t) ? 1 :0;
			}
		};
		
		push(key, name, v, false, true);
	}
	
	public void push(String key, CharSequence name, DOUBLE_O<T> value) {
		push(key, name, value, true);
	}
	
	public void pushI(String key, CharSequence name, INT_O<T> value) {
		DOUBLE_O<T> v = new DOUBLE_O<T>() {

			@Override
			public double getD(T t) {
				return value.get(t);
			}
		};
		
		push(key, name, v, false);
	}
	
	public Value<T> get(String key) {
		return map.get(key);
	}
	
	public String available() {
		return map.keysString();
	}
	
	public final class Locks {
		
		final KeyMap<Lockable<T>> map = new KeyMap<>();
		LinkedList<ACTION> inits = new LinkedList<>();
		boolean hasSpewed = false;
		public final Lockable<T> empty = new Lockable<>("", "", UI.icons().s.DUMMY, GVALUES.this);
		private Locks() {
			
		}
		
		public void init() {
			for (ACTION a : inits)
				a.exe();
			inits.clear();
		}

		void clear() {
			map.clear();
			inits.clear();
			hasSpewed = false;
		}
		
		public Lockable<T> get(String key) {
			return map.get(key);
		}
		
		public String available() {
			return map.keysString();
		}
		
		public Lockable<T> push(String key, CharSequence name, CharSequence desc, SPRITE icon){
			key = key.replace("__", "_");
			Lockable<T> t = new Lockable<T>(name, desc, icon, GVALUES.this);
			map.put(key, t);
			return t;
		}
		
		public Lockable<T> push(){
			Lockable<T> t = new Lockable<T>("", "", UI.icons().s.DUMMY, GVALUES.this);
			return t;
		}
		
	}
	
}
