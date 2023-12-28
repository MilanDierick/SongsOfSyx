package view.world.ui.region;

import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;


final class PlayMilitary extends GuiSection{

	PlayMilitary(GETTER_IMP<Region> g, int WIDTH){
		
		body().incrW(64);
		body().incrH(1);
		
		addRight(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, (int)RD.MILITARY().conscriptTarget.get(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(RD.MILITARY().conscriptTarget.name);
				b.text(RD.MILITARY().conscriptTarget.desc);
				b.sep();
				
				RD.MILITARY().conscriptTarget.hover(b, g.get(), null, true);
				
			};
			
		}.hv(UI.icons().m.sword));
		
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, RD.MILITARY().garrison.get(g.get()), (int)RD.MILITARY().garrisonTarget(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤garrison);
				b.NL();
				
				RD.MILITARY().bgarrison.hover(b, g.get(), DicMisc.¤¤Target, true);
			};
			
		}.hv(UI.icons().m.shield));
			
		
		addRightCAbs(64, MiscMore.garrison(g, WIDTH-body().width()-64));
		
		
		
		
	}
	



	
}
