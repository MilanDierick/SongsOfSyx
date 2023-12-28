package game.values;

import util.data.DOUBLE_O;

public class Value<T>{

	public final CharSequence name;
	public final DOUBLE_O<T> d;
	public final boolean percentage;
	public final boolean isBool;
	
	Value(CharSequence name, DOUBLE_O<T> d, boolean percentage, boolean isBool){
		this.name = name;
		this.d = d;
		this.percentage = percentage;
		this.isBool = isBool;
	}
	
}
