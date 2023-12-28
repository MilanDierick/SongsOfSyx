package world.regions.map;

import static world.WORLD.*;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.Bitmap2D;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.map.GMapTmp.TmpRegion;

class GMapAssign {

	private final Bitmap2D look = new Bitmap2D(TWIDTH(), THEIGHT(), false);
	private final GMapTmp map;
	private final int px, py;
	
	public GMapAssign(GMapTmp map, int px, int py) {
		this.map = map;
		this.px = px;
		this.py = py;
	}
	
	public int generate() {
		
		assign(px, py, 0);
		int id = 1;
		
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (id >= WREGIONS.MAX)
				break;
			if (!look.is(c) && map.get(c) != null && map.get(c).valid) {
				assign(c.x(), c.y(), id);
				id++;
			}
		}
		return id;
	}
	
	private void assign(int px, int py, int id) {
		Region r = REGIONS().getByIndex(id);
		r.info.clear();
		final TmpRegion ii = map.get(px, py);
		for (COORDINATE c : ii.bounds) {
			if (map.get(c) == ii) {
				look.set(c, true);
				REGIONS().map.set(c.x(), c.y(), r);
			}
		}
		
		r.info.init(ii.c.x(), ii.c.y(), ii.bounds);
		GeneratorCenter.init(r);
	}


	
}
