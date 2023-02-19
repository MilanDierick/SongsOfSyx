package menu;

import init.C;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.Fire;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;

final class Background {
	
	private float speed = 24;
	private final int tilesX;
	private final int maxWidth;
	private final RECTANGLE bounds;
	private final Rec bgBounds;
	private final Rec fameBounds;
	
	private int maxX2;
	
	Fire torch1 = new Fire(7);
	Fire torch2 = new Fire(7);
	Fire torch3 = new Fire(0.2);
	private final TILE_SHEET tiles;
	
	Background(RECTANGLE bounds2){
		
		this.tiles = RESOURCES.s().background;
		tilesX = tiles.tiles()/12;
		maxWidth = tilesX*32*2;
		maxX2 = maxWidth-C.WIDTH();
		this.bounds = new Rec(0, C.WIDTH(), (C.HEIGHT()-256*3)/2, (C.HEIGHT()-256*3)/2 + 256*3);
		bgBounds = new Rec(0, bounds.width(), 0, bounds.height());
		bgBounds.moveX1(RND.rInt(maxWidth - bounds.width()));
		fameBounds = new Rec(0, bounds.width(), 0, bounds.height()-1);
		fameBounds.moveX2(maxWidth);
		bgBounds.moveX1(0);
		torch1.flicker(1f);
		torch2.flicker(1f);
		
		double d = bounds.width()/C.MIN_WIDTH;
		
		torch1.setRadius((int) (1300.0*d));
		torch1.set(bounds.x1()-150, C.HEIGHT()/2);
		torch1.setFalloff(2f);
		torch1.setFlickerFactor(20f);
		torch1.setZ(50);
		torch2.setFalloff(3f);
		torch2.setRadius((int) (1300.0*d));
		torch2.set(bounds.x2() + 150, C.HEIGHT()/2);;
		torch2.setFlickerFactor(20f);
		torch2.setZ(50);
		
		torch3.setFalloff(3f);
		torch3.setRadius((int) (300.0*bounds.width()/C.MIN_WIDTH));
		torch3.setFlickerFactor(11f);
		torch3.setZ(60);
		
		
	}

	private final AmbientLight s = new AmbientLight(0.1, 0.05, 0.025, 90, 20);
	private final AmbientLight moon = new AmbientLight(0.1615, 0.1615, 0.23, 90, 35);
	
	public void render(SPRITE_RENDERER r, float ds){
		torch1.flicker(ds);
		torch2.flicker(ds);
		
		byte full = -1;
		byte none = 0;
		int fadeW = 100;
		CORE.renderer().registerLight(torch1, bounds.x1(), bounds.x1()+fadeW, bounds.y1(), bounds.y2(), full, full, none, none);	
		CORE.renderer().registerLight(torch1, bounds.x1()+fadeW, bounds.x2(), bounds.y1(), bounds.y2(), full, full, full, full);	
		
		CORE.renderer().registerLight(torch2, bounds.x2()-fadeW, bounds.x2(), bounds.y1(), bounds.y2(), none, none, full, full);	
		CORE.renderer().registerLight(torch2, bounds.x1(), bounds.x2()-fadeW, bounds.y1(), bounds.y2(), full, full, full, full);	

		bgBounds.incrX(speed*ds);
		
		if (bgBounds.x2() >= maxX2 && speed > 0){
			bgBounds.moveX2(maxX2 - (bgBounds.x2()-maxX2));
			if (speed > 0)
				speed*= -1;
		}else if(bgBounds.x1() <= 0 && speed < 0){
			bgBounds.moveX1(-bgBounds.x1());
			if (speed < 0)
				speed*= -1;
		}
		s.setDir(180);
		s.set(torch1.getRed()*0.05, torch1.getGreen()*0.05, torch1.getBlue()*0.05);
		
		moon.register(bounds);
		//s.register(bounds);
		//forBounds.incrementX(C.TILE_SIZE*3.0*ds);
		render(r, bounds.x1(), bounds.y1(), bgBounds);
		//pillsmall1.render(ds);
		//pillsmall2.render(ds);
		//pillbig.render(ds);
		//Sprites.foreground.render(0, bounds.getY2()-Sprites.foreground.getGameHeight(), forBounds);
	}
	
	public void renderFame(SPRITE_RENDERER r, float ds, COORDINATE mCoo, int ran){
		torch1.flicker(ds);
		torch2.flicker(ds);
		byte full = -1;
		byte none = 0;
		int fadeW = 100;
		CORE.renderer().registerLight(torch1, bounds.x1(), bounds.x1()+fadeW, bounds.y1(), bounds.y2(), full, full, none, none);	
		CORE.renderer().registerLight(torch1, bounds.x1()+fadeW, bounds.x2(), bounds.y1(), bounds.y2(), full, full, full, full);	
		
		CORE.renderer().registerLight(torch2, bounds.x2()-fadeW, bounds.x2(), bounds.y1(), bounds.y2(), none, none, full, full);	
		CORE.renderer().registerLight(torch2, bounds.x1(), bounds.x2()-fadeW, bounds.y1(), bounds.y2(), full, full, full, full);	
		
		torch3.set(mCoo);
		torch3.flicker(ds);
		torch3.register();
		fameBounds.moveX2(maxWidth-(ran&3)*50);
		render(r, bounds.x1(), bounds.y1(), fameBounds);
	}
	
	private void render(SPRITE_RENDERER r, int x1, int y1, RECTANGLE bb) {
		
		int dx = bb.x1()%tiles.size();
		x1 -= dx;
	
		int sx = bb.x1()/tiles.size();
		
		int ys = (int) Math.ceil((double)bb.height()/tiles.size());
		int xs = (int) Math.ceil((double)(bb.width()+dx)/tiles.size());
		
		for (int y = 0; y < ys; y++) {
			for (int x = 0; x < xs && x < tilesX; x++) {
				int t = sx + tilesX*y+x;
				if (t >= tiles.tiles())
					continue;
				
				tiles.render(r, t, x1+x*tiles.size(), y1+y*tiles.size());
				
			}
		}
		
		
	}
	
}
