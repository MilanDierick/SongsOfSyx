package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sets.ArrayList;
import util.dic.*;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.battle.UIDivCardW;
import world.WORLD;
import world.army.ADDiv;

class ArmyMerenary {


	private int max = WORLD.ARMIES().mercenaries().size();
	private final Card[] cards = new Card[WORLD.ARMIES().mercenaries().size()];
	private final ArrayList<Card> active = new ArrayList<>(cards.length);
	private GuiSection scards = new GuiSection();
	private final GuiSection section = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			arrange();
			super.render(r, ds);
			
		}
	};
	
	
	
	private static int xs = 10;
	private int width = 80;
	private int height = UIDivCardW.HEIGHT+20;
	
	ArmyMerenary() {
		
		for (int i = 0; i < WORLD.ARMIES().mercenaries().size(); i++) {
			Card c = new Card(i);
			cards[i] = c;
		}
		
		
		scards.body().setDim(xs*width, Math.ceil((double)max/xs)*height);
		
		section.add(scards);
		
		GuiSection bb = new GuiSection();
		
		bb.addRightC(8, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)FACTIONS.player().credits().credits());
			}
		}.hh(DicRes.¤¤Currs));
		
		bb.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				int co = cost();
				if (co > FACTIONS.player().credits().credits())
					text.errorify();
				else
					text.normalify();
				
				GFORMAT.iIncr(text, -co);
			}
		}.hh(DicMisc.¤¤Cost));
		
		bb.addRightC(64, new GButt.ButtPanel(DicArmy.¤¤Recruit) {
			
			@Override
			protected void renAction() {
				activeSet(Army.army.divs().canAdd() && cost() > 0 && cost() <= FACTIONS.player().credits().credits());
			}
			
			@Override
			protected void clickA() {
				for (Card c : active) {
					if (c.selectedIs() && Army.army.divs().canAdd()) {
						int cost = WORLD.ARMIES().mercenaries().signingCost(c.ii);
						if (cost < FACTIONS.player().credits().credits()) {
							c.div.reassign(Army.army);
							GAME.player().credits().inc(-WORLD.ARMIES().mercenaries().signingCost(c.ii), CTYPE.MERCINARIES);
						}
					}
				}
				VIEW.inters().popup.close();
				super.clickA();
			}
			
		});
		
		if (S.get().developer) {
			bb.addRightC(16, new GButt.ButtPanel("shuffle") {
				
				@Override
				protected void clickA() {
					WORLD.ARMIES().mercenaries().debug();
				}
				
			});
		}
		
		section.addRelBody(8, DIR.S, bb);
		
	}
	
	public void arrange() {
		active.clearSloppy();
		int max = WORLD.ARMIES().mercenaries().max();   
		for (int i = 0; i < max; i++) {
			ADDiv d = WORLD.ARMIES().mercenaries().get(i);
			if (d.army() != null)
				continue;
			if (d.men() == 0)
				continue;
			active.add(cards[i]);
		
			
		}

		int x1 = scards.body().x1();
		int y1 = scards.body().y1();
		scards.clear();
		for (int i = 0; i < active.size(); i++) {
			Card c = active.get(i);
			scards.add(c, (i%xs)*c.body().width(), (i/xs)*(c.body().height()));
			
		}
		scards.body().moveX1Y1(x1, y1);
		
	}
	
	private int cost() {
		int am = 0;
		for (Card c : active) {
			if (c.selectedIs()) {
				am += WORLD.ARMIES().mercenaries().signingCost(c.ii);
			}
		}
		return am;
	}

	public GuiSection get() {
		for (Card c : cards)
			c.selectedSet(false);
		arrange();
		return section;
	}
	
	private GText tmp = new GText(UI.FONT().S, 8);
	
	private class Card extends ClickableAbs{

		private final int ii;
		private final ADDiv div;
		
		Card(int ii){
			this.ii = ii;
			div = WORLD.ARMIES().mercenaries().get(ii);
			body.setDim(width, height);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

			isActive = WORLD.ARMIES().mercenaries().signingCost(ii) <= FACTIONS.player().credits().credits();
			isSelected &= isActive;
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			GButt.ButtPanel.renderFrame(r, body);

			VIEW.UI().battle.cardW.render(r, body().cX()-UIDivCardW.WIDTH/2, body().y1()+6, div, isActive, false, false);
			
			
			
			tmp.clear();
			GFORMAT.i(tmp, WORLD.ARMIES().mercenaries().signingCost(ii));
			tmp.adjustWidth();
			if (cost() + WORLD.ARMIES().mercenaries().signingCost(ii) > FACTIONS.player().credits().credits())
				tmp.errorify();
			else
				tmp.normalify();
			tmp.renderC(r, body().cX(), body().y2()-10);
			
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			VIEW.UI().battle.divHoverer.hover(div, text);
		}
		
		@Override
		protected void clickA() {
			selectedToggle();
		}
		
		
	}
	
}
