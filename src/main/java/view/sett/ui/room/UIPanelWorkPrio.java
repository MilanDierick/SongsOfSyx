package view.sett.ui.room;

import init.D;
import init.race.*;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.employment.RoomEmployment;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.sett.ui.room.UIPanelUtil.RoomRow;

final class UIPanelWorkPrio extends ISidePanel {


	private static CharSequence ¤¤MasterPrio = "¤Master Priority";
	private static CharSequence ¤¤MasterPrioD = "¤When subjects are assigned to workplaces, this is the master priority, and the highest will be filled first. Each workplace will be filled according to the species priorities.";
	static CharSequence ¤¤Adjust = "¤Adjust all by 1";
	
	static {
		D.ts(UIPanelWorkPrio.class);
	}
	
	
	private EGROUP group;
	
	public UIPanelWorkPrio() {
		D.t(this);
		titleSet(D.g("Work Priorities"));
		
		section.addRelBody(4, DIR.S, selector());
		section.addRelBody(2, DIR.S, new Details());
		
		RENDEROBJ q = new UIPanelUtil.BlueprintList(HEIGHT-section.body().y2()-16) {
			
			@Override
			RENDEROBJ row(RoomBlueprintIns<?> b) {
				if (!(b instanceof RoomBlueprintIns<?>)) {
					return null;
				}
				RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
				
				if (bb.employmentExtra() == null) {
					return null;
				}
				
				return new WorkButt(bb);
			}
			
			@Override
			protected void addToCat(GuiSection s, RoomCategoryMain cat) {
				SPRITE ss = new SPRITE.Imp(40, 10) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (group != null)
							return;
						int i = 0;
						double k = 0;
					
						for (int ri = 0; ri < cat.all().size(); ri++) {
							RoomBlueprintImp rb = cat.all().get(ri);
							
							if (rb instanceof RoomBlueprintIns<?>) {
								
								RoomBlueprintIns<?> rr = (RoomBlueprintIns<?>) rb;
								if (rr.employmentExtra() == null)
									continue;
								
								i += rr.employmentExtra().current().get(null);
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
								if (rr.employmentExtra() == null)
									continue;
								
								if (group == null)
									i += rr.employmentExtra().target.get();
								else
									i += rr.employmentExtra().target.group(group);
							}
						}
						
						if (group == null)
							GFORMAT.i(text, i);
						else if (STATS.WORK().workforce(group) > 0)
							GFORMAT.perc(text, (double)i/STATS.WORK().workforce(group));
					}
				}.r(DIR.N);
				sss.body().centerX(s);
				sss.body().moveY1(s.getLastY2());
				s.add(sss);
				
				RENDEROBJ r = new GButt.Glow(SPRITES.icons().s.magnifier) {
					
					@Override
					protected void clickA() {
						for (RoomBlueprintImp b : cat.all()) {
							if (b instanceof RoomBlueprintIns<?>) {
								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employmentExtra() != null) {
									if (group == null)
										bb.employmentExtra().priority.inc(1);
									else
										bb.employmentExtra().priorities.inc(group, 1);
								}
							}
						}
					}
					
				}.hoverInfoSet(¤¤Adjust);
				
				r.body().moveX1(s.body().x2()+4);
				r.body().moveCY(s.body().cY()-16);
				s.add(r);
				
				r = new GButt.Glow(SPRITES.icons().s.minifier) {
					
					@Override
					protected void clickA() {
						for (RoomBlueprintImp b : cat.all()) {
							if (b instanceof RoomBlueprintIns<?>) {
								RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
								if (bb.employmentExtra() != null) {
									if (group == null)
										bb.employmentExtra().priority.inc(-1);
									else
										bb.employmentExtra().priorities.inc(group, -1);
								}
							}
						}
					}
					
				}.hoverInfoSet(¤¤Adjust);
				
				s.addDownC(8, r);
			}
		};
		
		section.addRelBody(8, DIR.S, q);
		
		
	}
	
	void set(Race race, HCLASS cl) {
		group = EGROUP.get(cl == HCLASS.CITIZEN ? HTYPE.SUBJECT : HTYPE.SLAVE, race);
	}
	
	private class Details extends GuiSection {

		Details() {
			add(new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					employed(group, text);
				}
			}.hh(STATS.WORK().EMPLOYED.stat().info()));

			addRightC(64, new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					fullfillment(group, text);
				}
			}.hh(STATS.WORK().WORK_FULFILLMENT.info()));

			addRightC(64, new GStat() {

				@Override
				public void update(GText text) {
					if (group == null)
						return;
					skill(group, text);
				}
			}.hh(DicMisc.¤¤Skill));
		}
		
	

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (group == null)
				return;
			super.render(r, ds);
		}

	}
	
	
	private static GText employed(EGROUP group, GText text) {
		int of = STATS.POP().pop(group.r, group.t);
		int c = SETT.ROOMS().employment.TARGET.get(group);
		GFORMAT.iofkInv(text, c, of);
		return text;
	}
	
	private static GText fullfillment(EGROUP group, GText text) {
		double f = 0;
		double am = 0;
		for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
			f += group.r.pref().getWork(p) * p.target.group(group);
			am += p.target.group(group);
		}
		f /= am;
		GFORMAT.percGood(text, f);
		return text;
	}
	
	private static GText skill(EGROUP group, GText text) {
		double f = 0;
		int am = 0;
		for (RoomEmployment p : SETT.ROOMS().employment.ALL()) {
			if (p.blueprint().bonus() != null) {
				f += p.blueprint().bonus().get(group.r) * p.target.group(group);
			}
			am += p.target.group(group);
		}
		f /= am;

		GFORMAT.perc(text, f);
		return text;
	}
	
	private GuiSection selector() {
		GuiSection butts = new GuiSection();
		int y1 = 0;
		
		for (HTYPE t : new HTYPE[] {HTYPE.SUBJECT, HTYPE.SLAVE}) {
			
			int x1 = 0;
			for (Race r : RACES.all()) {
				SPRITE s = new SPRITE.Imp(Icon.M) {
					
					@Override
					public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
						r.appearance().icon.render(rr, X1, X2, Y1, Y2);
						t.CLASS.iconSmall().render(rr, X1+8, X2+8, Y1+4, Y2+4);
					}
				};
				
				GButt b = new GButt.ButtPanel(s) {
					
					@Override
					protected void clickA() {
						group = EGROUP.get(t, r);
					}
					
					@Override
					protected void renAction() {
						selectedSet(group == EGROUP.get(t, r));
					}
					
					@Override
					protected void render(SPRITE_RENDERER rr, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {

						super.render(rr, ds, isActive, isSelected, isHovered);
						if (STATS.WORK().workforce(EGROUP.get(t, r)) <= 0) {
							OPACITY.O50.bind();
							COLOR.BLACK.render(rr, body);
							OPACITY.unbind();
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.textLL(r.info.names);
						b.NL();
						b.textL(t.names);
						b.NL(4);
						
						b.textL(DicMisc.¤¤Employees);
						b.add(employed(EGROUP.get(t, r), b.text()));
						b.NL();
						b.textL(STATS.WORK().WORK_FULFILLMENT.info().name);
						b.add(fullfillment(EGROUP.get(t, r), b.text()));
						b.NL();
						b.textL(DicMisc.¤¤Skill);
						b.add(skill(EGROUP.get(t, r), b.text()));
						
						b.NL();
						
						
					}
					
				}.pad(3, 3);
				
				
				butts.add(b, x1, y1);
				x1 += b.body.width();
			}
			y1 = butts.getLastY2();
			
		}
		GButt b = new GButt.ButtPanel(SPRITES.icons().m.arrow_up) {
			
			@Override
			protected void clickA() {
				group = null;
			}
			
			@Override
			protected void renAction() {
				selectedSet(group == null);
			}
			
		};
		b.hoverTitleSet(¤¤MasterPrio).hoverInfoSet(¤¤MasterPrioD);
		b.body.setDim(butts.body().height());
		butts.addRelBody(0, DIR.W, b);
		
		butts.addRelBody(24, DIR.E, new GButt.ButtPanel(SPRITES.icons().m.repair) {
			@Override
			protected void clickA() {
				if (group == null)
					return;
				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.setPrioOnSkill(group);
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(group != null);
			};
			
		}.hoverInfoSet(D.g("SortW", "Set all priorities based on work skill.")));
		butts.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.heart) {
			@Override
			protected void clickA() {
				if (group == null)
					return;
				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.setPrioOnFullfillment(group);
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(group != null);
			};
			
		}.hoverInfoSet(D.g("SortF", "Set all priorities based on fulfillment.")));
		butts.addRightC(2, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			@Override
			protected void clickA() {
				if (group == null) {
					for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
						e.priority.set(e.priority.max()/2);
					}
					return;
				}
				
				for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
					e.priorities.set(group, e.priorities.max(group)/2);
				}
			}
			
		}.hoverInfoSet(D.g("clear", "Set all priorities to default")));
		
		return butts;
		
	}

	private SPRITE underline = new SPRITE.Imp(Icon.M) {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			COLOR.WHITE100.render(r, X1, X2, Y1, Y2);
			
		}
	};
	
	private class WorkButt extends RoomRow {

		private final RoomEmployment p;
		
		WorkButt(RoomBlueprintIns<?> b) {
			super(b);
			this.p = b.employmentExtra();
			
			addRelBody(2, DIR.E, new GStat() {
				
				@Override
				public void update(GText text) {
					if (group != null) {
						double wf = p.target.group(group);
						if (wf == 0)
							return;
						GFORMAT.perc(text, wf/STATS.WORK().workforce(group));
					}else {
						int n =  p.neededWorkers();
						if (n == 0)
							return;
						GFORMAT.iofkInv(text, p.target.get(), n);
					}
				}
			});
			
			
			addRelBody(80, DIR.E, new SPRITE.Imp(Icon.S*4, Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (group == null)
						return;
					
					double d = group.r.pref().getWork(p);
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					ColorImp.TMP.bind();
					int am = (int) Math.ceil(d*4);
					for (int i = 0; i < am; i++) {
						SPRITES.icons().s.arrowUp.render(r, X1+i*Icon.S/2, Y1);
					}
					COLOR.unbind();
				}
			});

			addRelBody(2, DIR.E, new SPRITE.Imp(Icon.S*4, Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (group == null)
						return;
					
					double d = RACES.boosts().getNorSkill(group.r, p);
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					ColorImp.TMP.bind();
					int am = (int) Math.ceil(d*4);
					for (int i = 0; i < am; i++) {
						SPRITES.icons().s.hammer.render(r, X1+i*Icon.S/2, Y1);
						
					}
					COLOR.unbind();
				}
			});
			
			
			
			INTE in = new INTE() {
				
				@Override
				public int min() {
					return p.priority.min();
				}
				
				@Override
				public int max() {
					return p.priority.max();
				}
				
				@Override
				public int get() {
					if (group != null)
						return p.priorities.get(group);
					return p.priority.get();
				}
				
				@Override
				public void set(int t) {
					if (group != null)
						p.priorities.set(group, t);
					else
						p.priority.set(t);
				}
			};
			
			GSliderInt t = new GSliderInt(in, 200, true);
		
			addRightC(8, t);
			body().incrW(6);

		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			
			if (p.priority.get() == 0 || (group != null && p.priorities.get(group) == 0)) {
				OPACITY.O35.bind();
				COLOR.RED100.render(r, body(), -2);
				OPACITY.unbind();
			}else if (p.neededWorkers() == 0){
				OPACITY.O25.bind();
				COLOR.BLACK.render(r, body(), -2);
				OPACITY.unbind();
			}
			
		}
		

		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(p.blueprint().info.names);
			
			b.textLL(¤¤MasterPrio);
			b.tab(4);
			b.add(GFORMAT.i(b.text(), p.priority.get()));
			b.NL();
			
			b.textLL(DicMisc.¤¤Employees);
			b.tab(4);
			b.add(GFORMAT.iofkInv(b.text(), p.target.get(), p.neededWorkers()));
			b.NL(4);
			

			b.tab(1).textL(HCLASS.CITIZEN.names);
			b.tab(4).textL(HCLASS.SLAVE.names);
			b.tab(7).textL(STATS.WORK().WORK_FULFILLMENT.info().name);
			b.tab(10).textL(DicMisc.¤¤Skill);
			b.NL();
			
			for (Race r :  RACES.all()) {
				
				if (group != null && r == group.r) {
					b.add(underline);
					b.rewind();
					b.add(r.appearance().icon);
				}else
					b.add(r.appearance().icon);
				b.tab(1);
				double wf = STATS.WORK().workforce(EGROUP.get(HTYPE.SUBJECT, r));
				b.add(GFORMAT.i(b.text(), p.priorities.get(EGROUP.get(HTYPE.SUBJECT, r))));
				b.tab(2);
				b.add(GFORMAT.perc(b.text(), wf == 0 ? 0 : p.target.group(EGROUP.get(HTYPE.SUBJECT, r))/wf));
				b.tab(4);
				wf = STATS.WORK().workforce(EGROUP.get(HTYPE.SLAVE, r));
				b.add(GFORMAT.i(b.text(), p.priorities.get(EGROUP.get(HTYPE.SLAVE, r))));
				b.tab(5);
				b.add(GFORMAT.perc(b.text(), wf == 0 ? 0 : p.target.group(EGROUP.get(HTYPE.SLAVE, r))/wf));
				b.tab(7);
				b.add(GFORMAT.perc(b.text(), r.pref().getWork(p)));
				b.tab(10);
				b.add(GFORMAT.perc(b.text(), RACES.boosts().getNorSkill(r, p)));
				b.NL(2);
				
			}
		}

	}

}
