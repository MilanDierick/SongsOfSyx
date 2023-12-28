package view.ui.battle;

import init.D;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import util.dic.*;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import world.army.*;

public final class UIDivHoverer {

	private final UIDivStats.WDivStats stat = new UIDivStats.WDivStats();

	private static CharSequence ¤¤NewConscripts = "¤Conscripts are training and will be ready in {0} days";
	private static CharSequence ¤¤NewConscriptsProblem = "¤There are no conscripts to train for this division.";
	private static CharSequence ¤¤Training = "¤This division is currently training to reach the desired training level. Days left: {0}";
	private static CharSequence ¤¤NotMustering = "¤This army is currently not mustering, and will not train conscripts.";

	static {
		D.ts(UIDivHoverer.class);
	}
	
	UIDivHoverer() {

	}



	public void hover(WDIV d, GUI_BOX box) {

		GBox b = (GBox) box;
		b.title(d.name());

		b.add(d.banner());
		b.add(GFORMAT.iofkInv(b.text(), d.men(), d.menTarget()));
		b.textL(d.race().info.names);

		b.sep();
		b.textLL(DicMisc.¤¤Power);
		b.add(GFORMAT.f0(b.text(), AD.UTIL().power.get(d)));

		b.NL();

		b.textL(DicArmy.¤¤Experience);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), d.experience()));
		b.NL();

		
		
		for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
			b.textL(tt.info().name);
			b.tab(6);
			b.add(GFORMAT.perc(b.text(), d.training(tt)));
			GText t = b.text();
			t.add('/');
			GFORMAT.perc(t, d.trainingTarget(tt), 0);
			b.add(t);
			b.NL();
		}

		b.NL();

		int t = 0;
		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			b.tab(t * 3);
			b.add(m.resource().icon());
			if (d.equipTarget(m) == 0) {
				b.add(b.text().color(COLOR.WHITE50).add('-'));
			} else {
				if (d instanceof ADDiv && d.needSupplies())
					b.add(GFORMAT.iofkInv(b.text(), d.equipTarget(m) * AD.supplies().get(m).getD(((ADDiv) d).army()),
							d.equipTarget(m)));
				else
					b.add(GFORMAT.i(b.text(), d.equipTarget(m)));
			}

			t++;
			if (t >= 4) {
				t = 0;
				b.NL();
			}
		}

		b.sep();

		b.add(stat.get(d));

		b.NL(8);

		if (d instanceof ADDiv) {
			ADDiv ad = (ADDiv) d;
			if (ad.costPerMan() > 0) {
				b.textL(DicRes.¤¤InitialCost);
				b.tab(3);
				b.add(GFORMAT.i(b.text(), 2 * ad.costPerMan() * ad.menTarget()));
				b.NL();

				b.textL(DicRes.¤¤Upkeep);
				b.tab(3);
				b.add(GFORMAT.i(b.text(), ad.costPerMan() * ad.menTarget()));
				b.NL();
			}

			if (ad.needConscripts()) {
				if (ad.army().acceptsSupplies()) {
					if (ad.men() < ad.menTarget()) {
						if (!AD.conscripts().canTrain(ad.race(), ad.faction())) {
							b.error(¤¤NewConscriptsProblem);
						} else {
							GText te = b.text();
							te.add(¤¤NewConscripts);
							te.insert(0, ad.daysUntilMenArrives());
							te.normalify2();
							b.add(te);
						}
					} else {
						int tt = trainingTime(ad);
						if (tt > 0) {
							GText te = b.text();
							te.add(¤¤Training);
							te.insert(0, tt);
							te.normalify2();
							b.add(te);

						}

					}
				} else if (ad.men() < ad.menTarget() || trainingTime(ad) > 0) {
					b.error(¤¤NotMustering);
				}

			}
		}

	}

	static int trainingTime(ADDiv div) {
		int m = 0;
		for (StatTraining tr : STATS.BATTLE().TRAINING_ALL) {
			m += WDivRegional.trainingDays(tr, div.trainingTarget(tr) - div.training(tr), div.faction());
		}
		return m;
	}

}
