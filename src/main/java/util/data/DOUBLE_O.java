package util.data;

import util.info.INFO;

public interface DOUBLE_O<T> {

	public double getD(T t);

	public interface DOUBLE_OE<T> extends DOUBLE_O<T>{
		
		public default DOUBLE_OE<T> incD(T t, double d) {
			setD(t, getD(t)+d);
			return this;
		}
		public DOUBLE_OE<T> setD(T t, double d);
	}
	
	public default INFO info() {
		return null;
	}
	
}
