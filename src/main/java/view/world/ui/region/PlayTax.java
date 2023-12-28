package view.world.ui.region;

import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;


final class PlayTax extends GuiSection{

	
	PlayTax(GETTER_IMP<Region> g, int width){
		
		
		addRight(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, (int)RD.TAX().boost.get(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				RD.TAX().boost.hover(b, g.get(), true);
			};
			
		}.hh(UI.icons().m.coins));
			
		INTE ii = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return RD.TAX().rate.max(g.get());
			}
			
			@Override
			public int get() {
				return RD.TAX().rate.get(g.get());
			}
			
			@Override
			public void set(int t) {
				RD.TAX().rate.set(g.get(), t);
			}
		};
		
		GSliderInt sl = new GSliderInt(ii, 200, 16, true) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				RD.TAX().rate.info().hover(text);	
				super.hoverInfoGet(text);
			}
		};
		
		
		
		addRightCAbs(120,sl);
		
		addRightC(16, new GStat() {
			
			@Override
			public void update(GText text) {
				int m = 0;
				for (Minable mi : RESOURCES.minables().all()) {
					m += g.get().info.minable(mi);
				}
				GFORMAT.i(text, m);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤Minerals);
				b.text(MiscBasics.¤¤mineralsD);
				b.sep();
				for (Minable mi : RESOURCES.minables().all()) {
					b.add(mi.resource.icon());
					b.text(mi.resource.name);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), g.get().info.minable(mi)));
					b.NL();
				}
			};
			
		}.hh(UI.icons().m.pickaxe));
		
		addRightC(80, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.percInv(text, RD.DEVASTATION().current.getD(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.add(RD.DEVASTATION().current.info());
			};
			
		}.hh(UI.icons().m.skull));
		
		
		
	}
	
}
