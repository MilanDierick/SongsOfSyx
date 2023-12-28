package world.regions;

import static world.WORLD.*;

import java.io.IOException;

import game.Profiler;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.*;
import world.WORLD;
import world.WORLD.WorldResource;
import world.regions.centre.WCentre;
import world.regions.map.RegionMap;

public final class WREGIONS extends WorldResource {

	public final static COLOR cNone = new ColorImp(100, 100, 100);
	public final static int MAX = 1023;
	private final ArrayList<Region> areas = new ArrayList<>(MAX);
	private final ArrayList<Region> active = new ArrayList<>(MAX);
	public final RegionMap map = new RegionMap();
	private final Bitmap2D edge = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D besige = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D ctile = new Bitmap2D(WORLD.TBOUNDS(), false);
	public WREGIONS() {
		
		
		for (int i = 0; i < MAX; i++) {
			Region r = new Region(i);
			areas.add(r);
			
		}
		
	}

	public Region getByIndex(int index) {
		return areas.get(index);
	}
	
	public LIST<Region> all(){
		return areas;
	}
	
	@Override
	protected void save(FilePutter file) {
		for (Region a : areas)
			a.save(file);
		map.save(file);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		for (Region a : areas)
			a.load(file);
		map.load(file);
		initAfterGenerated();
	}
	
	@Override
	protected void clear() {
		
		for (Region r : REGIONS().all())
			r.clear();
		map.clear();
		active.clear();
	}
	
	@Override
	protected void update(float ds, Profiler prof) {

	}
	
	void initAfterGenerated() {
		active.clear();
		for (Region r : areas) {
			if (r.info.area() > 0 && map.get(r.cx(), r.cy()) == r) {
				active.add(r);
			}
		}
		edge.clear();
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = map.get(c);
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(c, d) && map.get(c,d) != r) {
					edge.set(c, true);
					break;
				}
			}
		}
		
		besige.clear();
		ctile.clear();
		Rec bb = new Rec(WCentre.TILE_DIM+2, WCentre.TILE_DIM+2);
		for (Region reg : active) {
			bb.moveX1Y1(reg.cx(), reg.cy());
			bb.incr(-Math.ceil(WCentre.TILE_DIM/2.0), -Math.ceil(WCentre.TILE_DIM/2.0));
			for (COORDINATE c : bb) {
				if (WORLD.TBOUNDS().holdsPoint(c) && bb.isOnEdge(c.x(), c.y())) {
					besige.set(c, true);
				}
			}
			
			for (DIR d : DIR.ALLC) {
				ctile.set(reg.cx(), reg.cy(), d, true);
			}
		}
		
		WORLD.FOW().setDirty();
			
	}
	
	@Override
	protected void initAfterGameSetup() {
		
	}
	
	public LIST<Region> active(){
		return active;
	}
	
	public WGenRegions generator() {
		return new WGenRegions();
	}
	
	public MAP_BOOLEAN border() {
		return edge;
	}
	
	public MAP_BOOLEAN centreEdgeTile() {
		return besige;
	}
	
	public MAP_BOOLEAN centreTile() {
		return ctile;
	}
	
}
