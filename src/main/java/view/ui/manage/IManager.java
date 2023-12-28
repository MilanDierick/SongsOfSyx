package view.ui.manage;

import game.GAME;
import game.faction.FACTIONS;
import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicArmy;
import util.dic.DicRes;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.ui.UIView;
import view.ui.wiki.WIKI;

public final class IManager {
	
	public static final int TOP_HEIGHT = 40;
	
	private final GuiSection top = new GuiSection();
	private IFullView current;
	private final Inter inter = new Inter();
	
	public IManager(UIView view) {
		ArrayListGrower<IFullView> all = new ArrayListGrower<>();
		all.add(view.goods);
		all.add(view.trade);
		all.add(view.tourists);
		all.add(view.tech);
		all.add(view.factions);

		for (IFullView w : all) {
			
			GButt.ButtPanel b = new GButt.ButtPanel(w.title) {
				@Override
				protected void clickA() {
					show(w);
				};
				@Override
				protected void renAction() {
					selectedSet(w == current);
				}
			};
			b.setDim(180, 34);
			top.addRightC(0, b);		
		}
		
		{
			GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.questionmark) {
				@Override
				protected void clickA() {
					show(view.wiki);
				};
				@Override
				protected void renAction() {
					selectedSet(view.wiki == current);
				}
			};
			b.hoverTitleSet(WIKI.¤¤name);
			b.setDim(60, 34);
			top.addRightC(0, b);		
		}
		
		{
			GButt.ButtPanel b = new GButt.ButtPanel(FACTIONS.player().banner().MEDIUM) {
				@Override
				protected void clickA() {
					show(view.level);
				};
				@Override
				protected void renAction() {
					selectedSet(view.level == current);
				}
			};
			b.hoverTitleSet(view.level.title);
			b.setDim(60, 34);
			top.addRightC(0, b);		
		}
		
		top.body().centerX(C.DIM());
		GButt.ButtPanel exit = new GButt.ButtPanel(SPRITES.icons().m.exit) {
			
			@Override
			protected void clickA() {
				inter.hide();
			}
			
		};
		exit.body.moveX2(C.WIDTH()-8);
		exit.body.centerY(top);
		top.add(exit);
		
		top.body().centerY(0, TOP_HEIGHT);
	}
	
	public void show(IFullView view) {
		current = view;
		current.section.body().moveY1(IFullView.TOP_HEIGHT);
		current.section.body().moveX1(16);
		
		inter.activate();
		
	}
	
	public void close() {
		inter.hide();
	}
	
	private class Inter extends Interrupter {
		

		
		
		
		public Inter(){
			
			
			
		}
		
		
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			current.section.hover(mCoo); 
			top.hover(mCoo);
			return true;
		}
	 
		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.RIGHT) {
				if (!current.back())
					hide();
			}else if(button == MButt.LEFT) {
				current.section.click();
				top.click();
			}
		}
		
		@Override
		protected void hoverTimer(GBox text) {
			current.section.hoverInfoGet(text);
			top.hoverInfoGet(text);
		}

		@Override
		protected boolean update(float ds) {
			GAME.SPEED.tmpPause();
			return false;
		}
		
		@Override
		protected boolean render(Renderer r, float ds) {
			
			GCOLOR.UI().bg().render(r, C.DIM());
			UI.PANEL().butt.render(r, 0, C.WIDTH(), 0, TOP_HEIGHT, 0, DIR.S.mask());
			current.section.render(r, ds);
			top.render(r, ds);
			return false;
		}
		
		@Override
		public void hide() {
			// TODO Auto-generated method stub
			super.hide();
		}
		
		public void activate() {
			super.show(VIEW.inters().manager);
		}
		
	}
	
	public CLICKABLE butt() {
		CLICKABLE c = new CLICKABLE.ClickableAbs(145, 46) {
			
			private final GText tt = new GText(UI.FONT().S, 16);
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				
				tt.clear();
				GFORMAT.i(tt, (int)FACTIONS.player().credits().credits());
				render(r, SPRITES.icons().s.money, 0);

				tt.clear();
				render(r, SPRITES.icons().s.urn, 1);
				
				tt.clear();
				GFORMAT.i(tt, (int) GAME.player().tech.available().get());
				render(r, SPRITES.icons().s.vial, 2);
				
				tt.clear();
				int am = FACTIONS.DIP().war.getEnemies(FACTIONS.player()).size();
				if (am > 0) {
					GFORMAT.i(tt, am);
					tt.errorify();
				}
				render(r, SPRITES.icons().s.crown, 3);
				
			}
			
			private void render(SPRITE_RENDERER r, SPRITE s, int i) {
				
				int y1 = body().y1()+ 5 + 18*(i /2);
				int x1 = body().x1()+ 5 + 80*(i%2);
				s.render(r, x1, y1);
				tt.render(r, x1 + 18, y1);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				{
					b.add(SPRITES.icons().s.money);
					b.textLL(DicRes.¤¤Currs);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), (int)FACTIONS.player().credits().credits()));
					b.NL(4);
				}

				{
					b.add(SPRITES.icons().s.vial);
					b.textLL(FACTIONS.player().tech.info.name);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), (int)GAME.player().tech.available().get()));
					b.NL(4);
				}
				
				{
					
					b.add(UI.icons().s.urn);
					b.textLL(DicRes.¤¤Stored);
					b.tab(6);
					b.add(GFORMAT.iofkNoColor(b.text(), SETT.ROOMS().STOCKPILE.tally().totalAmount(), SETT.ROOMS().STOCKPILE.tally().totalSpace()));
					b.NL(4);
				}
				
				{
					b.add(SPRITES.icons().s.crown);
					b.textLL(DicArmy.¤¤Enemies);
					b.tab(6);
					b.add(GFORMAT.i(b.text().errorify(), (int)FACTIONS.DIP().war.getEnemies(FACTIONS.player()).size()));
					b.NL(4);
				}
				
				super.hoverInfoGet(text);
			}
			
			@Override
			protected void clickA() {
				if (current != null)
					show(current);
				else
					show(VIEW.UI().goods);
			}
			
		};

		return c;
	}
	
}