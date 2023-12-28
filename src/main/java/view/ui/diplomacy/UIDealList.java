package view.ui.diplomacy;

import game.faction.diplomacy.*;
import game.faction.diplomacy.Deal.DealBool;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.data.INT;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import world.regions.Region;

public class UIDealList extends GuiSection{

	private static CharSequence ¤¤YouGet = "You get";
	private static CharSequence ¤¤FactionGets = "{0} Gets";
	
	private final Object[] all = new Object[256];
	private int npcStart;
	private int dealsCount;
	
	private final Deal deal;
	
	static {
		D.ts(UIDealList.class);
	}
	
	public UIDealList(Deal deal, int height){
		this.deal = deal;
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return dealsCount;
			}
		};
		
		bu.column(null, 280, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		
		add(bu.createHeight(height, false));
		
		
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		int i = 0;
		npcStart = fill(deal.npc, i);
		dealsCount = fill(deal.player, npcStart);
		
		
		super.render(r, ds);
	}

	private int fill(DealParty dp, int i) {
		
		int start = i;
		i = set(null, i, start);
		
		for (DealBool bo : deal.bools()) {
			if (bo.is()) {
				i = set(bo, i, start);
			}
		}
		
		if (dp.credits.get() != 0) {
			i = set(dp.credits, i, start);
		}
		
		for (Region reg : dp.regs) {
			i = set(reg, i, start);
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (dp.resources.get(res) > 0) {
				i = set(res, i, start);
			}
		}
		return i;
	}
	
	private int set(Object o, int i, int start) {
		if (i >= all.length || i - start > all.length-1)
			return i;
		all[i] = o;
		
		return i+1;
	}
	
	private class Row extends CLICKABLE.ClickableAbs {
		
		private final GETTER<Integer> ier;
		private final GText header = new GText(UI.FONT().H2, 24).lablify();
		private final GText name = new GText(UI.FONT().H2, 24).normalify();
		
		Row(GETTER<Integer> ier){
			super(280, 32);
			this.ier = ier;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			Object o = all[ier.get()];
			DealParty p = ier.get() >= npcStart ? deal.player : deal.npc;
			
			if (o == null) {
				header.clear();
				if (p == deal.player) {
					header.add(¤¤FactionGets);
					header.insert(0, deal.faction().name);
					header.adjustWidth();
				}	
				else {
					header.set(¤¤YouGet);
				}
				
				header.renderCXY2(r, body.cX(), body.y2()-4);
			}else {
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				if (o instanceof DealBool) {
					DealBool b = (DealBool) o;
					render(r, b.icon, b.info.name);
				}else if (o instanceof INT) {
					name.clear().add(p.credits.get());
					render(r, UI.icons().s.money);
				}else if (o instanceof Region) {
					Region rr = (Region) o;
					render(r, rr.faction().banner().MEDIUM, rr.info.name());
				}else if (o instanceof RESOURCE) {
					RESOURCE rr = (RESOURCE) o;
					name.clear().add(p.resources.get(rr));
					render(r, rr.icon());
				}else {
					throw new RuntimeException(""+o);
				}
				
			}
			
		}
		
		
		private void render(SPRITE_RENDERER r, SPRITE icon) {
			icon.renderCY(r, body().x1()+8, body().cY());
			name.renderCY(r, body().x1()+40, body().cY());
		}
		
		private void render(SPRITE_RENDERER r,  SPRITE icon, CharSequence name) {
			this.name.clear().add(name);
			this.render(r, icon);
		}
		
		
		@Override
		protected void clickA() {
			Object o = all[ier.get()];
			DealParty p = ier.get() >= npcStart ? deal.player : deal.npc;
			
			if (o == null) {
				return;
			}else {
				if (o instanceof DealBool) {
					DealBool b = (DealBool) o;
					b.set(false);
				}else if (o instanceof INT) {
					p.credits.set(0);
				}else if (o instanceof Region) {
					p.regs.remove((Region) o);
				}else if (o instanceof RESOURCE) {
					p.resources.set((RESOURCE) o, 0);
				}
			}
			super.clickA();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			Object o = all[ier.get()];
			DealParty p = ier.get() >= npcStart ? deal.player : deal.npc;
			if (o == null)
				return;
			GBox b = (GBox) text;
			b.add(UI.icons().s.money);
			int value = 0;
			if (o instanceof DealBool) {
				value = (int) ((DealBool) o).value();
			}else if (o instanceof INT) {
				value = p.credits.get();
			}else if (o instanceof Region) {
				value = (int) DealRegions.valueRegion((Region) o, deal);
			}else if (o instanceof RESOURCE) {
				value = (int) DealValues.valueResource((RESOURCE) o, p.f.get(), deal.faction(), p.resources.get((RESOURCE) o));
			}
			b.add(GFORMAT.i(b.text(), value));
			super.hoverInfoGet(text);
		}
		
	}
	
	

	


	
}
