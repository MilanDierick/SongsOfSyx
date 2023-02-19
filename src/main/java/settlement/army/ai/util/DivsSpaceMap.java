package settlement.army.ai.util;

import static settlement.main.SETT.*;

import settlement.army.Div;
import settlement.army.formation.DivPositionAbs;
import settlement.main.SETT;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.sets.Bitsmap1D;

public final class DivsSpaceMap {

	public static final int radius = 4;
	private static final double radiusI = 1.0/radius;
	private Bitsmap1D map = new Bitsmap1D(0, 3, SETT.TAREA);
	
	public DivsSpaceMap(DivTDataStatus[] statuses) {
		
	}
	
	void add(Div div, DivPositionAbs next) {

		for (int i = 0; i < next.deployed(); i++) {
			int x = next.tile(i).x();
			int y = next.tile(i).y();
			add(x, y, i);
		}
	}
	
	private void add(int tx, int ty, int currentI) {
		
		for (int y = -radius+1; y < radius; y++) {
			for (int x = -radius+1; x < radius; x++) {
				
				int dx = tx+x;
				int dy = ty+y;
				if (SETT.IN_BOUNDS(dx, dy)) {
					int dist = radius - Math.abs(x)+Math.abs(y);
					if (dist > 0) {
						int t = dx + dy*SETT.TWIDTH;
						if (map.get(t) < dist)
							map.set(t, dist);
					}
				}
				
			}
		}
		
	}

	void clear() {
		map.clear();
	}
	
	public MAP_DOUBLE cost = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			return get(tx+ty*TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			return 32*map.get(tile)*radiusI;
		}
	};


	
}
