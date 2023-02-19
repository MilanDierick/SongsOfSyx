package game.faction;

import java.io.IOException;

import init.D;
import init.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class FBanner extends FactionResource{

	private static CharSequence ¤¤name = "¤Banner";
	
	static {
		D.t(FBanner.class);
	}
	
	public final BitmapSprite sprite = new BitmapSprite();
	

	private byte bannerI = (byte) RND.rInt(SPRITES.icons().l.banners.length);
	private final ColorImp background = new ColorImp(RND.rInt(87), RND.rInt(87), RND.rInt(87));
	private final ColorImp foreground = new ColorImp(40 + RND.rInt(87), 40 + RND.rInt(87), 40 + RND.rInt(87));
	private final ColorImp border = foreground.shade(0.25);
	private final ColorImp pole = new ColorImp(35 + RND.rInt0(5), 35 + RND.rInt0(5), 35 + RND.rInt(5));
	
	public FBanner(Faction f){
		
	}
	
	public ColorImp colorBG() {
		return background;
	}
	
	public ColorImp colorFG() {
		return foreground;
	}
	
	public ColorImp colorBorder() {
		return border;
	}
	
	public ColorImp colorPole() {
		return pole;
	}
	
	public int bannerType() {
		return bannerI;
	}
	
	public void bannerTypeSet(int i) {
		i&= 0xFFFF;
		bannerI = (byte) (i%SPRITES.icons().l.banners.length);
	}
	
	@Override
	protected void save(FilePutter file) {
		sprite.save(file);
		file.b(bannerI);
		background.save(file);
		foreground.save(file);
		border.save(file);
		pole.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		sprite.load(file);
		bannerI = file.b();
		background.load(file);
		foreground.load(file);
		border.load(file);
		pole.load(file);
	}
	
	@Override
	protected void clear() {
		
	}

	
	public static CharSequence name() {
		return ¤¤name;
	}

	public final SPRITE MEDIUM = new SPRITE() {
		
		@Override
		public int width() {
			return ICON.MEDIUM.SIZE;
		}
		
		@Override
		public int height() {
			return ICON.MEDIUM.SIZE;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int nopeX, int Y1, int nopeY) {
			SPRITES.icons().m.circle_frame.render(r, X1, Y1);
			background.bind();
			SPRITES.icons().m.circle_inner.render(r, X1, Y1);
			
			int sx = X1+(width()-BitmapSprite.WIDTH)/2;
			int sy = Y1+(height()-BitmapSprite.HEIGHT)/2;
			
			for (int y = 0; y < BitmapSprite.HEIGHT; y++) {
				for (int x = 0; x < BitmapSprite.WIDTH; x++) {
					if (sprite.is(x, y)) {
						foreground.render(r, sx+x, sx+x+1, sy+y, sy+y+1);
					}else if(sprite.is(x, y, DIR.N) || sprite.is(x, y, DIR.E)) {
						border.render(r, sx+x, sx+x+1, sy+y, sy+y+1);
					}
				}
			}
			
			
		}
	};
	
	public final SPRITE BIG = new SPRITE() {
		
		@Override
		public int width() {
			return ICON.BIG.SIZE;
		}
		
		@Override
		public int height() {
			return ICON.BIG.SIZE;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			SPRITES.icons().l.banners[bannerI].renderTextured(texture, X1, X2, Y1, Y2);
			SPRITES.icons().l.bannerPole.renderTextured(texture, X1, X2, Y1, Y2);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int nopeX, int Y1, int nopeY) {
			
			background.bind();
			SPRITES.icons().l.banners[bannerI].render(r, X1, nopeX, Y1, nopeY);
			pole.bind();
			SPRITES.icons().l.bannerPole.render(r, X1, nopeX, Y1, nopeY);
			
			sprite.scaled(r, X1+(width()-BitmapSprite.WIDTH*2)/2, Y1+(height()-BitmapSprite.HEIGHT*2)/2+1, 2, foreground, border, border);
			
			
		}
	};
	
	public final SPRITE HUGE = new SPRITE() {
		
		@Override
		public int width() {
			return ICON.BIG.SIZE*2;
		}
		
		@Override
		public int height() {
			return ICON.BIG.SIZE*2;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			SPRITES.icons().l.banners[bannerI].renderTextured(texture, X1, X2, Y1, Y2);
			SPRITES.icons().l.bannerPole.renderTextured(texture, X1, X2, Y1, Y2);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int nopeX, int Y1, int nopeY) {
			
			background.bind();
			SPRITES.icons().l.banners[bannerI].render(r, X1, nopeX, Y1, nopeY);
			pole.bind();
			SPRITES.icons().l.bannerPole.render(r, X1, nopeX, Y1, nopeY);
			
			sprite.scaled(r, X1+(width()-BitmapSprite.WIDTH*4)/2, Y1+(height()-BitmapSprite.HEIGHT*4)/2+2, 4, foreground, border, border);
			
			
		}
	};

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
	

	
	
}
