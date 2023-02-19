package util.gui.panel;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class GFrame extends RENDEROBJ.RenderImp implements RENDEROBJ{

	public final static int MARGIN = 1;
	
	private Rec bounds = new Rec();

	public GFrame(){

	}

	@Override
	public Rec body() {
		return bounds;
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		render(r, ds, bounds);
		
		
	}
	
	public static void renderVertical(SPRITE_RENDERER r, int x1, int y1, int height) {
		int y2 = y1+height;
		while(y1 < y2) {
			if (y1+sprite(0).height() > y2)
				y1 = y2-sprite(0).height();
			sprite(3).render(r, x1, y1);
			y1 += sprite(0).height();
		}
	}
	
	public static void renderHorizontal(SPRITE_RENDERER r, int x1, int x2, int y1) {
		
		while(x1 <= x2-sprite(0).width()) {
			sprite(1).render(r, x1, y1);
			x1 += sprite(0).width();
		}
		sprite(1).render(r, x2-sprite(0).width(), y1);
	}
	
	private static SPRITE sprite(int i) {
		return UI.PANEL().frame.get(i);
	}
	
	public static void render(SPRITE_RENDERER r, float ds, RECTANGLE b) {
		render(r, b.x1(), b.x2(), b.y1(), b.y2());
	}
	
	public static void render(LIST<SPRITE> sprite, int MARGIN, SPRITE_RENDERER r, int x1, int x2, int y1, int y2) {
		
		int width = x2-x1;
		int height = y2-y1;
		
		
		int sw = sprite.get(0).width();
		if (width < sw)
			throw new RuntimeException("too narrow to wrap");
		
		int d = sw-MARGIN;
		if (width < d)
			throw new RuntimeException("too narrow to wrap");
		
		int dwidth = (int) (width - d);
		int tiles = dwidth/sw;
		dwidth -= tiles*sw;
		int offX = d - (dwidth);
		int w = tiles +1;
		
		sw = sprite.get(0).height();
		if (height < sw)
			throw new RuntimeException("too narrow to wrap");
		
		d = sw-MARGIN;
		if (height < d)
			throw new RuntimeException("too short to wrap");
		
		dwidth = (int) (height - d);
		tiles = dwidth/sw;
		dwidth -= tiles*sw;
		int offY = d - (dwidth);
		int h = tiles +1;
		
		int ry = y1-MARGIN;
		
		for (int y = 0; y <= h; y++) {
			int rx = x1-MARGIN;
			for (int x = 0; x <= w; x++) {
				if (x == 0 && y == 0) {
					sprite.get(0).render(r, rx, ry);
				}else if(x == w && y == 0) {
					sprite.get(2).render(r, rx-offX, ry);
				}else if(x == w && y == h) {
					sprite.get(8).render(r, rx-offX, ry-offY);
				}else if(x == 0 && y ==h) {
					sprite.get(6).render(r, rx, ry-offY);
				}else if(y == 0) {
					sprite.get(1).render(r, rx, ry);
				}else if(y == h) {
					sprite.get(7).render(r, rx, ry-offY);
				}else if(x == 0) {
					sprite.get(3).render(r, rx, ry);
				}else if(x == w) {
					sprite.get(5).render(r, rx-offX, ry);
				}
				rx += sprite.get(0).width();
			}
			ry += sprite.get(0).height();
		}
		
	}
	
	public static void render(SPRITE_RENDERER r, int x1, int x2, int y1, int y2) {
		
		render(UI.PANEL().frame, MARGIN, r, x1, x2, y1, y2);
		
	}
	
	public static SPRITE separator(int width) {
		return new SPRITE() {
			
			@Override
			public int width() {
				return width;
			}
			
			@Override
			public int height() {
				return UI.PANEL().frame.size();
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				renderHorizontal(r, X1, X2, Y1+height()/2);
			}
		};
	}
	
}