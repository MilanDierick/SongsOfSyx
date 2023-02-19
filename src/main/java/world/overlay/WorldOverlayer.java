package world.overlay;

import util.info.INFO;

public class WorldOverlayer extends INFO{

	private boolean added;
	
	WorldOverlayer(CharSequence name, CharSequence desc) {
		super(name, desc);
	}

	public void add() {
		added = true;
	}
	
	public void remove() {
		added = false;
	}
	
	public boolean added() {
		return added;
	}
}
