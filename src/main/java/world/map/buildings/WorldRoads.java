package world.map.buildings;

import static world.World.*;

import java.io.IOException;

import settlement.main.RenderData.RenderIterator;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_PLACER;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap1D;
import util.rendering.ShadowBatch;
import view.world.IDebugPanelWorld;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;

public final class WorldRoads extends World.WorldResource implements MAP_PLACER{

	private final Bitmap1D road = new Bitmap1D(TAREA(), false);
	private final Bitmap1D roadMini = new Bitmap1D(TAREA(), false);
	private final Bitsmap1D roadData = new Bitsmap1D(0, 4, TAREA());
	
	WorldRoads() {
		IDebugPanelWorld.add(this, "road");
		IDebugPanelWorld.addClear(this, "roadClear");
	}
	
	@Override
	protected void save(FilePutter f) {
		road.save(f);
		roadData.save(f);
		roadMini.save(f);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		road.load(f);
		roadData.load(f);
		roadMini.load(f);
	}
	
	void render(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
		if (road.get(it.tile())) {
			int set = 16 * 4;
			Region reg = REGIONS().setter.get(it.tile());
			if (reg != null) {
				double v = REGIOND.POP().popValue(reg);
				if (roadMini.get(it.tile())) {
					if (v > 0.7) {
						World.BUILDINGS().sprites.roads.render(r,
								roadData.get(it.tile()) + set + 16 * (it.ran() & 0b011), it.x(), it.y());
					} else if (v > 0.3) {
						World.BUILDINGS().sprites.roads.render(r, roadData.get(it.tile()) + 16 * (it.ran() & 0b011),
								it.x(), it.y());
					}
				} else {
					int k = (int) ((v + 0.3) * 2);
					World.BUILDINGS().sprites.roads.render(r,
							roadData.get(it.tile()) + k * set + 16 * (it.ran() & 0b011), it.x(), it.y());
				}

			} else if (!roadMini.get(it.tile()))
				World.BUILDINGS().sprites.roads.render(r, roadData.get(it.tile()) + 16 * (it.ran() & 0b011), it.x(),
						it.y());
		}
		
	}
	
	@Override
	public MAP_PLACER set(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			set(tx + ty * TWIDTH());
		return this;
	}

	@Override
	public MAP_PLACER set(int tile) {
		int tx = tile % TWIDTH();
		int ty = tile / TWIDTH();
		road.set(tile, true);
		roadMini.set(tile, false);
		fix(tx, ty);
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			int dx = d.x() + tx;
			int dy = d.y() + ty;
			if (is(dx, dy)) {
				fix(dx, dy);
			}
		}
		return this;
	}

	private void fix(int tx, int ty) {
		int m = 0;
		for (DIR d : DIR.ORTHO) {
			int dx = d.x() + tx;
			int dy = d.y() + ty;
			if (is(dx, dy)) {
				m |= d.mask();
			}
		}
		roadData.set(tx + ty * TWIDTH(), m);
	}

	@Override
	public boolean is(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return is(tx + ty * TWIDTH());
		return false;
	}

	@Override
	public boolean is(int tile) {
		return road.get(tile);
	}

	@Override
	public MAP_PLACER clear(int tile) {
		int tx = tile % TWIDTH();
		int ty = tile / TWIDTH();
		road.set(tile, false);
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			int dx = d.x() + tx;
			int dy = d.y() + ty;
			if (is(dx, dy)) {
				fix(dx, dy);
			}
		}
		return this;
	}

	@Override
	public MAP_PLACER clear(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			clear(tx + ty * TWIDTH());
		return this;
	}

	public final MAP_PLACER MINIFIER = new MAP_PLACER() {

		@Override
		public MAP_PLACER set(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				set(tx + ty * TWIDTH());
			return this;
		}

		@Override
		public MAP_PLACER set(int tile) {
			roadMini.set(tile, true);
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
			return roadMini.get(tile);
		}

		@Override
		public MAP_PLACER clear(int tile) {
			roadMini.set(tile, false);
			return this;
		}

		@Override
		public MAP_PLACER clear(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				clear(tx + ty * TWIDTH());
			return this;
		}
	};
	
}
