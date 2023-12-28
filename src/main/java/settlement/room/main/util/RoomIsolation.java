package settlement.room.main.util;

import java.io.IOException;

import init.D;
import init.RES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.path.AvailabilityListener;
import settlement.room.main.*;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.MATH;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListIntegerResize;
import snake2d.util.sets.Bitmap1D;
import util.dic.DicMisc;
import util.info.INFO;

public final class RoomIsolation {

	private final Bitmap1D hasChanged = new Bitmap1D(SETT.TAREA, false);
	private final ArrayListIntegerResize changed = new ArrayListIntegerResize(ROOMS.ROOM_MAX, ROOMS.ROOM_MAX*8);
	private final Rec rec = new Rec();
	public final INFO info;
	private final RoomAreaWrapper wrap = new RoomAreaWrapper();
	
	public RoomIsolation(ROOMS r) {
		D.gInit(this);
		info = new INFO(DicMisc.¤¤Isolation, D.g("desc", "Isolation prevents room degradation and sound pollution. Surrounding walls increase isolation, while doors and gaps decrease it. Poorly isolated rooms need more maintenance. Poorly isolated homes degrade furniture faster."));
		new AvailabilityListener() {
			
			@Override
			protected void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
				setChanged(tx, ty, a, old);
			}
		};
	}
	
	private void setChanged(int tx, int ty, AVAILABILITY a, AVAILABILITY old) {
		if (a.player < 0 != old.player < 0) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				Room r = SETT.ROOMS().map.get(tx, ty, d);
				setChanged(r, tx+d.x(), ty+d.y());
			}
		}
	}
	
	private void setChanged(Room r, int x, int y) {
		if (r != null && r.constructor() != null && r.constructor().needsIsolation()) {
			int t = x + y*SETT.TWIDTH;
			if (hasChanged.get(t))
				return;
			hasChanged.set(t, true);
			changed.add(t);
		}
	}
	
	public void update() {
		for (int i = 0; i < changed.size(); i++) {
			int t = changed.get(i);
			hasChanged.set(t, false);
			int tx = t %SETT.TWIDTH;
			int ty = t /SETT.TWIDTH;
			
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r.constructor() != null && r.constructor().needsIsolation())
				update(tx, ty, r);
		}
		changed.clear();
	}
	
	public double getProspect(RoomBlueprint blue, AREA r, MAP_BOOLEAN isWall) {
		
		this.isWall = isWall;
		
		double unwalled = 0;
		double total = 0;
		rec.set(r.body());
		RES.marker().init(this);
		
		for (COORDINATE c : rec) {
			
			if (!r.is(c) || !isEdge(r, c))
				continue;
			
			total++;
			for (DIR d : DIR.ALL) {
				
				if (!SETT.IN_BOUNDS(c, d)) {
					unwalled++;
					continue;
				}
				if (r.is(c, d))
					continue;
				
				if (!wall.is(c, d) && !RES.marker().is(c, d)) {
					if (blue ==  SETT.ROOMS().HOMES.HOME) {
						if (blue != SETT.ROOMS().map.blueprintImp.get(c, d)) {
							RES.marker().set(c, d, true);
							unwalled ++;
						}
					}else {
						RES.marker().set(c, d, true);
						unwalled ++;
					}
					break;
				}
			}
		}
		
		RES.filler().done();
		
		int bonus = (int) Math.ceil(total/10.0);
		double v = total-unwalled + bonus;
		v /= total;
		v = CLAMP.d(v, 0, 1);
		v = MATH.pow15.pow(v);
		return v;
		
	}
	
	private MAP_BOOLEAN isWall;
	
	private final MAP_BOOLEAN wall = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}

		@Override
		public boolean is(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			if (isWall != null && isWall.is(tx,ty))
				return true;
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			return t.clearing().isStructure() && t.getAvailability(tx, ty) != null && t.getAvailability(tx, ty).player < 0;
		}
		
		
	};
	
	private void update(int rx, int ry, Room r) {
		if (r == null || r.constructor() == null || !r.constructor().mustBeIndoors()) {
			return;
		}
		r.isolationSet(rx, ry, getProspect(r.blueprint(), wrap.init(r, rx, ry), null));
		wrap.done();
	}
	
	private boolean isEdge(AREA r, COORDINATE c) {
		for (DIR d : DIR.ALL)
			if (!r.is(c, d))
				return true;
		return false;
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			
		}
		
		@Override
		public void clear() {
			
		}
	};
	
}
