package init.resources;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;

public final class Edible implements INDEXED{

	public final RESOURCE resource;
	private final int index;
	public final short bit;
	
	Edible(ArrayList<Edible> all, RESOURCE r) {
		index = all.add(this);
		resource = r;
		bit = (short) (1 << index);
	}

	@Override
	public int index() {
		return index;
	}
	
}
