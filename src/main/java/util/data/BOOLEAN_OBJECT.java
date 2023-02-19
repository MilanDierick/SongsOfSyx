package util.data;

import util.info.INFO;

public interface BOOLEAN_OBJECT<T> {

	public boolean is(T t);
	
	public default INFO info() {
		return null;
	}
	
	public interface BOOLEAN_OBJECTE<T> extends BOOLEAN_OBJECT<T>{
		
		public BOOLEAN_OBJECTE<T> set(T t, boolean b);
		
		public default BOOLEAN_OBJECTE<T> toggle(T t) {
			return set(t, !is(t));
		}
		
		public default BOOLEAN_OBJECTE<T> setOn(T t) {
			return set(t, true);
		}
		
		public default BOOLEAN_OBJECTE<T> setOff(T t) {
			return set(t, false);
		}
	}
	
}
