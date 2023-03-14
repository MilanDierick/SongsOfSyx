package world.overlay;

import init.C;
import init.D;
import init.resources.Minable;
import settlement.main.RenderData.RenderIterator;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import util.rendering.ShadowBatch;
import world.World;

class OverlayMineral extends WorldOverlayer{

	private static CharSequence ¤¤name = "¤Minerals";
	private static CharSequence ¤¤desc = "¤Shows the location of minerals.";
	static {
		D.ts(OverlayMineral.class);
	}
	
	
	OverlayMineral() {
		super(¤¤name, ¤¤desc);
	}
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
		Minable m = World.MINERALS().get(it.tile());
		if (m != null) {
			
			render(r, COLOR.WHITE100, it.x(), it.y(), 8);
			render(r, COLOR.WHITE10, it.x()+2, it.y()+2, 6);
			render(r, COLOR.WHITE30, it.x(), it.y(), 4);
			m.resource.icon().render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
		}
		
	}
	
	private void render(SPRITE_RENDERER r, COLOR c, int x, int y, int m) {
		c.render(r, x-m, x+C.TILE_SIZE+m, y-m, y+C.TILE_SIZE+m);
	}
	
}
