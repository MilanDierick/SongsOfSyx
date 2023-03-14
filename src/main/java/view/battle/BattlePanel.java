package view.battle;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.battle.BattleState;
import init.C;
import init.D;
import init.config.Config;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.army.*;
import settlement.army.ai.util.DivTDataStatus;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.DOUBLE_O;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import view.subview.GameWindow;
import view.ui.UIPanelTop;

public final class BattlePanel implements SAVABLE{
	
	private final GuiSection section = new GuiSection();
	private final UIPanelArtillery cards_cata;
	private final UIPanelUnitCards cards_player;
	private final UIPanelUnitCards cards_enemy;
	
	public BattlePanel(ISidePanels p, GameWindow w, UIPanelTop top, DivSelection selection, boolean battleview){
		
		CLICKABLE b;

		section.body().setHeight(C.HEIGHT());
		D.t(this);
		
		GuiSection butts = new GuiSection();
		
		b = new GButt.Panel(SPRITES.icons().l.view_military) {
			@Override
			protected void clickA() {
				if (p.added(cards_player))
					p.remove(cards_player);
				else
					p.add(cards_player, false, true);
				
			};
			@Override
			protected void renAction() {
				selectedSet(p.added(cards_player));
			}
		};
		b.hoverInfoSet(DicArmy.¤¤Army);
		butts.addRight(0, b);
		
		b = new GButt.Panel(SETT.ROOMS().ARTILLERY.get(0).iconBig()) {
			@Override
			protected void clickA() {
				if (p.added(cards_cata))
					p.remove(cards_cata);
				else
					p.add(cards_cata, false, true);
				
			};
			@Override
			protected void renAction() {
				selectedSet(p.added(cards_cata));
			}
		};
		b.hoverInfoSet(DicArmy.¤¤Artillery);
		butts.addRight(0, b);
		
		
		
		if (S.get().developer){
			
			b = new GButt.Panel(SPRITES.icons().l.view_military) {
				@Override
				protected void clickA() {
					if (p.added(cards_enemy))
						p.remove(cards_enemy);
					else
						p.add(cards_enemy, true, true);
					
				};
				@Override
				protected void renAction() {
					selectedSet(p.added(cards_enemy));
				}
			};
			b.hoverInfoSet("armyEnemy");
			butts.addRight(0, b);
		}
		
		if (!battleview) {
			b = new GButt.Panel(SPRITES.icons().m.for_muster) {
				
				boolean shouldmuster;
				
				@Override
				protected void clickA() {
					for (Div d : ARMIES().player().divisions())
						d.settings.musteringSet(shouldmuster);
					for (ROOM_ARTILLERY c : SETT.ROOMS().ARTILLERY) {
						for (int i = 0; i < c.instancesSize(); i++) {
							if (c.getInstance(i).army() == SETT.ARMIES().player())
								c.getInstance(i).muster(shouldmuster);
							
						}
					}
				};
				@Override
				protected void renAction() {
					shouldmuster = false;
					for (Div d : ARMIES().player().divisions())
						shouldmuster |= d.menNrOf() > 0 && !d.settings.mustering();
						
					for (ROOM_ARTILLERY c : SETT.ROOMS().ARTILLERY) {
						for (int i = 0; i < c.instancesSize(); i++) {
							shouldmuster |= c.getInstance(i).army() == SETT.ARMIES().player() && !c.getInstance(i).mustered();
						}
					}
					selectedSet(!shouldmuster); 
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox)text;
					b.title(DicArmy.¤¤Muster);
					b.text(DicArmy.¤¤MusterDesc);
					b.NL(8);
					for (Div d : ARMIES().player().divisions()) {
						if (d.menNrOf() > 0 && d.position().deployed() == 0) {
							b.error(DicArmy.¤¤MusterOneProblem);
						}
					}
						
				}
				
				
			};
			butts.addRight(C.SG*4, b);
		}
		

		
		b = new GButt.BStat2(SPRITES.icons().s.standard, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.perc(text, SETT.ARMIES().armies().get(0).morale());
			}
		}.decrease()) {
			@Override
			protected void clickA() {
				
			}
			
			@Override
			protected void renAction() {
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicArmy.¤¤Morale);
				b.text(DicArmy.¤¤MoraleD);
				b.NL(8);
				
				for (DOUBLE_O<Army> o : ArmyMorale.factors) {
					b.textL(o.info().name);
					b.tab(6);
					b.add(GFORMAT.f1(b.text(), o.getD(SETT.ARMIES().player())));
					if (o instanceof INT_OE<?>) {
						INT_OE<Army> oo = (INT_OE<Army>) o;
						b.tab(8);
						b.add(GFORMAT.i(b.text(), oo.get(SETT.ARMIES().player())));
					}
					b.NL();
					b.text(o.info().desc);
					b.NL(4);
				}
			}
		};
		butts.addRight(C.SG*4, b);
		
		if (S.get().developer) {
			b = new GButt.BStat2(SPRITES.icons().s.standard, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, SETT.ARMIES().armies().get(1).morale());
				}
			}.decrease()) {
				@Override
				protected void clickA() {
					
				}
				
				@Override
				protected void renAction() {
					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicArmy.¤¤Morale);
					b.text(DicArmy.¤¤MoraleD);
					b.NL(8);
					
					for (DOUBLE_O<Army> o : ArmyMorale.factors) {
						b.textL(o.info().name);
						b.tab(6);
						b.add(GFORMAT.f1(b.text(), o.getD(SETT.ARMIES().enemy())));
						if (o instanceof INT_OE<?>) {
							INT_OE<Army> oo = (INT_OE<Army>) o;
							b.tab(8);
							b.add(GFORMAT.i(b.text(), oo.get(SETT.ARMIES().enemy())));
						}
						b.NL();
						b.text(o.info().desc);
						b.NL(4);
					}
				}
			};
			butts.addRight(C.SG*4, b);
		}
		
		b = new GButt.BStat2(SPRITES.icons().s.human, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, SETT.ARMIES().armies().get(1).men());
				text.errorify();
			}
		}) {
			
			int di = 0;
			private final DivTDataStatus stat = new DivTDataStatus();
			@Override
			protected void clickA() {
				if (SETT.ARMIES().armies().get(1).men() > 0) {
					
					
					for (int i = 0; i < Config.BATTLE.DIVISIONS_PER_ARMY; i++) {
						di++;
						
						di %= Config.BATTLE.DIVISIONS_PER_ARMY;
						Div d = SETT.ARMIES().armies().get(1).divisions().get(di);
						if (d.menNrOf() > 0) {
							d.order().status.get(stat);
							w.centerer.set(stat.currentPixelCX(), stat.currentPixelCY());
							return;
						}
					}
					
				}
			}
			
			@Override
			protected void renAction() {
				
			}
		}.hoverInfoSet(HTYPE.ENEMY.names);
		butts.addRight(C.SG*4, b);

		if (!battleview) {
			CharSequence hi = D.g("explanation", "In order to deploy and use your army, press the muster button to the left. Click and drag to select an area of troops, or click the unit cards. You can use control to toggle unit card selection and shift to select several.");
			
			b = new GButt.Panel(SPRITES.icons().m.questionmark).hoverInfoSet(hi);
			butts.addRelBody(8, DIR.E, b);
		}
		
		
		section.body().moveY1(0);
		section.body().moveX1(0);
		butts.body().moveY1(4);
		butts.body().moveX1(section.body().x1()+8);
		section.add(butts);
		
		cards_cata = new UIPanelArtillery(SETT.ARMIES().player(), selection.artillery);
		cards_player = new UIPanelUnitCards(SETT.ARMIES().player(), selection);
		cards_enemy = new UIPanelUnitCards(SETT.ARMIES().enemy(), selection);
		
		top.addLeft(butts);
		p.add(cards_player, true, true);
		
		if (battleview){
			GuiSection r = new GuiSection();
			GButt bb = new GButt.ButtPanel(SPRITES.icons().m.rotate) {
				
				CharSequence t = D.g("restart");
				CharSequence m = D.g("restartD", "Are you sure you want to start the battle over?");
				
				private ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.b().state().reloadBattle();
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().yesNo.activate(m, a, null, true);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(t);
				}
				
			};
			r.addRightC(0, bb);
			bb = new GButt.ButtPanel(SPRITES.icons().m.flag) {
				
				CharSequence t = D.g("Retreat");
				CharSequence d1 = D.g("RetreatD1", "Retreat and lose {0} soldiers.");
				CharSequence d2 = D.g("RetreatD", "Are you sure you wish to retreat and lose {0} soldiers?");
				
				private ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.b().state().liveRetreat();
					}
				};
				
				@Override
				protected void clickA() {
					Str.TMP.clear().add(d2).insert(0, VIEW.b().state().liveRetreatLosses());
					VIEW.inters().yesNo.activate(Str.TMP, a, null, true);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(t);
					Text t = text.text();
					t.add(d1).insert(0, VIEW.b().state().liveRetreatLosses());
					text.add(t);

				}
				
			};
			r.addRightC(0, bb);
			
			r.addRightC(48, new GStat() {
				
				@Override
				public void update(GText text) {
					int tt = (int)VIEW.b().state().throneTimer();
					GFORMAT.iBig(text, (int)VIEW.b().state().throneTimer());
					
					if (tt < BattleState.throneMax/4) {
						text.color(GCOLOR.UI().badFlash());
					}else if (tt < BattleState.throneMax)
						text.color(GCOLOR.UI().SOSO.normal);
					else
						text.color(GCOLOR.UI().GOOD.normal);
					
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					// TODO Auto-generated method stub
					super.hoverInfoGet(b);
				}
			}.hh(SPRITES.icons().m.noble).hoverInfoSet(D.g("throneD","When enemies are standing by the throne, this timer will tick down, and once 0, the battle will be lost.")));
			
			top.addRight(r);
		}
		
		
		
		
		
	}

	@Override
	public void save(FilePutter file) {
		cards_player.saver.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		cards_player.saver.load(file);
		
	}

	@Override
	public void clear() {
		cards_player.saver.clear();
	}
	
	
	
}
