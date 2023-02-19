package init.sprite;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public class SettlementMap {



	public final TILE_SHEET wall_merge;
	public final FilthTexture filth;
	

	SettlementMap() throws IOException {

		final PATH path = PATHS.SPRITE_SETTLEMENT().getFolder("map");
		
		wall_merge = (new ITileSheet(path.get("WallMerge"), 152, 28) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s16;
				s.full.init(0, 0, 1, 1, 4, 1, t);
				s.full.setSkip(4, 0).paste(true);
				s.full.setSkip(4, 0).pasteRotated(3, true);

				return t.saveGame();
			}

		}).get();
		filth = new FilthTexture();
		

		
	}
	
	public static final class FilthTexture {
		
		private final TILE_SHEET sheet;
		private final static int vars = 16;
		private final static int amounts = 8;
		
		FilthTexture() throws IOException {
			sheet = new ITileSheet(PATHS.SPRITE_SETTLEMENT().getFolder("map").get("Filth"), 536, 140) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, vars, amounts, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}
			}.get();
		}
		
		public TextureCoords texture(double amount, int ran) {
			int i = (int) (amount*7)*vars;
			i += ran & 0x0F;
			return sheet.getTexture(i);
		}
	}
}
