package world.map.terrain;

import init.C;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import snake2d.CORE;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.sprite.TileTexture.TileTextureScroller;

final class WorldWaterAnimation {

	private final TileTextureScroller dis1 = SPRITES.textures().dis_tiny.scroller(1.5, 1.5);
	private final TileTextureScroller dis2 = SPRITES.textures().dis_small.scroller(-2, -2);
	private final TileTextureScroller tex1 = SPRITES.textures().water2.scroller(-1.0, 1);
	private final TileTextureScroller tex2 = SPRITES.textures().water.scroller(2, 2);
	private final OpacityImp o1 = new OpacityImp((int) (255 * 0.1));
	private final OpacityImp o2 = new OpacityImp((int) (255 * 0.2));

	void update(float ds) {
		// double w = Settlement.WEATHER().windStrength();
		// o1.set((int) ((100+155*w)*0.2));
		// o1.set((int) ((200+55*w)*0.5));
		// w = ds*(w*0.5+0.5);
		dis1.update(ds);
		dis2.update(ds);
		tex1.update(ds);
		tex2.update(ds);

	}

	void render(RenderData.RenderIterator i) {

		o2.bind();

		
		CORE.renderer().renderDisplace(
				dis1.x1(i.tx()), dis1.y1(i.ty()), tex1.x1(i.tx()), tex1.y1(i.ty()), 16, 16, 8, i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE);
		
		o1.bind();
		CORE.renderer().renderDisplace(
				dis2.x1(i.tx()), dis2.y1(i.ty()), tex2.x1(i.tx()), tex2.y1(i.ty()), 16, 16, 4, i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE);
//		d = dis1.get(i.tx() + 4, i.ty() + 4);
//		c = tex1.get(i.tx() + 4, i.ty() + 4);
//		
//		
//		CORE.renderer().renderDisplaced(i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE, d, c);
//
//		o2.bind();
//		d = dis2.get(i.tx(), i.ty());
//		c = tex2.get(i.tx(), i.ty());
//		CORE.renderer().renderDisplaced(i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE, d, c);

		OPACITY.unbind();
	}

}