package view.world.ui.army;

import init.sprite.SPRITES;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.data.INT.IntImp;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.battle.UIDivSpec;
import world.army.WDivRegional;
import world.entity.army.WArmy;

class ArmyEdit {

	private final GuiSection section = new GuiSection();
	private int men;
	private int divs;
	private final IntImp amount = new IntImp(0, 9);
	private final UIDivSpec spec = new UIDivSpec(4, 8, 0.5) {
		
		@Override
		public int men() {
			return (amount.get()+1)*ArmyRecruit.DMen;
		}
	};
	private WArmy army;
	private final boolean[] selected;
	private static CharSequence ¤¤sure = "¤Are you sure you wish to apply these settings to {0} divisions?";
	
	ArmyEdit(boolean[] selected){
		this.selected = selected;
		{
			
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, spec.men());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {

					b.title(DicArmy.¤¤SoldiersTarget);
					b.add(GFORMAT.i(b.text(), spec.men()));
					
					b.NL();
//					if (spec.men() > WARMYD.conscripts().available(race.race()).get(FACTIONS.player())) {
//						b.error(UIArmyRecruit.¤¤NoMapnpower);
//					}
				};
				
			};
			
			section.addRelBody(0, DIR.S, new UIDivSpec.Spec(SPRITES.icons().m.citizen, COLOR.GREEN100.makeSaturated(0.7), amount, s));
			
		}
		
		section.addRelBody(8, DIR.S, spec.section);
		
		ACTION apply = new ACTION() {
			
			@Override
			public void exe() {
				for (int i = 0; i < army.divs().size(); i++) {
					if (selected[i] && army.divs().get(i) instanceof WDivRegional) {
						WDivRegional d = (WDivRegional) army.divs().get(i);
						d.menTargetSet(spec.men());
						for (EquipBattle bb : STATS.EQUIP().BATTLE_ALL()) {
							d.equipTargetset(bb, spec.equipi(bb).get());
						}
						for (StatTraining bb : STATS.BATTLE().TRAINING_ALL) {
							d.trainingTargetSet(bb, spec.training(bb));
						}
					}
				}
				
			}
		};
		
		section.addRelBody(8, DIR.S, new GButt.ButtPanel(DicMisc.¤¤confirm) {
			
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤sure).insert(0, divs), apply, ACTION.NOP, true);
				super.clickA();
			}
			
		});
		
		
	}
	
	public GuiSection init(WArmy a) {
		this.army = a;
		divs = 0;
		men = 0;
		for (int i = 0; i < a.divs().size(); i++) {
			if (selected[i] && a.divs().get(i) instanceof WDivRegional) {
				divs ++;
				men += a.divs().get(i).menTarget();
			}
		}
		
		int mm = men/(divs*ArmyRecruit.DMen);
		amount.set(mm);
		return section;
	}
	
	public boolean canEdit(WArmy a) {
		int divs = 0;
		for (int i = 0; i < a.divs().size(); i++) {
			if (selected[i] && a.divs().get(i) instanceof WDivRegional) {
				divs ++;
			}
		}
		return divs > 0;
	}

	
	
}
