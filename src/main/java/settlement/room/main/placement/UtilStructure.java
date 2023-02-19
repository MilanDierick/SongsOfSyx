package settlement.room.main.placement;

import game.GAME;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.tilemap.TBuilding;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.util.datatypes.COORDINATE;
import util.data.GETTER.GETTER_IMP;

public final class UtilStructure extends GETTER_IMP<TBuilding> {
	
	private final RoomPlacer p;

	UtilStructure(RoomPlacer p){
		this.p = p;
		set(SETT.TERRAIN().BUILDINGS.getAt(0));
		if (SETT.TERRAIN().BUILDINGS.tryGet("WOOD") != null)
			set(SETT.TERRAIN().BUILDINGS.tryGet("WOOD"));
	}
	
	SETT_JOB getWallJob(int tx, int ty) {
		
		if (SETT.TERRAIN().CAVE.is(tx, ty))
			return SETT.JOBS().clearss.caveFill;
		return SETT.JOBS().build_structure.get(get().index()).wall;
		
	}
	
	SETT_JOB getCeilingJob(int tx, int ty) {
		
		if (SETT.TERRAIN().MOUNTAIN.is(tx, ty))
			return null;
		if (get().roof.is(tx, ty))
			return null;
		if (SETT.TERRAIN().CAVE.is(tx, ty))
			return null;
		return SETT.JOBS().build_structure.get(get().index()).ceiling;
	}
	
	int unroofed = 0;
	int tick = 0;
	
	public int roofs() {
		
		if (GAME.updateI() != tick) {
			unroofed = 0;
			for (COORDINATE c : p.instance.body()) {
				if (!p.instance.is(c))
					continue;
				
				if (getCeilingJob(c.x(), c.y()) != null) {
					unroofed ++;
				}
			}
			if (p.autoWalls.isOn()) {
				unroofed += p.door.getOpenings();
			}
		}
		return unroofed;
	}
	
	public int walls() {
		if (!p.autoWalls.isOn())
			return 0;
		return p.door.getWalls();
	}
	
	public int mountainWalls() {
		if (!p.autoWalls.isOn())
			return 0;
		return p.door.getMountains();
	}

	public void set(int tx, int ty) {
		TerrainTile t = SETT.TERRAIN().get(tx, ty);
		if (t instanceof TBuilding.BuildingComponent) {
			set(((TBuilding.BuildingComponent)t).building());
		}
			
		
	}
	
	
	
}
