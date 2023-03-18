package view.sett;

import java.util.LinkedList;

import game.GAME;
import game.faction.FACTIONS;
import game.time.Intervals;
import init.race.RACES;
import init.race.Race;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.health.HEALTH;
import settlement.stats.law.Crimes;
import settlement.stats.law.LAW;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.MATH;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.*;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.ui.army.UIArmy;
import view.sett.ui.home.UIHomes;
import view.sett.ui.law.UILaw;
import view.sett.ui.noble.UINobles;
import view.sett.ui.room.UIRooms;
import view.sett.ui.standing.UICitizens;
import view.sett.ui.standing.UISlaves;
import view.sett.ui.subject.UISubjects;
import view.ui.UIPanelTop;

public final class UISettManagePanel {

	private static LinkedList<RENDEROBJ> extrabutts = new LinkedList<>();
	public static void addExtraElement(RENDEROBJ o) {
		extrabutts.add(o);
	}
	
	
	private GuiSection buttss = new GuiSection();
	private int i;
	
	public final UIRooms rooms = new UIRooms();
	public final UISubjects subjects = new UISubjects();
	public final UIArmy army = new UIArmy(SETT.ARMIES());
	public final UICitizens standing = new UICitizens();
	public final UISlaves slaves = new UISlaves();
	public final UINobles nobles = new UINobles();
	public final UILaw law = new UILaw();
	public final UIHomes home = new UIHomes();
	
	
	public UISettManagePanel(UIPanelTop panel) {
		
		CLICKABLE b;
		

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
			add(b, "SUBJECTS");
			
			
			add(new StandingButt(HCLASS.CITIZEN, standing), "CITIZENS");
			add(new StandingButt(HCLASS.SLAVE, slaves), "SLAVES");
			b = new Buttt(SPRITES.icons().s.noble, nobles) {
				
				@Override
				double valueNext() {
					return value();
				}
				
				@Override
				double value() {
					return 1.0;
				}
				
				@Override
				int getNumber() {
					return GAME.NOBLE().active();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(HCLASS.NOBLE.names);
					text.text(HCLASS.NOBLE.desc);
				}

				@Override
				boolean isActive() {
					return getNumber() > 0;
				};
			};
			add(b, "NOBLES");
			
			b = new Buttt(SPRITES.icons().s.house, home) {
				
				@Override
				double valueNext() {
					return value();
				}
				
				@Override
				double value() {
					double pop = STATS.POP().POP.data(null).get(null);
					if (pop == 0)
						return 1;
					return MATH.pow15.pow((pop-STATS.HOME().GETTER.hasSearched.data(null).get(null))/pop);
				}
				
				@Override
				int getNumber() {
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
				boolean isActive() {
					return true;
				};
			};
			add(b, "HOUSING");			
			
			
			b = new Buttt(SPRITES.icons().s.law, law) {
				
				@Override
				double valueNext() {
					return 1.0-LAW.crimes().rate().getD();
				}
				
				@Override
				double value() {
					return 1.0-LAW.crimes().rate().getD(1);
				}
				
				@Override
				int getNumber() {
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
				boolean isActive() {
					return LAW.crimes().rate().getD() > 0 || STATS.POP().pop(HTYPE.PRISONER) > 0;
				};
			};
			add(b, "LAW");
			
			b = new Buttt(SPRITES.icons().s.hammer, rooms.main()) {
				
				@Override
				double valueNext() {
					return value();
				}
				
				@Override
				double value() {
					double t = STATS.WORK().workforce();
					double e = SETT.ROOMS().employment.NEEDED.get();
					if (t == 0)
						return e > 0 ? 0 : 1;
					return CLAMP.d(t/e, 0, 1);
				}
				
				@Override
				int getNumber() {
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
				boolean isActive() {
					return STATS.WORK().workforce() > 0;
				};
			};
			add(b, "ROOMS");
			
			b = new Buttt(VIEW.UI().trade.icon, VIEW.UI().trade) {
				
				@Override
				double valueNext() {
					return STATS.GOVERN().RICHES.data().getD(null);
				}
				
				@Override
				double value() {
					if (!isActive())
						return 1;
					return STATS.GOVERN().RICHES.data().getD(null, 1);
				}
				
				@Override
				int getNumber() {
					return (int)FACTIONS.player().credits().credits();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					b.title(DicRes.¤¤Treasury);
					b.text(DicRes.¤¤TreasuryDesc);
					b.NL();
				};

				@Override
				boolean isActive() {
					return FACTIONS.player().credits().credits() != 0 || FACTIONS.player().credits().creditsH().get(1) != 0;
				};
			};
			add(b, "ECONOMY");
			
			b = new Buttt(VIEW.UI().goods.icon, VIEW.UI().goods) {
				
				@Override
				double valueNext() {
					return value();
				}
				
				@Override
				double value() {
					double s = SETT.ROOMS().STOCKPILE.tally().totalSpace();
					if (s == 0)
						return 1;
					double d = SETT.ROOMS().STOCKPILE.tally().totalAmount();
					return d/s;
				}
				
				@Override
				int getNumber() {
					return (int) SETT.ROOMS().STOCKPILE.tally().totalAmount();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					b.title(VIEW.UI().goods.¤¤Name);
					b.text(VIEW.UI().goods.¤¤Desc);
					b.NL();
					
					b.textL(DicRes.¤¤Stored);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), SETT.ROOMS().STOCKPILE.tally().totalAmount()));
					b.NL();
					b.textL(DicRes.¤¤Capacity);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), SETT.ROOMS().STOCKPILE.tally().totalSpace()));
					b.NL();
				};

				@Override
				boolean isActive() {
					return SETT.ROOMS().STOCKPILE.tally().totalAmount() > 0;
				};
			};
			add(b, "GOODS");
			
			b = new Buttt(SPRITES.icons().s.sword, army) {
				

				@Override
				double valueNext() {
					return CLAMP.d(1.0-GAME.events().raider.CHANCE.getD(), 0, 1);
				}
				
				@Override
				double value() {
					return valueNext();
					
				}
				
				@Override
				int getNumber() {
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
					b.textLL(GAME.events().raider.CHANCE.info().name);
					b.tab(7);
					b.add(GFORMAT.percBig(b.text(), GAME.events().raider.CHANCE.getD()));
					b.NL();
					b.text(GAME.events().raider.CHANCE.info().desc);
					
					
				};

				@Override
				boolean isActive() {
					return GAME.events().raider.CHANCE.getD() != 0 || getNumber() != 0 || STATS.BATTLE().RECRUIT.stat().data(null).get(null, 0) != 0;
				};
			};
			add(b, "ARMY");
			
			b = new Buttt(SPRITES.icons().s.vial, VIEW.UI().tech) {
				
				@Override
				double valueNext() {
					return value();
				}
				
				@Override
				double value() {
					return GAME.player().tech.penalty().getD();
				}
				
				@Override
				int getNumber() {
					return (int) GAME.player().tech.available().get();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					FACTIONS.player().tech.info.hover(text);
				};

				@Override
				boolean isActive() {
					return GAME.player().tech.available().get() != 0 || GAME.player().tech.allocated().get() != 0;
				};
				
			};
			add(b, "TECH");
			
			b = new Buttt(SPRITES.icons().s.heart, VIEW.UI().health) {
				
				@Override
				double valueNext() {
					return HEALTH.rate().getD();
				}
				
				int upI = -60;
				double cache = 0;
				
				@Override
				double value() {
					if (GAME.updateI()-upI >= 60) {
						cache = HEALTH.rate().getPeriod(3, 0)/100.0;
						upI = GAME.updateI();
					}
					return cache;
				}
				
				@Override
				int getNumber() {
					return STATS.NEEDS().disease.infected().data().get(null);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(HEALTH.¤¤name);
					text.text(HEALTH.¤¤desc);
				};

				@Override
				boolean isActive() {
					return true;
				};
				
			};
			add(b, "HEALTH");
			
			
			

			
			

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
				buttss.addRelBody(8, DIR.E, b);
			}
			
		}
		
		panel.addLeft(buttss);
		
	}
	
	private void add(RENDEROBJ o, String key) {
		buttss.add(o, (i%6)*80, (i/6)*24);
		i++;
		UISettMap.add(o, key);
	}

	private static class StandingButt extends Buttt {
		
		private final HCLASS c;
		
		public StandingButt(HCLASS c, ISidePanel p) {
			super(c.iconSmall(), p);
			this.c = c;
			
		}

		@Override
		int getNumber() {
			return STATS.POP().POP.data(c).get(null);
		}

		@Override
		double value() {
			return (int)(100*STANDINGS.get(c).current())/100.0;
		}

		@Override
		double valueNext() {
			return (int)(100*STANDINGS.get(c).target())/100.0;
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
		boolean isActive() {
			return STATS.POP().POP.data(c).get(null) != 0;
		}


	}
	
	private static abstract class Buttt extends GButt {

		private final GStat stat = new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getNumber());
				text.lablify();
			}
		}.decrease();
		private final ISidePanel p;
		

		private static final COLOR worse = new ColorImp(60, 20, 0);
		private static final COLOR badbg = new ColorImp(60, 5, 5);
		private static final COLOR full = new ColorImp(20, 80, 15);
		private static final COLOR notFull = new ColorImp(100, 100,20);
		private static final COLOR fuller = new ColorImp(20, 120, 60);

		public Buttt(SPRITE icon, ISidePanel p) {
			super(icon);
			body.setWidth(icon.width() + stat.height()*4);
			body.setHeight(24);
			this.p = p;
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			renAction();
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			
			double op = 0;
			if (isHovered || isSelected) {
				if (isSelected)
					COLOR.WHITE85.render(r, body, -2);
				else
					COLOR.WHITE50.render(r, body, -2);
				op = 0.8;
			}else if(isActive) {
				op = 0.6;
			}else {
				op = 0.1;
			}
			
			boolean active = isActive();
			
			ColorImp.TMP.set(active ? badbg : COLOR.WHITE10).shadeSelf(op);
			ColorImp.TMP.render(r, body, -4);
		
			double cu = value();
			double ta = valueNext();
			ta = CLAMP.d(ta, 0, 1);
			ColorImp col = ColorImp.TMP;
			
			if (active) {
				if (cu == ta && cu == 1) {
					col.set(full);
				}else if (ta > cu){
					col.interpolate(notFull, fuller, Intervals.circlePow(VIEW.renderSecond(), 1));
				}else if (ta < cu){
					col.interpolate(notFull, worse, Intervals.circlePow(VIEW.renderSecond(),  1));
				}else {
					col.set(notFull);
				}
			}else {
				cu = 1.0;
				col.set(COLOR.WHITE35);
			}
			
			
			
			col.shadeSelf(op);
			col.render(r, body.x1()+4, (int) (body.x1()+4+(body.width()-8)*cu), body.y1()+4, body.y2()-4);
			
			label.renderCY(r, body().x1()+4, body.cY());
			
			if (active) {
				OPACITY.O35.bind();
				stat.adjust();
				COLOR.BLACK.render(r, body().x1()+4+label.width()+2, body().x1()+4+label.width()+2+stat.width()+4, body.y1()+4, body.y2()-4);
				OPACITY.unbind();
				stat.renderCY(r, body().x1()+4+label.width()+4, body.cY());
			}
			
			
			

			
		}
//		
//		@Override
//		public void hoverInfoGet(GUI_BOX text) {
//			
//			GBox b = (GBox) text;
//			
//			
//			super.hoverInfoGet(text);
//			b.NL(8);
//			b.add(GFORMAT.perc(b.text(), value()));
//			b.add(value() <= valueNext() ? SPRITES.icons().m.arrow_right : SPRITES.icons().m.arrow_left);
//			b.add(GFORMAT.perc(b.text(), valueNext()));
//		}
		
		@Override
		protected void renAction() {
			selectedSet(p != null && VIEW.s().panels.added(p));
		}
		
		@Override
		protected void clickA() {
			if (p != null)
				VIEW.s().panels.add(p, true);
		}
		
		abstract int getNumber();
		abstract double value();
		abstract double valueNext();
		abstract boolean isActive();
		
	}
	
}
