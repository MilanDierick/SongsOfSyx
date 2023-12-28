package snake2d.util.sets;

public abstract class FactoryList<T> {

	private final ArrayList<T> all;
	private final ArrayList<T> current;
	private final T dummy = create();
	
	public FactoryList(int max){
		
		all = new ArrayList<T>(max);
		current = new ArrayList<T>(max);
		
	}
	
	public void clear() {
		current.clearSloppy();
	}
	
	public T next() {
		if (current.size() > all.size()) {
			if (!all.hasRoom())
				return dummy;
			all.add(create());
		}
		T t = all.get(current.size());
		current.add(t);
		return t;
	}
	
	public LIST<T> current(){
		return current;
	}
	
	public abstract T create();
	
}
