package view.world.ui.regions;

import static view.world.ui.regions.UIRegion.*;

import game.faction.FACTIONS;
import init.config.Config;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.slider.GAllocator;
import util.info.GFORMAT;
import view.main.VIEW;
import view.world.ui.army.DivCard;
import world.map.regions.*;


final class Military extends GuiSection{

	Military(){
		
		RegionMilitary m = REGIOND.MILITARY();
		
		
		Card[] divs = new Card[Config.BATTLE.REGION_MAX_DIVS];
		for (int i = 0; i < divs.length; i++)
			divs[i] = new Card(i);
		
		addRightC(8, new DecreeButton(m.decreeSoldiers, SPRITES.icons().m.shield, COLOR.RED100) {
			
			@Override
			void hov(GBox b) {
				b.title(m.decreeSoldiers.info().name);
				b.add(GFORMAT.i(b.text(), (int)m.soldiers_target.getD(reg)));
				b.add(SPRITES.icons().s.arrow_right);
				b.add(GFORMAT.i(b.text(), (int)m.soldiers_target.next(reg)));
				b.NL();
				b.text(m.decreeSoldiers.info().desc);
				b.NL(8);
				
				UIRegion.hover(b, m.soldiers_target);
				
				b.NL(6);
				
				for (int di = 0; di < REGIOND.MILITARY().divisions(reg).size() && di < divs.length; di++) {
					b.add(divs[di]);
				}
				
			}

			@Override
			double current() {
				return (double)REGIOND.MILITARY().soldiers.getD(reg);
			}

			@Override
			double next() {
				
				return (double)REGIOND.MILITARY().soldiers_target.next(reg)/Config.BATTLE.REGION_MAX_MEN;
			}
		});
		
		
		
	}
	
	private static class Card implements SPRITE {

		private final int di;
		
		Card(int di){
			this.di = di;
		}
		
		@Override
		public int width() {
			return DivCard.WIDTH;
		}

		@Override
		public int height() {
			return DivCard.HEIGHT;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			if (di < REGIOND.MILITARY().divisions(reg).size())
				VIEW.world().UI.armies.divCard.render(r, X1, Y1, REGIOND.MILITARY().divisions(reg).get(di), true, false, false);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		visableSet(reg != FACTIONS.player().capitolRegion());
		super.render(r, ds);
	}
	
	private static abstract class DecreeButton extends GuiSection {
		

		public DecreeButton(RegionDecree d, ICON icon, COLOR color) {
			
			add(icon, 0, 0);
			addRightC(2, new GAllocator(color, new INTE() {
				
				@Override
				public int min() {
					return d.min(reg);
				}
				
				@Override
				public int max() {
					return d.max(reg);
				}
				
				@Override
				public int get() {
					return d.get(reg);
				}
				
				@Override
				public void set(int t) {
					d.set(reg, t);
				}
			}, 6, 16));
			
			add(new RENDEROBJ.RenderImp(body().width(), 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GMeter.renderDelta(r, current(), next(), body);
				}
			}, body().x1(), body().y2()+2);
			
			pad(2);
			
			
		}
		
		abstract double current();
		abstract double next();
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(),-1);
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			hov((GBox) text);
		}
		
		abstract void hov(GBox b);
		
	}
	
}
