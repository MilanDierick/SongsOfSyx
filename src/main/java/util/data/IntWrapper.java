package util.data;

import util.data.INT.INTE;

public abstract class IntWrapper implements INTE{
	
	public IntWrapper(){
		
	}
	
	public abstract INTE getOther();
	
	@Override
	public int min() {
		return getOther().min();
	}

	@Override
	public int max() {
		return getOther().max();
	}

	@Override
	public void set(int t) {
		getOther().set(t);
	}
	
	@Override
	public int get() {
		return getOther().get();
	}

}
