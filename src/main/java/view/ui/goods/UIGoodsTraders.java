package view.ui.goods;

import java.util.Arrays;
import java.util.Comparator;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.D;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;

public abstract class UIGoodsTraders extends GuiSection{

	private GText buttPrice = new GText(UI.FONT().S, 64);
	private final FactionNPC[] facs = new FactionNPC[FACTIONS.MAX];
	private int max = 0;
	
	private static CharSequence ¤¤TradeYes = "¤You have a trade agreement with this faction, and trade is possible.";
	private static CharSequence ¤¤TradeNo = "¤You do not have a trade agreement with this faction. Trade is not possible.";
	private static CharSequence ¤¤Click = "¤Click to go to the diplomacy screen for this faction.";
	
	static {
		D.ts(UIGoodsTraders.class);
	}
	
	public UIGoodsTraders(int hi) {
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return max;
			}
		};
		
		bu.column(null, new Butt(null).body().width(), new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Butt(ier);
			}
		});
		
		add(bu.create(hi, false));
		
	}
	
	
	private class Butt extends CLICKABLE.ClickableAbs {

		private final GETTER<Integer> ier;
		
		Butt(GETTER<Integer> ier){
			body.setDim(96, 32);
			this.ier = ier;
		}
		
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			FactionNPC f = facs[ier.get()];
			
			isSelected |= FACTIONS.DIP().trades(FACTIONS.player(), f);
			
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			GButt.ButtPanel.renderFrame(r, body);
			
			f.banner().MEDIUM.renderCY(r, body.x1()+4, body.cY());
			
			buttPrice.clear();
			GFORMAT.i(buttPrice, price(f));
			buttPrice.adjustWidth();
			
			buttPrice.renderCY(r,  body.x1()+4+24+4, body.cY());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			FactionNPC f = facs[ier.get()];
			VIEW.UI().factions.hover(text, f);
			GBox b = (GBox) text;
			
			b.sep();
			
			b.textLL(DicRes.¤¤Price);
			b.tab(6).add(GFORMAT.i(b.text(), price(f)));
			b.NL(8);
			if (FACTIONS.DIP().trades(FACTIONS.player(), f))
				b.text(¤¤TradeYes);
			else
				b.text(¤¤TradeNo);
			b.NL(4);
			b.text(¤¤Click);
		}
		
		@Override
		protected void clickA() {
			FactionNPC f = facs[ier.get()];
			if (!FACTIONS.DIP().trades(FACTIONS.player(), f))
				VIEW.UI().factions.openTrade(f);
			else
				VIEW.UI().factions.openDip(f);
			VIEW.inters().popup.close();
		}
		
		
	}
	
	private final Comparator<FactionNPC> comp = new Comparator<FactionNPC>() {

		@Override
		public int compare(FactionNPC o1, FactionNPC o2) {
			return sortValue(o1) - sortValue(o2);
		}
		

	};
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		max = 0;
		
		for (FactionNPC f : FACTIONS.pRel().tradersPotential()) {
			if (price(f) > 0) {
				facs[max++] = f;
			}
		}
		
		Arrays.sort(facs, 0, max, comp);
		
		
		super.render(r, ds);
	}
	
	protected abstract int price(FactionNPC f);
	
	protected abstract int sortValue(FactionNPC f);
	
	
}
