package world;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.time.TIME;
import init.C;
import init.RES;
import init.settings.S;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.RGB;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import util.rendering.ShadowBatch;
import world.ai.WorldAI;
import world.army.WArmies;
import world.entity.WEntities;
import world.map.buildings.WorldBuildings;
import world.map.buildings.camp.WorldCamp;
import world.map.landmark.WorldLandmarks;
import world.map.regions.Regions;
import world.map.terrain.*;
import world.overlay.WorldOverlays;

public class World extends GameResource{

	private static Data w;
	public final static double SPEED = 1;//TIME.days().bitSeconds()/60;
	
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
	
	public static Regions REGIONS() {
		return w.areas;
	}
	
	public static WArmies ARMIES() {
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
	
	public static WorldAI ai() {
		return w.ai;
	}
	
	public static WorldCamp camps() {
		return w.buildings.camp;
	}
	
	public static void addTopRender(WorldTopRenderable r) {
		w.top = r;
	}
	
	public static WorldGen GEN() {
		return w.stage;
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
		private final Regions areas;
		private final WArmies armies;
		private final WorldBuildings buildings;
		private final RenderData renData;
		private final WorldLandmarks landmarks;
		private final WorldMinimap minimap;
		private final WorldOverlays overlay;
		private final WorldAI ai;
		private final WorldGen stage;
		private WorldTopRenderable top;
		
		
		private final ShadowBatch.Real shadowBatch = new ShadowBatch.Real();
		private final ShadowBatch shadowDummy = new ShadowBatch.Dummy();
		
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
			
			mountain = new WorldMountain();
			MINABLES = new WorldMinerals();
			climate = new WorldClimate();
			fertility = new WorldFertility();
			water = new WorldWater();
			GROUND = new WorldGround();
			FOREST = new WorldForest(World.this);
			landmarks = new WorldLandmarks();
			areas = new Regions();
			armies = new WArmies();
			buildings = new WorldBuildings();
			ENTITIES = new WEntities(World.this);
			ai = new WorldAI();
			renData = new RenderData(tWidth, tHeight);
			minimap = new WorldMinimap();
			overlay = new WorldOverlays();
			stage = new WorldGen(World.this);
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
		}
		
		public void render(Renderer r, float ds, int zoomout,
				RECTANGLE renWindow, int offX, int offY) {

			ds *= GAME.SPEED.speedTarget();

			renData.init(renWindow, offX, offY);
			
			ShadowBatch s = shadowDummy;
			if (S.get().shadows.get() > 0){
				shadowBatch.init(zoomout, TIME.light().shadow.sx(), TIME.light().shadow.sy());
				s = shadowBatch;
			}

			double seasonValue = 0;
			{
				double am = 0;
				RenderIterator it = renData.onScreenTiles();
				while(it.has()) {
					am++;
					seasonValue += climate.getter.get(it.tile()).seasonChange;
					it.next();
				}
				seasonValue/=am;
			}
			
			if (top != null) {
				r.newLayer(false, zoomout);
				TIME.light().applyGuiLight(ds, offX, offX + renWindow.width(), offY,
						offY + renWindow.height());
				top.render(r, s, renData);
				top = null;
			}
			
			r.newLayer(false, zoomout);
			TIME.light().applyGuiLight(ds, offX, offX + renWindow.width(), offY,
					offY + renWindow.height());
			overlay.render(r, s, renData, zoomout);
			
			r.newFinalLightWithShadows(zoomout);
			TIME.light().apply(offX, offX + renWindow.width(), offY,
					offY + renWindow.height(), RGB.WHITE);
			
			
			ENTITIES.renderAboveTerrain(r, s, ds, renWindow, offX, offY);
			if (zoomout <= 1)
				BUILDINGS().renderAboveTerrain(r, s, renData);
			r.newLayer(false, zoomout);
			
			MINABLES.render(r, renData, seasonValue);
			r.newLayer(false, zoomout);
			
			water.render(r, renData, seasonValue);
			r.newLayer(false, zoomout);
			
			
			FOREST.render(r, s, renData);
			r.newLayer(false, zoomout);
			
			mountain.render(r, s, renData);
			r.newLayer(false, zoomout);

			water.renderMid(r, renData, seasonValue);
			r.newLayer(false, zoomout);
			
			ENTITIES.renderBelowTerrain(r, s, ds, renWindow, offX, offY);
			r.newLayer(false, zoomout);
			
			buildings.renderAbove(r, s, renData);
			r.newLayer(false, zoomout);
			
			GROUND.render(r, renData, seasonValue);
			buildings.renderAboveGround(r, s, renData);
			water.renderShorelines(r, renData, seasonValue);
			
			REGIONS().renderBorders(r, s, renData, zoomout);
			
			r.newLayer(false, zoomout);
			
			CORE.getSoundCore().set(renWindow.cX()+offX, renWindow.cY()+offY);
		}
	}
	
	public World(int tileSizeX, int tileSizeY) throws IOException {
		super(false);
		new Data(tileSizeX, tileSizeY);
	}

	static void clear() {
		for (WorldResource r : w.resources) {
			r.clear();
		}
	}
	
//	public void generate(GameConRandom random) {
//		RND.setSeed(RND.rInt(999999999));
//		w.generate(random);
//	}
//	
//	public void regenerate() {
//		w.generate(w.random);
//	}
//	
//	public void generateInit() {
//		w.initGenerated();
//	}
//	
//	public static GameConRandom conRandom() {
//		return w.random;
//	}

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
	protected void update(float ds) {
		
		for (WorldResource r : w.resources) {
			r.update(ds);
		}
		w.minimap.update(ds);
	}
	
	@Override
	protected void afterTick() {
		for (WorldResource r : w.resources) {
			r.afterTick();
		}
		w.overlay.clear();
//		if (RegionPathing.paths > 0)


	};

	public void render(Renderer r, float ds, int zoomout,
			RECTANGLE renWindow, int offX, int offY) {
		w.render(r, ds, zoomout, renWindow, offX, offY);
	}

	public static abstract class WorldResource {

		

		protected WorldResource() {
			w.resources.add(this);
		}

		protected abstract void save(FilePutter file);

		protected abstract void load(FileGetter file) throws IOException;

		protected void clear() {
			
		}
		
		protected void update(float ds) {

		}
		
		protected void afterTick() {
			
		}
	}
	
	public interface WorldTopRenderable {
		
		public abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data);
		
	}



}
