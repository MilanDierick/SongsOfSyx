package util.data;

import snake2d.util.misc.CLAMP;
import util.data.INT_O.INT_OE;

public abstract class IntObject<T> implements INT_OE<T> {

	@Override
	public void set(T t, int i) {
		int old = get(t);
		i = CLAMP.i(i, min(t), max(t));
		setP(t, i);
		change(t, old, get(t));
	}

	protected abstract void setP(T t, int i);

	protected void change(T t, int old, int current) {
		
	}
	
}
