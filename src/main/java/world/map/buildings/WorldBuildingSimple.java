package world.map.buildings;

import static world.World.*;

import java.io.IOException;

import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.tool.*;
import view.world.IDebugPanelWorld;
import world.World;

public abstract class WorldBuildingSimple extends WorldBuilding{

	public final CharSequence name;
	public final PlacableMulti placer;
	
	protected WorldBuildingSimple(LISTE<WorldBuilding> all, CharSequence name){
		super(all);
		this.name = name;
		placer = new PlacableMulti(name) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				int tile = tx + ty*TWIDTH();
				BUILDINGS().map.set(tile, WorldBuildingSimple.this);
				data().set(tx, ty, fix(tx, ty));
				
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					int dx = tx+d.x();
					int dy = ty +d.y();
					if (IN_BOUNDS(dx, dy))
						BUILDINGS().map.set(dx+dy*TWIDTH(), BUILDINGS().map.get(dx, dy));
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!IN_BOUNDS(tx, ty))
					return PlacableMessages.¤¤IN_MAP;
				if (World.REGIONS().isCentre.is(tx, ty))
					return PlacableMessages.¤¤BLOCKED;
				return null;
			}
		};
		
		IDebugPanelWorld.add(placer, "building");
	}
	
	protected abstract int fix(int tx, int ty);
	
	@Override
	protected void unplace(int tx, int ty) {
		
	}
	
	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}
	
}
