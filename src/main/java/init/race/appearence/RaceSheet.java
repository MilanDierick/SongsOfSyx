package init.race.appearence;

import java.io.IOException;

import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RaceSheet {
	
	public final TILE_SHEET sheet;
	public final TILE_SHEET sleep;
	public final TILE_SHEET lay;
	
	public RaceSheet(int x2) throws IOException {
		sheet = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				int a = 22;
				s.singles.init(x2, 0, 1, 1, 2, a, d.s24);
				for (int i = 0; i < a; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s24.saveGame();
			}
		}.get();
		
		
		sleep = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 4, 1, d.s32);
				for (int i = 0; i < 2; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s32.saveGame();
			}
		}.get();
		
		lay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				int a = 6;
				s.singles.init(s.singles.body().x1(), s.singles.body().y2(), 1, 1, 4, 3, d.s32);
				for (int i = 0; i < a; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s32.saveGame();
			}
		}.get();

	}
	
}