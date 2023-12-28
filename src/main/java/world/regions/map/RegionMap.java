package world.regions.map;

import static world.WORLD.*;

import java.io.IOException;

import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.Bitsmap1D;
import util.rendering.RenderData.RenderIterator;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.centre.WCentre;

public final class RegionMap implements MAP_OBJECT<Region>, SAVABLE {


	private final Bitsmap1D mapID = new Bitsmap1D(0, Integer.numberOfTrailingZeros(WREGIONS.MAX+1), TAREA());
	
	public RegionMap() {
		
	}


	@Override
	public Region get(int tile) {
		if (mapID.get(tile) == 0)
			return null;
		return WORLD.REGIONS().getByIndex(mapID.get(tile)-1);
	}
	
	@Override
	public Region get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH());
		return null;
	}
	
	void set(int tile, Region object) {
		if (object == null)
			mapID.set(tile, 0);
		else
			mapID.set(tile, object.index()+1);
	}

	void set(int tx, int ty, Region object) {
		if (IN_BOUNDS(tx, ty)) {
			set(tx+ty*TWIDTH(), object);
		}
	}
	
	public MAP_BOOLEAN isCentre = new MAP_BOOLEAN() {
		
		final int min = WCentre.TILE_DIM/2;
		final int max = WCentre.TILE_DIM/2;
		@Override
		public boolean is(int tx, int ty) {
			Region r = get(tx, ty);
			if (r != null) {
				int dx = tx-r.info.cx();
				int dy = ty-r.info.cy();
				return dx >= -min && dx <= max && dy >= -min && dy <= max;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile % WORLD.TWIDTH(), tile/WORLD.TWIDTH());
		}
	};
	
	public MAP_OBJECT<Region> cTile = new MAP_OBJECT<Region>() {

		@Override
		public Region get(int tile) {
			int tx = tile%WORLD.TWIDTH();
			int ty = tile/WORLD.TWIDTH();
			return get(tx, ty);
		}

		@Override
		public Region get(int tx, int ty) {
			Region r = RegionMap.this.get(tx, ty);
			if (r != null && r.cx() == tx && r.cy() == ty)
				return r;
			return null;
		}
	
	
	};
	
	public MAP_OBJECT<Region> centre = new MAP_OBJECT<Region>() {

		@Override
		public Region get(int tile) {
			int tx = tile%WORLD.TWIDTH();
			int ty = tile/WORLD.TWIDTH();
			return get(tx, ty);
		}

		@Override
		public Region get(int tx, int ty) {
			
			final int min = WCentre.TILE_DIM/2;
			final int max = WCentre.TILE_DIM-min;
			
			for (int dy = -min; dy < max; dy++) {
				for (int dx = -min; dx < max; dx++) {
					Region r = RegionMap.this.get(tx+dx, ty+dy);
					if (r != null && r.cx() == tx+dx && r.cy() == ty+dy)
						return r;
				}
			}
			return null;
		}
	
	
	};

	@Override
	public void save(FilePutter file) {
		mapID.save(file);
		for (Region r : WORLD.REGIONS().all())
			r.info.save(file);
	}


	@Override
	public void load(FileGetter file) throws IOException {
		mapID.load(file);
		for (Region r : WORLD.REGIONS().all())
			r.info.load(file);
	}
	
	@Override
	public void clear() {
		
		mapID.clear();
		for (Region r : WORLD.REGIONS().all())
			r.info.clear();
		
	}
	
	public final void renderBorders(Renderer r, RenderIterator it) {
		
		if (!WORLD.REGIONS().border().is(it.tile()))
			return;
		

		Region a = WORLD.REGIONS().map.get(it.tile());
		if (a != null) {
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				int ii = it.tile()+d.x()+d.y()*TWIDTH();
				Region r2 = WORLD.REGIONS().map.get(ii);
				if (!IN_BOUNDS(it.tx(), it.ty(), d) || a == r2) {
					m |= d.mask();
					
				}
			}
			if (m != 0x0F) {
				if (a.faction() == null)
					COLOR.WHITE35.bind();
				else
					a.faction().banner().colorBG().bind();
				OPACITY.O50.bind();
				SPRITES.cons().BIG.outline_dashed_small.render(r, m, it.x(), it.y());
					
			}
			OPACITY.unbind();
		}
		
		
	}
	
}
