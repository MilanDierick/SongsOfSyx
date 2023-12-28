package settlement.army.formation;

import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import util.rendering.RenderData;

public final class DivRenderer {

	
	private DivRenderer() {
		
	}
	
	public static void render(SPRITE_RENDERER ren, DivFormation p, RenderData data) {
		
		if (p == null)
			return;
		
		int men = p.deployed();
		
		if (men == 0)
			return;
		
		if (!p.body().touches(data.gBounds())) {
			return;
		}
		
		SPRITE s = CORE.renderer().getZoomout() >= 3 ? SPRITES.cons().TINY.flat.get(0) : SPRITES.cons().ICO.arrows2.get(p.dir().id());
		
		int ox = data.offX1()+s.width()/2;
		int oy = data.offY1()+s.height()/2;
		
		
		
		for (int i = 0; i < men; i++) {
			//colors[i].bind();
			int m = p.dirMaskOrtho(i);
			COORDINATE c = p.pixel(i);
			
			int rx = c.x()-ox;
			int ry = c.y()-oy;

			if (m != 0x0F) {
				//COLOR.WHITE50.bind();
				SPRITES.cons().BIG.dots.render(ren, m, rx, ry);
				//COLOR.unbind();
			}
			
			if (CORE.renderer().getZoomout() < 3) {
				DIR d = p.dir(i);
				if (d == null)
					d = p.dir();
				s = SPRITES.cons().ICO.arrows2.get(d.id());
				
				s.render(ren, rx, ry);
			}
		}

		//COLOR.unbind();
	}

	
}
