package world.map.regions;

import static world.World.*;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.sets.*;
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
	
	public void generate() {
		REGIOND.saver().clear();
		for (FRegions r : realms)
			r.saver.clear();
		RES.loader().print("building history...");
		new Generator();
	}
	
	public void makePlayerRoads() {
		GeneratorRoad.makePlayer();
	}
	
	public void generateRoad() {
		new GeneratorRoad();
	}
	
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
	

	
	
}
