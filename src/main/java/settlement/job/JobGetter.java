package settlement.job;

import settlement.main.SETT;
import settlement.tilemap.floor.Floors.Floor;
import settlement.tilemap.terrain.*;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.map.MAP_OBJECT;

final class JobGetter implements MAP_OBJECT<Job>{

	@Override
	public Job get(int tile) {
		return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
	}

	@Override
	public Job get(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return null;
		{
			Job j = SETT.JOBS().getter.get(tx, ty);
			if (j != null)
				return j;
		}
		TerrainTile t = SETT.TERRAIN().get(tx, ty);
		if (t instanceof TFortification){
			return SETT.JOBS().build_fort.get(((TFortification) t).index());
		}
		if (t instanceof TBuilding.BuildingComponent) {
			JobBuildStructure tt = SETT.JOBS().build_structure.get(((TBuilding.BuildingComponent) t).building().index());
			if (t instanceof TBuilding.Ceiling || t instanceof TBuilding.Ceiling.Opening)
				return tt.ceiling;
			return tt.wall;
		}
		if (t instanceof TFence) {
			return SETT.JOBS().fences.get(((TFence) t).index());
		}
		if (t instanceof TFortification.Stairs) {
			return SETT.JOBS().build_stairs;
		}
		
		if (t == SETT.TERRAIN().MOUNTAIN) {
			return SETT.JOBS().clearss.caveFill;
		}
		
		
		Floor f = SETT.FLOOR().getter.get(tx, ty);
		if (f != null && f.isRoad) {
			return SETT.JOBS().roads.get(f.indexRoad());
		}
		if (t == SETT.TERRAIN().CAVE) {
			return SETT.JOBS().clearss.tunnel;
		}
		return null;
	}

	
	
}
