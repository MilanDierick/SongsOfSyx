package view.sett.ui.room;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.industry.module.*;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.statistics.HISTORY_INT;
import view.main.VIEW;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleIndustry implements ModuleMaker {

	private final GChart chart = new GChart();
	private final boolean[] rCheck = new boolean[RESOURCES.ALL().size()];

	private static CharSequence ¤¤Production = "¤Production";
	private static CharSequence ¤¤ProductionDesc = "¤The amount that is estimated to be produced each day. The actual production can vary greatly depending on a number of factors.";
	private static CharSequence ¤¤Consumption = "¤Consumption";
	private static CharSequence ¤¤ConsumptionD = "¤Estimation of how many resources are consumed each day.";
	private static CharSequence ¤¤Recipes = "¤Change Recipe";
	private static CharSequence ¤¤RecipesWarning = "¤Note that changing recipe will reset the room.";

	private static CharSequence ¤¤ProducedNow = "¤Produced This Year";
	private static CharSequence ¤¤ProducedEstimate = "¤Estimated this year";
	private static CharSequence ¤¤ProducedPrevious = "¤Produced last year";

	private static CharSequence ¤¤ConsumedNow = "¤Consumed This Year";
	private static CharSequence ¤¤ConsumedPrevious = "¤Consumed last year";

	private static CharSequence ¤¤NoStore = "¤Internal storage is full and production is stalled. Have a warehouse fetch the produce.";
	
	public ModuleIndustry(Init init) {
		D.t(this);

	}

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof INDUSTRY_HASER) {
			INDUSTRY_HASER blue = ((INDUSTRY_HASER) p);
			if (!blue.industryIgnoreUI())
				l.add(new I((RoomBlueprintIns<?>) p, blue.industries()));
		}

	}

	private final class I extends UIRoomModule {

		private final LIST<Industry> ins;
		private final INDUSTRY_HASER indu;

		I(RoomBlueprintIns<?> blue, LIST<Industry> ins) {
			this.ins = ins;
			indu = (INDUSTRY_HASER) blue;
			// this.blue = blue;
		}

		@Override
		public void appendManageScr(GGrid grid, GGrid r, GuiSection sExta) {

			int y1 = r.section.body().y2() + 32;

			LinkedList<RESOURCE> ress = new LinkedList<RESOURCE>();
			long m = 0;
			for (Industry i : ins) {
				for (IndustryResource res : i.outs()) {
					if ((res.resource.bit & m) == 0) {
						ress.add(res.resource);
						m |= res.resource.bit;
					}
				}

			}

			if (ress.size() > 0) {

				r.section.add(new GHeader(¤¤Production), r.section.body().x1(), y1);
				GuiSection ins = new GuiSection();

				int ri = 0;
				for (RESOURCE res : ress) {
					IndustryResource[] os = resourcesOut(res);
					if (os == null)
						continue;
					GStat s = new GStat() {
						@Override
						public void update(GText text) {
							int am = 0;
							for (IndustryResource r : os)
								am += r.history().get();
							GFORMAT.iIncr(text, am);
						}
					};
					HISTORY_INT hi = new HISTORY_INT() {

						@Override
						public TIMECYCLE time() {
							return os[0].history().time();
						}

						@Override
						public int historyRecords() {
							return os[0].history().historyRecords();
						}

						@Override
						public double getD(int fromZero) {
							return get(fromZero) / (double) max();
						}

						@Override
						public int min() {
							return os[0].history().min();
						}

						@Override
						public int max() {
							return os[0].history().max();
						}

						@Override
						public int get(int fromZero) {
							int am = 0;
							for (IndustryResource i : os)
								am += i.history().get(fromZero);
							return am;
						}
					};

					ins.add(new GHeader.HeaderHorizontal(res.icon(), s) {
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.title(res.name);
							text.add(text.text().add(¤¤Production));
							chart.clear();
							chart.add(hi);
							text.NL();
							text.add(chart.sprite);
						}
					}, (ri % 3) * 96, (ri / 3) * 26);
					ri++;
				}

				r.section.addDown(2, ins);
				y1 = r.section.body().y2() + 4;
			}

			ress = new LinkedList<RESOURCE>();
			m = 0;
			for (Industry i : ins) {
				for (IndustryResource res : i.ins()) {
					if ((res.resource.bit & m) == 0) {
						ress.add(res.resource);
						m |= res.resource.bit;
					}
				}

			}

			if (ress.size() > 0) {

				r.section.add(new GHeader(¤¤Consumption), r.section.body().x1(), y1);
				GuiSection ins = new GuiSection();

				int ri = 0;
				for (RESOURCE res : ress) {
					IndustryResource[] os = resourcesIn(res);
					if (os == null)
						continue;
					GStat s = new GStat() {
						@Override
						public void update(GText text) {
							int am = 0;
							for (IndustryResource r : os)
								am += r.history().get();
							GFORMAT.iIncr(text, -am);
						}
					};
					HISTORY_INT hi = new HISTORY_INT() {

						@Override
						public TIMECYCLE time() {
							return os[0].history().time();
						}

						@Override
						public int historyRecords() {
							return os[0].history().historyRecords();
						}

						@Override
						public double getD(int fromZero) {
							return get(fromZero) / (double) max();
						}

						@Override
						public int min() {
							return os[0].history().min();
						}

						@Override
						public int max() {
							return os[0].history().max();
						}

						@Override
						public int get(int fromZero) {
							int am = 0;
							for (IndustryResource i : os)
								am += i.history().get(fromZero);
							return am;
						}
					};

					ins.add(new GHeader.HeaderHorizontal(res.icon(), s) {
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.title(res.name);
							text.add(text.text().add(¤¤Consumption));
							chart.clear();
							chart.add(hi);
							text.NL();
							text.add(chart.sprite);
						}
					}, (ri % 3) * 90, (ri / 3) * 26);
					ri++;
				}

				r.section.addDown(2, ins);
				y1 = r.section.body().y2() + 4;
			}

		}

		private IndustryResource[] resourcesOut(RESOURCE res) {
			int am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.outs())
					if (r.resource == res) {
						am++;
					}
			}
			if (am == 0)
				return null;
			IndustryResource[] o = new IndustryResource[am];
			am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.outs())
					if (r.resource == res) {
						o[am++] = r;
					}
			}
			return o;
		}

		private IndustryResource[] resourcesIn(RESOURCE res) {
			int am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.ins())
					if (r.resource == res) {
						am++;
					}
			}
			if (am == 0)
				return null;
			IndustryResource[] o = new IndustryResource[am];
			am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.ins())
					if (r.resource == res) {
						o[am++] = r;
					}
			}
			return o;
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters,
				LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {

		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			ROOM_PRODUCER p = ((ROOM_PRODUCER) room);
			box.NL();
			int t = 0;
			for (IndustryResource i : p.industry().outs()) {

				box.tab(t * 3);
				box.add(i.resource.icon().small);
				GText te = box.text();
				indu.industryFormatProductionRate(te, i, (RoomInstance) room);
				box.add(te);
				t++;
				if (t == 3) {
					t = 0;
					box.NL();
				}
			}

			box.NL(8);

			for (IndustryResource i : p.industry().ins()) {
				box.add(i.resource.icon().small).add(GFORMAT.iIncr(box.text(), -(int) i.day.getD(p)));
				box.NL();
			}
		}

		@Override
		public void problem(GBox box, Room rr, int rx, int ry) {
			ROOM_PRODUCER p = ((ROOM_PRODUCER) rr);
			if (p.industry().outs().size() == 0)
				return;
			
			RoomInstance room = (RoomInstance) rr;

			Arrays.fill(rCheck, false);
			boolean any = false;
			for (COORDINATE c : room.body()) {
				if (room.is(c)) {
					RESOURCE_TILE t = room.resourceTile(c.x(), c.y());
					if (t != null && t.resource() != null && !t.hasRoom()) {
						rCheck[t.resource().index()] = true;
						any = true;
					}
				}
			}
			
			if (!any)
				return;
			
			box.NL(8);
			box.add(box.text().errorify().add(¤¤NoStore));
			box.NL();
			for (RESOURCE r : RESOURCES.ALL()) {
				if (rCheck[r.index()]) {
					box.add(r.icon());
				}	
			}
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {

			int resOut = 0;

			for (Industry i : ins)
				resOut = Math.max(resOut, i.outs().size());

			if (resOut > 0) {

				GuiSection all = new GuiSection();

				for (int rii = 0; rii < resOut; rii++) {
					RENDEROBJ s = resOut(rii, get, indu);
					s.body().moveX1Y1((rii % 3) * 90, (rii / 3) * (s.body().height()));
					all.add(s);

				}

				all.addRelBody(2, DIR.N, new GHeader(¤¤Production));

				section.addRelBody(32, DIR.S, all);

			}

			int resIn = 0;

			for (Industry i : ins)
				resIn = Math.max(resIn, i.ins().size());

			if (resIn > 0) {
				boolean ccc = resOut > 0;
				GuiSection all = new GuiSection();

				for (int rii = 0; rii < resIn; rii++) {
					RENDEROBJ s = resIn(rii, get, indu, ccc);
					s.body().moveX1Y1((rii % 3) * 90, (rii / 3) * (s.body().height()));
					all.add(s);
				}

				all.addRelBody(2, DIR.N, new GHeader(¤¤Consumption));

				section.addRelBody(4, DIR.S, all);
			}

			if (ins.size() <= 1)
				return;

			{
				LinkedList<RENDEROBJ> rows = new LinkedList<>();
				for (int ii = 0; ii < ins.size(); ii++) {

					final int k = ii;
					final Industry i = ins.get(k);
					GButt.BSection b = new GButt.BSection() {

						@Override
						protected void clickA() {
							if (FACTIONS.player().locks.unlockText(i) == null) {
								ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
								p.setIndustry(k);
								VIEW.inters().popup.close();
								if (g(get) instanceof JOBMANAGER_HASER) {
									JOBMANAGER_HASER j = (JOBMANAGER_HASER) g(get);
									j.getWork().resetResourceSearch();
								}
							}
							super.clickA();
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;

							if (activeIs()) {
								b.error(¤¤RecipesWarning);
								b.NL(8);
							}

							b.textL(¤¤Consumption);
							b.NL();
							for (IndustryResource r : i.ins())
								b.text(r.resource.name);
							b.NL(8);
							b.textL(¤¤Production);
							b.NL();
							for (IndustryResource r : i.outs())
								b.text(r.resource.name);
							b.NL(8);

							CharSequence e = FACTIONS.player().locks.unlockText(i);

							if (e != null) {

								GText t = b.text();
								t.add(e);
								t.errorify();
								b.add(t);
							}
						}

						@Override
						public void renAction() {
							activeSet(FACTIONS.player().locks.unlockText(i) == null);
						}

					};

					for (IndustryResource r : i.ins()) {
						b.addRightC(48, new GStat() {

							@Override
							public void update(GText text) {
								GFORMAT.f0(text, -r.rate);

							}
						}.hh(r.resource.icon()));
					}

					b.addRightC(48, SPRITES.icons().m.arrow_right);

					for (IndustryResource r : i.outs()) {
						b.addRightC(48, new GStat() {

							@Override
							public void update(GText text) {
								GFORMAT.f0(text, r.rate);

							}
						}.hh(r.resource.icon()));
					}

					b.body().incrW(48);
					b.pad(4);
					b.body().setWidth(450);

					rows.add(b);

				}

				CLICKABLE rr = new GScrollRows(rows, 400).view();
				CLICKABLE c = new GButt.ButtPanel(¤¤Recipes) {
					@Override
					protected void clickA() {
						VIEW.inters().popup.show(rr, this);
					};
				}.pad(6, 3);

				section.addRelBody(8, DIR.S, c);

			}

		}

		private ROOM_PRODUCER g(GETTER<RoomInstance> g) {
			return (ROOM_PRODUCER) g.get();
		}

	}

	private static ROOM_PRODUCER g(GETTER<RoomInstance> g) {
		return (ROOM_PRODUCER) g.get();
	}

	private static RENDEROBJ resIn(int ri, GETTER<RoomInstance> get, INDUSTRY_HASER indu, boolean outs) {
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				if (ri >= p.industry().ins().size())
					return;

				IndustryResource i = p.industry().ins().get(ri);

				GBox b = (GBox) text;
				b.title(i.resource.name);

				

				if (outs) {
					b.text(¤¤ConsumptionD);
					b.NL(8);
					IndustryUtil.hoverConsumptionRate(text, i.rate, p.industry(), (RoomInstance) get.get());
				}
				b.NL(8);
				b.textLL(¤¤ConsumedNow);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.year.get(p)));
				b.NL();

				b.textLL(¤¤ConsumedPrevious);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.yearPrev.get(p)));

				b.NL(8);
			}

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				visableSet(p.industry().ins().size() > ri);
				if (visableIs())
					super.render(r, ds);
			}
		};
		s.add(new RENDEROBJ.RenderImp(ICON.MEDIUM.SIZE) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				IndustryResource i = p.industry().ins().get(ri);
				i.resource.icon().render(r, body);
			}
		});

		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER) g(get)).industry().ins().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				double n = IndustryUtil.calcConsumptionRate(i.rate, (g(get)).industry(), (RoomInstance) get.get());
				n *= ins.employees().employed();
				text.add('-');
				GFORMAT.f(text, n * ins.employees().efficiency());
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}

	private static RENDEROBJ resOut(int ri, GETTER<RoomInstance> get, INDUSTRY_HASER indu) {

		GuiSection s = new GuiSection() {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				visableSet(p.industry().outs().size() > ri);
				if (visableIs())
					super.render(r, ds);
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {

				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				if (ri >= p.industry().outs().size())
					return;

				RoomInstance ins = (RoomInstance) get.get();

				IndustryResource i = p.industry().outs().get(ri);

				GBox b = (GBox) text;
				b.title(i.resource.name);

				b.text(¤¤ProductionDesc);
				b.NL(8);

				indu.industryHoverProductionRate(b, i, ins);

				b.NL(8);

				b.textLL(¤¤ProducedNow);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.year.get(p)));
				b.NL();

				b.textLL(¤¤ProducedPrevious);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.yearPrev.get(p)));
				b.NL();


				double e = indu.industryFormatProductionRate(b.text(), i, ins);

				double pa = TIME.years().bitPartOf();

				int eyear = (int) (i.year.get(p) + e * (1.0 - pa) * TIME.years().bitConversion(TIME.days()));
				b.textLL(¤¤ProducedEstimate);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), eyear));
				b.NL();

				b.NL(8);

			}

		};
		s.add(new RENDEROBJ.RenderImp(ICON.MEDIUM.SIZE) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER p = ((ROOM_PRODUCER) g(get));
				IndustryResource i = p.industry().outs().get(ri);
				i.resource.icon().render(r, body);
			}
		});

		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER) g(get)).industry().outs().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				indu.industryFormatProductionRate(text, i, ins);
			}
		}.r();

		s.addRightC(6, h);
		
		h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER) g(get)).industry().outs().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				indu.industryFormatProductionRateEmpl(text, i, ins);
			}
		}.r();

		s.add(h, s.getLast().x1(), s.getLastY2()+1);

		s.body().incrW(48);
		s.pad(4);
		return s;
	}

}
