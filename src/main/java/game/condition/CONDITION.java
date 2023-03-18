package game.condition;

import util.data.BOOLEAN;

public abstract class CONDITION implements BOOLEAN {
	
	public final CharSequence name;
	public final COMPARATOR comp;
	public final double target;
	public final boolean isInt;
	
	public CONDITION(CharSequence name, COMPARATOR comp, double target, boolean isInt){
		this.name = name;
		this.comp = comp;
		this.target = target;
		this.isInt = isInt;
	}
	
	@Override
	public boolean is() {
		return comp.passes(current(), target);
	}
	
	public abstract double current();
	
}
