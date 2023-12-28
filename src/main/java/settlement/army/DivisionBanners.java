package settlement.army;

import java.io.IOException;

import init.paths.PATHS;
import init.sprite.BitmapSprite;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class DivisionBanners implements SAVABLE{

	private final DivisionBanner[] all;
	
	DivisionBanners(){
		
		short[][] data = BitmapSprite.read(PATHS.SPRITE_UI().get("DivisionSymbols"));
		all = new DivisionBanner[data.length];
		COLOR[] cols = COLOR.generateUnique(40, data.length, true);
		for (int i = 0; i < data.length; i++) {
			DivisionBanner d = new DivisionBanner(new BitmapSprite());
			d.sprite.paint(data[i]);
			d.col.set(cols[i]);
			all[i] = d;
		}
		
	}
	
	public DivisionBanner get(int index) {
		index = index % all.length;
		if (index < 0)
			index += all.length;
		return all[index];
	}
	


	@Override
	public void save(FilePutter file) {
		file.i(all.length);
		for (DivisionBanner d : all) {
			d.sprite.save(file);
			d.col.save(file);
			d.bg.save(file);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int am = file.i();
		for(int i = 0; i < am; i++) {
			get(i).sprite.load(file);
			get(i).col.load(file);
			get(i).bg.load(file);
		}
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	public static class DivisionBanner implements SPRITE{
		
		public final BitmapSprite sprite;
		public final ColorImp col = new ColorImp();
		public final ColorImp bg = new ColorImp(20, 20, 20);
		private final int m = 2;
		public DivisionBanner(BitmapSprite sprite) {
			this.sprite = sprite;
		}

		@Override
		public int width() {
			return BitmapSprite.WIDTH*2+m*2;
		}
		
		@Override
		public int height() {
			return BitmapSprite.HEIGHT*2+m*2;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int nopeX, int Y1, int nopeY) {
			
			bg.render(r, X1, X1+width(), Y1, Y1+width());
			ColorImp.TMP.set(bg).shadeSelf(0.75);
			ColorImp.TMP.renderFrame(r, X1, X1+width(), Y1, Y1+height(), 1, 1);
			ColorImp.TMP.shadeSelf(0.75);
			ColorImp.TMP.renderFrame(r, X1, X1+width(), Y1, Y1+height(), 0, 1);
			
			
			renderSymbol(r, X1+m, Y1+m);
			
			
		}
		
		public void renderSymbol(SPRITE_RENDERER r, int X1, int Y1) {
			
			
			
			for (int y = 0; y < BitmapSprite.WIDTH*2; y++) {
				for (int x = 0; x < BitmapSprite.HEIGHT*2; x++) {
					int dx = (x-1)/2;
					int dy = (y-1)/2;
					
					if (sprite.is(dx, dy)) {
						COLOR c = col;
						for (DIR d: DIR.ALL) {
							int ddx = (x-1+d.x())/2;
							int ddy = (y-1+d.y())/2;
							if (!sprite.is(ddx, ddy)) {
								c = ColorImp.TMP.set(c).shadeSelf(0.6);
								break;
							}
						}
						c.render(r, X1+x, X1+x + 1, Y1+y, Y1+y+1);
					}else {
						for (DIR d: DIR.ALL) {
							int ddx = (x-1+d.x())/2;
							int ddy = (y-1+d.y())/2;
							if (sprite.is(ddx, ddy)) {
								COLOR.WHITE100.render(r, X1+x, X1+x + 1, Y1+y, Y1+y+1);
								break;
							}
						}
					}

				}
			}

			
			
		}
	}

	public int size() {
		return all.length;
	}
	
}
