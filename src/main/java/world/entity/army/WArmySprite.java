package world.entity.army;

import java.io.IOException;

import game.GAME;
import init.C;
import init.config.Config;
import init.paths.PATHS;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.colors.GCOLORS_MAP;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import world.WORLD;
import world.army.AD;

class WArmySprite {


	
	final TILE_SHEET sheet = (new ITileSheet(PATHS.SPRITE().getFolder("world").getFolder("entity").get("Army"), 136, 104) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			
			s.singles.init(0, 0, 2, 1, 2, 6, d.s8);
			s.singles.setVar(0);
			for (int i = 0; i < 7; i++) {
				s.singles.setSkip(i*2, 2);
				s.singles.paste(3, true);
			}
			s.singles.setVar(1);
			for (int i = 0; i < 7; i++) {
				s.singles.setSkip(i*2, 2);
				s.singles.paste(3, true);
			}
			
			
			return d.s8.saveGame();
			
		}
	}).get();
	
	static final int OFF = 8*7;
	static final int OFF_BOAT = 8*3;
	
	WArmySprite() throws IOException{
		
	}
	
	private static final int[][] grid = new int[][] {
		{ 0,11,10, 1},
		{ 9,12,13,8},
		{ 7,14,15,6},
		{ 3, 5, 4, 2},
	};
	
	void render(WArmy a, Renderer r, ShadowBatch s, int x, int y, DIR dir) {
		
		COLOR color = a.faction() == null ? COLOR.WHITE50 : a.faction().banner().colorBG();
		
		GCOLORS_MAP.get(a.faction()).bind();
		
		SPRITES.cons().BIG.outline_dashed.renderBox(r, x+16, y+16, a.body().width()-32, a.body().height()-32);
		COLOR.unbind();
//		World.ENTITIES().armies.sprite.sMid.renderC(r, x+a.body().width()/2, y+a.body().height()/2);
//		COLOR.unbind();

		s.setHeight(1).setDistance2Ground(1);
		s.setHard();
		
		int d = 0;
		if (a.path().moving(a.body()))
			d = 8*GAME.intervals().get05() % 3;
		d*= 8;
		if (WORLD.WATER().has.is(a.ctx(), a.cty()))
			d += OFF_BOAT;
		else if(a.state() == WArmyState.fortified) {
			d = 2*OFF_BOAT;
		}
		
		
		
		
		
		
		
		int size = (int) Math.ceil(32*(double)AD.men(null).get(a)/Config.BATTLE.MEN_PER_ARMY);
		
		for (int yy = 0; yy < 4; yy++) {
			for (int xx = 0; xx < 4; xx++) {
				if (grid[yy][xx] + size < 15)
					continue;
				sheet.render(r, d+WArmySprite.OFF+dir.id(), x+xx*C.TILE_SIZEH, y+yy*C.TILE_SIZEH);
				color.bind();
				sheet.render(r, d+dir.id(), x+xx*C.TILE_SIZEH, y+yy*C.TILE_SIZEH);
				COLOR.unbind();
				sheet.render(s, d+WArmySprite.OFF+dir.id(), x+xx*C.TILE_SIZEH, y+yy*C.TILE_SIZEH);
			}
		}
		
	}
	
}
