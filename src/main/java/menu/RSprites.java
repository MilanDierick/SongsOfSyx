package menu;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSprite;

final class RSprites {
	
	private final PATH g = PATHS.SPRITE().getFolder("menu");
	
	public final TILE_SHEET background = new ITileSheet(g.get("Background"), 13464, 396) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, 0, 1, 1, 210, 12, d.s32);
			s.full.paste(true);
			return d.s32.save(2);
		}
	}.get();
	
	private final static int lHeight = 67;
	public final SPRITE[] logoGlyps = new SPRITE[] {
		new ITileSprite(45,lHeight,1,g.get("GamatronLogo"), 1552, 92) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 3, 5, d.s16);
				s.full.paste(true);
				return d.s16.save(1);
			}
		},
		glyph(56),
		glyph(64),
		glyph(55),
		glyph(46),
		glyph(57),
		glyph(48),
		glyph(57),		
	};
	public final SPRITE logoFlash = glyph(55);
	public final SPRITE logoPresents = new ITileSprite(8*16,19,1) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(s.full.body().x2(), 0, 1, 1, 8, 2, d.s16);
			s.full.paste(true);
			return d.s16.save(1);
		}
	};
	public final SPRITE logo = new ITileSprite(352,192,1,g.get("Logo"), 728, 204) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, 0, 1, 1, 11, 6, d.s32);
			s.full.paste(true);
			return d.s32.save(1);
		}
	};
	public final COLOR[] logoColors = new COLOR[] {
		new ColorImp(61, 5, 15),
		new ColorImp(75, 26, 5),
		new ColorImp(84, 60, 10),
		new ColorImp(15, 75, 4),
		new ColorImp(15, 75, 10),
		new ColorImp(2, 10, 75),
		new ColorImp(61, 5, 15),
		new ColorImp(75, 30, 5),
	};
	
	public final SPRITE creditsSmallFrame;
	public final SPRITE[] creditsSmall;
	public final SPRITE creditsBigFrame;
	public final SPRITE[] creditsBig;
	
	private SPRITE glyph(int width) throws IOException {
		int tx = (int)Math.ceil((double)width/(16));
		return new ITileSprite(width,lHeight,1) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), 0, 1, 1, tx, 5, d.s16);
				s.full.paste(true);
				return d.s16.save(1);
			}
		};
	}

	RSprites() throws IOException{
		creditsSmall = new SPRITE[13];
		creditsSmallFrame =new ITileSprite(64,64,3,g.get("CreditSmall"), 2128, 76) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.full.init(0, 0, creditsSmall.length+1, 1, 2, 2, d.s32);
				s.full.setVar(0).paste(true);
				return d.s32.save(3);
			}
		};
	
		for (int i = 0; i < creditsSmall.length; i++) {
			final int k = i;
			
			creditsSmall[i] = new ITileSprite(64,64,3) {
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.setVar(k+1).paste(true);
					return d.s32.save(3);
				}
			};
		}
		creditsBig = new SPRITE[11];
		creditsBigFrame = new ITileSprite(96,128,3,g.get("CreditLarge"), 2596, 140) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.full.init(0, 0, creditsBig.length+1, 1, 3, 4, d.s32);
				s.full.setVar(0).paste(true);
				return d.s32.save(3);
			}
		};
		
		for (int i = 0; i < creditsBig.length; i++) {
			final int k = i;
			creditsBig[i] = new ITileSprite(96,128,3) {
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.setVar(k+1).paste(true);
					return d.s32.save(3);
				}
			};
		}
	}
}