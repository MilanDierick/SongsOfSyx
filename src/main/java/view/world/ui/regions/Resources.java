package view.world.ui.regions;

import static view.world.ui.regions.UIRegion.*;

import java.util.ArrayList;

import init.settings.S;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.colors.GCOLOR;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import world.map.regions.REGIOND;
import world.map.regions.RegionTaxes.RegionIndustryResource;
import world.map.regions.RegionTaxes.RegionResource;

final class Resources extends GButt.BSection {

	private final UIIndustry industry = new UIIndustry();
	
	Resources(){
		
		add(new GHeader(DicRes.¤¤Taxes));
		
		
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(REGIOND.RES().res.size());
		
		for (int ri = 0; ri < REGIOND.RES().res.size();) {
			GuiSection s = new GuiSection() {
				@Override
				protected void clickA() {
					VIEW.world().panels.add(industry, false);
				}
			};
			for (int i = 0; i < 5 && ri < REGIOND.RES().res.size(); i++, ri++) {
				RegionResource r = REGIOND.RES().res.get(ri);
				s.addRightC(2, new ResButton(r));
			}
			rows.add(s);
		}
		int am = (int) Math.round(CORE.getGraphics().nativeHeight*5/800.0);
		CLICKABLE s = new GScrollRows(rows, rows.get(0).body().height()*am).view();
		s.clickActionSet(new ACTION() {
			
			@Override
			public void exe() {
				VIEW.world().panels.add(industry, false);
			}
		});
		addDownC(4, s);
		
		pad(12, 6);
		
	}
	
	@Override
	protected void clickA() {
		VIEW.world().panels.add(industry, false);
	}
	
	private class ResButton extends GuiSection {
		
		private final RegionResource d;
		
		public ResButton(RegionResource d) {
			this.d = d;
			add(d.resource.icon(), 0, 0);
			
			addRightC(2, new GStat() {
				
				@Override
				public void update(GText text) {
					int dd = (int)(d.output_target.getD(reg) - d.current_output.get(reg));
					GFORMAT.i(text, (int)d.output_target.getD(reg));
					if (dd > 0) {
						text.color(GCOLOR.T().IGOOD);
					}else if (dd < 0) {
						text.color(GCOLOR.T().IBAD);
					}
				}
			});
			
			body().incrW(48);
			
			
			
			pad(4);
			
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(),-1);
			super.render(r, ds);
		}
		
		@Override
		public boolean click() {
			VIEW.world().panels.add(industry, false);
			return super.click();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(d.resource.names);
			for (RegionIndustryResource ins : d.industries()) {
				b.add(ins.industry.industry.blue.iconBig());
				b.add(GFORMAT.iIncr(b.text(), (int)(ins.resource().rate*ins.industry.factors.getD(reg))));
				
			}
			
			b.NL(8);
			
			b.add(GFORMAT.i(b.text(), d.current_output.get(reg)));
			b.add(SPRITES.icons().s.arrow_right);
			b.add(GFORMAT.i(b.text(), (int)d.output_target.getD(reg)));
			
			if (S.get().developer) {
				b.add(GFORMAT.i(b.text(), d.maxOutput(reg)));
				b.NL(8);
			}
			
			
		}
		
	}
	
}
