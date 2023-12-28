package view.world.ui.army;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.data.INT.IntImp;
import util.dic.DicArmy;
import util.gui.common.UIPickerRace;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.ui.battle.*;
import world.WORLD;
import world.army.AD;
import world.army.WDivRegional;
import world.army.util.DIV_STATS;

class ArmyRecruit extends ISidePanel{

	static CharSequence ¤¤NoMapnpower = "¤Insufficient Manpower!";
	static CharSequence ¤¤Note = "¤NOTE: once a division has finished training, it will need to be supplied from your army supply depot. Without supplies, the men will desert you.";
	static CharSequence ¤¤Time = "¤The amount of days it will take to train this division to specification.";
	
	{
		D.ts(this.getClass());
		titleSet(DicArmy.¤¤Recruit);
	}
	
	final static int DMen = (int) Math.ceil(Config.BATTLE.MEN_PER_DIVISION/10.0);
	private final IntImp amount = new IntImp(0, 9);
	private final UIDivBannerEditor ee = new UIDivBannerEditor();
	
	private final DIV_STATS div = new DIV_STATS() {
		
		@Override
		public double training(StatTraining tr) {
			return spec.training(tr);
		}
		
		@Override
		public Race race() {
			return race.race();
		}
		
		@Override
		public Faction faction() {
			return FACTIONS.player();
		}
		
		@Override
		public double experience() {
			return 0;
		}
		
		@Override
		public double equip(EquipBattle e) {
			return spec.equip(e);
		}

		@Override
		public int men() {
			return ArmyRecruit.this.men();
		}
	};
	
	private final UIDivSpec spec = new UIDivSpec(3, 6, 0.5) {
		
		@Override
		public int men() {
			return div.men();
		}
	};
	
	private final UIPickerRace race = new UIPickerRace(RACES.playable()) {
		
		@Override
		public void hover(GBox b, Race race) {
			b.title(race.info.names);
			b.textL(DicArmy.¤¤Conscriptable);
			b.add(GFORMAT.i(b.text(), AD.conscripts().available(race).get(FACTIONS.player())));
			b.NL(8);
			b.text(race.info.desc);
			b.sep();
			
			race.boosts.hover(b, 1.0, null, BOOSTABLES.BATTLE().filter, -1);
		};
		
	};
	
	public int men() {
		return (int) (amount.get()+1)*DMen;
	}
	

	
	ArmyRecruit() {
		
		race.set(0);

		section.addRightC(0, ee.butt());
		section.addRightC(16, race.section);
		
		
		{
			
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, men());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {

					b.title(DicArmy.¤¤SoldiersTarget);
					b.add(GFORMAT.i(b.text(), men()));
					
					b.NL();
					if (men() > AD.conscripts().available(race.race()).get(FACTIONS.player())) {
						b.error(¤¤NoMapnpower);
					}
				};
				
			};
			
			section.addRightC(8, new UIDivSpec.Spec(SPRITES.icons().m.citizen, COLOR.GREEN100.makeSaturated(0.7), amount, s));
			
		}
		
		section.addRelBody(8, DIR.S, spec.section);
		
		section.addRelBody(8, DIR.S, new UIDivStats.WDivStats().get(div));
		
		{
			GuiSection row = new GuiSection();
			
			
			
			GStat ss = new GStat() {
				
				@Override
				public void update(GText text) {
					
					GFORMAT.i(text, (long) ti());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.text(¤¤Time);
				}
				
				double ti() {
					
					int am = WDivRegional.DAYS_TO_TRAIN;
					
					for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
						am += WDivRegional.trainingDays(t, spec.training(t), FACTIONS.player());
					}
					
					return am;
					
				}
				
			};
			
			row.addRightC(48, ss.hh(SPRITES.icons().m.time));
			
			GButt b = new GButt.ButtPanel(DicArmy.¤¤Recruit) {
				
				@Override
				protected void clickA() {
					if (Army.army.divs().canAdd()) {
						WDivRegional d = WORLD.ARMIES().regional().create(race.race(), (1 + amount.get())/10.0, Army.army);
						d.bannerSet(ee.bannerI());
						
						for (StatTraining s: STATS.BATTLE().TRAINING_ALL) {
							d.trainingTargetSet(s, spec.training(s));
						}
						
						for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
							d.equipTargetset(s, spec.equipi(s).get());
						}
						
						
						
					}
				}
				
				@Override
				protected void renAction() {
					activeSet(men() <= AD.conscripts().available(race.race()).get(FACTIONS.player()));
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					if (men() > AD.conscripts().available(race.race()).get(FACTIONS.player())) {
						b.error(¤¤NoMapnpower);
					}else {
						b.text(¤¤Note);
					}
					
					
				}
				
			};
			
			row.addRightC(48, b);
			
			section.addRelBody(8, DIR.S, row);
			
		}
		
		
		
		
	}
	
	
}
