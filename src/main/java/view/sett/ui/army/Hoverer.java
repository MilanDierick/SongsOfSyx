package view.sett.ui.army;

import init.D;
import settlement.army.Div;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.gui.GUI_BOX;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.main.VIEW;
import world.WORLD;

class Hoverer {

	private static CharSequence ¤¤needs = "¤Needs to Train";
	private static CharSequence ¤¤fully = "¤Fully Trained";
	private static CharSequence ¤¤currently = "¤Currently Training";
	
	private static CharSequence ¤¤army = "¤Division is currently attached to the world army '{0}'. It must be recalled in order to be edited;";
	
	static {
		D.ts(Hoverer.class);
	}
	
	public void hover(GUI_BOX box, Div div) {
		GBox b = (GBox) box;
		b.title(div.info.name());
		
		if (WORLD.ARMIES().cityDivs().attachedArmy(div) != null){
			GText t = b.text().warnify();
			t.add(¤¤army);
			t.insert(0, WORLD.ARMIES().cityDivs().attachedArmy(div).name);
			b.add(t);
			b.NL(8);
		}
		
		{
			int i = 0;
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				if (e.target(div) > 0) {
					
					b.add(e.resource.icon());
					b.add(GFORMAT.i(b.text(), e.target(div)));
					i++;
					if (i > 6) {
						i = 0;
						b.NL();
					}
					
				}
			}
		}
		b.sep();
		b.textLL(DicArmy.¤¤Experience);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(div)));
		b.NL();
		
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			b.textLL(t.info().name);
			b.tab(6);
			b.add(GFORMAT.perc(b.text(), t.div().getD(div)).normalify());
			b.tab(8);
			b.add(b.text().add('|'));
			b.add(GFORMAT.perc(b.text(), div.info.trainingD(t.room).getD()).normalify());
			b.NL();
		}
		
		b.sep();
		
		b.textLL(DicArmy.¤¤Deployable);
		b.tab(6);
		b.add(GFORMAT.iofk(b.text(), div.menNrOf(), div.info.men()));
		b.NL();
		
		b.textLL(DicArmy.¤¤Recruits);
		b.tab(6);
		b.add(GFORMAT.iofk(b.text(), STATS.BATTLE().RECRUIT.inDiv(div), div.info.men()-div.menNrOf()));
		b.NL(8);

		b.sep();
		


		
		b.textLL(¤¤needs);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), VIEW.s().ui.army.spec.needsTraining(div)));
		b.NL();
		
		b.textLL(¤¤currently);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), VIEW.s().ui.army.spec.training(div)));
		b.NL();
		
		b.textLL(¤¤fully);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), div.info.men() -  VIEW.s().ui.army.spec.needsTraining(div)));
		b.NL();
		
		b.sep();
		
		b.tab(2);
		b.textLL(DicMisc.¤¤Specification);
		b.NL();
		
		b.add(VIEW.UI().battle.statsw.get(div.info));		
		
		
		//div.hoverInfo((GBox)box);
	}
	
}
