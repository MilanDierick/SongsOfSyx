package settlement.environment;

import init.paths.PATH;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.tilemap.terrain.TBuilding;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.LISTE;

public final class SettEnvRoundness extends SettEnv{

	
	private final SettEnv area;

	SettEnvRoundness(LISTE<SettEnv> all, PATH pj, PATH tj, EUpdater uper) {
		super(all, "_ROUNDNESS", pj, tj, 1, uper);
		
		area = new SettEnv(all, "_URBANISATION", pj, tj, 1, uper) {
			@Override
			public double radius() {
				return 0.5;
			}
			
			@Override
			double getBaseValue(int tx, int ty) {
				if (wall.is(tx, ty)) {
					return 1.0;
				}
				return 0;
			}
			
			@Override
			public double getCost(int toX, int toY) {
				return wall.is(toX, toY) ? SettEnvMap.RADIUS : 1;
			}
		};
		
	}

	public SettEnv area() {
		return area;
	}
	
	@Override
	double getBaseValue(int tx, int ty) {
		
		if (isser.is(tx, ty)) {
			return 1.0;
		}
		return 0;

	}
	
	@Override
	public double getCost(int toX, int toY) {
		return wall.is(toX, toY) ? SettEnvMap.RADIUS : 1;
	}
	
	@Override
	public double radius() {
		return 0.5;
	} 
	
	private MAP_BOOLEAN isser = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (!wall.is(tx, ty))
				return false;
			
			if (!isFacer(tx, ty))
				return false;
			
			for (int di = 0; di < DIR.NORTHO.size(); di++) {
				DIR d = DIR.NORTHO.get(di);
				if (wall.is(tx, ty, d.next(1)) && wall.is(tx, ty, d.next(-1)))
					continue;
				if (wall.is(tx, ty, d))
					return true;
				
			}
			
			return false;
		}
		
		private boolean isFacer(int tx, int ty) {
			boolean ff = false;
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (!wall.is(tx+d.x(), ty+d.y())) {
					if (ff)
						return true;
					ff = true;
				}else
					ff = false;
			}
			return false;
		}

		@Override
		public boolean is(int tile) {
			int tx = tile%SETT.TWIDTH;
			int ty = tile/SETT.TWIDTH;
			return is(tx, ty);
		}
	};
	
	private MAP_BOOLEAN wall = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			
			if (t.clearing().isStructure()) {
				if (t instanceof TBuilding.Ceiling.Opening)
					return true;
				if (t.getAvailability(tx, ty) != null && t.getAvailability(tx, ty).player < 0)
					return true;
			}
			
			return false;
		}

		@Override
		public boolean is(int tile) {
			int tx = tile%SETT.TWIDTH;
			int ty = tile/SETT.TWIDTH;
			return is(tx, ty);
		}
	};
	
}
