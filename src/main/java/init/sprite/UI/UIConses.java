package init.sprite.UI;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public class UIConses{
	
	{
		new IInit(PATHS.SPRITE_UI().get("Cons"), 1088, 155) {
			
			@Override
			protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
				s.singles.init(0, 144, 1, 1, 32, 1, d.s16);
			}
		};
	}
	
	public final Color color = new Color();
	public final Small TINY = new Small();
	public final Big BIG = new Big();
	
	public final Icons ICO = new Icons();
	public final Rotaters ROT = new Rotaters();
	public final TILE_SHEET fullArrows = new ITileSheet() {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, s.singles.body().y2(), 1, 1, 1, 1, d.s16);
			s.singles.paste(3, true);
			s.combo.init(s.singles.body().x2(), s.singles.body().y1(), 1, 1, 2, d.s16);
			s.combo.paste(3, true);
			s.combo.init(s.combo.body().x2(), s.singles.body().y1(), 1, 1, 3, d.s16);
			s.combo.paste(3, true);
			return d.s16.saveGame();

		}
	}.get();
	
	
	

	
	public UIConses() throws IOException{
		
	}


	

	
	public class Color {
		
		public final COLOR ok = COLOR.BLUE100;
		public final COLOR great = new ColorImp(0, 127, 127);
		public final COLOR ok2 = new ColorImp(20, 127, 127);
		public final COLOR ok3 = ok2.shade(0.75);
		public final COLOR blocked = COLOR.RED100;
		public final COLOR semiblocked = COLOR.ORANGE100;
		
		private Color() {
			
		}
		
	}
	
	public final class Small {
		
		public final UICons high = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, 0, 6, 1, d.s8);
				s.house.setVar(0).paste(true);
				return d.s8.save(C.SCALE*2);
			}
		}.get());
		public final UICons low = getTiny(1);
		public final UICons flat = getTiny(2);
		public final UICons outline = getTiny(3);
		public final UICons dashed = getTiny(4);
		public final UICons full = getTiny(5);
		
		private Small() throws IOException{
			
		}
		
		
		private UICons getTiny(int nr) throws IOException {
			
			return new UICons(new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.setVar(nr).paste(true);
					return d.s8.save(C.SCALE*2);
				}
			}.get());
			
		}
		
	}
	
	public final class Big {
		
		public final UICons outline = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.house.init(0, s.house.body().y2(), 5, 2, d.s16);
				s.singles.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
				s.house.setVar(0).paste(true);
				s.singles.setSkip(0, 1);
				s.singles.pasteEdges(true);
				return d.s16.saveGame();
			}
		}.get(), TINY.outline);
		public final UICons dashed = getSmall(1, TINY.dashed);
		public final UICons dashedThick = getSmall(2, TINY.dashed);
		public final UICons solid = getSmall(3, TINY.full);
		public final UICons dots = getSmall(4, TINY.dashed);
		public final UICons outline_dashed = getSmall(5, TINY.dashed);
		public final UICons outline_dashed_small = getSmall(6, TINY.dashed);
		public final UICons filled = getSmall(7, TINY.full);
		public final UICons outline_thin = getSmall(8, TINY.outline);
		public final UICons filled_striped = getSmall(9, TINY.full);
		
		private Big() throws IOException{
			
		}
		
		private UICons getSmall(int nr, UICons tiny) throws IOException {
			
			return new UICons(new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.setVar(nr).paste(true);
					s.singles.setSkip(nr, 1);
					s.singles.pasteEdges(true);
					return d.s16.saveGame();
				}
				
				
			}.get(), tiny);
			
		}
	}
	

	
	public final class Icons {
		
		public final SPRITE unclear = ISprite.game(new ISpriteData() {

			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, s.singles.body().y2(), 1, 1, 19, 1, d.s16);
				s.singles.setSkip(0, 1).paste(true);
				return d.s16.saveSprite();
			}
			
		}.get()); 
		public final SPRITE clear = getS(1);
		public final SPRITE cancel = getS(2);

		
		public final LIST<SPRITE> arrows = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(3, 1).pasteRotated(i, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return 4;
			}
			
		}.get()); 
		public final LIST<SPRITE> arrows2 = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(4+ (i & 1), 1).pasteRotated(i/2, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return 8;
			}
			
		}.get());
		public final SPRITE crosshair = getS(6);
		public final SPRITE smallup = getS(7);
		public final SPRITE repair = getS(8);
		public final SPRITE arrows_inwards = getS(9);
		public final SPRITE warning = getS(10);
		public final SPRITE tile = getS(11);
		public final SPRITE scratch = getS(12);
		
		private Icons() throws IOException{
			
		}
		
		private SPRITE getS(int nr) throws IOException {
			return ISprite.game(new ISpriteData() {

				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(nr, 1).paste(true);
					return d.s16.saveSprite();
				}
				
			}.get()); 
			
		}
		
		
	}
	
	public final class Rotaters {
		
		public final LIST<SPRITE> single = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(0, 1).pasteRotated(i, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, s.singles.body().y2(), 1, 1, 8, 1, d.s16);
				return 4;
			}
			
		}.get()); 
		public final LIST<SPRITE> join = getS(1);
		public final LIST<SPRITE> join_thin = getS(2);
		public final LIST<SPRITE> north_south = getS(3);
		public final LIST<SPRITE> full = getS(4);
		public final LIST<SPRITE> join_big = getS(5);

		

		
		private Rotaters() throws IOException{
			
		}
		
		private LIST<SPRITE> getS(int nr) throws IOException {
			return ISprite.game(new ISpriteList() {

				@Override
				protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(nr, 1).pasteRotated(i, true);
					return d.s16.saveSprite();
				}

				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return 4;
				}
				
			}.get()); 
			
		}
		
		
	}
	
}