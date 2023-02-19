package view.sett.ui.room;

import static settlement.main.SETT.*;

import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables;
import settlement.stats.StatsEquippables.StatEquippableWork;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIPanelMain extends ISidePanel {

	final UIPanelWorkPrio work = new UIPanelWorkPrio();
	private final UIPanelWorkTools[] tools = new UIPanelWorkTools[STATS.EQUIP().work().size()];

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

			}.hv(STATS.WORK().EMPLOYED.stat().info().name));
			
			temp.addRightC(48, new GStat() {

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
					
					for (Race r: RACES.all()) {
						b.textL(r.info.names);
						b.tab(5);
						b.add(GFORMAT.i(b.text(), STATS.WORK().workforce(r) - STATS.WORK().EMPLOYED.stat().data().get(r)));
						b.NL();
					}
						
				};
				
			}.hv(D.g("Odd-jobbers")).hoverInfoSet(D.g("OddjobbersD",
					"If your workforce is greater than what is needed, the surplus workers are odd-jobbers and will build and do odd jobs over your entire city map. If this number is negative you are missing workers to employ, and need to review your rooms. Ill employed rooms will function poorly.")));

			section.addRelBody(0, DIR.S, temp);
		}

		{
			GuiSection equip = new GuiSection();
			
			int k = 0;
			{
				GuiSection temp = new GuiSection();
				
				temp.addRightC(32, new GButt.ButtPanel(DicMisc.¤¤Priority) {
					@Override
					protected void clickA() {
						last().add(work, false);
					}
				}.icon(SPRITES.icons().m.arrow_up));

				
				equip.add(temp, (k%2)*128, (k/2)*32);
				k++;
			}
			
			for (StatEquippableWork w : STATS.EQUIP().work()) {
				tools[w.tIndex] = new UIPanelWorkTools(w);
				RENDEROBJ o = new GButt.ButtPanel(new GStat() {
					
					@Override
					public void update(GText text) {
						int current = w.stat().data().get(null);
						current += SETT.ROOMS().STOCKPILE.tally().amountTotal(w.resource());
						int needed = 0;
						for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
							if (w.target(p) > 0)
								needed += w.target(p)*p.neededWorkers();
						}
						GFORMAT.iofkInv(text, current, needed);
					}
				}) {
					@Override
					protected void clickA() {
						last().add(tools[w.tIndex], false);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(w.resource.names);
						b.text(w.stat().info().desc);
						b.NL(8);
						
						b.textL(StatsEquippables.¤¤WearRate);
						b.add(GFORMAT.perc(b.text(), w.wearRate));
						b.NL();
						b.text(StatsEquippables.¤¤WearRateD);
					}
					
				} .setDim(124, 24+8).icon(w.resource().icon());
				
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
	
	public void open(StatEquippableWork w) {
		VIEW.s().panels.add(tools[w.tIndex], true);
	}



}
