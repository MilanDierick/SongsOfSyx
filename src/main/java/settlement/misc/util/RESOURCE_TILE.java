package settlement.misc.util;

import init.resources.RESOURCE;

public interface RESOURCE_TILE extends FINDABLE{

	public RESOURCE resource();
	public void resourcePickup();
	public int reservable();
	public int amount();
	
	public default boolean isStoring() {
		return isfetching();
	}
	public default boolean isfetching() {
		return false;
	}
	
	public default double spoilRate() {
		return 1.0;
	}
	
	public default boolean hasRoom() {
		return true;
	}
	
	public interface RESOURCE_TILE_HASER {
		public RESOURCE_TILE resourceTile(int tx, int ty);
		public double degradeRate();
	}
	
}
