package settlement.army.ai.general;

import init.config.Config;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.main.SETT;
import snake2d.util.sets.*;

final class UtilDivMap {
	
	private final GTile[] tiles = new GTile[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int tileNewI = 0;
	private final GTile[][] grid = new GTile[(int) Math.ceil(SETT.TWIDTH/16.0)][(int) Math.ceil(SETT.THEIGHT/16.0)];
	private Bitmap2D is = new Bitmap2D(SETT.TILE_BOUNDS, false);
	
	private final ArrayList<MDiv> res = new ArrayList<MDiv>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final LIST<MDiv> none = new ArrayList<MDiv>(0);
	public UtilDivMap() {
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = new GTile();
		}
	}
	
	void clear() {
		tileNewI = 0;
		is.clear();
		for (GTile[] tt : grid) {
			for (int i = 0; i < tt.length; i++)
				tt[i] = null;
		}
	}
	
	void add(MDiv div) {

		is.set(div.tx, div.ty, true);
		int gx = div.tx/16;
		int gy = div.ty/16;
		GTile t = tiles[tileNewI++];
		t.div = div;
		t.next = grid[gy][gx];
		grid[gy][gx] = t;
		
	}

	public LIST<MDiv> get(int tx, int ty){
		if (!SETT.IN_BOUNDS(tx, ty))
			return none;
		if (!is.is(tx, ty))
			return none;
		res.clearSloppy();
		GTile t = grid[ty/16][tx/16];
		while(t != null && res.hasRoom()) {
			if (t.div.tx == tx && t.div.ty == ty)
				res.add(t.div);
			t = t.next;
		}
		return res;
	}
	
	private static final class GTile {
		
		private MDiv div;
		private GTile next;
		
	}
	

	
}

