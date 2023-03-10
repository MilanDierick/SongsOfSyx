package snake2d.util.sprite;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;

public abstract interface SPRITE extends DIMENSION{

	/**
	 * 
	 * @param r
	 * @param dt
	 * @param quad
	 * @return true if end of animation. Always false for sprites.
	 */
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2);
	
	public default void render(SPRITE_RENDERER r, RECTANGLE rec){
		render(r, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}
	
	/**
	 * 
	 * @param r
	 * @param dt
	 * @param X1
	 * @param Y1
	 * @return true if end of animation. Always false for sprites.
	 */
	public default void render(SPRITE_RENDERER r, int X1, int Y1){
		render(r, X1, X1+width(), Y1, Y1+height());
	}
	
	public default void renderC(SPRITE_RENDERER r, int cx, int cy){
		render(r, cx-width()/2, cx-width()/2+width(), cy-height()/2, cy-height()/2+height());
	}
	
	public default void renderCY(SPRITE_RENDERER r, int x1, int cy){
		render(r, x1, x1+width(), cy-height()/2, cy-height()/2+height());
	}
	
	public default void renderC(SPRITE_RENDERER r, RECTANGLE c){
		renderC(r, c.cX(), c.cY());
	}
	
	public default void renderC(SPRITE_RENDERER r, COORDINATE c){
		renderC(r, c.x(), c.y());
	}
	
	public default void renderScaled(SPRITE_RENDERER r, int X1, int Y1, int scale){
		render(r, X1, X1+width()*scale, Y1, Y1+height()*scale);
	}
	
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2);
	
	public default void renderTextured(TextureCoords texture, RECTANGLE rec){
		renderTextured(texture, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}
	
	public default void renderTextured(TextureCoords texture, int X1, int Y1){
		renderTextured(texture, X1, X1+width(), Y1, Y1+height());
	}
	
	public default TextureCoords texture() {
		return null;
	}
	
	public abstract class Imp implements SPRITE {
		
		protected int width,height;
		
		public Imp(){
		}
		
		public Imp(int d){
			this(d, d);
		}
		
		public Imp(int w, int h){
			this.width = w;
			this.height = h;
		}
		
		public void setDim(int w, int h) {
			this.width = w;
			this.height = h;
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
		
	}
	
	public class Twin implements SPRITE {
		
		private final int w,h;
		private final SPRITE a,b;
		
		public Twin(SPRITE a, SPRITE b){
			this.a = a;
			this.b = b;
			w = a.width();
			h =a.height();
		}

		@Override
		public int width() {
			return w;
		}

		@Override
		public int height() {
			return h;
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {

		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			a.render(r, X1, X2, Y1, Y2);
			b.render(r, X1, X2, Y1, Y2);
		}
		
	}
	
	public class SpriteImp implements SPRITE, TextureCoords{
		
		public final short x1;
		public final short x2;
		public final short y1;
		public final short y2;
		
		protected final int gameWidth;
		protected final int gameHeight;
		
		public SpriteImp(int x1, int x2, int y1, int y2, int gameWidth, int gameHeight){
			this.x1 = (short) x1;
			this.x2 = (short) x2;
			this.y1 = (short) y1;
			this.y2 = (short) y2;
			this.gameWidth = gameWidth;
			this.gameHeight = gameHeight;
		}

		@Override
		public int width() {
			return gameWidth;
		}

		@Override
		public int height() {
			return gameHeight;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			r.renderSprite(X1, X2, Y1, Y2, 
					this);
		}

		@Override
		public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
			CORE.renderer().renderTextured(X1, X2, Y1, Y2, 
					other, this);
		}

		@Override
		public short x1() {
			return x1;
		}

		@Override
		public short x2() {
			return x2;
		}

		@Override
		public short y1() {
			return y1;
		}

		@Override
		public short y2() {
			return y2;
		}

		@Override
		public TextureCoords texture() {
			return this;
		}

	}
	
	public class SpriteFromSheet implements SPRITE{
		
		private final int tile;
		private final TILE_SHEET sheet;
		
		public SpriteFromSheet(TILE_SHEET sheet, int tile){
			this.sheet = sheet;
			this.tile = tile;
		}

		@Override
		public int width() {
			return sheet.size();
		}

		@Override
		public int height() {
			return sheet.size();
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			sheet.render(r, tile, X1, X2, Y1, Y2);
		}

		@Override
		public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
			sheet.renderTextured(other, tile, X1, Y1);
		}
		
		@Override
		public TextureCoords texture() {
			return sheet.getTexture(tile);
		}


	}
	
	

	
}
