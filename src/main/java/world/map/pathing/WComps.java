package world.map.pathing;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.map.*;
import snake2d.util.sets.Bitsmap2D;
import world.WORLD;

final class WComps implements MAP_OBJECT<WComp>{

	private final Bitsmap2D ids;
	private final WComp[] comps;
	final Bitsmap2D dirMap = new Bitsmap2D(0, 8, WORLD.TBOUNDS());;
	public MAP_BOOLEAN route = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (dirMap == null)
				return false;
			return dirMap.get(tx, ty) != 0;
		}
		
		@Override
		public boolean is(int tile) {
			if (dirMap == null)
				return false;
			return dirMap.get(tile) != 0;
		}
	};
	
	
	
	WComps(MAP_BOOLEAN tmp){
		WGenComps map = new WGenComps(tmp);
		comps = map.makeComps();
		int bits = 32-Integer.numberOfLeadingZeros(map.MAX);
		ids = new Bitsmap2D(0, bits, WORLD.TBOUNDS());
		for (COORDINATE c : WORLD.TBOUNDS()) {
			
			ids.set(c, map.map.get(c));
			
		}
		for (COORDINATE c : WORLD.TBOUNDS())
			dirMap.set(c, map.dirMap.get(c));
	}
	
	@Override
	public WComp get(int tile) {
		return comps[ids.get(tile)];
	}

	@Override
	public WComp get(int tx, int ty) {
		return comps[ids.get(tx, ty)];
	}
	
	public WComp getByID(int id) {
		return comps[id];
	}
	
	public MAP_INT ids() {
		return ids;
	}
	
	public final int maxID() {
		return comps.length;
	}
	
}
