package view.world.ui.army;

import game.GAME;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.dic.DicArmy;
import util.dic.DicRes;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import world.World;
import world.army.WDIV;

class UIArmyMerenary extends GuiSection{


	private int max = World.ARMIES().mercenaries().size();
	private final Card[] cards = new Card[World.ARMIES().mercenaries().size()];
	
	UIArmyMerenary() {
		
		for (int i = 0; i < World.ARMIES().mercenaries().size(); i++) {
			Card c = new Card(i);
			cards[i] = c;
			add(c, (i%12)*c.body().width(), (i/12)*(c.body().height()+4));
		}
		
		addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)FACTIONS.player().credits().credits());
			}
		}.hv(DicRes.¤¤Currs));
		
		
		GStat creds = new GStat() {
			
			@Override
			public void update(GText text) {
				int am = cost();
				GFORMAT.i(text, am);
				if (am == 0 || am > FACTIONS.player().credits().credits() || !UIArmy.army.divs().canAdd())
					text.errorify();
				else
					text.normalify();
				
			}
		};
		
		GButt b = new GButt.BStat2(DicArmy.¤¤Recruit, creds) {
			
			@Override
			protected void clickA() {
				int am = cost();
				if (am > FACTIONS.player().credits().credits())
					return;
				for (Card c : cards) {
					if (c.activeIs() && c.selectedIs() && UIArmy.army.divs().canAdd()) {
						c.div.reassign(UIArmy.army);
						GAME.player().credits().mercinaries.OUT.inc(World.ARMIES().mercenaries().upkeepCost(c.ii));
					}
				}
				VIEW.inters().popup.close();
			}
			
			@Override
			protected void renAction() {
				activeSet(cost() > 0 && cost() <= FACTIONS.player().credits().credits());
			}
			
		};
		
		addRelBody(8, DIR.S, b);
		
	}
	
	private int cost() {
		int am = 0;
		for (Card c : cards) {
			if (c.activeIs() && c.selectedIs()) {
				am += World.ARMIES().mercenaries().signingCost(c.ii)*2;
			}
		}
		return am;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		max = (int) (World.ARMIES().mercenaries().size()*CLAMP.i(FACTIONS.player().kingdom().realm().regions().size(), 1, 10)/10.0);
		super.render(r, ds);
		
	}
	

	
	
	private GText tmp = new GText(UI.FONT().S, 8);
	
	private class Card extends ClickableAbs{

		private final int ii;
		private final WDIV div;
		
		Card(int ii){
			this.ii = ii;
			div = World.ARMIES().mercenaries().get(ii);
			body.setDim(DivCard.WIDTH+20, DivCard.HEIGHT+20);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			visableSet(ii < max);
			activeSet(div.army() == null);
			selectedSet(selectedIs() && activeIs() && visableIs());
			
			if (!selectedIs()) {
				if (cost() + World.ARMIES().mercenaries().signingCost(ii) > FACTIONS.player().credits().credits())
					activeSet(false);
			}
			
			GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-1);
			

			VIEW.world().UI.armies.divCard.render(r, body().x1(), body().y1(), div, activeIs(), selectedIs(), isHovered);
			
			
			
			tmp.clear();
			GFORMAT.i(tmp, World.ARMIES().mercenaries().signingCost(ii));
			tmp.adjustWidth();
			if (cost() + World.ARMIES().mercenaries().signingCost(ii) > FACTIONS.player().credits().credits())
				tmp.errorify();
			else
				tmp.normalify();
			tmp.renderC(r, body().cX(), body().y2()-10);
			
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			div.hover(b);
			if (div.army() != null) {
				b.NL();
				b.textL(DicArmy.¤¤Conscripted);
				b.text(div.army().name);
				
			}
		}
		
		@Override
		protected void clickA() {
			selectedToggle();
		}
		
		
	}
	
}
