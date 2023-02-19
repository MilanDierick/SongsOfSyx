package view.sett.ui.standing;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.stats.*;
import settlement.stats.StatsPopulation.StatsDeath.PopData;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;

final class CatPopulation extends Cat {

	private static CharSequence ¤¤age = "¤Age {0} to {1} : {2} Subjects";
	private static CharSequence ¤¤immi = "¤Aspiring Immigrants";
	private static CharSequence ¤¤Athorize = "¤Authorize";
	private static CharSequence ¤¤Auto = "¤Auto";
	private static CharSequence ¤¤AutoDesc = "¤Automatically authorize new immigrants up until this amount.";
	private static CharSequence ¤¤ageAverage = "¤Average Age:";
	
	CatPopulation(HCLASS cl){
		super(new StatCollection[] {STATS.POP()});
		StatCollection c = STATS.POP();
		
		D.ts(CatPopulation.class);
		titleSet(c.info.name);
		
		if (cl == HCLASS.CITIZEN) {
			section.add(pop(cl));
		}
		else {
			section.add(numbers());
		}
		section.addDown(4, popChart(cl, section.body().width()));
		if (cl == HCLASS.CITIZEN) {;
			section.addDown(16, immi());
		}
		
		
		LinkedList<RENDEROBJ> rens = new LinkedList<RENDEROBJ>();
		
		for (STAT s : c.all()) {
			if (s.key() == null)
				continue;
			if (s == STATS.POP().AGE) {
				continue;
			}else
				rens.add(new StatRow(s, cl));
		}
		
		
		
		

		
		section.addDown(16, new GScrollRows(rens, HEIGHT-section.getLastY2()-32, 0).view());
		
	}
	
	private RENDEROBJ numbers() {
		GuiSection s = new GuiSection();
		
		int i = 0;
		for (Race r : RACES.all()) {
			RENDEROBJ rr = new GStat() {
				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().POP.data(HCLASS.SLAVE).get(r));
				}
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(r.info.names);
					b.add(VIEW.s().ui.standing.hi.init(HCLASS.SLAVE, STATS.POP().POP, false, r, false));
					b.NL(8);
					b.textL(BOOSTABLES.BEHAVIOUR().SUBMISSION.name);
					b.add(GFORMAT.f0(b.text(), BOOSTABLES.BEHAVIOUR().SUBMISSION.get(HCLASS.SLAVE, r)));
					
				};
			}.hv(r.appearance().icon);
			rr.body().moveC(60*(i%8), rr.body().height()*(i/8));
			i++;
			s.add(rr);
			
		}
		
		return s;
	}
	
	private GuiSection immi() {
		
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				visableSet(CitizenMain.current != null);
				if (CitizenMain.current != null)
					super.render(r, ds);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				if (CitizenMain.current != null && text.emptyIs())
				{
					text.title(STATS.POP().TYPE.IMMIGRANT.info().names);
					SETT.ENTRY().immi().hoverImmigrants(text, CitizenMain.current);
				}
			}
		};
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, SETT.ENTRY().immi().wanted(CitizenMain.current));
			}
			
		}.hh(¤¤immi));
		
		INTE m = new INTE() {
			
			double d = 1.0;
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return SETT.ENTRY().immi().wanted(CitizenMain.current);
			}
			
			@Override
			public int get() {
				return (int) (max()*d);
			}
			
			@Override
			public void set(int t) {
				d = (double) t / max();
			}
		};
		s.addDown(4, new GGaugeMutable(m, 200) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.i(text, m.get());
				return 48;
			}
		});
		
		s.addRightC(8, new GButt.ButtPanel(¤¤Athorize) {
			
			@Override
			protected void clickA() {
				if (CitizenMain.current != null)
					SETT.ENTRY().immi().admit(CitizenMain.current, m.get());
			}
		});
		
		s.add(new HOVERABLE.Sprite(new GText(UI.FONT().S, ¤¤Auto)).hoverInfoSet(¤¤AutoDesc), s.getLastX2()+16, s.body().y1());
		
		INTE a = new INTE() {
			
			
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return SETT.ENTRY().immi().auto(CitizenMain.current).max()/25;
			}
			
			@Override
			public int get() {
				return SETT.ENTRY().immi().auto(CitizenMain.current).get()/25;
			}
			
			@Override
			public void set(int t) {
				SETT.ENTRY().immi().auto(CitizenMain.current).set(t*25);
			}
		};
		
		s.addDown(4, new GGaugeMutable(a, 200) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.i(text, SETT.ENTRY().immi().auto(CitizenMain.current).get());
				return 48;
			}
		});
		
		return s;
		
	}
	
	private GuiSection pop(HCLASS cl) {
		GuiSection s = new GuiSection();
		
		
		for (HTYPE t : HTYPE.ALL()) {
			if (t.CLASS != HCLASS.CHILD &&( t.CLASS != cl || !t.visible))
				continue;
			s.addDown(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().pop(CitizenMain.current, t));
					text.lablifySub();
				}
			}.decrease().hh(t.names, 130).hoverInfoSet(t.desc));
		}
		
		s.addDown(8, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(cl).get(CitizenMain.current));
			}
		}.decrease().hh(STATS.POP().POP.info().name, 130).hoverInfoSet(STATS.POP().POP.info().desc));
		
		
		GStaples staples = new GStaples(STATS.POP().demography().historyRecords()) {
			double demoMax;
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				demoMax = 0;
				for (int i = 0; i < STATS.POP().demography().historyRecords(); i++)
					if (STATS.POP().demography().getD(CitizenMain.current, i) > demoMax)
						demoMax = STATS.POP().demography().getD(CitizenMain.current, i);
				super.render(r, ds, isHovered);
			}
			
			@Override
			protected void hover(GBox text, int stapleI) {
				int k = stapleI;
				text.title(STATS.POP().demography().info().name);
				if (CitizenMain.current != null) {
					Text t = text.text();
					t.add(¤¤age);
					
					int from = (int) (k*BOOSTABLES.PHYSICS().DEATH_AGE.get(cl, CitizenMain.current)/STATS.POP().demography().historyRecords());
					t.insert(0, from);
					
					if (k == STATS.POP().demography().historyRecords()-1) {
						t.insert(1, '+');
					}else {
						int to = (int) ((k+1)*BOOSTABLES.PHYSICS().DEATH_AGE.get(cl, CitizenMain.current)/STATS.POP().demography().historyRecords());
						t.insert(1, to);
					}
					
					t.insert(2, (int) STATS.POP().demography().getD(CitizenMain.current, k));
					text.add(t);
				}
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				int k = stapleI;
				double am = STATS.POP().demography().getD(CitizenMain.current, k);
				if (demoMax > 0) {
					am /= demoMax;
				}
				return am;
			}
			
			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				c.set(GCOLOR.UI().SOSO.hovered);
			}
		};
		
		staples.body().setWidth(10*STATS.POP().demography().historyRecords());
		staples.body().setHeight(100);
		s.addRelBody(48, DIR.E, staples);
		RENDEROBJ h = new GStat() {
			
			@Override
			public void update(GText text) {
				double pop = STATS.POP().POP.data(cl).get(CitizenMain.current);
				double d = 0;
				if (pop > 0) {
					d = STATS.POP().AGE.data(cl).get(CitizenMain.current);
					d /= pop*TIME.years().bitConversion(TIME.days());
				}
				GFORMAT.f(text, d);
			}
		}.hh(¤¤ageAverage);
				
		h.body().moveC(staples.body().cX(), staples.body().y1()-h.body().height()/2-2);
		s.add(h);
		
		
		return s;
		
	}
	
	private RENDEROBJ popChart(HCLASS cl, int width) {
		
		GuiSection ss = new GuiSection();
		
		GStaples s = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				
				box.title(DicMisc.¤¤Population);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);
				
				box.textLL(DicMisc.¤¤Population);
				box.tab(7);
				box.add(GFORMAT.iBig(box.text(), (int) getValue(stapleI)));
				box.NL(8);
				box.tab(7);
				box.add(GFORMAT.iIncr(box.text(), (int) (getValue(stapleI)-getValue(stapleI-1))));
				box.NL(4);
				
				for (CAUSE_ARRIVE a : CAUSE_ARRIVE.ALL()) {
					
					int am = STATS.POP().COUNT.enters().get(a.index()).statistics(cl).history(CitizenMain.current).get(i);
					if (cl == HCLASS.CITIZEN)
						am += STATS.POP().COUNT.enters().get(a.index()).statistics(HCLASS.CHILD).history(CitizenMain.current).get(i);
					if (am > 0) {
						box.textL(a.name);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), am));
						box.NL();
					}
				}
				
				box.NL(4);
				
				for (CAUSE_LEAVE a : CAUSE_LEAVE.ALL()) {
					
					int am = STATS.POP().COUNT.leaves().get(a.index()).statistics(cl).history(CitizenMain.current).get(i);
					if (cl == HCLASS.CITIZEN)
						am += STATS.POP().COUNT.leaves().get(a.index()).statistics(HCLASS.CHILD).history(CitizenMain.current).get(i);
					if (am > 0) {
						box.textL(a.names);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), -am));
						box.NL();
					}
				}
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = STATS.DAYS_SAVED-stapleI-1;
				if (i >= STATS.DAYS_SAVED)
					i = STATS.DAYS_SAVED-1;
				int am = STATS.POP().POP.data(cl).get(CitizenMain.current, i);
				if (cl == HCLASS.CITIZEN)
					am += STATS.POP().POP.data(HCLASS.CHILD).get(CitizenMain.current, i);
				return am;
			}
		};
		

		s.body().setWidth(width);
		s.body().setHeight(80);
		ss.add(s);
		
		
		final SPRITE[] cols = new SPRITE[STATS.POP().COUNT.leaves().size()];
		for (int i = 0; i < cols.length; i++) {
			int k = i;
			cols[i]= new SPRITE.Imp(ICON.MEDIUM.SIZE, ICON.MEDIUM.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					COLOR.UNIQUE.getC(k).bind();
					SPRITES.icons().m.circle_inner.render(r, X1, Y1);
					COLOR.unbind();
					//SPRITES.icons().m.circle_frame.render(r, X1, Y1);
					
				}
			};
		}
		
		s = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				box.title(STATS.POP().WRONGFUL.info().names);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);
				int di = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					if (CAUSE_LEAVE.ALL().get(di).defaultStanding() <= 0) {
						di++;
						continue;
					}
					box.add(cols[di]);
					box.textL(s.info().name);
					box.tab(7);
					box.add(GFORMAT.iIncr(box.text(), s.statistics(cl).history(CitizenMain.current).get(i)));
					box.NL();
					di++;
				}
			}
			
			@Override
			protected double getValue(int stapleI) {
				double am = 0;
				int i = STATS.DAYS_SAVED-stapleI-1;
				int di = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					if (CAUSE_LEAVE.ALL().get(di++).defaultStanding() <= 0)
						continue;
					am += s.statistics(cl).history(CitizenMain.current).get(i); 
				}
				return am;
			}
			
			@Override
			protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
					int x1, int x2, int y1, int y2) {
				
				double am = 0;
				int i = STATS.DAYS_SAVED-stapleI-1;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					am += s.statistics(cl).history(CitizenMain.current).get(i);
				}
				
				int h = y2-y1;
				if (h <= 0)
					h = 1;
				if (am == 0)
					return;
				
				int ci = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					if (CAUSE_LEAVE.ALL().get(ci).defaultStanding() <= 0) {
						ci++;
						continue;
					}
					double d = s.statistics(cl).history(CitizenMain.current).get(i);
					d /= am;
					int hh = (int) Math.ceil(h*d); 
					
					if (hh > 0) {
						ColorImp c = ColorImp.TMP;
						c.set(COLOR.UNIQUE.getC(ci));
						c.shadeSelf(hovered ? 0.75 : 0.55);
						c.render(r, x1, x2, y2-hh, y2);
						c.set(COLOR.UNIQUE.getC(ci));
						c.shadeSelf(hovered ? 1 : 0.80);
						c.render(r, x1+1, x2-1, y2-hh+1, y2-1);
						y2-= hh;
					}
					ci++;
					
				}
				
			}
		};
		
		s.body().setWidth(width);
		s.body().setHeight(80);
		ss.addDown(6, s);
		
		return ss;
		
	}
	
	
	
}
