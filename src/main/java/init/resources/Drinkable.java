package init.resources;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;

public final class Drinkable implements INDEXED{

	public final RESOURCE resource;
	private final int index;
//	public final short bit;
	
	Drinkable(ArrayList<Drinkable> all, RESOURCE r) {
		index = all.add(this);
		resource = r;
//		bit = (short) (1 << index);
	}

	@Override
	public int index() {
		return index;
	}
	
}
