package view.world.ui.regions;

import static view.world.ui.regions.UIRegion.*;

import java.util.ArrayList;

import init.sprite.ICON;
import settlement.room.industry.module.Industry.IndustryResource;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.misc.GMeter.GGaugeColor;
import util.gui.slider.GAllocator;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import world.map.regions.REGIOND;
import world.map.regions.RegionTaxes.RegionIndustry;

final class UIIndustry extends ISidePanel {

	UIIndustry(){
		
		titleSet(DicRes.¤¤Taxes);
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(REGIOND.RES().all.size());
		
		for (int ri = 0; ri < REGIOND.RES().all.size();) {
			GuiSection s = new GuiSection();
			for (int i = 0; i < 2 && ri < REGIOND.RES().all.size(); i++, ri++) {
				RegionIndustry r = REGIOND.RES().all.get(ri);
				s.addRightC(4, new ResButton(r));
			}
			rows.add(s);
		}
		
		CLICKABLE c = new GButt.CheckboxTitle(REGIOND.RES().exhausting.info().name) {
			@Override
			protected void renAction() {
				activeSet(!REGIOND.RES().exhaustion.isMax(reg));
				selectedSet(REGIOND.RES().exhausting.get(reg)==1);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.clear();
				b.title(REGIOND.RES().exhausting.info().name);
				b.text(REGIOND.RES().exhausting.info().desc);
				UIRegion.hoverLight(b, REGIOND.RES().exhausting);
			}
			
			@Override
			protected void clickA() {
				if (!REGIOND.RES().exhaustion.isMax(reg))
					REGIOND.RES().exhausting.set(reg, (REGIOND.RES().exhausting.get(reg)+1)&1);
			};
			
		}.hoverSet(REGIOND.RES().exhausting.info());
		
		section.add(c);
		
		
		section.add(new GScrollRows(rows, HEIGHT-section.body().height()-8).view(), 0, section.getLastY2()+8);
		
	}
	
//	private static class Entry extends HoverableAbs{
//		
//		private int index = -1;
//		static final int width = 100;
//		static final GText text = new GText(UI.FONT().M, 10);
//		
//		Entry(){
//			body().setDim(width, ICON.MEDIUM.SIZE);
//		}
//		
//		private final RegionResource r() {
//			if (index == -1)
//				return null;
//			return REGIOND.INDU().res().get(index);
//		}
//
//		@Override
//		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
//			if (index == -1)
//				return;
//			
//			
//		}
//		
//		@Override
//		public void hoverInfoGet(GUI_BOX text) {
//			// TODO Auto-generated method stub
//			super.hoverInfoGet(text);
//		}
//		
//	}
	
	private class ResButton extends GuiSection {

		private final RegionIndustry d;
		
		public ResButton(RegionIndustry d) {
			this.d = d;
			add(d.industry.blue.iconBig(), 0, 0);
			
			addRightC(2, new GAllocator(COLOR.YELLOW100, new INTE() {
				
				@Override
				public int min() {
					return d.decree.min(reg);
				}
				
				@Override
				public int max() {
					return d.decree.max(reg);
				}
				
				@Override
				public int get() {
					return d.decree.get(reg);
				}
				
				@Override
				public void set(int t) {
					d.decree.set(reg, t);
				}
			}, 6, 16));
			
			
			
			
			addRightC(2, new HoverableAbs(12, ICON.MEDIUM.SIZE) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					GGaugeColor c = GMeter.C_BLUE;
					if (REGIOND.RES().exhausting.get(reg) == 1)
						c = GMeter.C_ORANGE;
					else if(REGIOND.RES().exhaustion.get(reg) > 0)
						c = GMeter.C_RED;
					GMeter.renderH(r, c, d.prospect.getD(reg), body());
					
				}
			});
			
			pad(4, 2);
			
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(),-1);
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(d.industry.blue.info.names);
			UIRegion.hoverLight(b, d.decree);
			b.NL(8);
			
			b.textL(DicRes.¤¤Rate);
			b.NL();
			for (IndustryResource rr : d.industry.outs()) {
				b.add(rr.resource.icon());
				b.add(GFORMAT.f(b.text(), rr.rate));
			}
			b.NL(8);
			UIRegion.hover(b, d.factors);
			super.hoverInfoGet(text);


			
			
		}
		
	}
	
}
