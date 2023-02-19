package view.sett.ui.standing;

import init.biomes.BUILDING_PREF;
import init.biomes.BUILDING_PREFS;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.gui.misc.GBox;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.sett.ui.standing.Cats.Cat;

final class CatEnv extends Cat {
	
	CatEnv(HCLASS cl){
		super(new StatCollection[] { STATS.ENV(), STATS.ACCESS(), STATS.ACCESS().MONUMENTS, STATS.BATTLE(), STATS.STORED()});
		titleSet(cs[0].info.name);
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		for (StatCollection c : cs){
			if (c != STATS.STORED()) {
				rens.add(new StatRow.Title(c.info));
				for (STAT s : c.all()) {
					if (s == STATS.ENV().BUILDING_PREF) {
						rens.add(new StatRow(s, cl) {
							
							@Override
							public void hoverInfoGet(GUI_BOX text) {
								
								super.hoverInfoGet(text);
								if (CitizenMain.current != null) {
									GBox b = (GBox) text;
									b.NL(8);
									for (BUILDING_PREF p : BUILDING_PREFS.ALL()) {
										b.add(p.icon());
										b.add(GFORMAT.perc(b.text(), CitizenMain.current.pref().structure(p)));
									}
								}
							}
							
						});
					}else {
						rens.add(new StatRow(s, cl));
					}
					
					
					
					
				}
			}
		}
		
		rens.add(new StatRow.Title(STATS.STORED().info));
		
		for (STAT s : STATS.STORED().createTheOnesThatMatter(cl)) {
			rens.add(new StatRow(s, cl));
		}

		section.addDown(4, new GScrollRows(rens, HEIGHT, 0).view());
		
	}

}