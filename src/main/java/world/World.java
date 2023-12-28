package world;

import java.io.IOException;

import game.GAME.GameResource;
import game.Profiler;
import init.C;
import init.RES;
import snake2d.Renderer;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import world.army.WARMIES;
import world.battle.WBattles;
import world.entity.WEntities;
import world.fow.FOW;
import world.log.WorldLog;
import world.map.buildings.WorldBuildings;
import world.map.buildings.camp.WorldCamp;
import world.map.landmark.WorldLandmarks;
import world.map.pathing.WPATHING;
import world.map.road.WorldRoads;
import world.map.terrain.*;
import world.overlay.WorldOverlays;
import world.regions.WREGIONS;
import world.regions.centre.WCentre;
import world.regions.data.RD;

public class WORLD extends GameResource{

	private static Data w;
	
	public static int TWIDTH() {
		return w.tWidth;
	}
	
	public static int THEIGHT() {
		return w.tHeight;
	}
	
	public static RECTANGLE TBOUNDS() {
		return w.tDim;
	}
	
	public static RECTANGLE PIXELS() {
		return w.dim;
	}
	
	public static int PWIDTH() {
		return w.width;
	}
	
	public static int PHEIGHT() {
		return w.height;
	}
	
	public static int TAREA() {
		return w.tHeight*w.tWidth;
	}
	
	public static boolean IN_BOUNDS(int tx, int ty) {
		return w.tDim.holdsPoint(tx, ty);
	}
	
	public static boolean IN_BOUNDS(COORDINATE c, DIR d) {
		return IN_BOUNDS(c.x()+d.x(), c.y()+d.y());
	}
	
	public static boolean IN_BOUNDS(int tx, int ty, DIR d) {
		return IN_BOUNDS(tx+d.x(), ty+d.y());
	}
	
	public static WorldMountain MOUNTAIN() {
		return w.mountain;
	}
	
	public static WorldWater WATER() {
		return w.water;
	}
	
	public static WorldGround GROUND() {
		return w.GROUND;
	}
	
	public static WorldForest FOREST() {
		return w.FOREST;
	}
	
	public static WEntities ENTITIES() {
		return w.ENTITIES;
	}
	
	public static WorldClimate CLIMATE() {
		return w.climate;
	}
	
	public static WorldFertility FERTILITY() {
		return w.fertility;
	}
	
	public static WREGIONS REGIONS() {
		return w.areas;
	}
	
	public static WARMIES ARMIES() {
		return w.armies;
	}
	
	public static WorldMinerals MINERALS() {
		return w.MINABLES;
	}
	
	public static WorldBuildings BUILDINGS() {
		return w.buildings;
	}
	
	public static WorldLandmarks LANDMARKS() {
		return w.landmarks;
	}
	
	public static WorldMinimap MINIMAP() {
		return w.minimap;
	}
	
	public static WorldOverlays OVERLAY() {
		return w.overlay;
	}
	
	public static Sprites sprites() {
		return w.sprites;
	}
	
	public static WorldCamp camps() {
		return w.buildings.camp;
	}
	
	public static WorldGen GEN() {
		return w.stage;
	}
	
	public static WorldRoads ROADS() {
		return w.roads;
	}
	
	public static WPATHING PATH() {
		return w.pathing;
	}
	
	public static WCentre CENTRE() {
		return w.centre;
	}
	
	public static FOW FOW() {
		return w.fow;
	}
	
	public static WorldLog LOG() {
		return w.log;
	}
	
	public static WBattles BATTLES() {
		return w.battles;
	}
	
	private final class Data {
		
		private final ArrayList<WorldResource> resources = new ArrayList<WorldResource>(100);
		
		private final RECTANGLE dim;
		private final RECTANGLE tDim;
		private final int tHeight;
		private final int tWidth;
		private final int height;
		private final int width;

		private final Sprites sprites;
		private final WorldWater water;
		private final WorldGround GROUND;
		private final WorldForest FOREST;
		private final WEntities ENTITIES;
		private final WorldMountain mountain;
		private final WorldMinerals MINABLES;
		private final WorldClimate climate;
		private final WorldFertility fertility;
		private final WorldRoads roads;
		private final WREGIONS areas;
		private final WARMIES armies;
		private final WorldBuildings buildings;
		private final WorldLandmarks landmarks;
		private final WorldMinimap minimap;
		private final WorldOverlays overlay;
		private final WPATHING pathing;
		private final WorldGen stage;
		private final WCentre centre;
		private final Render render;
		private final FOW fow;
		private final WorldLog log;
		private final WBattles battles;
		
		private Data(int tileSizeX, int tileSizeY) throws IOException{
			w = this;
			tWidth = tileSizeX;
			tHeight = tileSizeY;

			if (tWidth > 512 || tHeight > 512)
				throw new RuntimeException("too big a map!");

			width = tWidth * C.TILE_SIZE;
			height = tHeight * C.TILE_SIZE;
			tDim = new Rec(0, tWidth, 0, tHeight);
			dim = new Rec(0, width, 0, height);
			
			sprites = new Sprites();
			render = new Render(tileSizeX, tileSizeY);
			
			
			mountain = new WorldMountain();
			MINABLES = new WorldMinerals();
			climate = new WorldClimate();
			fertility = new WorldFertility();
			roads = new WorldRoads();
			water = new WorldWater();
			GROUND = new WorldGround();
			FOREST = new WorldForest(WORLD.this);
			landmarks = new WorldLandmarks();
			areas = new WREGIONS();
			centre = new WCentre();
			new RD(null);
			armies = new WARMIES();
			buildings = new WorldBuildings();
			pathing = new WPATHING();
			ENTITIES = new WEntities(WORLD.this);
			fow = new FOW();
			minimap = new WorldMinimap();
			log = new WorldLog();
			overlay = new WorldOverlays();
			stage = new WorldGen(WORLD.this);
			battles = new WBattles();
		}
	
		protected void save(FilePutter file) {
			
			for (int i = 0; i < resources.size(); i++) {
				WorldResource r = resources.get(i);
				file.mark(r);
				RES.loader().print("Saving world: " + i + "/" + resources.size());
				r.save(file);
				file.mark(r);
			}
		}
		
		protected void load(FileGetter file) throws IOException {
			for (int i = 0; i < resources.size(); i++) {
				WorldResource r = resources.get(i);
				r.clear();
			}
			for (int i = 0; i < resources.size(); i++) {
				WorldResource r = resources.get(i);
				file.check(r);
				RES.loader().print("Loading world: " + i + "/" + resources.size());
				r.load(file);
				file.check(r);
			}
			for (int i = 0; i < resources.size(); i++) {
				WorldResource r = resources.get(i);
				r.initBeforePlay();
				w.minimap.setDirty();
			}
		}
	}
	
	public WORLD(int tileSizeX, int tileSizeY) throws IOException {
		super(false);
		new Data(tileSizeX, tileSizeY);
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		w.save(saveFile);
		w.stage.save(saveFile);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		w.load(file);
		w.stage.load(file);
	}
	
	@Override
	protected void update(float ds, Profiler prof) {
		prof.logStart(WORLD.class);
		for (WorldResource r : w.resources) {
			r.update(ds, prof);
		}
		w.minimap.update();
		prof.logEnd(WORLD.class);
	}
	
	@Override
	protected void afterTick() {
		w.battles.poll();
		

	};
	
	@Override
	protected void initAfterGameIsSetUp() {
		for (WorldResource r : w.resources)
			r.initAfterGameSetup();
	}
	
	public static void initBeforePlay() {
		for (int i = 0; i < w.resources.size(); i++) {
			WorldResource r = w.resources.get(i);
			r.initBeforePlay();
		}
	}
	
	public void render(Renderer r, float ds, int zoomout,
			RECTANGLE renWindow, int offX, int offY) {
		w.render.render(r, ds, zoomout, renWindow, offX, offY);
	}

	public static abstract class WorldResource {

		

		protected WorldResource() {
			w.resources.add(this);
		}

		protected abstract void save(FilePutter file);

		protected abstract void load(FileGetter file) throws IOException;


		
		protected void clear() {
			
		}
		
		protected void update(float ds, Profiler prof) {

		}
		
		
		protected void initAfterGameSetup() {
			
		}
		
		protected void initBeforePlay() {
			
		}
	}



}
