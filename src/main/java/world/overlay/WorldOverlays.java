package world.overlay;

import java.io.IOException;

import game.faction.Faction;
import init.C;
import init.sprite.SPRITES;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.info.INFO;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.WORLD;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.regions.Region;

public class WorldOverlays {

	public final OverlayTileNormal minerals = new OverlayMineral();
	public final OverlayRegnames regNames = new OverlayRegnames();
	public final EThings things = new EThings();
	public final ERegion regionOutline = new ERegion();
	public final OverlayExplore landmarks = new OverlayExplore();
	public final EPath path = new EPath();
	
	private final Edger edger = new Edger(WORLD.TWIDTH(), WORLD.THEIGHT());
	private final Army army = new Army();
	
	
	public final LIST<OverlayTileNormal> togglable = new ArrayList<>(
			new OverlayPathing(),
			new OverlayFaction(),
			new OverlayDiplomacy(),
			new OverlayMineral()
			);
	
	private Overlay current;
	public Overlay debug;
	
	public WorldOverlays() throws IOException {
		
	}
	
	private boolean hide = false;

	public void hide() {
		this.hide = true;
	}
	
	
	public boolean renderBelow(Renderer r, ShadowBatch s, RenderData data, int zoomout){
		
		if (current == null)
			return false;
		Overlay o = current;
		current = null;
		boolean ret = !hide && o.renderBelow(r, s, data);
		COLOR.unbind();
		OPACITY.unbind();
		hide = false;
		return ret;
	}
	
	public void render(Renderer r, ShadowBatch s, RenderData data, int zoomout){
		if (hide) {
			things.clear();
			return;
		}
		if (debug != null)
			current = debug;
		if (current == null)
			current = regNames;
		if (current != null)
			current.renderAbove(r, s, data);
		things.render(r, s, data);
		path.render(r, s, data);
		regionOutline.renderAbove(r, s, data);
		edger.render(r, data, zoomout);
		things.clear();
		COLOR.unbind();
		OPACITY.unbind();
	}

	public void hover(Region reg) {
		regionOutline.add(reg);
		regNames.exclude(reg);
		hoverBox(reg);
	}
	
	public void hoverEntity(WEntity ent) {
		things.hover(ent);
		if (ent.path() != null) {
			path.add(ent.ctx(), ent.cty(), ent.path().destX(), ent.path().destY(), ent.path().treaty());
		}
		
	}
	
	public void hoverArmy(WArmy army) {
		if (army == null) {
			this.army.add(null);
			
		}
		else {
			this.army.add(army.faction());
			hoverEntity(army);
		}
	}
	
	
	public void hoverArmy(Faction f) {
		this.army.add(f);
	}
	
//	public void hover(RECTANGLE body, COLOR color, boolean thick, int margin) {
//		things.add(body.x1()-margin, body.y1()-margin, body.width()+margin*2, body.height()+margin*2, color, thick);
//	}
//	
//	public void hover(int x1, int y1, int w, int h, COLOR color, boolean thick) {
//		things.add(x1, y1, w, h, color, thick);
//	}
//	
//	public void hover(WEntity e) {
//		hover(e.body(), GCOLORS_MAP.get(e.faction()), true, 6);
//	}
	
	public void hoverBox(Region region) {
		
		if (region == null)
			return;
		
		int x1 = region.cx()*C.TILE_SIZE;
		int y1 = region.cy()*C.TILE_SIZE;
		
		if (region.capitol()) {
			things.hover(x1-C.TILE_SIZE-C.TILE_SIZEH, y1-C.TILE_SIZE-C.TILE_SIZEH, C.TILE_SIZE*4, C.TILE_SIZE*4, region.faction().banner().colorBG(), true);
		}else if (region.faction() != null) {
			things.hover(x1-C.TILE_SIZE+C.TILE_SIZEH/2, y1-C.TILE_SIZE+C.TILE_SIZEH/2, C.TILE_SIZE*2+C.TILE_SIZEH, C.TILE_SIZE*2+C.TILE_SIZEH, region.faction().banner().colorBG(), false);
		}else {
			things.hover(x1-C.TILE_SIZE+C.TILE_SIZEH/2, y1-C.TILE_SIZE+C.TILE_SIZEH/2, C.TILE_SIZE*2+C.TILE_SIZEH, C.TILE_SIZE*2+C.TILE_SIZEH, COLOR.WHITE65, false);
		}
		WORLD.MINIMAP().hilight(region);
	}
	
	
	
	public static abstract class Overlay {
		
		public Overlay() {
			
		}
		
		protected void add() {
			WORLD.OVERLAY().current = this;
		}
		
		public boolean added() {
			return WORLD.OVERLAY().current == this;
		}
		
		protected void remove() {
			WORLD.OVERLAY().current = null;
		}
		
		public abstract void renderAbove(Renderer r, ShadowBatch s, RenderData data);
		public abstract boolean renderBelow(Renderer r, ShadowBatch s, RenderData data);
		private static Coo coo = new Coo();
		protected COORDINATE mouse(RenderData data) {
			coo.xSet(data.gBounds().x1() + ((VIEW.mouse().x() - data.absBounds().x1())<<CORE.renderer().getZoomout()));
			coo.ySet(data.gBounds().y1() + ((VIEW.mouse().y() - data.absBounds().y1())<<CORE.renderer().getZoomout()));
			coo.xSet(coo.x()/C.TILE_SIZE);
			coo.ySet(coo.y()/C.TILE_SIZE);
			return coo;
		}
		
	}
	
	public static class OverlayTile extends Overlay{

		private boolean above;
		private boolean below;
		
		public OverlayTile(boolean above, boolean below) {
			this.above = above;
			this.below = below;
				
		}
		
		@Override
		public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
			if (above) {
				RenderIterator it = data.onScreenTiles(0,0,0,0);
				while(it.has()) {
					renderAbove(r, s, it);
					it.next();
				}
			}
			
		}

		@Override
		public boolean renderBelow(Renderer r, ShadowBatch s, RenderData data) {
			if (below) {
				RenderIterator it = data.onScreenTiles(0,0,0,0);
				while(it.has()) {
					renderBelow(r, s, it);
					it.next();
				}
			}
			return below;
		}
		
		protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
			
		}
		protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
			
		}
		
		protected static void renderUnder(int m, SPRITE_RENDERER r, RenderIterator it) {
			SPRITES.cons().BIG.filled.render(r, m, it.x(), it.y());
		}

	}
	
	public static abstract class OverlayTileNormal extends OverlayTile{

		public final INFO info;
		public OverlayTileNormal(CharSequence name, CharSequence desc, boolean above, boolean below) {
			super(above, below);
			info = new INFO(name, desc);
		}

		@Override
		public void add() {
			super.add();
		}
		
	}

}
