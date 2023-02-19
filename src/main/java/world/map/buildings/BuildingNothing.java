package world.map.buildings;

import snake2d.util.sets.LISTE;

final class BuildingNothing extends WorldBuildingSimple{

	BuildingNothing(LISTE<WorldBuilding> all) {
		super(all, "building clear");
	}

	@Override
	protected int fix(int tx, int ty) {
		return 0;
	}

	@Override
	public boolean isVisible(int ran, int tile) {
		return false;
	}
	
}
