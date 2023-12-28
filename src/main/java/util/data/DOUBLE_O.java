package util.data;

import game.GAME;
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
	
	public static abstract class DoubleOCached<T> implements DOUBLE_O<T> {
		
		private int upI = -1;
		private T upR = null;
		private double cache;
		
		@Override
		public double getD(T t) {
			if (upI != GAME.updateI() || upR != t) {
				upI = GAME.updateI();
				upR = t;
				cache = getValue(t);
			}
			return cache;
		}
		
		public abstract double getValue(T t);
		
	}
	
}
