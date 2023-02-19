package init.sprite;

import init.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public abstract class ICON implements SPRITE{
	
	
	public abstract int size();
	
	@Override
	public final int width() {
		return size();
	}

	@Override
	public final int height() {
		return size();
	}
	
	public static abstract class SMALL extends ICON{

		public final SPRITE game = new SPRITE() {
			
			@Override
			public int width() {
				return pixelSize*C.SCALE;
			}
			
			@Override
			public int height() {
				return pixelSize*C.SCALE;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				SMALL.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				SMALL.this.render(r, X1, X2, Y1, Y2);
			}

			@Override
			public TextureCoords texture() {
				return SMALL.this.texture();
			}
		};
		
		public final static int pixelSize = 16;
		public final static int SIZE = pixelSize*C.SG;

		@Override
		public final int size() {
			return SIZE;
		}
		
		public static class Twin extends SMALL{

			private final ICON.SMALL a;
			private final SPRITE b;
			
			public Twin(SMALL a, SPRITE b) {
				this.a = a;
				this.b = b;
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				a.render(r, X1, X2, Y1, Y2);
				b.render(r, X1, X2, Y1, Y2);
				
			}

			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				a.renderTextured(texture, X1, X2, Y1, Y2);
				b.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public TextureCoords texture() {
				return null;
			}
			
			
			
		}
		
	}
	
	public static abstract class MEDIUM extends ICON{

		public final static int SIZE = 24*C.SG;
		public final ICON.SMALL small = new ICON.SMALL() {
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				MEDIUM.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				MEDIUM.this.render(r, X1, X2, Y1, Y2);
			}
			@Override
			public TextureCoords texture() {
				return MEDIUM.this.texture();
			}
		};
		public final SPRITE huge = new SPRITE() {
			
			@Override
			public int width() {
				return SIZE*2;
			}
			
			@Override
			public int height() {
				return SIZE*2;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				MEDIUM.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				MEDIUM.this.render(r, X1, X2, Y1, Y2);
			}
			@Override
			public TextureCoords texture() {
				return MEDIUM.this.texture();
			}
		};
		
		@Override
		public final int size() {
			return SIZE;
		}
		
		public static class Twin extends MEDIUM{

			private final ICON.MEDIUM a;
			private final SPRITE b;
			
			public Twin(ICON.MEDIUM a, SPRITE b) {
				this.a = a;
				this.b = b;
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				a.render(r, X1, X2, Y1, Y2);
				b.renderC(r, X1 + (X2-X1)/2, Y1 + (Y2-Y1)/2);
				
			}

			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				a.renderTextured(texture, X1, X2, Y1, Y2);
				b.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public TextureCoords texture() {
				return null;
			}
			
			
			
		}
		
	}
	
	public static abstract class BIG extends ICON{

		public final static int SIZE = 32*C.SG;
		public final ICON.SMALL small = new ICON.SMALL() {
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				BIG.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				BIG.this.render(r, X1, X2, Y1, Y2);
			}

			@Override
			public TextureCoords texture() {
				return BIG.this.texture();
			}
		};
		public final ICON.MEDIUM nomal = new ICON.MEDIUM() {
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				BIG.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				BIG.this.render(r, X1, X2, Y1, Y2);
			}

			@Override
			public TextureCoords texture() {
				return BIG.this.texture();
			}
		};
		public final SPRITE huge = new SPRITE() {
			
			@Override
			public int width() {
				return SIZE*2;
			}
			
			@Override
			public int height() {
				return SIZE*2;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				BIG.this.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				BIG.this.render(r, X1, X2, Y1, Y2);
			}
			
			@Override
			public TextureCoords texture() {
				return BIG.this.texture();
			}
		};
		
		@Override
		public final int size() {
			return SIZE;
		}
		
		public static class Twin extends BIG{

			private final ICON.BIG a;
			private final SPRITE b;
			
			public Twin(ICON.BIG a, SPRITE b) {
				this.a = a;
				this.b = b;
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				a.render(r, X1, X2, Y1, Y2);
				b.render(r, X1, X2, Y1, Y2);
				
			}

			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				a.renderTextured(texture, X1, X2, Y1, Y2);
				b.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			
			
		}
		
	}
	
}
