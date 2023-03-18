package world.map.regions;

import static world.World.*;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.sets.*;
import util.rendering.ShadowBatch;
import world.World;
import world.World.WorldResource;

public final class Regions extends WorldResource {

	public final static COLOR cNone = new ColorImp(100, 100, 100);
	public final static int MAX = 1023;
	final static int none = 0;
	private final ArrayList<Region> areas = new ArrayList<>(MAX);
	private final Bitsmap1D mapID = new Bitsmap1D(none, Integer.numberOfTrailingZeros(MAX+1), TAREA());
	private final RegionUpdater updater = new RegionUpdater(this);
	private final ArrayList<FRegions> realms = new ArrayList<>(FACTIONS.MAX);
	boolean ownershipHasChanged = false;
	public final AIOutputter outputter;
	
	private int besigeUpdateI = 0;
	
	public Regions() {
		
		RegionInit init = new RegionInit();
		
		REGIOND.init(init);
		outputter = new AIOutputter();
		
		for (int i = 0; i < MAX; i++)
			areas.add(new Region(i, init.count.intCount()));
		for (int i = 0; i < FACTIONS.MAX; i++) {
			realms.add(new FRegions(init, i));
		}
	}

	public Region getByIndex(int index) {
		return areas.get(index);
	}
	
	public LIST<Region> all(){
		return areas;
	}
	
	public boolean ownershipHasChanged() {
		return ownershipHasChanged;
	}
	
	public MAP_OBJECTE<Region> setter = new MAP_OBJECTE<Region>() {

		@Override
		public Region get(int tile) {
			if (mapID.get(tile) == none)
				return null;
			return areas.get(mapID.get(tile)-1);
		}

		@Override
		public Region get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return null;
		}

		@Override
		public void set(int tile, Region object) {
			if (object == null)
				mapID.set(tile, none);
			else
				mapID.set(tile, object.index()+1);
		}

		@Override
		public void set(int tx, int ty, Region object) {
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH(), object);
			}
		}
	};
	
	public final MAP_OBJECT<Region> getter = setter;
	
	public final MAP_BOOLEAN haser = setter;
	
	public MAP_BOOLEAN placable = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tile) {
			int x = tile%TWIDTH();
			int y = tile/TWIDTH();
			return is(x, y);
		}

		@Override
		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && !WATER().coversTile.is(tx, ty) && MOUNTAIN().heighter.get(tx, ty) <= 1;
		}
	};
	
	public MAP_BOOLEAN isCentre = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			Region r = getter.get(tx, ty);
			if (r != null) {
				int dx = tx-r.cx();
				int dy = ty-r.cy();
				return dx >= -1 && dx < 3 && dy >= -1 && dy < 3;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile % World.TWIDTH(), tile/World.TWIDTH());
		}
	};
	
	

	@Override
	protected void save(FilePutter file) {
		for (Region a : areas)
			a.save(file);
		mapID.save(file);
		updater.save(file);
		REGIOND.saver().save(file);
		for (FRegions r : realms)
			r.saver.save(file);
		file.i(besigeUpdateI);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		for (Region a : areas)
			a.load(file);
		mapID.load(file);
		updater.load(file);
		REGIOND.saver().load(file);
		for (FRegions r : realms)
			r.saver.load(file);
		besigeUpdateI = file.i();
	}
	
	@Override
	public void clear() {
		
		REGIOND.saver().clear();
		for (Region r : REGIONS().all())
			r.clear();
		for (FRegions r : realms)
			r.saver.clear();
		mapID.clear();
		updater.clear();
		
	}
	
//	public void generate(int px, int py) {
//		super.clear();
//		RES.loader().print("building history...");
//		new Generator(px, py);
//	}
//	
//	public void makePlayerRoads() {
//		GeneratorRoad.makePlayer();
//	}
//	
//	public void generateRoad() {
//		new GeneratorRoad();
//	}
	
	@Override
	protected void update(float ds) {
		
		updater.update(ds);
		
	}
	
	@Override
	protected void afterTick() {
		ownershipHasChanged = false;
		besigeUpdateI %= areas.size();
		Region r = getByIndex(besigeUpdateI);
		if (r.besieged != (GAME.updateI() & 0x07FFF))
			r.besieged = -1;
	}
	
	public LIST<FRegions> realms(){
		return realms;
	}
	
	public FRegions realm(int i) {
		return realms.get(i);
	}
	
	public FRegions realm(Faction f) {
		return realms.get(f.index());
	}
	
	
	public void renderBorders(Renderer r, ShadowBatch s, RenderData data, int zoomout) {
		RenderIterator it = data.onScreenTiles(0,0,0,0);
		
		OPACITY.O50.bind();
		while(it.has()) {
			int m = 0;
			Region a = World.REGIONS().getter.get(it.tile());

			
			if (a != null) {
				boolean large = a.faction() == null;
				
				
				for (DIR d : DIR.ORTHO) {
					int ii = it.tile()+d.x()+d.y()*TWIDTH();
					Region r2 = World.REGIONS().getter.get(ii);
					if (!IN_BOUNDS(it.tx(), it.ty(), d) || a == r2) {
						m |= d.mask();
						
					}else {
						large |= r2 == null || r2.faction() != a.faction();
					}
				}
				if (m != 0x0F) {
					if (REGIOND.REALM(a) == null)
						COLOR.WHITE35.bind();
					else
						REGIOND.REALM(a).faction().banner().colorBG().bind();
					if (large)
						SPRITES.cons().BIG.outline_dashed.render(r, m, it.x(), it.y());
					else
						SPRITES.cons().BIG.outline_dashed_small.render(r, m, it.x(), it.y());
						
				}
			}

			it.next();
		}
		COLOR.unbind();
		OPACITY.unbind();
		
	}
	
	
}
