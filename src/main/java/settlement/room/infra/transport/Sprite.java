package settlement.room.infra.transport;

import java.io.IOException;

import init.C;
import init.resources.RESOURCE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

final class Sprite {

	private final TILE_SHEET sheetCart;
	private final int M = 1*C.SCALE;
	Sprite() throws IOException{
		
		sheetCart = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d)  {
				s.singles.init(0, s.singles.body().y2(), 1, 1, 2, 5, d.s32);
				for (int i = 0; i < 5; i++) {
					for (int r = 0; r < 4; r++) {
						s.singles.setSkip(i*2, 1).pasteRotated(r, true);
						s.singles.setSkip(i*2+1, 1).pasteRotated(r, true);
					}
				}
				
				return d.s32.saveGame();
			}
		}.get();
		
	}

	public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int rot, int cx, int cy, double mov, int ran, double degrade, RESOURCE res, int resamount) {
		int i = rot;
		i+= ((int)(mov*3)&3)*8;
		DIR d = DIR.ALL.get(rot).perpendicular();
		

		
		int x =cx+ d.x()*M ;
		int y = cy+ d.y()*M ;
		sheetCart.renderC(r, i, x, y);
		s.setHeight(4).setDistance2Ground(0);
		sheetCart.renderC(s, i, x, y);
		
		if (res != null && resamount > 0) {
			x = (int) (d.xN()*M*2 + cx-C.TILE_SIZEH);
			y = (int) (d.yN()*M*2 + cy-C.TILE_SIZEH);
			res.renderLaying(r, x, y, ran, resamount);
		}
	}
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, int rot, int cx, int cy, double degrade) {
		int i = rot + 8*4;
		DIR d = DIR.ALL.get(rot).perpendicular();
		int x = (int) (cx+ d.xN()*M);
		int y = (int) (cy+ d.yN()*M);
		sheetCart.renderC(r, i, x, y);
		s.setHeight(1).setDistance2Ground(0);
		sheetCart.renderC(s, i, x, y);
	}
	
}
