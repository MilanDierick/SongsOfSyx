package view.sett.ui.standing;

import init.D;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.standing.CatServices.StatRowService;
import view.sett.ui.standing.Cats.Cat;

class CatOccupation extends Cat {

	CatOccupation(HCLASS cl){
		super(new StatCollection[] {STATS.WORK(), STATS.EDUCATION()});
		D.gInit(this);
		titleSet(DicMisc.¤¤Occupation);
		LinkedList<RENDEROBJ> rens = new LinkedList<>();

		
		rens.add(new StatRow.Title(STATS.WORK().info));
		for (STAT s : STATS.WORK().all()) {

			if (s == STATS.WORK().RETIREMENT_HOME && cl != HCLASS.SLAVE) {
				rens.add(new StatRow(s, cl));
				rens.add(makeHomes(cl));
			}else {
				rens.add(new StatRow(s, cl));
			}
		}
		
		rens.add(new GButt.ButtPanel(D.g("WorkPrio", "Work Priorities")) {
			@Override
			protected void clickA() {
				VIEW.s().ui.rooms.prio(cl, CitizenMain.current, this);
			}
		}.pad(16, 2));
		
		if (cl != HCLASS.SLAVE) {
			
			rens.add(new StatRow.Title(STATS.EDUCATION().info));
			for (STAT s : STATS.EDUCATION().all()) {
				rens.add(new StatRow(s, cl));
			}
			
			for (ROOM_UNIVERSITY u : SETT.ROOMS().UNIVERSITIES) {
				GuiSection s = new GuiSection();
				s.body().incrW(24);
				s.hoverInfoSet(u.info.desc);
				s.addRightC(0, new RENDEROBJ.Sprite(u.iconBig()));
				s.addRightC(8, new GText(UI.FONT().S, u.info.names));
				s.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.iofk(text, u.employment().employed(), u.employment().neededWorkers());
					}
				}.hh(UI.icons().s.citizen), 0, s.body().y2()+2);
				
				s.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, u.bonus().get(HCLASS.CITIZEN.get(CitizenMain.current)));
					}
				}.hh(UI.icons().s.clock), 0, s.body().y2()+2);
				
				
				
				INTE in = new INTE() {

					@Override
					public int get() {
						return (int) (u.limit.getD(CitizenMain.current)*16);
					}

					@Override
					public int min() {
						return 0;
					}

					@Override
					public int max() {
						return 16;
					}

					@Override
					public void set(int t) {
						u.limit.setD(CitizenMain.current, t/16.0);
					}
				
				};
				s.addRightC(128, new GGaugeMutable(in, 120).hoverTitleSet(u.limit.info().name).hoverInfoSet(u.limit.info().desc));
				
				rens.add(s);
			}
			rens.add(new StatRowService(STATS.SERVICE().SCHOOLS, HCLASS.CHILD, true));
			
			{
				GuiSection s = new GuiSection();
				
				GButt.ButtPanel p = new GButt.ButtPanel(D.g("Educate")) {
					
					@Override
					protected void clickA() {
						STATS.EDUCATION().policyIndoctor.set(CitizenMain.current, false);
					}
					
					@Override
					protected void renAction() {
						selectedSet(!STATS.EDUCATION().policyIndoctor.is(CitizenMain.current));
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.text(STATS.EDUCATION().EDUCATION.info().desc);
						
						b.NL(8);
						b.textLL(DicMisc.¤¤Boosts);
						b.NL();
						for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
							if (e.educationFactor > 0) {
								b.add(e.blueprint().iconBig().small);
								b.text(e.blueprint().info.names);
								b.tab(6);
								b.add(GFORMAT.f0(b.text(), e.educationFactor));
								b.NL();
							}
						}
						
					}
					
				};
				p.pad(32, 4);
				s.body().incrW(48).incrH(1);
				s.addRightC(0, p);
				p = new GButt.ButtPanel(STATS.EDUCATION().policyIndoctor.info().name) {
					
					@Override
					protected void clickA() {
						STATS.EDUCATION().policyIndoctor.toggle(CitizenMain.current);
					}
					
					@Override
					protected void renAction() {
						selectedSet(STATS.EDUCATION().policyIndoctor.is(CitizenMain.current));
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(STATS.EDUCATION().policyIndoctor.info().name);
						b.text(STATS.EDUCATION().policyIndoctor.info().desc);
						
						b.NL(8);
						b.textLL(DicMisc.¤¤Boosts);
						b.NL();
						for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
							if (e.indoctorFactor > 0) {
								b.add(e.blueprint().iconBig().small);
								b.text(e.blueprint().info.names);
								b.tab(6);
								b.add(GFORMAT.f0(b.text(), e.indoctorFactor));
								b.NL();
							}
						}
						
					}
					
				};
				p.pad(32, 4);
				s.body().incrW(48).incrH(1);
				s.addRightC(0, p);
				
				
				rens.add(s);
			}
			
			{
				GuiSection s = new GuiSection();
				
				s.addRightCAbs(180, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, STATS.POP().pop(CitizenMain.current, HTYPE.CHILD));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						for (STAT s : STATS.EDUCATION().all()) {
							b.textLL(s.info().name);
							b.tab(5);
							b.add(GFORMAT.perc(b.text(), s.data(HCLASS.CHILD).getD(CitizenMain.current)));
							b.NL();
						}
					};
					
				}.hv(HCLASS.CHILD.names));
				s.pad(16, 8);
				rens.add(s);
				
			}
		}
		
		
		section.add(new GScrollRows(rens, HEIGHT, 0).view());
		
	}
	
	GuiSection makeHomes(HCLASS c) {
		GuiSection s = new GuiSection();
		
		int i = 0;
		
		for (ROOM_RESTHOME hh : SETT.ROOMS().RESTHOMES) {
			
			final ROOM_RESTHOME h = hh;
			
			
			SPRITE icon = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (CitizenMain.current != null && CitizenMain.current.pref().getWork(h.employment()) <= 0) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, X1, X2, Y1, Y2);
						OPACITY.unbind();
					}
					h.iconBig().render(r, X1, X2, Y1, Y2);
				}
			};
			
			RENDEROBJ r = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, h.employment().neededWorkers()-h.employment().employed());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(h.info.names);
					b.text(h.info.desc);
					b.NL(8);
					b.textLL(HTYPE.RETIREE.names);
					b.tab(5);
					b.add(GFORMAT.iofk(b.text(), h.employment().employed(), h.employment().neededWorkers()));
					b.NL();
					b.textLL(DicMisc.¤¤Quality);
					b.tab(5);
					b.add(GFORMAT.perc(b.text(), h.quality()));
					b.NL();
					if (CitizenMain.current != null) {
						b.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
						b.tab(5);
						b.add(GFORMAT.perc(b.text(), CitizenMain.current.pref().getWork(h.employment())));
					}
						
				};
				
			}.hh(icon);
			
			s.add(r, 150*(i%4), 40*(i/4));
			i++;
			
		}
		
		s.pad(8);
		return s;
		
	}
	
	
	
}
