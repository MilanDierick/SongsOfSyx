package view.sett.ui.standing;

import game.boosting.*;
import init.race.RACES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.colls.StatsTraits;
import settlement.stats.colls.StatsTraits.StatTrait;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;

class MenuProp extends ISidePanel {

	private final HCLASS c;
	
	MenuProp(HCLASS c){
		titleSet(DicMisc.¤¤Properites);
		this.c = c;
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (BoostableCat col : BOOSTABLES.colls()) {
			
			rows.add(new GHeader(col.name));
			
			for (Boostable bo : col.all()) {
				rows.add(new Row(bo));
			}
			
			rows.add(new RENDEROBJ.RenderDummy(10, 16));
			
		}
		
		rows.add(new GHeader(STATS.TRAITS().info.names));
		

		StatsTraits pp = STATS.TRAITS();
		
		for (StatTrait p : pp.all()) {
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, p.getD(c, CitizenMain.current));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(p.name);
					b.text(p.desc);
					b.NL();
					b.add(GFORMAT.i(b.text(), p.get(c, CitizenMain.current)));
				};
				
			}.hh(p.name, 150));

			
		}

		
		section.add(new GScrollRows(rows, HEIGHT).view());
		
	}
	
	private class Row extends GuiSection {
		
		private final Boostable bo;
		
		Row(Boostable bo){
			this.bo = bo;
			
			add(bo.icon, 0, 0);
			addRightC(2, new GText(UI.FONT().M, bo.name));
			addRightCAbs(250, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f(text, bo.get(RACES.clP(CitizenMain.current, c)));
				}
			});
			body().incrW(64);
			pad(2, 2);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (hoveredIs())
				COLOR.WHITE15.render(r, body());
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(bo.name);
			b.text(bo.desc);
			b.NL(8);
			
			bo.hover(b, RACES.clP(CitizenMain.current, c), true);
			
		}
		
		
	}
	
	@Override
	protected void update(float ds) {
		
	}

}