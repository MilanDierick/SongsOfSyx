package util.data;

import util.data.INT.INTE;
import util.data.INT_O.INT_OE;

public abstract class IntFromObject<T> implements INTE{

	
	public IntFromObject() {

	}

	
	@Override
	public int min() {
		return iGet().min(oGet());
	}

	@Override
	public int max() {
		return iGet().max(oGet());
	}

	@Override
	public void set(int t) {
		iGet().set(oGet(), t);
	}


	@Override
	public int get() {
		return iGet().get(oGet());
	}
	
	protected abstract INT_OE<T> iGet();
	
	protected abstract T oGet();

}
