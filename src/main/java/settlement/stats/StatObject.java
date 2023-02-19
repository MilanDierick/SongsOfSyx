package settlement.stats;

import settlement.entity.humanoid.Humanoid;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.info.INFO;

public abstract class StatObject<T> implements GETTER_TRANSE<Humanoid, T> {

	public final INFO info;
	
	StatObject(CharSequence name, CharSequence desc){
		this.info = new INFO(name, desc);
	}
	
	public abstract T get(Induvidual i);
	
	@Override
	public T get(Humanoid f) {
		return get(f.indu());
	}
	
	public abstract STAT stat();
	
}
