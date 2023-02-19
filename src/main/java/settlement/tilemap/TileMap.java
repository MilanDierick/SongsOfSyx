package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.C;
import init.RES;
import init.sprite.SPRITES;
import settlement.main.*;
import settlement.main.RenderData.RenderIterator;
import settlement.path.AvailabilityListener;
import settlement.room.main.Room;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.TextureHolder.TextureHolderChunk;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import snake2d.util.sprite.TextureCoords;
import util.rendering.Minimap;
import util.rendering.ShadowBatch;

public class TileMap extends SETT.SettResource{
	

	{Resource.resources.clear();}
	public final Ground ground;
	public final Floors floors;
	public final Grass grass;
	public final Fertility fertility;
	public final Terrain topology;
	public final Snow snow;
	public final SettBorder borders = new SettBorder();
	public final TGrowth growth;
	private final TerrainHotspots hotspots = new TerrainHotspots();
	
	public TileMap() throws IOException{
		
		ground = new Ground(this);
		grass = new Grass();
		fertility = new Fertility();
		floors = new Floors();
		topology = new Terrain();
		growth = new TGrowth(topology);
		snow = new Snow();

		new GeneratorTests();
	}
	
	@Override
	protected void generate(CapitolArea area) {
		minimap.clear();
		SETT.MINIMAP().setOpen(false);

			for (Resource r : Resource.resources)
				r.clearAll();
			fertility.clear();
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
		fertility.load(saveFile);
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
		fertility.save(saveFile);
	}
	
	@Override
	protected void update(float ds){
		for (Resource r : Resource.resources)
			r.update(ds);
	}
	
	@Override
	protected void init(boolean loaded) {
		hotspots.init();
		minimap.clear();
	}
	
	public void renderAboveEnts(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData){
		r.newLayer(false, zoomout);
		SETT.WEATHER().apply(renData.absBounds());		
		topology.renderAbove(r, s, renData);
		r.newFinalLightWithShadows(zoomout);
		SETT.WEATHER().apply(renData.absBounds());
		SETT.ROOMS().renderAbove(r, s, renData, zoomout);
		r.newLayer(true, zoomout);
		
		
	}
	
	public void renderTheRest(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData){
		r.newLayer(false, zoomout);
		
		SETT.ROOMS().render(r, s, renData, zoomout);
		if (renData.isLit) {
			RenderData.RenderIterator it = renData.onScreenTiles();
			TIME.light().bindRoom();
			while(it.has()) {
				if (it.litIs()) {
					byte nw = (byte) (it.litIs(DIR.NW) && it.litIs(DIR.W) && it.litIs(DIR.N) ? 255 : 0);
					byte ne = (byte) (it.litIs(DIR.NE) && it.litIs(DIR.E) && it.litIs(DIR.N) ? 255 : 0);
					byte se = (byte) (it.litIs(DIR.SE) && it.litIs(DIR.E) && it.litIs(DIR.S) ? 255 : 0);
					byte sw = (byte) (it.litIs(DIR.SW) && it.litIs(DIR.W) && it.litIs(DIR.S) ? 255 : 0);
					r.renderTileLight(it.x(), it.y(), C.TILE_SIZE,nw,ne,se,sw);
				}
				it.nextAll();
			}
			
			renData.isLit = false;
		}
		
		r.newLayer(false, zoomout);
		borders.render(r, renData);
		r.newLayer(false, zoomout);
		topology.renderMid(r, s, renData);
		r.newLayer(false, zoomout);
		topology.renderBelow(r, s, renData);
		
		if (!SETT.OVERLAY().renderOnGround(r, renData, zoomout)) {
			r.newLayer(false, zoomout);
			snow.render(r, renData);
			r.newLayer(false, zoomout);
			grass.render(r, renData);
			r.newLayer(false, zoomout);
			floors.render(r, ds, s, renData);
		}else {
			r.newLayer(false, zoomout);
			floors.render(r, ds, s, renData);
		}
		
		
		
	}
	
	public void renderSemiMap(Renderer r, float ds, RenderData renData) {
		
		
		
		int zoomout = 3;
		SETT.OVERLAY().renderOnGround(r, renData, zoomout);
		
		
		RenderIterator it = renData.onScreenTiles();
		r.newLayer(false, zoomout);
		//AmbientLight.full.register(0, C.WIDTH<<zoomout, 0, C.HEIGHT<<zoomout);
		TIME.light().apply(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout, RGB.WHITE);
		
		while (it.has()) {
			
			
			
			
			if (ROOMS().map.is(it.tile())){
				Room room = ROOMS().map.get(it.tile());
				int mask = 0;
				for (DIR d : DIR.ORTHO) {
					if (room.isSame(it.tx(), it.ty(), it.tx()+d.x(), it.ty()+d.y()))
						mask |= d.mask();
				}
				SPRITES.cons().TINY.low.render(r, mask, it.x(), it.y());
			}else {
				TerrainTile t = TERRAIN().get(it.tile());
				int depth = t.miniDepth();
				if (depth == 0) {
					if (floors.getter.is(it.tile())) {
						int mask = 0;
						for (DIR d : DIR.ORTHO) {
							if (floors.getter.is(it.tx(), it.ty(), d))
								mask |= d.mask();
						}
						SPRITES.cons().TINY.flat.render(r, mask, it.x(), it.y());
					}else {
						SPRITES.cons().TINY.low.render(r, 0x0F, it.x(), it.y());
					}
				}else {
					int mask = 0;
					for (DIR d : DIR.ORTHO) {
						if (TERRAIN().get(it.tx(), it.ty(), d).miniDepth() == depth)
							mask |= d.mask();
					}
					
					if (depth == 2)
						SPRITES.cons().TINY.high.render(r, mask, it.x(), it.y());
					else if (depth == 1)
						SPRITES.cons().TINY.low.render(r, mask, it.x(), it.y());
					else
						SPRITES.cons().TINY.flat.render(r, mask, it.x(), it.y());
				}
				
				
				
			}
			it.next();
		}
		
		
		
		OPACITY.O99.bind();
		double px1 = (double)renData.gBounds().x1()/C.TILE_SIZE;
		double py1 = (double)renData.gBounds().y1()/C.TILE_SIZE;
		
		SETT.MINIMAP().render(r, px1, py1, renData.absBounds().x1(), renData.absBounds().y1(), renData.absBounds().width(), renData.absBounds().height(), C.TILE_SIZE);
		
//		int w = (renData.tBounds().width()*C.TILE_SIZE);
//		int h = (renData.tBounds().height()*C.TILE_SIZE);
//		TextureCoords coo = SETT.MINIMAP().getTexture(renData.tBounds());
//		r.renderSprite(renData.x1(), renData.x1()+w, renData.y1(), renData.y1()+h, coo);
		OPACITY.unbind();
		
	}
	
	public void renderMiniMap(Renderer r, float ds, RenderData renData, int zoomout) {
		
		int zoom = C.TILE_SIZE >> zoomout;
		
		
		r.newLayer(false, 0);
		//AmbientLight.full.register(0, C.WIDTH<<zoomout, 0, C.HEIGHT<<zoomout);
		TIME.light().apply(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout, RGB.WHITE);
		
		SETT.MINIMAP().render(r, 
				renData.gBounds().x1()/(double)C.TILE_SIZE, renData.gBounds().y1()/(double)C.TILE_SIZE,
				renData.absBounds().x1(), renData.absBounds().y1(), 
				renData.absBounds().width(), renData.absBounds().height(), 
				zoom);
		
		
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
		growth.update(tx, ty, tile);
	}
	
	public final MinimapColorGetter minimap = new MinimapColorGetter();
	
//	COLOR getMiniMapColor(int x, int y){
//		
//		for (int i = Resource.resources.size()-1; i >= 0; i--){
//			COLOR c = Resource.resources.get(i).getMinimapColor(x, y);
//			if (c != null){
//				return c;
//			}
//			boolean n = false;
//			boolean s = false;
//			
//		}
//		return null;
//		
//	}
//	
//	public void updateMinimap(int tx, int ty) {
//		for (int i = Resource.resources.size()-1; i >= 0; i--){
//			COLOR c = Resource.resources.get(i).getMinimapColor(tx, ty);
//			if (c != null){
//				SETT.MINIMAP().putPixel(tx, ty, c);
//				return;
//			}
//		}
//	}
	
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
		
		protected COLOR miniColor(int tx, int ty) {
			return null;
		}
		
		protected COLOR miniColorPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
			return origional;
		}
		
		protected abstract void clearAll();
		
		protected final void updateMiniMap(int x, int y){
			TILE_MAP().minimap.update(x, y);
//			for (int i = Resource.resources.size()-1; i >= 0; i--){
//				COLOR c = Resource.resources.get(i).miniColor(x, y);
//				if (c != null){
//					if (i <= index) {
//						SETT.MINIMAP().putPixel(x, y, c);
//					}
//					return;
//				}
//			}
		}
	}

	public LOS LOS(int tx, int ty) {
		return topology.get(tx, ty).los(tx, ty);
	}

	public final static class MinimapColorGetter {
		
		private final ColorImp col = new ColorImp();
		private DIR[] dirNorth = new DIR[] {DIR.W, DIR.NW,DIR.N, DIR.NE};
		private DIR[] dirShade = new DIR[] {DIR.E, DIR.SE,DIR.S, DIR.SW};
		
		private final int qs = 8;
		private final int sc = Integer.numberOfTrailingZeros(qs);
		private final int ww = TWIDTH >> sc;
		
		private Bitmap1D bits = new Bitmap1D(TWIDTH*THEIGHT/(qs*qs), false);
		private ArrayListShort queue = new ArrayListShort(TWIDTH*THEIGHT/(qs*qs));
		private final TextureHolderChunk chunk = new TextureHolderChunk(qs, qs);
		
		COLOR get(int x, int y){
			
			
			Resource r = miniR(x, y);
			COLOR c = r.miniColor(x, y);
			boolean n = false;
			boolean s = false;
			
			for (DIR d : dirNorth) {
				int dx = x + d.x();
				int dy = y + d.y();
				if (IN_BOUNDS(dx, dy)) {
					Resource r2 = miniR(dx, dy);
					if (r2 == null || r != r2) {
						n = true;
						break;
					}
				}
				
			}
			for (DIR d : dirShade) {
				int dx = x + d.x();
				int dy = y + d.y();
				if (IN_BOUNDS(dx, dy)) {
					Resource r2 = miniR(dx, dy);
					if (r2 == null || r != r2) {
						s = true;
						break;
					}
				}
				
			}
			
			col.set(c);
			
			return r.miniColorPimped(col, x, y, n, s);
			
		}
		
		private Resource miniR(int x, int y) {
			if (!SETT.IN_BOUNDS(x, y))
				return null;
			for (int i = Resource.resources.size()-1; i >= 0; i--){
				COLOR c = Resource.resources.get(i).miniColor(x, y);
				if (c != null){
					return Resource.resources.get(i);
				}
			}
			return null;
		}
		
		public void update(int tx, int ty) {
			for (int di = 0; di < DIR.ALLC.size(); di++) {
				DIR dir = DIR.ALLC.get(di);
				int dx = tx+dir.x();
				int dy = ty+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					dx = dx >> sc;
					dy = dy >> sc;
					int i = dx + dy*ww;
					if (!bits.get(i)) {
						queue.add(i);
						bits.setTrue(i);
					}
				}
			}
			
			//SETT.MINIMAP().putPixel(tx, ty, get(tx, ty));
		}
		
		void clear() {
			queue.clear();
			bits.clear();
		}
		
		void update() {
			
			if (queue.size() > 0) {
				int q = queue.remove(queue.size()-1);
				bits.setFalse(q);
				int tx = q%ww;
				int ty = q/ww;
				tx = tx << sc;
				ty = ty << sc;
				
				int i = 0;
				for (int dy = 0; dy < qs; dy++) {
					for (int dx = 0; dx < qs; dx++) {
						COLOR c = get(tx+dx, ty+dy);

						chunk.put(i++, Minimap.getC(c.red()), Minimap.getC(c.green()), Minimap.getC(c.blue()), (byte)0x0FF);
					}
				}
				
				TextureCoords c = SETT.MINIMAP().texture(tx, ty, chunk.width, chunk.height);
				
				GAME.texture().addChunk(c.x1(), c.y1(), chunk.width, chunk.width*chunk.height, chunk);
				
			}
		}
		
	}

	
	
}
