package init.race.appearence;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerDests.Tile;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RaceSprites{
	
	private static int from = 0;
	public final TILE_SHEET blood;
	public final TILE_SHEET grit;
	public final TILE_SHEET Lblood;
	public final TILE_SHEET Lgrit;
	public final TILE_SHEET pBlood;
	public final TILE_SHEET pFilth;
	public final TILE_SHEET gore_stencil;
	public final TILE_SHEET gore_overlay;

	public RaceSprites() throws IOException{
		

		blood = new ITileSheet(PATHS.SPRITE().getFolder("race").getFolder("misc").get("Overlays"), 564, 486) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 2, 24, d.s24);
				from = 0;
				return gets(8, d.s24, s);
			}
		}.get();
		grit = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return gets(8, d.s24, s);
			}
		}.get();


		
		Lblood = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 2, 24, d.s32);
				from = 0;
				return gets(8, d.s32, s);
			}
		}.get();
		

		
		Lgrit = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 2, 24, d.s32);
				from = 0;
				return gets(8, d.s32, s);
			}
		}.get();
		
		pFilth = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.singles.body().x2(), 0, 1, 4, PortraitFrame.TILES_X, PortraitFrame.TILES_Y, d.s8);
				for (int i = 0; i < 4; i++)
					s.full.setVar(i).paste(true);
				return d.s8.save(1);
			}
		}.get();
		
		pBlood = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x1(), s.full.body().y2(), 1, 4, PortraitFrame.TILES_X, PortraitFrame.TILES_Y, d.s8);
				for (int i = 0; i < 4; i++)
					s.full.setVar(i).paste(true);
				return d.s8.save(1);
			}
		}.get();
		
		gore_stencil = new ITileSheet(PATHS.SPRITE().getFolder("race").getFolder("misc").get("Gore"), 316, 158) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 4, 4, d.s32);
				s.singles.setSkip(0, 8).paste(true);
				return d.s32.saveGame();
			}
		}.get();
		
		gore_overlay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(8, 8).paste(true);
				return d.s32.saveGame();
			}
		}.get();
		
	}
	
	private TILE_SHEET gets(int nr, Tile d, ComposerSources s) {
		for (int i = 0; i < nr; i++) {
			s.singles.setSkip((from+i) * 2, 2).paste(3, true);
		}
		from += nr;
		return d.saveGame();
	}
	
}