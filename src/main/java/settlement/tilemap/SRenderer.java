package settlement.tilemap;

import static settlement.main.SETT.*;

import game.time.TIME;
import init.C;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.util.color.OPACITY;
import snake2d.util.color.RGB;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class SRenderer {
	
	private final TileMap m;
	
	SRenderer(TileMap m){
		this.m = m;
	}
	
	
	public void renderAboveEnts(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData){
		r.newLayer(false, zoomout);
		SETT.WEATHER().apply(renData.absBounds());		
		m.topology.renderAbove(r, s, renData);
		r.newFinalLightWithShadows(zoomout, this);
		SETT.WEATHER().apply(renData.absBounds());
		SETT.ROOMS().renderAbove(r, s, renData, zoomout);
		r.newLayer(true, zoomout);
		
		
	}
	
	public void renderTheRest(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData, RECTANGLE renWindow, int offX, int offY){
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
		SETT.ENTRY().render(r, renData);
		r.newLayer(false, zoomout);
		m.topology.renderMid(r, s, renData);
		r.newLayer(false, zoomout);
		SETT.HALFENTS().renderBelow(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		m.topology.renderBelow(r, s, renData);
		
		if (!SETT.OVERLAY().renderOnGround(r, renData, zoomout)) {
			r.newLayer(false, zoomout);
			m.snow.render(r, renData);
			
			r.newLayer(false, zoomout);
			m.floors.render(r, ds, s, renData);
			r.newLayer(false, zoomout);
			m.grass.render(ds, r, renData);
			r.newLayer(false, zoomout);
			m.ground.render(r, ds, s, renData);
		}else {
			r.newLayer(false, zoomout);
			m.floors.render(r, ds, s, renData);
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
					if (m.floors.getter.is(it.tile())) {
						int mask = 0;
						for (DIR d : DIR.ORTHO) {
							if (m.floors.getter.is(it.tx(), it.ty(), d))
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
	
	
}
