package view.keyboard;

import snake2d.util.sets.*;

public abstract class KeyPage {

	final ArrayListResize<Key> all = new ArrayListResize<>(64, 1024);
	final IntHashMap<Key> map = new IntHashMap<Key>();
	public final String key;
	
	KeyPage(String key){
		this.key = key;
	}

	public Key get(int modCode, int keyCode) {
		if (map.contains(Key.hash(modCode, keyCode)))
			return map.get(Key.hash(modCode, keyCode));
		return null;
	}
	
	public LIST<Key> all(){
		return all;
	}
	
	public abstract CharSequence name();
	
}
