package view.ui.diplomacy;

import game.faction.diplomacy.*;
import game.faction.diplomacy.Deal.DealBool;
import init.D;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.dic.DicGeo;
import util.dic.DicRes;
import util.gui.common.UIPickerRegion;
import util.gui.common.UIPickerResAm;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.Region;

public final class UIDealConfig extends GuiSection{
	
	
	private static CharSequence ¤¤offer = "¤offer";
	private static CharSequence ¤¤demand = "¤Demand";
	
	static {
		D.ts(UIDealConfig.class);
	}
	
	private static int BW = 300;
	
	public UIDealConfig(Deal deal){

		GuiSection s = new GuiSection();
		
		s.addRelBody(2, DIR.S, new GHeader(¤¤offer));
		for (DealBool b : deal.bools())
			s.addRelBody(2, DIR.S, new Bool(b));
		s.addRelBody(16, DIR.S, new Regionlist(deal, deal.player, deal.npc));
		s.addRelBody(2, DIR.S, new Reslist(deal, deal.player));
		s.addRelBody(2, DIR.S, new Sum(deal.player));
		
		

		
		s.addRelBody(32, DIR.S, new GHeader(¤¤demand));
		s.addRelBody(2, DIR.S, new Regionlist(deal, deal.npc, deal.npc));
		s.addRelBody(2, DIR.S, new Reslist(deal, deal.npc));
		s.addRelBody(2, DIR.S, new Sum(deal.npc));

		
		add(s);
		
		
		
	}


	private static class Bool extends GButt.ButtPanel {

		private final DealBool bool;
		
		public Bool(DealBool bool) {
			super(bool.info.name);
			this.bool = bool;
			icon(bool.icon);
			body().setDim(BW, 30);
			hoverSet(bool.info);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isActive = bool.possible();
			isSelected = bool.is();
			super.render(r, ds, isActive, isSelected, isHovered);
		}
		
		@Override
		protected void clickA() {
			if (bool.possible())
				bool.toggle();
			super.clickA();
		}
		
	}
	
	
	
	private static class Regionlist extends GButt.ButtPanel{
		
		private final GuiSection pop;
		private final DealParty p;
		
		Regionlist(Deal deal, DealParty p, DealParty ff){
			super(DicGeo.¤¤Regions);
			this.p = p;
			icon(UI.icons().s.world);
			body().setDim(BW, 30);
			pop = new UIPickerRegion(p.f, 400) {
				
				@Override
				protected void toggle(Region reg) {
					if (p.regs.contains(reg))
						p.regs.remove(reg);
					else
						p.regs.add(reg);
					
					for (int ri = 0; ri < p.regs.size(); ri++) {
						if (!DealRegions.canOffer(deal, ff.f.get(), p.regs.get(ri), p.regs)) {
							p.regs.remove(ri);
							ri--;
						}
					}
				}
				
				@Override
				protected boolean active(Region reg) {
					return DealRegions.canOffer(deal, ff.f.get(), reg, p.regs);
				}
				
				@Override
				protected boolean selected(Region reg) {
					return p.regs.contains(reg);
				}
				
				@Override
				protected void hoverInfo(GBox b, Region reg) {
					
					b.add(UI.icons().s.money);
					b.add(GFORMAT.i(b.text(), (long) DealRegions.valueRegion(reg, deal)));
					b.NL(8);
					
					super.hoverInfo(b, reg);
				}
				
			};
		}
		
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
		@Override
		protected void renAction() {
			activeSet(p.f.get().realm().regions() > 1);
		}
		
		
	}
	
	private static class Reslist extends GButt.ButtPanel{
		
		private final GuiSection pop;
		private final DealParty g;

		Reslist(Deal deal, DealParty p){
			super(DicRes.¤¤Resource);
			icon(UI.icons().s.urn);
			this.g = p;
			body().setDim(BW, 30);
			pop = new UIPickerResAm(p.resources, 16) {
				@Override
				protected void addToRow(GuiSection row, GETTER<RESOURCE> g) {
					
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							text.add('/');
							GFORMAT.i(text, p.resources.max(g.get()));
						}
					});
					row.body().incrW(64);
					row.addRelBody(8, DIR.E, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, (long) DealValues.valueResource(g.get(), p.f.get(), deal.faction(), p.resources.get(g.get())));
						}
					}.hh(UI.icons().s.money));
					row.body().incrW(64);
				}
			};
		}
		
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(pop, this);
		}
		
		@Override
		protected void renAction() {
			activeSet(g.f.get().res().get(null) > 0);
		}
		
		
	}
	
	private class Sum extends GInputInt{
		
		Sum(DealParty party){
			super(party.credits, true, true);
			addRelBody(4, DIR.W,UI.icons().s.money);
			addRelBody(8, DIR.E, new GStat() {
				
				@Override
				public void update(GText text) {
					text.add('(');
					GFORMAT.i(text, party.credits.max());
					text.add(')');
				}
			});
			body().setWidth(BW);
		}
		
	}
	
}
