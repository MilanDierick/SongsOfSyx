package world.entity;

import snake2d.util.sets.LISTE;

public abstract class WEntityConstructor<T extends WEntity> {
	
	final int index;
	
	protected WEntityConstructor(LISTE<WEntityConstructor<? extends WEntity>> all){
		index = all.add(this);
	}
	
	protected abstract T create();
	
	protected abstract void clear();
	
}
