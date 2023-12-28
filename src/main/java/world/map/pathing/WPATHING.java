package world.map.pathing;

import java.io.IOException;

import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_INT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.Bitmap2D;
import view.interrupter.IDebugPanel;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.WORLD.WorldResource;
import world.regions.Region;

public class WPATHING extends WorldResource{

	WComps COMPS;
	WCompsPather path;
	public final WRegs tmpRegs = new WRegs();
	
	public WPATHING() {
		IDebugPanelWorld.add("path test", new ACTION() {
			
			@Override
			public void exe() {
				new DebugTest();
			}
		});
		
		IDebugPanel.add("path overlay", new ACTION() {
			
			@Override
			public void exe() {
				WORLD.OVERLAY().debug = new DebugOverlay();
			}
		});
		
	}
	
	@Override
	protected void save(FilePutter file) {
		Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
		for (COORDINATE c : WORLD.TBOUNDS())
			tmp.set(c, COMPS == null ? false : COMPS.route.is(c));
		tmp.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
		tmp.load(file);
		generate(tmp);
	}

	void generate(MAP_BOOLEAN tmp) {
		COMPS = new WComps(tmp);
		path = new WCompsPather(COMPS);
	}
	
	public static double movementSpeed(int tx, int ty) {
		if (WORLD.WATER().isBig.is(tx, ty)) {
			return 1;
		}
		if (WORLD.MOUNTAIN().heighter.get(tx, ty) >= 1)
			return 0.008;
		if (WORLD.FOREST().amount.get(tx, ty) == 1.0)
			return 0.15;
		return 0.3;
	}
	
	public static double getTerrainCost(int tx, int ty) {
		if (WORLD.WATER().isBig.is(tx, ty)) {
			return 1;
		}
		if (WORLD.MOUNTAIN().heighter.get(tx, ty) >= 1)
			return 12;
		if (WORLD.FOREST().amount.get(tx, ty) == 1.0)
			return 6;
		return 3;
	}
	
	public static void test() {
		new DebugTest();
	}
	
	public PathTile path(int sx, int sy, int destX, int destY) {
		return path.get(sx, sy, destX, destY, WTREATY.DUMMY());
	}
	
	public PathTile path(int sx, int sy, int destX, int destY, WTREATY treaty) {
		return path.get(sx, sy, destX, destY, treaty);
	}
	
	public int distance(Region from, Region to) {
		if (from == to)
			return 0;
		if (path.dest.find(from.cx(), from.cy(), to.cx(), to.cy(), WTREATY.DUMMY())) {
			if (path.cPath.find(from.cx(), from.cy(), path.dest, WTREATY.DUMMY()) != null)
				return path.cPath.distance;
		}
		
		
		return 0;
	}
	
	public COORDINATE rnd(Region r) {
		return path.rnd(r);
	}
	
	public COORDINATE rndDist(int tx, int ty, int min) {
		return path.rndDist(tx, ty, min);
	}
	
	public COORDINATE rndDistOwn(int tx, int ty, int min) {
		return path.rndDistOwn(tx, ty, min);
	}
	
	public MAP_BOOLEAN route = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (COMPS.dirMap == null)
				return false;
			return COMPS.dirMap.get(tx, ty) != 0;
		}
		
		@Override
		public boolean is(int tile) {
			if (COMPS.dirMap == null)
				return false;
			return COMPS.dirMap.get(tile) != 0;
		}
	};

	public MAP_INT dirMap() {
		return COMPS.dirMap;
	}

}
