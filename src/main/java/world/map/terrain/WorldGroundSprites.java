package world.map.terrain;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

final class WorldGroundSprites {

	public final TILE_SHEET stencil;
	public final TILE_SHEET lush;
	public final TILE_SHEET normal;
	public final TILE_SHEET desert;
	public final TILE_SHEET steppe;
	public final TILE_SHEET cracked;
	
	public final int DIM = 8;
	
	public WorldGroundSprites() throws IOException {
		
		stencil = new ITileSheet(PATHS.SPRITE_WORLD_MAP().get("Ground"), 576, 300) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, 0, 4, 1, d.s16);
				for (int i = 0; i < 4; i++)
					s.house.setVar(i).paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
		normal = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.house.body().y2(), 2, 4, DIM, DIM, d.s16);
				s.full.setVar(0);
				s.full.paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
		lush = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setVar(1);
				s.full.paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
		cracked = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setVar(2);
				s.full.paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
		desert = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setVar(3);
				s.full.paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
		steppe = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setVar(4);
				s.full.paste(true);
				return d.s16.saveGame();
				
			}
		}.get();
		
	}
	
	public int ran(int tx, int ty) {
		return (tx&(DIM-1)) + (ty&(DIM-1))*DIM;
	}
	
	public final void renderNormal(TILE_SHEET sheet, SPRITE_RENDERER r, int x, int y, int ran) {
		sheet.render(r, ran&63, x, y);
	}
	
	public final void renderStenciled(TILE_SHEET sheet, SPRITE_RENDERER r, int x, int y, int mask, int ran1, int ran2) {
		stencil.renderTextured(sheet.getTexture(ran1&63), mask + 16*((ran2)&3), x, y);
	}
	
	
}
