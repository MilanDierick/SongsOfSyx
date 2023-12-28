package view.ui.goods;

import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;

class UIGoodsTax {

	public UIGoodsTax() {
		
	}
	
	private GAME.Cache cc = new GAME.Cache(10);
	private int[] cache = new int[RESOURCES.ALL().size()];
	private Pop pop = new Pop();
	
	private int am(RESOURCE res) {
		if (cc.shouldAndReset()) {
			Arrays.fill(cache, 0);
			for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
				Region r = FACTIONS.player().realm().region(i);
				for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
					if (RD.OUTPUT().get(RESOURCES.ALL().get(ri)).getDelivery(r) > 0)
						cache[ri] += RD.OUTPUT().get(RESOURCES.ALL().get(ri)).getDelivery(r);
				}
				
			}
		}
		
		return cache[res.index()];
	}
	
	public RENDEROBJ butt(RESOURCE res) {
		
		if (res == null)
			return new RENDEROBJ.RenderDummy(100, 60);
		
		GStat s = new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, am(res));
			}
		};
		
		GButt.ButtPanel b = new GButt.ButtPanel(s) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(RTYPE.TAX.name);
				b.NL(8);
				for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
					Region r = FACTIONS.player().realm().region(i);
					if (RD.OUTPUT().get(res).getDelivery(r) > 0) {
						b.text(r.info.name());
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), RD.OUTPUT().get(res).getDelivery(r)));
						b.NL();
					}
				}
			}
			
			@Override
			protected void clickA() {
				pop.res = res;
				VIEW.inters().popup.show(pop, this);
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				activeSet(am(res) > 0);
			}
			
		};
		b.body.setDim(100, 60);
		return b;
	}
	
	private static class Pop extends GuiSection {
		
		private RESOURCE res;
		private ArrayList<Region> regs = new ArrayList<Region>(128);
		
		Pop(){
			GTableBuilder bu = new GTableBuilder() {
				
				@Override
				public int nrOFEntries() {
					return regs.size();
				}
			};
			
			bu.column(null, 250, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					GuiSection s = new GuiSection() {
						@Override
						protected void clickA() {
							Region r = regs.get(ier.get());
							if (r != null) {
								VIEW.world().activate();
								VIEW.world().UI.regions.open(r);
							}
							
							super.clickA();
						}
					};
					s.add(new GStat() {
						
						@Override
						public void update(GText text) {
							Region r = regs.get(ier.get());
							if (r != null) {
								text.add(r.info.name());
							}
						}
					}.r());
					
					s.addRightC(180, new GStat() {
						
						@Override
						public void update(GText text) {
							Region r = regs.get(ier.get());
							if (r != null) {
								GFORMAT.iIncr(text, RD.OUTPUT().get(res).getDelivery(r));
							}
						}
					}.r());
					s.pad(0, 6);
					
					return s;
					
				}
			});
			
			add(bu.createHeight(400, true));
		}
		
		@Override
		public void render(SPRITE_RENDERER ren, float ds) {
			regs.clear();
			for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
				Region r = FACTIONS.player().realm().region(i);
				if (RD.OUTPUT().get(res).getDelivery(r) > 0) {
					regs.add(r);
				}
			}
			
			super.render(ren, ds);
		}
		
	}
	
}
