package view.sett.ui.army;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.INT;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.common.UIPickerRace;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;
import view.ui.battle.*;
import world.army.WDivRegional;
import world.army.util.DIV_STATS;

final class Edit {

	private static CharSequence ¤¤title = "{0} divisions";
	private static CharSequence ¤¤Time = "¤The amount of days it will take to train this division to specification.";
	
	static {
		D.ts(Edit.class);
	}
	
	private GuiSection section = new GuiSection();
	private final ArrayList<Div> all = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final INT.IntImp men = new INT.IntImp(0, (int) Math.ceil(Config.BATTLE.MEN_PER_DIVISION/10));
	
	public int realMen() {
		return men.get()*10;
	}
	
	private final UIPickerRace race = new UIPickerRace(RACES.all()) {
		
		@Override
		public void hover(GBox b, Race race) {
			b.title(race.info.names);
			b.text(race.info.desc);
			b.sep();
			
			race.boosts.hover(b, 1.0, null, BOOSTABLES.BATTLE().filter, -1);
		};
		
	};
	private final UIDivBannerEditor ee = new UIDivBannerEditor() {
		@Override
		public void accept() {
			VIEW.inters().popup.show(Edit.this.section, trigger);
		};
	};
	
	private final UIDivSpec spec = new UIDivSpec(3, 6, 100000.0) {
		
		@Override
		public int men() {
			return realMen();
		}
	};
	
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
			return realMen();
		}
	};
	
	
	Edit() {
		
		
		{
			
			CLICKABLE title = new GInput(new  StringInputSprite(20, UI.FONT().H2) {
				@Override
				public void renAction() {
					text().clear().add(all.get(0).info.name());
				}
				
				@Override
				protected void change() {
					all.get(0).info.name().clear().add(text());
				};
			});
			
			RENDEROBJ desc = new RENDEROBJ.RenderImp(title.body().width(), title.body().height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GText.TMP.clear().add(¤¤title).insert(0, all.size());
					UI.FONT().M.render(r, ¤¤title, body.x1(), body.y1());
				}
			};

			section.add(new CLICKABLE.ClickWrap(title) {
				
				@Override
				protected RENDEROBJ pget() {
					return all.size() == 1 ? title : desc;
				}
			});
			
		}
		
		{
			GuiSection s = new GuiSection();
			s.add(ee.butt());
			s.addRightC(8, race.section);
			
			GStat st = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, realMen());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {

					b.title(DicArmy.¤¤SoldiersTarget);
					b.add(GFORMAT.i(b.text(), realMen()));
				};
				
			};
			
			s.addRightC(8, new UIDivSpec.Spec(SPRITES.icons().m.citizen, COLOR.GREEN100.makeSaturated(0.7), men, st));
			
			section.addRelBody(8, DIR.S, s);
		}
		
		section.addRelBody(8, DIR.S, spec.section);
		
		section.addRelBody(8, DIR.S, new UIDivStats.WDivStats().get(div));
		
		{
			GuiSection s = new GuiSection();
			
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
						am += t.room.TRAINING_DAYS*spec.training(t)/t.room.bonus().get(FACTIONS.player());
					}
					
					return am;
					
				}
				
			};
			
			s.addRightC(0, ss.hh(SPRITES.icons().m.time));
			
			s.addRightC(48, new GButt.ButtPanel(DicMisc.¤¤Accept) {
				
				@Override
				protected void clickA() {
					for (Div d : all) {
						d.info.race.set(race.race());
						d.info.men.set(realMen());
						d.info.symbolSet(ee.bannerI());
						for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
							d.info.trainingD(t.room).setD(div.training(t));
						}
						for (EquipBattle t : STATS.EQUIP().BATTLE_ALL()) {
							t.targetSet(d, (int) Math.round(div.equip(t)*t.equipMax));
						}
					}
					
					VIEW.inters().popup.close();
				};
				
			}.setDim(180, 32));
			s.addRightC(0, new GButt.ButtPanel(DicMisc.¤¤cancel) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.close();
				};
				
			}.setDim(180, 32));
			
			section.addRelBody(8, DIR.S, s);
		}
	}
	
	private CLICKABLE trigger;
	
	public GuiSection get(LIST<Div> all, CLICKABLE trigger) {
		this.all.clearSloppy();
		this.all.add(all);
		this.trigger = trigger;
		race.set(all.get(0).info.race().index);
		ee.bannerISet(all.get(0).info.symbolI());

		{
			int am = 0;
			for (Div d : all) {
				am += d.info.men();
			}
			am = (int) Math.round(am/(10.0*all.size()));
			am = CLAMP.i(am, 0, Config.BATTLE.MEN_PER_DIVISION);
			men.set(am);
		}

		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			double am = 0;
			int tot = 0;
			for (Div d : all) {
				am += d.info.training(t)*d.info.men();
				tot += d.info.men();
			}
			am = am/tot;
			am = CLAMP.d(am, 0, 1);
			spec.traini(t).setD(am);
			
		}
		for (EquipBattle t : STATS.EQUIP().BATTLE_ALL()) {
			double am = 0;
			int tot = 0;
			for (Div d : all) {
				am += t.target(d)*d.info.men();
				tot += d.info.men();
			}
			am = am/(tot);
			am = CLAMP.d(am, 0, t.max());
			spec.equipi(t).set((int) am);
		}
		return section;
		
	}
	
}
