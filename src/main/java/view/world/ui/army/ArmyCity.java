package view.world.ui.army;

import init.config.Config;
import settlement.army.Div;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.main.VIEW;
import view.ui.battle.UIDivCardSett;
import world.WORLD;

class ArmyCity extends GuiSection{


	private static int xs = 8;
	private final ArrayList<Card> cards = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<Card> current = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<Div> li = new ArrayList<Div>(1);
	private final UIDivCardSett card = VIEW.UI().battle.cardS;
	int selected;
	
	ArmyCity(){
		
		for (Div d : SETT.ARMIES().player().divisions()) {
			cards.add(new Card(d));
		}
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				int am = CLAMP.i(current.size()+1, 0, Config.BATTLE.DIVISIONS_PER_ARMY);
				return (int) Math.ceil((double)am/xs);
			}
		};
		
		bu.column(null, xs*card.width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return row(ier);
			}
		});
		
		add(bu.create(4, false));
		
		GButt.ButtPanel but = new GButt.ButtPanel(DicMisc.¤¤confirm) {
			
			@Override
			protected void renAction() {
				activeSet(selected > 0);
			}
			
			@Override
			protected void clickA() {
				
				for (Card c : current) {
					li.clearSloppy();
					li.add(c.div);
					if (Army.army.divs().canAdd() && c.selectedIs() && c.div.info.men() > 0 && WORLD.ARMIES().cityDivs().attachedArmy(c.div) == null && VIEW.s().ui.army.hoverSendOutProblem(li, GBox.Dummy())) {
						WORLD.ARMIES().cityDivs().attach(Army.army, c.div);
					}
				}
				
				VIEW.inters().popup.close();
			}
			
		};
		
		addRelBody(16, DIR.S, but);
		
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		init();
		super.render(r, ds);
	}
	
	private void init() {
		current.clearSloppy();
		selected = 0;
		for (Card c : cards) {
			li.clearSloppy();
			li.add(c.div);
			if (c.div.info.men() > 0 && WORLD.ARMIES().cityDivs().attachedArmy(c.div) == null && VIEW.s().ui.army.hoverSendOutProblem(li, GBox.Dummy())) {
				current.add(c);
				if (c.selectedIs())
					selected ++;
			}else {
				c.selectedSet(false);
			}
		}
	}
	
	private RENDEROBJ row(GETTER<Integer> ier) {
		GuiSection ss = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				int x1 = body().x1();
				int y1 = body().y1();
				clear();
				for (int i = 0; i < xs; i++) {
					int k = ier.get()*xs + i;
					if (k >= current.size()){
						break;
					}else {
						addRightC(0, current.get(k));
					}
				}
				body().moveX1Y1(x1, y1);
				body().setWidth(card.width*xs);
				body().setHeight(card.height);
				super.render(r, ds);
			}
			
		};
		ss.body().setWidth(card.width*xs);
		ss.body().setHeight(card.height);
		return ss;
	}
	
	private class Card extends ClickableAbs {

		private final Div div;
		
		Card(Div div){
			super(card.width, card.height);
			this.div = div;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			card.render(r, body.x1(), body.y1(), div, isActive, isSelected, isHovered);
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			VIEW.s().ui.army.hover(div, text);
			text.NL();
			li.clearSloppy();
			li.add(div);
			VIEW.s().ui.army.hoverSendOutProblem(li, text);
		}
		
		@Override
		protected void clickA() {
			selectedSet(!selectedIs());
		}
		
		
	}
	
}
