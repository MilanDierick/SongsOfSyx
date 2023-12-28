package view.world.ui.region;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.gui.GUI_BOX;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import world.WORLD;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;

class MiscDistances extends GButt.ButtPanel{
	
	private final GETTER<Region> g;
	
	public MiscDistances(GETTER<Region> g) {
		super(UI.icons().s.expand);
		this.g = g;
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		b.text(DicMisc.¤¤Distance);
		b.NL();
		for (RDist r : WORLD.PATH().tmpRegs.all(g.get(),  WTREATY.NEIGHBOURS(g.get()), WRegSel.DUMMY(g.get()))) {
			b.textLL(r.reg.info.name());
			b.tab(6);
			b.add(SPRITES.icons().s.arrow_right);
			b.add(GFORMAT.i(b.text(), r.distance));
			if (r.water) {
				b.tab(8);
				b.add(SPRITES.icons().s.ship);
			}
			b.NL();
		}
	}
	
	
}
