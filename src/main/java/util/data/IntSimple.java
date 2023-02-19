package util.data;

import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;

public abstract class IntSimple implements INTE{
	
	
	
	@Override
	public int min() {
		return Integer.MIN_VALUE;
	}

	@Override
	public int max() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void set(int t) {
		int old = get();
		t = CLAMP.i(t, min(), max());
		setp(t);
		change(old, get());
	}
	
	protected abstract void setp(int t);
	
	protected void change(int old, int current) {
		
	}

}
