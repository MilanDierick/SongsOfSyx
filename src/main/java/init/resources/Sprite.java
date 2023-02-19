package init.resources;

import java.io.IOException;
import java.nio.file.Path;

import init.sprite.ICON;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

final class Sprite{
	
	public final ICON.MEDIUM icon;
	public final TILE_SHEET carry;
	public final TILE_SHEET lay;
	public final COLOR color;
	
	Sprite(Path path) throws IOException {
		
		icon = IIcon.MEDIUM.get(new ISpriteData(path, 316, 94) {
			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 1, 1, d.s24).paste(true);;
				return d.s24.saveSprite();
			}
		}.get());
		
		carry = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 1, 4, d.s16);

				s.singles.setSkip(0, 2).paste(3, true);
				return d.s16.saveGame();
			}
		}.get();
		
		color = new IColorSamplerSingle() {
			
			@Override
			protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(2, 1);
				return s.singles.sample();
			}
		}.get();
		
		lay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(
						s.singles.body().x2(), 0, 
						1, 1, 
						4, 4, 
						d.s16);
				s.singles.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
	}

	
	static class Util {
		
		public TILE_SHEET getMinable(Path path) throws IOException {
			return new ITileSheet(path, 364, 94) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 4, d.s16);
					s.singles.paste(true);	
					return d.s16.saveGame();
				}
			}.get();
		}
		
		public TILE_SHEET getGrowable(Path path) throws IOException {
			return new ITileSheet(path, 364, 182) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 8, d.s16);
					s.singles.paste(true);
					return d.s16.saveGame();
				}
			}.get();
		}
		
	}
}