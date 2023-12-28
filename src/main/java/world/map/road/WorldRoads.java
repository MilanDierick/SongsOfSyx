package world.map.road;

import static world.WORLD.*;

import java.io.IOException;

import init.C;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_PLACER;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap1D;
import util.rendering.RenderData.RenderIterator;
import view.world.panel.IDebugPanelWorld;
import world.*;
import world.regions.Region;
import world.regions.data.RD;

public final class WorldRoads extends WORLD.WorldResource implements MAP_BOOLEAN{

	private static final int T_NONE = 0;
	private static final int T_ROAD = 1;
	private static final int T_HARBOUR = 2;
	
	private COLOR[] rColors;
	
	private final Bitsmap1D type = new Bitsmap1D(0, 3, TAREA());
	private final Bitsmap1D roadData = new Bitsmap1D(0, 4, TAREA());
	public final Bitmap2D minified = new Bitmap2D(TBOUNDS(), false);
	
	public WorldRoads() {
		IDebugPanelWorld.add(ROAD, "road");
		IDebugPanelWorld.addClear(ROAD, "roadClear");
		Json j = WConfig.json("Road");
		ColorImp low = new ColorImp(j, "COLOR_SMALL");
		ColorImp hi = new ColorImp(j, "COLOR_BIG");
		rColors = COLOR.interpolate(low, hi, 16);
	}
	
	@Override
	protected void save(FilePutter f) {
		type.save(f);
		roadData.save(f);
		minified.save(f);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		type.load(f);
		roadData.load(f);
		minified.load(f);
	}
	
	@Override
	public void clear() {
		type.setAll(T_NONE);
		roadData.clear();
		minified.clear();
	}
	
	public void render(WRenContext data, RenderIterator it) {
		
		int t = type.get(it.tile());
		if (t == T_NONE)
			return;
		if (t == T_ROAD || t == T_HARBOUR) {
			double d = levelRoad(it.tile());
			if (minified.is(it.tile()))
				d *= 0.25;
			else
				d = 0.25 + 0.75*d;
			d = CLAMP.d(d, 0, 1);
			rColors[(int) (d*15)].bind();
		}
		WORLD.BUILDINGS().sprites.roads.render(data.r,
				roadData.get(it.tile()) + 16 * (it.ran() & 0b0111), it.x(), it.y());
		COLOR.unbind();
	}
	
	private double levelRoad(int tile) {
		Region reg = REGIONS().map.get(tile);
		if (reg != null) {
			return RD.BUILDINGS().levelRoad.get(reg);
		}
		return 0;
	}
	
	public void renderBridge(WRenContext con, RenderIterator it) {
		int t = type.get(it.tile());
		if (t == T_ROAD && WATER().isBig.is(it.tile()) && !minified.is(it.tile())) {
			int data = roadData.get(it.tile());
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR dir = DIR.ORTHO.getC(di);
				if ((data & dir.mask()) != 0 && !WATER().isBig.is(it.tx()+dir.x(), it.ty()+dir.y()) && !WATER().isBig.is(it.tx()-dir.x(), it.ty()-dir.y())){
					int level = (int)(levelRoad(it.tile())*4);
					Region reg = REGIONS().map.get(it.tile());
					if (reg != null) {
						level = reg.info.cx()%3;
					}
					
					WORLD.BUILDINGS().sprites.bridge.render(con.r, level+ di, it.x(), it.y());
					return;
				}
			}
		}
		if (t == T_HARBOUR) {
			int data = roadData.get(it.tile());
			con.s.setDistance2Ground(0).setHeight(1);
			DIR d = pDir(data).perpendicular();
			int x = it.x() - 4*C.SCALE;
			int y = it.y() - 4*C.SCALE;
			int tile = d.orthoID()*16 + (it.ran()&0b11);
			
			
			if (Integer.bitCount(data) != 1) {
				tile += 3*4;
			}else {
				Region r = WORLD.REGIONS().map.get(it.tile());
				if (r == null)
					tile += 2*4;
				
				tile += 3*(1.0-RD.BUILDINGS().levelRoad.get(r));
				
			}
			
			WORLD.BUILDINGS().sprites.harbour.render(con.r, tile, x, y);
			WORLD.BUILDINGS().sprites.harbour.render(con.s, tile, x, y);
		}
		
	}
	
	private DIR pDir(int data) {
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			if ((data & d.mask()) != 0)
				return d;
		}
		return DIR.N;
	}

	@Override
	public boolean is(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return is(tx + ty * TWIDTH());
		return false;
	}

	@Override
	public boolean is(int tile) {
		return type.get(tile) != T_NONE;
	}
	
	void setP(int tile, int t) {
		int tx = tile % TWIDTH();
		int ty = tile / TWIDTH();

		
		type.set(tile, t);
		fix(tx, ty);
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			int dx = d.x() + tx;
			int dy = d.y() + ty;
			if (IN_BOUNDS(dx, dy) && type.get(dx+dy*TWIDTH()) != T_NONE) {
				fix(dx, dy);
			}
		}
	}
	
	private void fix(int tx, int ty) {
		int m = 0;
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			int dx = d.x() + tx;
			int dy = d.y() + ty;
			if (IN_BOUNDS(dx, dy) && type.get(dx+dy*TWIDTH()) != T_NONE) {
				m |= d.mask();
			}
		}
		roadData.set(tx + ty * TWIDTH(), m);
	}
	
	public final MAP_BOOLEAN CONNECTION = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return ROAD.is(tx, ty) || HARBOUR.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return ROAD.is(tile) || HARBOUR.is(tile);
		}
	};
	
	public final ROAD_TYPE ROAD = new ROAD_TYPE(T_ROAD) {

		@Override
		protected boolean placable(int tx, int ty) {
			if (WORLD.MOUNTAIN().coversTile(tx, ty))
				return false;
			if (WORLD.WATER().isBig.is(tx, ty)) {
				return ok(tx, ty, DIR.N) || ok(tx, ty, DIR.E);
			}
			return true;
		}
		
		private boolean ok(int tx, int ty, DIR d) {
			if (WORLD.WATER().isBig.is(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d.perpendicular())) {
				d = d.next(2);
				return !WORLD.WATER().isBig.is(tx, ty, d) && !WORLD.WATER().isBig.is(tx, ty, d.perpendicular());
			}
			return false;
		}
		
	};
	public final ROAD_TYPE HARBOUR = new ROAD_TYPE(T_HARBOUR) {
		@Override
		protected boolean placable(int tx, int ty) {
			if (WORLD.MOUNTAIN().getHeight(tx, ty) > 0)
				return false;
			if (WORLD.WATER().isBig.is(tx, ty)) {
				return ok(tx, ty, DIR.N) || ok(tx, ty, DIR.E);
			}
			return true;
		}
		
		private boolean ok(int tx, int ty, DIR d) {
			return WORLD.WATER().isBig.is(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d.perpendicular());
		}
		

	};
	
	public boolean isGoodForHarbour(int tx, int ty) {
		
		if (!WORLD.WATER().isBig.is(tx, ty))
			return false;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (WORLD.WATER().RIVER.is(tx, ty)) {
				if (WORLD.WATER().RIVER.is(tx, ty, d.next(2)) && WORLD.WATER().RIVER.is(tx, ty, d.next(-2)))
					return true;
			}else {
				if (WORLD.WATER().isBig.is(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d.next(2)) && WORLD.WATER().isBig.is(tx, ty, d.next(-2)) && !WORLD.WATER().isBig.is(tx, ty, d.perpendicular()))
					return true;
			}
		}
		return false;
		
	}
	
	public abstract class ROAD_TYPE implements MAP_PLACER {
		
		private final int t;
		
		ROAD_TYPE(int t){
			this.t = t;
		}
		
		@Override
		public MAP_PLACER set(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				set(tx + ty * TWIDTH());
			return this;
		}

		@Override
		public MAP_PLACER set(int tile) {
			setP(tile, t);
			return this;
		}

		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return is(tx + ty * TWIDTH());
			return false;
		}

		@Override
		public boolean is(int tile) {
			return type.get(tile) == t;
		}

		@Override
		public MAP_PLACER clear(int tile) {
			setP(tile, T_NONE);
			return this;
		}

		@Override
		public MAP_PLACER clear(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				clear(tx + ty * TWIDTH());
			return this;
		}
		
		protected abstract boolean placable(int tx, int ty);
		
		public final MAP_BOOLEAN placable = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				if (!WORLD.IN_BOUNDS(tx, ty))
					return false;
				return placable(tx, ty);
			}
			
			@Override
			public boolean is(int tile) {
				return placable(tile%WORLD.TWIDTH(), tile/WORLD.TWIDTH());
			}
		};
	}
	


	
}
