package snake2d.util.sprite;

import snake2d.util.datatypes.DIMENSION;
import snake2d.util.rnd.RND;

public class TileTexture implements DIMENSION{

	private final int sx, sy;
	private final int width,height;
	private final int size;
	private final TextureCoords.Imp tex = new TextureCoords.Imp();
	
	public TileTexture(int tileSize, int tilesX, int tilesY, int px, int py){
		this.sx = px;
		this.sy = py;
		this.width = tilesX;
		this.height = tilesY;
		this.size = tileSize;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}
	
	public TextureCoords get(int tx, int ty, double offX, double offY) {
		int px = (tx & (width-1))*size;
		px += size*offX;
		int py = (ty & (height-1))*size;
		py += size*offY;
		return tex.get(sx+px, sy+py, size, size);
	}
	
	public TileTextureScroller scroller(double speedx, double speedy) {
		return new TileTextureScroller(this, speedx, speedy);
	}
	
	public static class TileTextureScroller {
		
		private final TextureCoords.Imp tex = new TextureCoords.Imp();
		private final TileTexture scroller;
		private double speedx,speedy;
		private double dx;
		private double dy;
		
		
		public TileTextureScroller(TileTexture scroller, double speedx, double speedy) {
			this.scroller = scroller;
			this.speedx = speedx;
			this.speedy = speedy;
			dx = RND.rFloat()*(scroller.width)*scroller.size;
			dy = RND.rFloat()*(scroller.height)*scroller.size;
		}
		
		public void update(double ds) {
			update(ds*speedx, ds*speedy);
		}
		
		public void update(double x, double y) {
			
			dx+= x;
			dy+= y;
			if (dx > (scroller.width)*scroller.size) {
				dx = 0;
			}else if (dx < 0) {
				dx += (scroller.width)*scroller.size;
			}
			if (dy > (scroller.height)*scroller.size) {
				dy = 0;
			}else if (dy < 0) {
				dy+= (scroller.height)*scroller.size;
			}
		}
		
		public TextureCoords get(int tileX, int tileY) {
			int px = (int) x1(tileX);
			int py = (int) y1(tileY);
			return tex.get(px, py, scroller.size, scroller.size);
		}
		
		public float x1(int tileX) {
			
			double x = tileX*scroller.size + dx;
			x %= scroller.width*scroller.size;
			return scroller.sx +  (float) x;
			
		}
		
		public float y1(int tileY) {
			double y = tileY*scroller.size + dy;
			y %= scroller.height*scroller.size;
			return scroller.sy +  (float) y;
		}
		
	}
	
}
