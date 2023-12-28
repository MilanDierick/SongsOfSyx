package world.overlay;

import init.C;
import init.D;
import init.resources.Minable;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLORS_MAP;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.regions.Region;

class OverlayMineral extends WorldOverlays.OverlayTileNormal{

	private static CharSequence ¤¤name = "¤Minerals";
	private static CharSequence ¤¤desc = "¤Shows the location of minerals.";
	static {
		D.ts(OverlayMineral.class);
	}
	
	
	OverlayMineral() {
		super(¤¤name, ¤¤desc, true, true);
	}
	
	@Override
	public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
		Minable m = WORLD.MINERALS().get(it.tile());
		if (m != null) {
			
			render(r, COLOR.WHITE100, it.x(), it.y(), 8);
			render(r, COLOR.WHITE10, it.x()+2, it.y()+2, 6);
			render(r, COLOR.WHITE30, it.x(), it.y(), 4);
			m.resource.icon().render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
		}
		
	}
	
	@Override
	protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		int m = 0;
		Region reg = WORLD.REGIONS().map.get(it.tile());
		COLOR c = GCOLORS_MAP.FRebel;
		for (DIR d : DIR.ORTHO) {
			if (WORLD.MINERALS().get(it.tx(), it.ty(), d) != null)
				c =  GCOLORS_MAP.bestOverlay;
			if (!WORLD.IN_BOUNDS(it.tx(), it.ty(), d) || reg == WORLD.REGIONS().map.get(it.tx(), it.ty(), d)){
				m |= d.mask();
			}
		}
		
		c.bind();
		renderUnder(m, r, it);
	}
	
	private void render(SPRITE_RENDERER r, COLOR c, int x, int y, int m) {
		c.render(r, x-m, x+C.TILE_SIZE+m, y-m, y+C.TILE_SIZE+m);
	}
	
}
