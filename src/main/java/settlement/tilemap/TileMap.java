package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.Profiler;
import init.RES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.path.AvailabilityListener;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.floor.*;
import settlement.tilemap.generator.Generator;
import settlement.tilemap.generator.GeneratorTests;
import settlement.tilemap.growth.Fertility;
import settlement.tilemap.growth.TGrowth;
import settlement.tilemap.terrain.Terrain;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public class TileMap extends SETT.SettResource{
	

	{Resource.resources.clear();}
	public final Ground ground;
	public final Floors floors;
	public final Grass grass;
	public final Fertility fertility;
	public final Terrain topology;
	public final Snow snow;
	public final TGrowth growth;
	private final TerrainHotspots hotspots = new TerrainHotspots();
	private final SRenderer renderer = new SRenderer(this);
	
	public TileMap() throws IOException{
		
		ground = new Ground(this);
		grass = new Grass();
		fertility = new Fertility(this);
		floors = new Floors(this);
		topology = new Terrain(this);
		growth = new TGrowth(topology);
		snow = new Snow(this);

		new GeneratorTests();
	}
	
	@Override
	protected void generate(CapitolArea area) {
		minimap.clear();
		SETT.MINIMAP().setOpen(false);
		for (Resource r : Resource.resources)
			r.clearAll();
		new Generator(area);
		SETT.MINIMAP().setOpen(true);
		minimap.clear();
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException{
		for (Resource r : Resource.resources) {
			saveFile.check(r);
			RES.loader().print(r.toString());
			r.load(saveFile);
		}
		AvailabilityListener.listenAll(false);
		for (int y = 0; y < THEIGHT; y++) {
			for (int x = 0; x < TWIDTH; x++) {
				
				PATH().availability.updateAvailability(x, y);
				
			}
		}
		AvailabilityListener.listenAll(true);
		minimap.clear();
		Generator.paintMinimap();
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		for (Resource r : Resource.resources) {
			saveFile.mark(r);
			RES.loader().print(r.toString());
			r.save(saveFile);
		}
	}
	
	@Override
	protected void update(float ds, Profiler profiler){
		for (Resource r : Resource.resources)
			r.update(ds);
	}
	
	@Override
	protected void init(boolean loaded) {
		hotspots.init();
		minimap.clear();
	}
	
	public void renderAboveEnts(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData){
		renderer.renderAboveEnts(r, s, ds, zoomout, renData);
	}
	
	public void renderTheRest(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData, RECTANGLE renWindow, int offX, int offY){
		renderer.renderTheRest(r, s, ds, zoomout, renData, renWindow, offX, offY);
	}
	
	public void renderSemiMap(Renderer r, float ds, RenderData renData) {
		renderer.renderSemiMap(r, ds, renData);
	}
	
	public void renderMiniMap(Renderer r, float ds, RenderData renData, int zoomout) {
		renderer.renderMiniMap(r, ds, renData, zoomout);
	}
	
	

	
	
	@Override
	protected void afterTick() {
		for (Resource r : Resource.resources)
			r.afterTick();
		minimap.update();
	}
	
	public TerrainHotspots hotspots() {
		return hotspots;
	}
	
	@Override
	protected void updateTileDay(int tx, int ty, int tile) {
		
		growth.updateTileDay(tx, ty, tile);
	}
	
	public final MinimapColorGetter minimap = new MinimapColorGetter();
	
	public COLOR miniC(int tx, int ty) {
		return minimap.get(tx, ty);
	}
	
	public void miniCUpdate(int tx, int ty) {
		minimap.update(tx, ty);
	}
	
	public static abstract class Resource{
		
		private static final ArrayList<Resource> resources = new ArrayList<Resource>(10);
		@SuppressWarnings("unused")
		private final int index;
		
		protected Resource() {
			index = resources.add(this);
		}
		
		protected abstract void save(FilePutter saveFile);
		protected abstract void load(FileGetter saveFile) throws IOException;
		
		
		protected void update(float ds) {
			
		}
		
		protected void afterTick() {
			
		}
		
//		protected COLOR miniColor(int tx, int ty) {
//			return null;
//		}
//		
//		protected COLOR miniColorPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
//			return origional;
//		}
		
		protected abstract void clearAll();
		
//		protected final void updateMiniMap(int x, int y){
//			TILE_MAP().minimap.update(x, y);
////			for (int i = Resource.resources.size()-1; i >= 0; i--){
////				COLOR c = Resource.resources.get(i).miniColor(x, y);
////				if (c != null){
////					if (i <= index) {
////						SETT.MINIMAP().putPixel(x, y, c);
////					}
////					return;
////				}
////			}
//		}
	}

	public LOS LOS(int tx, int ty) {
		return topology.get(tx, ty).los(tx, ty);
	}

	public interface SMinimapGetter {
		
		public COLOR miniC(int x, int y);
		
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern);
		
	}
	
	
}
