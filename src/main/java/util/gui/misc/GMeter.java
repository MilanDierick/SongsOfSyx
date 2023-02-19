package util.gui.misc;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.data.DOUBLE;

public class GMeter{

	public final static GGaugeColor C_ORANGE = new GGaugeColor(
			new ColorImp(52, 26, 9),
			new ColorImp(96, 38, 0),
			new ColorImp(127, 53, 0));
	
	public final static GGaugeColor C_REDGREEN = new GGaugeColor(
			new ColorImp(45, 16, 16),
			new ColorImp(31, 82, 35).shade(0.5),
			new ColorImp(23, 80, 28));
	
	public final static GGaugeColor C_INACTIVE = new GGaugeColor(
			new ColorImp(16, 16, 16),
			new ColorImp(48, 48, 48),
			new ColorImp(78, 78, 78));
	
	public final static GGaugeColor C_REDORANGE = new GGaugeColor(
			new ColorImp(45, 16, 16),
			new ColorImp(96, 38, 0),
			new ColorImp(127, 53, 0));
	
	public final static GGaugeColor C_REDPURPLE = new GGaugeColor(
			new ColorImp(45, 16, 16),
			new ColorImp(127, 16, 60).shade(0.5),
			new ColorImp(127, 16, 60));
	
	public final static GGaugeColor C_REDBLUE = new GGaugeColor(
			new ColorImp(45, 16, 16),
			new ColorImp(0, 0, 85),
			new ColorImp(0, 40, 127));
	
	public final static GGaugeColor C_GREENRED = new GGaugeColor(
			new ColorImp(31, 82, 35),
			new ColorImp(45, 16, 16),
			new ColorImp(100, 16, 16));
	
	public final static GGaugeColor C_RED = new GGaugeColor(
			new ColorImp(16, 16, 16),
			new ColorImp(45, 16, 16),
			new ColorImp(100, 16, 16));
	
	public final static GGaugeColor C_GRAY = new GGaugeColor(
			new ColorImp(15, 15, 15),
			new ColorImp(30, 30, 30),
			new ColorImp(60, 60, 60));
	
	public final static GGaugeColor C_GREEN = new GGaugeColor(
			new ColorImp(16, 16, 16),
			new ColorImp(16, 45, 16),
			new ColorImp(16, 100, 16));
	
	public final static GGaugeColor C_GREEN_DARK = new GGaugeColor(
			new ColorImp(10, 10, 10),
			new ColorImp(0, 22, 0),
			new ColorImp(0, 45, 0));
	
	public final static GGaugeColor C_BLUE = new GGaugeColor(
			new ColorImp(9, 9, 45),
			new ColorImp(0, 0, 85),
			new ColorImp(0, 40, 127));
	
	private GMeter() {
		
	}
	
	public static void render(SPRITE_RENDERER r, GGaugeColor color, double d, RECTANGLE body) {
		render(r, color, d, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void render(SPRITE_RENDERER r, GGaugeColor color, double d, int x1, int x2, int y1, int y2) {

		d = CLAMP.d(d, 0, 1);
		
		int w = x2-x1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		color.bg.render(r, x1+2, x2-2, y1+2, y2-2);
		
		
		
		w = (int) Math.ceil((x2-x1-4)*d);
		if (w > 0) {
			color.dark.render(r, x1+2, x1+2+w, y1+2, y2-2);
		}
		w = (int) Math.ceil((x2-x1-6)*d);
		
		int dy = y2-y1 > 12 ? 4: 3;
		if (y2-y1 > 20)
			dy = 5;
		
		if (w > 0) {
			color.bright.render(r, x1+3, x1+3+w, y1+dy, y2-dy);
		}
	}
	
	public static void renderC(SPRITE_RENDERER r, double d, RECTANGLE body) {

		renderC(r, d, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void renderC(SPRITE_RENDERER r, double d, int x1, int x2, int y1, int y2) {

		d = CLAMP.d(d, 0, 2);
		
		int w = x2-x1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		
		if (d < 1) {
			int sx = (int) (x1 + d*w/2);
			C_ORANGE.dark.render(r, sx+2, x1+w/2, y1+2, y2-2);
			C_ORANGE.bright.render(r, sx+2, x1+w/2, y1+3, y2-3);
		}else if (d>1){
			int sx2 = (int) (x1 + w/2 + (d-1)*w/2);
			C_ORANGE.dark.render(r, x1+w/2, sx2-2, y1+2, y2-2);
			C_ORANGE.bright.render(r, x1+w/2, sx2-2, y1+3, y2-3);
		}
		GCOLOR.UI().border().render(r, x1+w/2, x1+w/2+1, y1, y2);
	}
	
	public static void renderH(SPRITE_RENDERER r, GGaugeColor color, double d, int x1, int x2, int y1, int y2) {

		int h = y2-y1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		color.bg.render(r, x1+1, x2-1, y1+1, y2-1);
		
		int dh = (int) ((h-4)*d);
		
		if (dh > 0) {
			color.dark.render(r, x1+2, x2-2, y2-2-dh, y2-2);
		}
		dh = (int) ((h-6)*d);
		if (dh > 0) {
			color.bright.render(r, x1+3, x2-3, y2-3-dh, y2-3);
		}
	}
	
	public static void renderH(SPRITE_RENDERER r, GGaugeColor color, double d, RECTANGLE body) {
		renderH(r, color, d, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void render(SPRITE_RENDERER r, GGaugeColor color, double d1, double d2, int x1, int x2, int y1, int y2) {

		int w = x2-x1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		
		int dw = (int)((w-1)*d1);
		color.bg.render(r, x1+1, x1+dw-1, y1+1, y2-1);
		
		w = (int) ((w-2)*d2);
		if (w > 0) {
			color.dark.render(r, x1+2, x1+w, y1+2, y2-2);
		}
		if (w > 2) {
			color.bright.render(r, x1+3, x1+w-1, y1+3, y2-3);
		}
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, RECTANGLE body) {
		renderDelta(r, now, next, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2) {
		int w = x2-x1;
		now = CLAMP.d(now, 0, 1);
		next = CLAMP.d(next, 0, 1);
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		C_REDGREEN.bg.render(r, x1+2, x2-2, y1+2, y2-2);
		
		if (next > now) {
			OPACITY.O25TO100.bind();
			renderFG(r, C_BLUE, next, x1, x2, y1, y2);
			OPACITY.unbind();
			renderFG(r, C_REDGREEN, now, x1, x2, y1, y2);
		}else {
			OPACITY.O25TO100.bind();
			renderFG(r, C_ORANGE, now, x1, x2, y1, y2);
			OPACITY.unbind();
			renderFG(r, C_REDGREEN, next, x1, x2, y1, y2);
		}
	}
	
	private static void renderFG(SPRITE_RENDERER r, GGaugeColor color, double d, int x1, int x2, int y1, int y2) {

		int w = x2-x1;
		
		w = (int) Math.ceil((x2-x1-4)*d);
		if (w > 0) {
			color.dark.render(r, x1+2, x1+2+w, y1+2, y2-2);
		}
		w = (int) Math.ceil((x2-x1-6)*d);
		
		int dy = y2-y1 > 12 ? 4: 3;
		if (y2-y1 > 20)
			dy = 5;
		
		if (w > 0) {
			color.bright.render(r, x1+3, x1+3+w, y1+dy, y2-dy);
		}
	}
	
	public static class GGaugeColor {
		
		public final COLOR bg;
		public final COLOR dark;
		public final COLOR bright;
		
		private GGaugeColor(COLOR bg, COLOR bg1, COLOR bg2) {
			this.bg = bg; 
			this.dark = bg1;
			this.bright = bg2;
		}
	}
	
	public static SPRITE sprite(GGaugeColor c, DOUBLE d, int width, int height) {
		return new GMeterSprite(c, d, width, height);
	}
	
	public static class GMeterSprite implements SPRITE{
		
		private final int width,height;
		private final DOUBLE d;
		private final GGaugeColor c;
		
		public GMeterSprite(GGaugeColor c, DOUBLE d, int width, int height){
			this.c = c;
			this.d = d;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int width() {
			return width;
		}
		
		@Override
		public int height() {
			return height;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			double dd = d.getD();
			GMeter.render(r, dd < 0 ? C_INACTIVE : c, dd, X1, X2, Y1, Y2);
		}
	}
	
	public static class GMeterSpriteC implements SPRITE{
		
		private final int width,height;
		private final DOUBLE d;
		
		public GMeterSpriteC(DOUBLE d, int width, int height){
			this.d = d;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int width() {
			return width;
		}
		
		@Override
		public int height() {
			return height;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			GMeter.renderC(r, d.getD(), X1, X2, Y1, Y2);
		}
	}
	


}
