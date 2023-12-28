package view.world.ui.region;

import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicGeo;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RDOutput.RDResource;
import world.regions.data.pop.RDRace;

final class PlayHov {

	private final GETTER_IMP<Region> g = new GETTER_IMP<>();
	
	void hover(Region reg, GUI_BOX box) {
		g.set(reg);
		box.title(reg.info.name());
		GBox b = (GBox) box;
		
		if (reg.capitol()) {
			b.text(DicGeo.¤¤CapitolYou);
			return;
		}
		
		{
			double ii = 32.0;
			double tot = RD.RACES().maxPop();
			if (tot > 0) {
				for (RDRace r : RD.RACES().all) {
				
					int am = (int) (ii*r.pop.get(reg)/tot);
					if (RD.RACES().visuals.cRace(reg) == r.race) {
						am++;
					}
					while(am-- > 0) {
						b.add(r.race.appearance().icon);
						b.rewind(16);
					}
				}
			}
			b.NL(8);
		}
		
		
		b.add(UI.icons().m.workshop);
		b.add(GFORMAT.i(b.text(), (int)RD.BUILDINGS().points.get(g.get())));
		b.tab(3);
		b.add(UI.icons().m.stength);
		b.add(GFORMAT.i(b.text(), (int)RD.RACES().workforce.get(g.get())));
		b.tab(6);
		b.add(UI.icons().m.admin);
		b.add(GFORMAT.i(b.text(), (int)RD.RACES().workforce.get(g.get())));
		b.NL(8);
		
		b.add(UI.icons().m.heart);
		b.add(GFORMAT.perc(b.text(), RD.HEALTH().getD(g.get())));
		b.tab(3);
		b.add(UI.icons().m.rebellion);
		b.add(GFORMAT.perc(b.text(), RD.RACES().loyaltyAll.getD(g.get())));
		b.tab(6);
		b.add(UI.icons().m.flag);
		b.add(GFORMAT.perc(b.text(), RD.OWNER().affiliation.getD(g.get())));
		b.NL(8);
		
		int i = 0;
		
		i = addTax(b, i, UI.icons().s.money, RD.TAX().boost.get(reg));
		
		for (RDResource o : RD.OUTPUT().all)
			i = addTax(b, i, o.res.icon().small, o.getDelivery(reg));
		
		
	}
	
	private int addTax(GBox b, int i, SPRITE icon, double amount) {
		if (amount > 1) {
			
			if (i > 4) {
				i = 0;
				b.NL();
			}
			
			b.tab(i*3);
			b.add(icon);
			b.add(GFORMAT.i(b.text(), (int)amount));
			i++;
		}
		return i;
		
		
	}
	
}
