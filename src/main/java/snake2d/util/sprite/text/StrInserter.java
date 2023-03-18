package snake2d.util.sprite.text;

public abstract class StrInserter<T> {

	public final String key;
	private final static Str tmp = new Str(128);
	public StrInserter(String key){
		this.key = key;
	}
	
	protected abstract void set(T t, Str str);
	
	public boolean insert(T t, Str str) {
		if (!str.hasinsert(key))
			return false;
		tmp.clear();
		set(t, tmp);
		str.insert(key, tmp);
		return true;
	}
	
}
