package init.sprite;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.*;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.IInit;
import util.spritecomposer.ComposerThings.ITileSheet;

public class LoadScreen implements SPRITE {
	
	private final static int TILE_SIZE = 32*C.SCALE_NORMAL;
	private final static int tilesX = 21;
	private final static int tilesY = 8;
	private final static int width = tilesX*TILE_SIZE;
	private final static int height = tilesY*TILE_SIZE;
	
	private final TILE_SHEET sheet;
	
	LoadScreen() throws IOException{
		
		new IInit(PATHS.SPRITE_UI().get("LoadScreen"), 1368, 268);
		
		sheet = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, tilesX, tilesY, d.s32);
				s.full.paste(true);
				return d.s32.saveNormal();
			}
		}.get();
		
	}
	
	public void init() {
		
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
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		int startX = X1;
		int tile = 0;
		for (int ty = 0; ty < tilesY; ty++) {
			X1 = startX;
			for (int tx = 0; tx < tilesX; tx++) {
				sheet.render(r, tile, X1, Y1);
				X1 += TILE_SIZE;
				tile++;
			}
			Y1 += TILE_SIZE;
		}
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		throw new RuntimeException();
	}
	
}