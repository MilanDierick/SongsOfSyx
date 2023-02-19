package init.sprite.UI;

import java.io.IOException;

import init.C;
import init.paths.PATHS;
import init.race.Race;
import init.sprite.ICON;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.*;
import util.data.BOOLEAN;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public final class UIPanels {

	
	public final TILE_SHEET panelLarge;
	public final LIST<SPRITE> box;
	public final LIST<SPRITE> boxPanel;
	public final LIST<SPRITE> frame;
	public final LIST<ICON.SMALL> buttCheckbox;
	public final LIST<SPRITE> buttBG;
	public final UICons panelM;
	public final UICons panelL;


	UIPanels(Race race) throws IOException {

		panelLarge = new ITileSheet(PATHS.SPRITE_UI().get("Panel"), 760, 332) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

				
				s.full.init(0, 0, 1, 1, 8, 3, d.s32);
				s.full.setSkip(8, 0).paste(true);
				s.full.setSkip(1, 0).pasteRotated(1, true);
				s.full.setSkip(1, 8).paste(true);
				s.full.setSkip(1, 9).paste(true);
				s.full.setSkip(1, 15).paste(true);
				s.full.setSkip(8, 16).paste(true);
				return d.s32.saveGui();
			}
		}.get();

		box = ISprite.gui(new ISpriteList() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 3, 3, d.s16);
				return 9;
			}

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setSkip(1, i).paste(true);
				return d.s16.saveSprite();
			}

		}.get());
		
		boxPanel = ISprite.gui(new ISpriteList() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 3, 3, d.s16);
				return 9;
			}

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setSkip(1, i).paste(true);
				return d.s16.saveSprite();
			}

		}.get());
		
		frame = ISprite.gui(new ISpriteList() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 3, 3, d.s16);
				return 9;
			}

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setSkip(1, i).paste(true);
				return d.s16.saveSprite();
			}

		}.get());

		buttCheckbox = IIcon.SMALL.get(new ISpriteList() {
			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 13, 1, d.s16);
				s.full.setSkip(1, 0);
				return 4;
			}

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.paste(true);
				s.full.setNextSingle();
				return d.s16.saveSprite();
			}

		}.get());
		
		{
			ArrayList<SPRITE> sp = new ArrayList<>(3);
			sp.add(new ITileSprite(36, 36, C.SG) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.full.body().y2(), 1, 1, 2, 2, d.s24);
					s.full.paste(true);
					return d.s24.saveGui();
				}
			});
			sp.add(ISprite.gui(new ISpriteData() {
				
				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 1, 1, d.s32);
					s.full.paste(true);
					return d.s32.saveSprite();
				}
			}.get()));
			
			sp.add(ISprite.gui(new ISpriteData() {
				
				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 1, 1, d.s24);
					s.full.paste(true);
					return d.s24.saveSprite();
				}
			}.get()));
			
			
			buttBG = sp;
		}

		panelM = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(s.full.body().x2(), s.full.body().y1(), 1, 1, d.s24);
				s.house.paste(true);
				return d.s24.saveGui();
			}
		}.get());

		panelL = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(s.house.body().x2(), s.house.body().y1(), 1, 1, d.s32);
				s.house.paste(true);
				return d.s32.saveGui();
			}
		}.get());
		
	}

	public SPRITE checkSprite(BOOLEAN b) {
		
		return new SPRITE() {
			
			@Override
			public int width() {
				return buttCheckbox.get(0).width();
			}
			
			@Override
			public int height() {
				return buttCheckbox.get(1).width();
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (b.is()) {
					buttCheckbox.get(2).render(r, X1, Y1);
				}else {
					buttCheckbox.get(0).render(r, X1, Y1);
				}
				
			}
		};
		
	}


}