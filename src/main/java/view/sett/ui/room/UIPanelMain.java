package view.sett.ui.room;

import static settlement.main.SETT.*;

import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.employment.RoomEmployment;
import settlement.room.main.employment.RoomEquip;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.INT;
import util.data.INT.IntImp;
import util.dic.DicMisc;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HISTORY_INT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIPanelMain extends ISidePanel {

	final UIPanelWorkPrio work = new UIPanelWorkPrio();
	private final UIPanelWorkTools[] tools = new UIPanelWorkTools[SETT.ROOMS().employment.tools.ALL.size()];

	public UIPanelMain(UIRoom[] rooms) {

		D.gInit(this);
		section.body().setWidth(C.SG * 270);

		titleSet(D.g("title", "workforce & rooms"));

		{
			GuiSection temp = new GuiSection();
			temp.add(new GStat() {

				final CharSequence emp = D.g("Employable");

				@Override
				public void update(GText text) {

					GFORMAT.iofkInv(text, ROOMS().employment.TARGET.get(null), ROOMS().employment.NEEDED.get());
				}

				@Override
				public void hoverInfoGet(GBox b) {
					b.title(STATS.WORK().EMPLOYED.stat().info().name);
					b.textL(DicMisc.¤¤Needed);
					b.tab(5);
					b.add(GFORMAT.iBig(b.text(), ROOMS().employment.NEEDED.get()));
					b.NL(8);

					b.textL(emp);
					b.tab(5);
					b.add(GFORMAT.iBig(b.text(), STATS.WORK().workforce()));
					b.NL();
					for (HTYPE t : HTYPE.ALL()) {

						if (t.works) {
							b.text(t.names);
							b.tab(5);
							b.add(GFORMAT.iBig(b.text(), STATS.POP().pop(t)));
							b.NL();
						}
					}
					b.NL(8);
					b.textL(DicMisc.¤¤Total);
					b.tab(5);
					b.add(GFORMAT.iIncr(b.text(), STATS.WORK().workforce() - ROOMS().employment.NEEDED.get()));

					b.NL(8);
					b.textL(DicMisc.¤¤Rate);
					b.tab(5);
					b.add(GFORMAT.perc(b.text(), STATS.WORK().EMPLOYED.stat().data().getD(null)));
					b.NL();
				};

			}.hh(UI.icons().s.hammer));
			
			temp.addRightC(90, new GStat() {

				CharSequence title = D.g("Odd-jobbers");
				
				@Override
				public void update(GText text) {
					int am = 0;
					for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
						am += p.target.get();
					}
					am = STATS.WORK().workforce() - am;
					if (am < 0)
						am = 0;
					GFORMAT.i(text, am);
					
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.NL();
					b.title(title);
					for (Race r: RACES.all()) {
						b.textL(r.info.names);
						b.tab(5);
						b.add(GFORMAT.i(b.text(), STATS.WORK().workforce(r) - STATS.WORK().EMPLOYED.stat().data().get(r)));
						b.NL();
					}
					
					INT.IntImp ii = new IntImp();
					
					new EntityIterator.Humans() {
						
						@Override
						protected boolean processAndShouldBreakH(Humanoid h, int ie) {
							if (h.indu().hType().works)
								ii.inc(1);
							return false;
						}
					}.iterate();
					
				};
				
			}.hh(UI.icons().s.human).hoverInfoSet(D.g("OddjobbersD",
					"If your workforce is greater than what is needed, the surplus workers are odd-jobbers and will build and do odd jobs over your entire city map. If this number is negative you are missing workers to employ, and need to review your rooms. Ill employed rooms will function poorly.")));

			
			temp.addRightC(90, new GButt.ButtPanel(DicMisc.¤¤Priority) {
				@Override
				protected void clickA() {
					last().add(work, false);
				}
			}.icon(SPRITES.icons().m.arrow_up));

			
			
			section.addRelBody(0, DIR.S, temp);
		}
		
		{
			HISTORY_INT em = SETT.ROOMS().employment.hEmployed();
			GStaples chart = new GStaples(em.historyRecords()) {
				
				@Override
				protected void hover(GBox box, int stapleI) {
					
					box.title(STATS.WORK().EMPLOYED.stat().info().name);
					
					int ii = em.historyRecords()-stapleI - 1;
					GText t = box.text();
					DicTime.setDaysAgo(t, ii);
					t.adjustWidth();
					box.add(t.lablify());
					box.NL();
					box.add(GFORMAT.i(box.text(), em.get(ii)));
					box.NL(8);
				
					
					if (stapleI > 0) {
						for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
							int now = e.history().get(ii);
							int delta = now -  e.history().get(ii+1);
							if (delta != 0) {
								box.add(e.blueprint().iconBig().small);
								box.textLL(e.blueprint().info.names);
								box.tab(7);
								box.add(GFORMAT.iIncr(box.text(), delta));
								box.NL();
							}
						}
					}
					
				}
				
				@Override
				protected double getValue(int stapleI) {
					return em.get(em.historyRecords()-stapleI - 1);
				}
			};
			chart.normalize(true);
			
			chart.body().setWidth(410).setHeight(80);
			
			section.addRelBody(8, DIR.S, chart);
		}

		{
			GuiSection equip = new GuiSection();
			
			int k = 0;
			
			for (RoomEquip w : SETT.ROOMS().employment.tools.ALL) {
				tools[w.index()] = new UIPanelWorkTools(w);
				RENDEROBJ o = new GButt.ButtPanel(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.iofkInv(text, w.currentTotal(), w.neededTotal());
					}
				}) {
					@Override
					protected void clickA() {
						last().add(tools[w.index()], false);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.add(w.info);
						
						b.sep();
						w.boosts.hover(text, 1.0, -1);
						
					}
					
				} .setDim(124, 24+8).icon(w.resource.icon());
				
				equip.add(o, (k%2)*140, (k/2)*32);
				k++;
			}
			
			
			
			section.addRelBody(8, DIR.S, equip);
		}

		
		
		{
			GuiSection s = new UIPanelUtil.BlueprintList(HEIGHT - section.body().y2() - C.SG * 16) {
				
				@Override
				RENDEROBJ row(RoomBlueprintIns<?> b) {
					return rooms[b.index()].clicker;
				}
				
				@Override
				protected void addToCat(GuiSection s, RoomCategoryMain cat) {
					SPRITE ss = new SPRITE.Imp(40, 10) {
						
						@Override
						public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
							int i = 0;
							double k = 0;
						
							for (int ri = 0; ri < cat.all().size(); ri++) {
								RoomBlueprintImp rb = cat.all().get(ri);
								
								if (rb instanceof RoomBlueprintIns<?>) {
									
									RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
									if (rr.employment() == null)
										continue;
									
									i += rr.employment().employed();
									k += rr.employment().neededWorkers();
								}
							}
							
							if (k > 0)
								GMeter.render(r, GMeter.C_REDGREEN, i/k, X1, X2, Y1, Y2);
							
						}
					};
					s.addDownC(2, ss);
					
					RENDEROBJ sss = new GStat() {

						@Override
						public void update(GText text) {
							int i = 0;
						
							for (int ri = 0; ri < cat.all().size(); ri++) {
								RoomBlueprintImp rb = cat.all().get(ri);
								
								if (rb instanceof RoomBlueprintIns<?>) {
									
									RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
									if (rr.employment() == null)
										continue;
									
									i += rr.employment().employed();
								}
							}

							GFORMAT.i(text, i);

						}
					}.r(DIR.N);
					sss.body().centerX(s);
					sss.body().moveY1(s.getLastY2());
					s.add(sss);
				};
			};
			section.addRelBody(8, DIR.S, s);
			
		}

		
		


	}
	
	public void open(RoomEquip w) {
		VIEW.s().panels.add(tools[w.index()], true);
	}



}
