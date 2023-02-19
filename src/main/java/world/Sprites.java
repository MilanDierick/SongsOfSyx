package world;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.ICON;
import snake2d.util.color.COLOR;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public class Sprites{

	private final PATH path = PATHS.SPRITE().getFolder("world").getFolder("map");
	
	public final TILE_SHEET forest = (new ITileSheet(path.get("Forest"), 972, 364) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s24;
			s.singles.init(0, 0, 1, 1, 16, 11, t);
			for (int i = 0; i < 11; i++)
				s.singles.setSkip(i*16, 16).paste(true);
			return t.saveGame();
			
		}
	}).get();
	
	public final LIST<COLOR> forest_colors = new IColorSampler() {
		
		@Override
		protected COLOR next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
			return s.singles.setSkip(i, 1).sample();
		}
		
		@Override
		protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, s.singles.body().y2(), 1, 1, 4, 4, d.s16);
			return 16;
		}
	}.getHalf();
	
	public final TILE_SHEET edge = (new ITileSheet(path.get("Edge"), 1176, 28) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			final ComposerSources.Full f = s.full;
			f.init(0, 0, 1, 1, 36, 1, t);
			f.setVar(0).paste(true);
			return t.saveGame();
			
		}
	}).get();
	
	public final TILE_SHEET mountain = (new ITileSheet(path.get("Mountain"), 576, 316) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house2.init(0, 0, 3, 3, t);

			s.house2.setVar(0).paste(2, true);
			s.house2.setVar(1).paste(2, true);
			s.house2.setVar(2).paste(1, true);
			s.house2.setVar(3).paste(2, true);
			s.house2.setVar(4).paste(2, true);
			s.house2.setVar(5).paste(1, true);
			
			s.house2.setVar(6).paste(true);
			s.house2.setVar(7).paste(true);
			s.house2.setVar(8).paste(1, true);

			s.full.init(0, s.house2.body().y2(), 1, 1, 16, 3, t);
			s.full.setSkip(16, 0).paste(1, true);
			s.full.setSkip(4, 16).paste(3, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET lake = (new ITileSheet(path.get("Lake"), 576, 100) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			s.house.setVar(0).setSkip(0, 1).pasteEdges(true);
			s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
			s.full.paste(true);
			
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET ocean = (new ITileSheet(path.get("Ocean"), 576, 144) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			s.full.init(0, s.house.body().y2(), 1, 1, 1, 1, d.s16);
			s.full.pasteEdges(true);
			
			s.house.init(0, s.full.body().y2(), 4, 1, t);
			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET river = (new ITileSheet(path.get("River"), 576, 116) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET delta = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.full.init(0, s.house.body().y2(), 1, 1, 16,2, t);
			s.full.paste(true);
			return t.saveGame();
		}
	}).get();
	

	
	public final Icons icons = new Icons(path);
	
	Sprites() throws IOException {
		
		
	}
	
	public final class Icons {
		
		public final ICON.MEDIUM ocean;
		public final ICON.MEDIUM abyss;
		public final ICON.MEDIUM mountain;
		public final ICON.MEDIUM lake;
		public final ICON.MEDIUM river;
		public final ICON.MEDIUM forest;

		private Icons(PATH path) throws IOException {
			SpriteData [] d = new ISpriteList(path.get("Icon"), 972, 36) {
				
				@Override
				protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(i, 1).paste(true);
					return d.s24.saveSprite();
				}
				
				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 16, 1, d.s24);
					return 16;
				}
			}.get();
			ocean = IIcon.MEDIUM.get(d[0]);
			abyss = IIcon.MEDIUM.get(d[1]);
			mountain = IIcon.MEDIUM.get(d[2]);
			lake = IIcon.MEDIUM.get(d[3]);
			river = IIcon.MEDIUM.get(d[4]);
			forest = IIcon.MEDIUM.get(d[5]);
		}
		
	}
	
	
}
