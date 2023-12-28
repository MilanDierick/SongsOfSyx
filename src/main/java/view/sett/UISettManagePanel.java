package view.sett;

import java.util.LinkedList;

import game.GAME;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.health.HEALTH;
import settlement.stats.law.Crimes;
import settlement.stats.law.LAW;
import settlement.stats.standing.STANDINGS;
import settlement.stats.stat.STAT;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.dic.*;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.ui.army.UIArmy;
import view.sett.ui.food.UIFood;
import view.sett.ui.home.UIHomes;
import view.sett.ui.law.UILaw;
import view.sett.ui.noble.UINobles;
import view.sett.ui.room.UIRooms;
import view.sett.ui.standing.UICitizens;
import view.sett.ui.standing.UISlaves;
import view.sett.ui.subject.UISubjects;
import view.ui.top.*;

public final class UISettManagePanel {

	private static LinkedList<RENDEROBJ> extrabutts = new LinkedList<>();
	public static void addExtraElement(RENDEROBJ o) {
		extrabutts.add(o);
	}
	
	

	private int i;
	
	public final UIRooms rooms = new UIRooms();
	public final UISubjects subjects = new UISubjects();
	public final UIArmy army = new UIArmy(SETT.ARMIES());
	public final UICitizens standing = new UICitizens();
	public final UISlaves slaves = new UISlaves();
	public final UINobles nobles = new UINobles();
	public final UILaw law = new UILaw();
	public final UIHomes home = new UIHomes();
	public final UIFood prod = new UIFood();
	
	public UISettManagePanel(SettView w, UIPanelTop panel) {
		
		CLICKABLE b;
		
		GuiSection big = new GuiSection();
		GuiSection small = new GuiSection();
		
		{
			addB(big, new StandingButt(HCLASS.CITIZEN, standing), "CITIZENS");
			addB(big, new StandingButt(HCLASS.SLAVE, slaves), "SLAVES");
			b = new UIPanelTopButtL(SPRITES.icons().s.noble) {
				
				@Override
				protected double valueNext() {
					return value();
				}
				
				@Override
				protected double value() {
					return 1.0;
				}
				
				@Override
				protected int getNumber() {
					return GAME.NOBLE().active();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(HCLASS.NOBLE.names);
					text.text(HCLASS.NOBLE.desc);
				}

				@Override
				protected boolean isActive() {
					return getNumber() > 0;
				};
				
				@Override
				protected void renAction() {
					selectedSet(nobles != null && VIEW.s().panels.added(nobles));
				}
				
				@Override
				protected void clickA() {
					if (nobles != null)
						VIEW.s().panels.add(nobles, true);
				}
			};
			addB(big, b, "NOBLES");
			
			b = new UIPanelTopButtL(SPRITES.icons().s.hammer) {
				
				@Override
				protected double valueNext() {
					return value();
				}
				
				@Override
				protected double value() {
					double t = STATS.WORK().workforce();
					double e = SETT.ROOMS().employment.NEEDED.get();
					if (t == 0)
						return e > 0 ? 0 : 1;
					return CLAMP.d(t/e, 0, 1);
				}
				
				@Override
				protected int getNumber() {
					int t = STATS.WORK().workforce();
					int e = SETT.ROOMS().employment.NEEDED.get();
					return t-e;
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					b.title(DicMisc.¤¤Workforce);
					b.text(DicMisc.¤¤WorkforceD);
					b.NL();
					
					int e = STATS.WORK().workforce();
					int t = SETT.ROOMS().employment.NEEDED.get();
					
					b.textLL(DicMisc.¤¤Needed);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), t));
					b.NL();
					
					b.textLL(DicMisc.¤¤Employees);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), e));
					b.NL();
					
					b.textLL(DicMisc.¤¤Oddjobbers);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), e-t));
					b.NL();
					
				};

				@Override
				protected boolean isActive() {
					return STATS.WORK().workforce() > 0;
				};
				
				@Override
				protected void renAction() {
					selectedSet(rooms.main() != null && VIEW.s().panels.added(rooms.main()));
				}
				
				@Override
				protected void clickA() {
					if (rooms.main() != null)
						VIEW.s().panels.add(rooms.main(), true);
				}
			};
			addB(big, b, "ROOMS");
		}
		
		{

			
			
			
			
			
			
			b = new GButt.BStat2(SPRITES.icons().s.human, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().POP.data().get(null));
				}
			}.decrease()) {
				@Override
				protected void clickA() {
					subjects.show();
				}
				
				@Override
				protected void renAction() {
					selectedSet(subjects.listActive());
				}
			}.hoverInfoSet(STATS.POP().POP.info().name);
			add(small, b, "SUBJECTS");
			
			b = new Buttt(SPRITES.icons().s.house, home) {
				
				@Override
				protected double valueNext() {
					return value();
				}
				
				@Override
				protected double value() {
					double pop = STATS.POP().POP.data(null).get(null);
					if (pop == 0)
						return 1;
					return MATH.pow15.pow((pop-STATS.HOME().GETTER.hasSearched.data(null).get(null))/pop);
				}
				
				@Override
				protected int getNumber() {
					return STATS.HOME().GETTER.hasSearched.data(null).get(null);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(DicMisc.¤¤Housing);
					GBox b = (GBox) text;
					
					b.tab(2);
					b.textLL(DicMisc.¤¤HomeLess);
					b.NL();
					
					STAT s = STATS.HOME().GETTER.hasSearched;
					
					b.NL();
					b.tab(6);
					b.textLL(HCLASS.CITIZEN.names);
					b.tab(9);
					b.textLL(HCLASS.SLAVE.names);
					b.tab(12);
					b.textLL(HCLASS.NOBLE.names);
					b.NL();
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race r = FACTIONS.player().races.get(ri);
						b.add(r.appearance().icon);
						b.textL(r.info.names);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), s.data(HCLASS.CITIZEN).get(r)));
						b.tab(9);
						b.add(GFORMAT.i(b.text(), s.data(HCLASS.SLAVE).get(r)));
						b.tab(12);
						b.add(GFORMAT.i(b.text(), s.data(HCLASS.NOBLE).get(r)));
						b.NL();
					}
				}

				@Override
				protected boolean isActive() {
					return true;
				};
			};
			add(small, b, "HOUSING");			
			
			
			b = new Buttt(SPRITES.icons().s.law, law) {
				
				@Override
				protected double valueNext() {
					return 1.0-LAW.crimes().rate().getD();
				}
				
				@Override
				protected double value() {
					return 1.0-LAW.crimes().rate().getD(1);
				}
				
				@Override
				protected int getNumber() {
					return STATS.POP().pop(HTYPE.PRISONER);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					b.title(DicMisc.¤¤Law);
					
					b.title(HTYPE.PRISONER.name);
					
					b.textLL(HTYPE.PRISONER.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), STATS.POP().pop(HTYPE.PRISONER)));
					b.NL();
					b.text(HTYPE.PRISONER.desc);
					b.NL(7);
					
					
					
					b.textLL(DicMisc.¤¤Crime);
					b.tab(7);
					b.add(GFORMAT.percInv(b.text(), LAW.crimes().rate().getD()));
					b.tab(9);
					b.add(GFORMAT.percIncInv(b.text(), LAW.crimes().rate().getD(1) - LAW.crimes().rate().getD()));
					b.NL();
					b.text(Crimes.¤¤expl);
					b.NL(8);
					
					
				};

				@Override
				protected boolean isActive() {
					return LAW.crimes().rate().getD() > 0 || STATS.POP().pop(HTYPE.PRISONER) > 0;
				};
			};
			add(small, b, "LAW");
			

			
			
			
			b = new Buttt(SPRITES.icons().s.sword, army) {
				

				@Override
				protected double valueNext() {
					return CLAMP.d(1.0-GAME.events().world.raider.CHANCE.getD(), 0, 1);
				}
				
				@Override
				protected double value() {
					return valueNext();
					
				}
				
				@Override
				protected int getNumber() {
					return (int)STATS.BATTLE().DIV.stat().data(null).get(null, 0);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicArmy.¤¤Army);
					
					b.text(DicArmy.¤¤SoldiersD);
					b.NL(8);
					
					b.textL(DicArmy.¤¤Soldiers);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), getNumber()));
					b.NL();
					b.textL(DicArmy.¤¤Recruits);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), STATS.BATTLE().RECRUIT.stat().data(null).get(null, 0)));
					b.NL();
					
					b.NL(8);
					b.textLL(GAME.events().world.raider.CHANCE.info().name);
					b.tab(7);
					b.add(GFORMAT.percBig(b.text(), GAME.events().world.raider.CHANCE.getD()));
					b.NL();
					b.text(GAME.events().world.raider.CHANCE.info().desc);
					
					
				};

				@Override
				protected boolean isActive() {
					return GAME.events().world.raider.CHANCE.getD() != 0 || getNumber() != 0 || STATS.BATTLE().RECRUIT.stat().data(null).get(null, 0) != 0;
				};
			};
			add(small, b, "ARMY");
			
			
			
			b = new Buttt(SPRITES.icons().s.heart, VIEW.UI().health) {
				
				@Override
				protected double valueNext() {
					return HEALTH.rate().getD();
				}
				
				int upI = -60;
				double cache = 0;
				
				@Override
				protected double value() {
					if (GAME.updateI()-upI >= 60) {
						cache = HEALTH.rate().getPeriod(3, 0)/100.0;
						upI = GAME.updateI();
					}
					return cache;
				}
				
				@Override
				protected int getNumber() {
					return STATS.NEEDS().disease.infected().data().get(null);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(HEALTH.¤¤name);
					text.text(HEALTH.¤¤desc);
				};

				@Override
				protected boolean isActive() {
					return true;
				};
				
			};
			add(small, b, "HEALTH");
			
			
			b = new Buttt(SPRITES.icons().s.plate, prod) {
				
				@Override
				protected double valueNext() {
					
					return STATS.FOOD().FOOD_DAYS.data().getD(null);
				}
				
				@Override
				protected double value() {
					return STATS.FOOD().FOOD_DAYS.data().getPeriodD(null, 8, 0);
				}
				
				@Override
				protected int getNumber() {
					return (int) (STATS.FOOD().FOOD_DAYS.data(null).getD(null, 0)* STATS.FOOD().FOOD_DAYS.dataDivider());
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(DicRes.¤¤Food);
				};

				@Override
				protected boolean isActive() {
					return true;
				};
				
			};
			add(small, b, "PRODUCTION");

			
			

			
			
		}
		
		GuiSection s = new GuiSection();
		s.add(big);
		s.addRightC(0, small);
		if (S.get().developer) {
			  b = new GButt.Glow(SPRITES.icons().s.cog) {
				@Override
				protected void clickA() {
					VIEW.s().debug.show();
				}
				@Override
				protected void renAction() {
					selectedSet(VIEW.s().debug.isActivated());
				}
			}.hoverInfoSet("developer tools");
			s.addRelBody(8, DIR.E, b);
		}
		
		panel.addLeft(s);
		
	}
	
	private void add(GuiSection bb, RENDEROBJ o, String key) {
		bb.add(o, (i/2)*80, (i%2)*24);
		i++;
		UISettMap.add(o, key);
	}
	
	private void addB(GuiSection bb, RENDEROBJ o, String key) {
		bb.addRightC(0, o);
		UISettMap.add(o, key);
	}

	private static class StandingButt extends UIPanelTopButtL {
		
		private final HCLASS c;
		private final ISidePanel p;
		
		public StandingButt(HCLASS c, ISidePanel p) {
			super(c.iconSmall());
			this.c = c;
			this.p = p;
		}

		@Override
		protected int getNumber() {
			return STATS.POP().POP.data(c).get(null);
		}

		@Override
		protected double value() {
			return v(STANDINGS.get(c).current());
		}

		@Override
		protected double valueNext() {
			return v(STANDINGS.get(c).target());
		}
		
		private double v(double v) {
			v = (int)(100*v)/100.0;
			return v;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(c.names);
			
			b.textLL(c.names);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), STATS.POP().POP.data(c).get(null)));
			b.NL();
			b.text(c.desc);
			b.NL(7);
			
			b.textLL(STANDINGS.get(c).info().name);
			b.tab(7);
			b.add(GFORMAT.perc(b.text(), STANDINGS.get(c).current()));
			b.add(SPRITES.icons().m.arrow_right);
			b.add(GFORMAT.perc(b.text(), STANDINGS.get(c).target()));
			b.NL();
			b.text(STANDINGS.get(c).info().desc);
		}

		@Override
		protected boolean isActive() {
			return STATS.POP().POP.data(c).get(null) != 0;
		}
		@Override
		protected void renAction() {
			selectedSet(p != null && VIEW.s().panels.added(p));
		}
		
		@Override
		protected void clickA() {
			if (p != null)
				VIEW.s().panels.add(p, true);
		}

	}
	
	private static abstract class Buttt extends UIPanelTopButtS {


		private final ISidePanel p;

		public Buttt(SPRITE icon, ISidePanel p) {
			super(icon);
			this.p = p;
		}
		
		@Override
		protected void renAction() {
			selectedSet(p != null && VIEW.s().panels.added(p));
		}
		
		@Override
		protected void clickA() {
			if (p != null)
				VIEW.s().panels.add(p, true);
		}
		
	}
	
}
