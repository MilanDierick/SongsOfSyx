package world.army;

import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;

final class Hoverer {

	private final Stat[] stats = new Stat[BOOSTABLES.military().size()];
	private final GText text = new GText(UI.FONT().S, 16);
	Hoverer(){
		
		int i = 0;
		for (BOOSTABLE b : BOOSTABLES.military()) {
			stats[i++] = new Stat(b);
		}
		
	}
	
	public void hover(WDIV d, GUI_BOX box) {
		
		GBox b = (GBox)box;
		b.title(d.name());
		
		b.add(d.banner());
		b.add(GFORMAT.iofk(b.text(), d.men(), d.menTarget()));
		b.textL(d.race().info.names);
		b.NL();
		
		b.textL(DicArmy.¤¤Experience);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), d.experience()));
		b.NL();
		
		b.textL(STATS.BATTLE().TRAINING_MELEE.info().name);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(),d.training_melee()));
		b.NL();
		
		b.textL(STATS.BATTLE().TRAINING_ARCHERY.info().name);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(),d.training_ranged()));
		b.NL();
		
		b.NL();
		
		int t = 0;
		for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
			b.tab(t*2);
			b.add(m.resource().icon());
			b.add(GFORMAT.i(b.text(), d.equipTarget(m)));
			t ++;
			if (t >= 4) {
				t = 0;
				b.NL();
			}
		}
		
		b.NL(2);
		
		for (Stat ss : stats) {
			b.add(ss.get(d));
			b.NL();
		}
		
		b.NL();
		
		b.textLL(DicMisc.¤¤Power);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), WARMYD.boosts().power(d)));
	}
	
	private class Stat extends SPRITE.Imp  {
		
		private WDIV div;
		private final BOOSTABLE b;

		
		Stat(BOOSTABLE b){
			super(250, 16);
			this.b = b;
		}
		
		SPRITE get(WDIV div) {
			this.div = div;
			return this;
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			b.icon().render(r, X1, Y1);
			
			double eq = WARMYD.boosts().boost(div, b);
			double ra = eq - WARMYD.boosts().equip(div, b);
			double ma = WARMYD.boosts().max(div, b);
			text.clear();
			GFORMAT.f0(text, eq);
			text.render(r, X1+24, Y1);
			GMeter.render(r, GMeter.C_BLUE, ra/ma, eq/ma, X1+100, X2, Y1+2, Y2-2);
		}
	}
	
}
