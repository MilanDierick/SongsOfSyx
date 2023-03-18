package view.sett.ui.room;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.room.main.*;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables;
import settlement.stats.StatsEquippables.StatEquippableWork;
import settlement.stats.StatsMultipliers.StatMultiplierWork;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.gui.slider.GTarget;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleEmployment implements ModuleMaker {

	private final CharSequence ¤¤EMPLOYEES_DESC = "¤Actual employees / Target Employees. When you set the target, unemployed people will soon sign up and start working the room. Without sufficient workers, or if subjects are prevented from working due to priorities, a room will perform poorly. The worker amount isn't fixed. It can vary greatly depending on how the room is laid out, and how well your city is planned. The tool-tip when creating a room will tell you how a guess of many workers will be required in an average city. But there is no way for the game to actually figure out the exact number. It may also vary from time to time.";
	private final CharSequence ¤¤WORKLOAD_LOW = "¤Workload is low";
	private final CharSequence ¤¤SHIFT_START = "¤What time the work shift starts";
	private final CharSequence ¤¤SHIFT_NIGHT = "¤This room is employed all hours of the day.";
	private final CharSequence ¤¤WORKERS_INC = "¤Workers +{0}";
	private final CharSequence ¤¤WORKERS_DEC = "¤Workers -{0}";
	private final CharSequence ¤¤WORKERS_NONE = "¤Insufficient workers available. If there are idle subjects, these will soon sign up and start working";
	private final CharSequence ¤¤WORKERS_INSUFFICIENT = "¤Insufficient workers allocated.";
	private final CharSequence ¤¤MISSING_RESOURCE = "¤Missing:";
	private final CharSequence ¤¤WORKERS_INSPECT = "¤Inspect";
	private final CharSequence ¤¤AUTO = "¤Auto Employ";
	private final CharSequence ¤¤AUTO_DESC = "¤Let the AI adjust worker amount each day based on workload.";
	
	ModuleEmployment(Init init){
		D.t(this);
	}
	

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> pi = (RoomBlueprintIns<?>) p;
			if (pi.employment() != null) {
				l.add(new I(pi));

			}
		}
		
	}
	
	private class I extends UIRoomModule {
		
		private final RoomBlueprintIns<?> blueprint;
		
		I(RoomBlueprintIns<?> blue){
			this.blueprint = blue;
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, 
							blueprint.employment().employed(), 
							blueprint.employment().neededWorkers());
				}
			}.hh(SPRITES.icons().s.human).hoverInfoSet(¤¤EMPLOYEES_DESC));
			
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, blueprint.employment().efficiency());
					
				}
			}.hh(SPRITES.icons().s.cog).hoverInfoSet(DicMisc.¤¤WorkloadD));
			

			if (blueprint.employment().worksNights()) {
				grid.add(new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.f(text, Double.NaN);
					}
				}.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤SHIFT_NIGHT));
			}else {
				grid.add(new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.i(text, (int)(blueprint.employment().getShiftStart()*TIME.hoursPerDay));
					}
				}.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤SHIFT_START));
			}
			
			if (blueprint.employmentExtra() != null){
				
				
				GTarget t = new GTarget(20, false, true, blueprint.employmentExtra().priority);
				RENDEROBJ r = new CLICKABLE.Pair(new GHeader(SPRITES.icons().s.alert), t, DIR.E, 2).hoverInfoSet(DicMisc.¤¤Priority);
				grid.add(r);
			}
			
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, blueprint.employment().accidentsPerYear*100 / (1+BOOSTABLES.CIVICS().ACCIDENT.get(null, null)));
					
				}
			}.hh(SPRITES.icons().s.death).hoverInfoSet(DicMisc.¤¤AccidentRate));
			
			for (StatEquippableWork w : STATS.EQUIP().work()) {
				if (w.max(blueprint.employment()) > 0) {
					
					INTE i = new INTE() {
						
						@Override
						public int min() {
							return 0;
						}
						
						@Override
						public int max() {
							return w.max(blueprint.employment());
						}
						
						@Override
						public int get() {
							return w.target(blueprint.employment());
						}
						
						@Override
						public void set(int t) {
							w.targetSet(blueprint.employment(), t);
						}
					};
					
					GTarget t = new GTarget(20, false, true, i);
					
					
					RENDEROBJ r = new CLICKABLE.Pair(new GHeader(w.resource.icon()), t, DIR.E, 2).hoverInfoSet(w.stat().info().desc);
					grid.add(r);
					
					
				}
			}
			
			
			
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {

			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+25)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()+25);
				}
			});
			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+5)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()+5);
				}
			});
			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+1)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()+1);
				}
			});
			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+1)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()-1);
				}
			});
			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+5)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()-5);
				}
			});
			appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+25)) {
				
				@Override
				protected void apply(RoomInstance t) {
					t.employees().neededSet(t.employees().needed()-25);
				}
			});
			
			if (STATS.MULTIPLIERS().OVERTIME.canMark(blueprint)) {
				appliers.add(new UIRoomBulkApplier(STATS.MULTIPLIERS().OVERTIME.name) {
					
					@Override
					protected void apply(RoomInstance t) {
						for (Humanoid a : t.employees().employees()) {
							if (STATS.MULTIPLIERS().OVERTIME.canBeMarked(a.indu()))
								STATS.MULTIPLIERS().OVERTIME.mark(a, true);
						}
					
					}
				});
			}
			
			if (blueprint.employmentExtra() != null)
				sorts.add(new GTSort<RoomInstance>(DicMisc.¤¤Workload) {
	
					@Override
					public int cmp(RoomInstance current, RoomInstance cmp) {
						double e1 = current.employees().efficiency();
						double e2 = cmp.employees().efficiency();
						if (e1 == e2)
							return 0;
						if (e1 < e2)
							return -1;
						return 1;
					}
	
					@Override
					public void format(RoomInstance h, GText text) {
						GFORMAT.perc(text, h.employees().efficiency());
					}
					
				});
			
		}

		@Override
		public void appendButt(GuiSection s, GETTER<RoomInstance> get) {

			INTE in = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return get.get().employees().max();
				}
				
				@Override
				public int get() {
					return get.get().employees().needed();
				}
				
				@Override
				public void set(int t) {
					get.get().employees().neededSet(t);
				}
			};
			
			GStat st = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, get.get().employees().employed(), get.get().employees().needed());
				}
			};
			
			GTarget ss = new GTarget(52, false, true, st, in) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(DicMisc.¤¤Employees);
				}
			};
			
			s.addRelBody(8, DIR.E, ss);
			
			s.addRightC(16, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, get.get().employees().efficiency());
				}
			}.decrease().hh(SPRITES.icons().s.cog));
			
			s.body().incrW(48);
			
			if (blueprint instanceof ROOM_EMPLOY_AUTO) {
				CLICKABLE c = new GButt.Checkbox() {
					
					@Override
					protected void clickA() {
						boolean b = ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get());
						if (!b && ((RoomInstance)get.get()).employees().needed() == 0)
							((RoomInstance)get.get()).employees().neededSet(1);
						
						((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get(), !b);
					}
					@Override
					protected void renAction() {
						selectedSet(((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get()));
					}
					
				}.hoverTitleSet(¤¤AUTO).hoverInfoSet(¤¤AUTO_DESC);
				c.body().moveCY(s.getLast().cY());
				c.body().moveX2(s.body().width());
				s.addRightC(36, c);
				
			}
			
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			RoomInstance i = (RoomInstance) room;
			box.text(i.blueprint().employment().title);
			box.add(GFORMAT.iofkInv(box.text(), i.employees().employed(), i.employees().needed()));
			box.space();
			box.text();
			if (i.blueprintI().employmentExtra() != null)
				box.text(DicMisc.¤¤Workload).add(GFORMAT.perc(box.text(), i.employees().efficiency()));
			box.NL();
			highlightWorkers(room);
		}

		@Override
		public void problem(GBox box, Room room, int rx, int ry) {
			RoomInstance i = (RoomInstance) room;
			
			if (i.blueprintI().employmentExtra() != null) {
				if(i.employees().needed() == 0) {
					box.add(box.text().errorify().add(¤¤WORKERS_NONE));
					box.NL();
				}
				if(i.employees().employed() < i.employees().needed()) {
					box.add(box.text().errorify().add(¤¤WORKERS_INSUFFICIENT));
					box.NL();
				}
				if ((1.0-i.employees().efficiency())*(i.employees().employed()-1) > 1){
					box.error(¤¤WORKLOAD_LOW);
					box.NL();
				}
				
			}
			
			
			
			if (room instanceof JOBMANAGER_HASER) {
				JOBMANAGER_HASER h = (JOBMANAGER_HASER) room;
				boolean m = false;
				for (RESOURCE r : RESOURCES.ALL()) {
					if (!h.getWork().resourceReachable(r)) {
						m = true;
					}
				}
				if (m) {
					box.error(¤¤MISSING_RESOURCE);
					int t = 0;
					for (RESOURCE r : RESOURCES.ALL()) {
						if (!h.getWork().resourceReachable(r)) {
							t++;
							box.add(r.icon());
							if (t == 9) {
								t = 0;
								box.NL();
							}
								
						}
					}
				}
			}
		}
		
		private void highlightWorkers(Room room) {
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					if (STATS.WORK().EMPLOYED.get(a) == room) {
						SETT.OVERLAY().add(e);
					}
				}
			}
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			GuiSection s = new GuiSection() {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					highlightWorkers(get.get());
					super.render(r, ds);
				}
			};
			RENDEROBJ r;
			r = new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, g(get).employees().employed(), g(get).employees().needed());
				}
			}.hh(SPRITES.icons().s.human).hoverTitleSet(blueprint.employment().title).hoverInfoSet(¤¤EMPLOYEES_DESC);

			
			s.add(r);
			
			if (blueprint.employmentExtra() != null) {
				r = new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.perc(text, g(get).employees().efficiency());
						
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(DicMisc.¤¤Workload);
						b.text(DicMisc.¤¤WorkloadD);
						b.NL();
						b.textL(DicTime.¤¤Today);
						b.add(GFORMAT.perc(b.text(), g(get).employees().efficiencySoFar()));
					};
					
				}.hh(SPRITES.icons().s.cog);
				s.addRightC(60, r);
			}
			
			for (StatsEquippables.StatEquippableWork e : STATS.EQUIP().work()) {
				if (e.max(blueprint.employment()) > 0) {
					r = new GStat() {

						@Override
						public void update(GText text) {
							double am = 0;
							
							for (Humanoid h : g(get).employees().employees()) {
								am += e.stat().indu().get(h.indu());
							}
							if (am > 0)
								am /= g(get).employees().employees().size();
							GFORMAT.f0(text, am);
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							b.title(e.targetInfo.name);
							b.NL(8);
							
							double d = 0;
							int t = e.target(blueprint.employment());
							double am = 0;
							
							for (Humanoid h : g(get).employees().employees()) {
								am += e.stat().indu().get(h.indu());
							}
							
							
							b.textLL(DicMisc.¤¤Equipped);
							b.tab(4);
							b.add(GFORMAT.i(b.text(), (int) am));
							b.NL();
							b.textLL(DicMisc.¤¤Target);
							b.tab(4);
							b.add(GFORMAT.i(b.text(), (int) t*g(get).employees().employees().size()));
							b.NL(8);
							
							if (am > 0)
								d = am / (g(get).employees().employees().size()*t);
							b.textL(BOOSTABLES.INFO().name);
							b.tab(4);
							b.add(GFORMAT.percInc(b.text(), d*e.maxBoost(blueprint.employment())));
						};
						
					}.hh(e.resource().icon().small);
					s.addRightC(60, r);
					
					
					
					
					
				}
				
			}
			
			
			
			
			INTE t = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return g(get).employees().max();
				}
				
				@Override
				public int get() {
					return g(get).employees().needed();
				}
				
				@Override
				public void set(int t) {
					g(get).employees().neededSet(t);
				}
			};
			
			GSliderInt m = new GSliderInt(t, 200, true) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(blueprint.employment().title);
					super.hoverInfoGet(text);
					text.NL(4);
					text.text(¤¤EMPLOYEES_DESC);
				}
			};
			s.addRelBody(4, DIR.S, m);
			
			r = new GButt.ButtPanel(SPRITES.icons().s.menu) {
				@Override
				protected void clickA() {
					VIEW.s().ui.subjects.showProfession(g(get));
				}
				
				@Override
				protected void renAction() {
					activeSet(g(get).employees().employed() > 0);
				}
			}.repetativeSet(true).hoverInfoSet(¤¤WORKERS_INSPECT);
			s.addRightC(12, r);
			
			if (STATS.MULTIPLIERS().OVERTIME.canMark(blueprint)) {
				
				StatMultiplierWork mm = STATS.MULTIPLIERS().OVERTIME;
				
				CLICKABLE c = new GButt.ButtPanel(mm.icon) {
					
					boolean someactive;
					boolean someEmployed;
					
					@Override
					protected void renAction() {
						
						someactive = false;
						someEmployed = get.get().employees().employed() > 0;
						for (Humanoid h : get.get().employees().employees()) {
							if (!mm.canBeMarked(h.indu()))
								someactive = true;
						}
						
						activeSet(someEmployed);
						selectedSet(someactive);
					};
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						
						b.title(mm.name);
						b.text(mm.desc);
					}
					
					@Override
					protected void clickA() {
						for (Humanoid h : get.get().employees().employees()) {
							mm.mark(h, !someactive);
						}
					}
					
				}.setDim(40, 40);
				
				s.addRightC(12, c);
			}
			
			if ((blueprint instanceof ROOM_EMPLOY_AUTO)) {
				CLICKABLE c = new GButt.CheckboxTitle(¤¤AUTO) {
					@Override
					protected void clickA() {
						boolean b = ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get());
						if (!b && ((RoomInstance)get.get()).employees().needed() == 0)
							((RoomInstance)get.get()).employees().neededSet(1);
						
						((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get(), !b);
					}
					@Override
					protected void renAction() {
						selectedSet(((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get()));
					}
				}.hoverInfoSet(¤¤AUTO_DESC);
				s.addRelBody(4, DIR.S, c);
				
			}
			
			
			
			s.addRelBody(2, DIR.N, new GHeader(DicMisc.¤¤Employment));
			
			section.addRelBody(8, DIR.S, s);
			
			
			
		}

		private RoomInstance g(GETTER<RoomInstance> g) {
			return (RoomInstance) g.get();
		}


		
	}

	


	
}
