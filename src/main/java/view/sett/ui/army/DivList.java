package view.sett.ui.army;

import init.config.Config;
import settlement.army.Div;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.GETTER;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.keyboard.KEYS;
import view.main.VIEW;

final class DivList extends GuiSection{

	private static int xs = 6;
	private final ArrayList<Card> cards = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<Card> current = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<Div> selection;
	private Card clicked;
	private boolean dragging;
	
	DivList(int height, ArrayList<Div> selection){
		
		this.selection = selection;
		
		for (Div d : SETT.ARMIES().player().divisions()) {
			cards.add(new Card(d.indexArmy()));
		}
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				int am = CLAMP.i(current.size(), 0, Config.BATTLE.DIVISIONS_PER_ARMY);
				return (int) Math.ceil((double)am/xs);
			}
		};
		
		bu.column(null, xs*VIEW.UI().battle.cardS.width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return row(ier);
			}
		});
		
		add(bu.createHeight(height, false));
		
	}
	

	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		init();
		super.render(r, ds);
		if (!MButt.LEFT.isDown()) {
			dragging = false;
		}
	
	}
	
	private void init() {
		current.clearSloppy();
		selection.clearSloppy();
		for (Card c : cards) {
			if (c.div().info.men() > 0) {
				current.add(c);
				if (c.selectedIs())
					selection.add(c.div());
			}else {
				c.selectedSet(false);
			}
		}
	}
	
	public LIST<Div> selection(){
		return selection;
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
				body().setWidth(VIEW.UI().battle.cardS.width*xs);
				body().setHeight(VIEW.UI().battle.cardS.height);
				super.render(r, ds);
			}
			
		};
		ss.body().setWidth(VIEW.UI().battle.cardS.width*xs);
		ss.body().setHeight(VIEW.UI().battle.cardS.height);
		return ss;
	}
	
	private class Card extends ClickableAbs {

		private final int di;
		
		Card(int di){
			super(VIEW.UI().battle.cardS.width, VIEW.UI().battle.cardS.height);
			this.di = di;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Div div = div();
			VIEW.UI().battle.cardS.render(r, body.x1(), body.y1(), div, isActive, isSelected, isHovered);

			if (dragging && isHovered && clicked != null && clicked != this && !KEYS.MAIN().UNDO.isPressed() && !KEYS.MAIN().MOD.isPressed()) {
				
				COLOR.GREEN100.render(r, body().x1()-2, body().x1()+2, body().y1(), body().y2());
				if (!MButt.LEFT.isDown()) {
					
					SETT.ARMIES().player().setDivAtOrderedIndex(div(), clicked.div());
					clicked.selectedSet(false);
					selectedSet(true);
					clicked = this;
				}
			}
			
			
		}
		
		@Override
		protected void clickA() {

			if (KEYS.MAIN().UNDO.isPressed() && clicked != null) {
				int ci = current.indexOf(this);
				int di = current.indexOf(clicked);
				int f = Math.min(ci, di);
				int t = Math.max(ci, di);
				
				for (int i = 0; i < current.size(); i++) {
					current.get(i).selectedSet(i >= f && i <= t);
				}
				
				
			}else if(KEYS.MAIN().MOD.isPressed()) {
				selectedSet(!selectedIs());
			}else {
				for (int i = 0; i < current.size(); i++) {
					current.get(i).selectedSet(false);
				}
				selectedSet(true);
				clicked = this;
				dragging = true;
			}
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			VIEW.s().ui.army.hover(div(), text);
		}
		
		private Div div() {
			return SETT.ARMIES().player().ordered().get(di);
			
			//stats
			
		}
		
	}
	
	
}
