package view.sett.ui.room;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIRoomTable extends ISidePanel {

	private static final TableSorter tableSort = new TableSorter();
	private final RoomBlueprintIns<?> blueprint;
	private GTFilter<RoomInstance> filterCurrent;
	private GTSort<RoomInstance> sortCurrent;
	private RoomInstance hovered;
	private boolean wasHovering = false;
	private Coo oldC = new Coo();

	private static CharSequence ¤¤ReallyDelete = "¤Delete room?";
	private static CharSequence ¤¤DeleteRoom = "Delete Room";
	private static CharSequence ¤¤NrOfRooms = "Number of Rooms";
	private static CharSequence ¤¤Bulk = "¤Bulk";
	private static CharSequence ¤¤Showing = "¤Showing";

	static {
		D.ts(UIRoomTable.class);
	}

	ISidePanel get() {
		wasHovering = false;
		tableSort.set(blueprint, filterCurrent, sortCurrent);
		return this;
	}

	UIRoomTable(RoomBlueprintIns<?> b, UIRoom gui, UIRoomModule... appliers) {
		this.blueprint = b;

		titleSet(blueprint.info.names);

		section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				tableSort.sort();
				super.render(r, ds);
				if (hovered != null) {
					SETT.OVERLAY().add(hovered.mX(), hovered.mY());
					VIEW.s().getWindow().centerAtTile(hovered.body().cX(), hovered.body().cY());
					wasHovering = true;
					hovered = null;
				} else {
					if (wasHovering)
						VIEW.s().getWindow().centerAt(oldC);
					wasHovering = false;
				}
			}
		};
		
		int width = 200;
		RENDEROBJ o = makeRow(gui, new GETTER_IMP<Integer>(), appliers);
		if (o.body().width() > width)
			width = o.body().width();
		
		section.body().setWidth(width).setHeight(1);

		GuiSection sExtra = new GuiSection();
		
		int y1 = 0;

		
		
		{
			GuiSection s = new GuiSection();

			GGrid grid = new GGrid(s, section.body().width(), 1, 0, 0).setAlignment(DIR.W);
			GGrid text = new GGrid(new GuiSection(), 1, 1, 0, 0).setAlignment(DIR.C);
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, blueprint.instancesSize());
				}
			}.hh(SPRITES.icons().s.house).hoverInfoSet(¤¤NrOfRooms));

			
			
			for (UIRoomModule m : appliers) {
				m.appendManageScr(grid, text, sExtra);
			}

			int k = 0;
			for(RENDEROBJ r : s.elements()) {
				r.body().moveX1Y1(8 + (k%2)*section.body().width()/2, y1 + (k/2)*24);
				section.add(r);
				k++;
			}
			
			for(RENDEROBJ r : text.section.elements())
				section.addRelBody(4, DIR.S, r);

			y1 = section.body().y2();

		}

		{

			final ArrayListResize<GTFilter<RoomInstance>> filters = new ArrayListResize<>(10, 20);
			filters.add(new GTFilter<RoomInstance>(DicMisc.¤¤None) {

				@Override
				public boolean passes(RoomInstance h) {
					return true;
				}

			});

			final ArrayListResize<GTSort<RoomInstance>> sorts = new ArrayListResize<>(10, 20);
			sorts.add(new GTSort<RoomInstance>(DicMisc.¤¤name) {

				@Override
				public int cmp(RoomInstance current, RoomInstance cmp) {
					return Dictionary.compare(current.name(), cmp.name());
				}

				@Override
				public void format(RoomInstance h, GText text) {
					text.add(h.name());
					text.normalify();
				}

			});

			final ArrayListResize<UIRoomBulkApplier> apps = new ArrayListResize<>(10, 20);

			for (UIRoomModule m : appliers) {
				m.appendTableFilters(filters, sorts, apps);
			}

			GuiSection filter = new GuiSection();
			filter.add(UI.PANEL().panelM.get(DIR.E, DIR.S), 0, 0);
			for (int i = 0; i <= 7; i++)
				filter.addRightC(0, UI.PANEL().panelM.get(DIR.E, DIR.W, DIR.S));
			filter.addRightC(0, UI.PANEL().panelM.get(DIR.W, DIR.S));
			
			filter.add(UI.PANEL().panelM.get(DIR.E, DIR.N, DIR.S), 0, filter.getLastY2());
			for (int i = 0; i <= 7; i++)
				filter.addRightC(0, UI.PANEL().panelM.get(DIR.S, DIR.E, DIR.W, DIR.N));
			filter.addRightC(0, UI.PANEL().panelM.get(DIR.W, DIR.N, DIR.S));
			
			filter.add(UI.PANEL().panelM.get(DIR.E, DIR.N), 0, filter.getLastY2());
			for (int i = 0; i <= 7; i++)
				filter.addRightC(0, UI.PANEL().panelM.get(DIR.E, DIR.W, DIR.N));
			filter.addRightC(0, UI.PANEL().panelM.get(DIR.W, DIR.N));

			GDropDown<CLICKABLE> d = new GDropDown<CLICKABLE>(DicMisc.¤¤Sort);
			for (GTSort<RoomInstance> s : sorts) {
				SPRITE sp = (SPRITE)new Text(UI.FONT().S, s.name).setMaxWidth(120).setMultipleLines(false);
				CLICKABLE c = new GButt.Glow(sp) {

					{
						if (sortCurrent == null) {
							sortCurrent = s;
						}
					}

					@Override
					protected void clickA() {
						sortCurrent = s;
						tableSort.setSort(s);
					};
				}.hoverInfoSet(s.name);
				d.add(c);
			}
			d.init();
			d.body.moveY1(filter.body().y1() + 4);
			d.body.moveX1(filter.body().x1() + 26);
			filter.add(d);

			d = new GDropDown<CLICKABLE>(DicMisc.¤¤Filter);
			for (GTFilter<RoomInstance> f : filters) {
				SPRITE sp = (SPRITE)new Text(UI.FONT().S, f.name).setMaxWidth(120).setMultipleLines(false);
				CLICKABLE c = new GButt.Glow(sp) {
					{
						if (filterCurrent == null)
							filterCurrent = f;
					}

					@Override
					protected void clickA() {
						tableSort.setFilter(f);
						filterCurrent = f;

					};
				}.hoverInfoSet(f.name);
				d.add(c);
			}
			d.init();
			filter.addDown(2, d);

			if (apps.size() > 0) {
				final GDropDown<CLICKABLE> bulk = new GDropDown<CLICKABLE>(¤¤Bulk);
				for (UIRoomBulkApplier a : apps) {
					SPRITE sp = (SPRITE)new Text(UI.FONT().S, a.name).setMaxWidth(120).setMultipleLines(false);
					CLICKABLE c = new GButt.Glow(sp) {

						@Override
						protected void clickA() {
							for (int i = 0; i < tableSort.size(); i++) {
								RoomInstance t = tableSort.get(i);
								if (t != null) {
									a.apply(t);
								}

							}
							bulk.setSelected(null);

						};
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							a.hover((GBox) text);
						}
					}.hoverInfoSet(a.name);
					bulk.add(c);
				}
				bulk.setSelected(null);
				d = bulk;
				d.init();
				d.body.moveX1(24);
				d.body.moveY2(filter.body().y2() - 4);

				filter.addDown(2, d);
			}

			RENDEROBJ r = new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, tableSort.size(), blueprint.all().size());
					text.normalify();
				}
			}.hh(¤¤Showing);
			r.body().moveX1(100);
			r.body().moveY1(filter.body().y2() + 5);
			filter.addRelBody(6, DIR.S, r);

			filter.body().centerX(section);
			filter.body().moveY1(y1 + C.SG * 4);
			section.add(filter);
			y1 = filter.body().y2();
		}

		{
			GuiSection s = table(y1, gui, appliers);
			s.body().moveX1Y1(0, y1 + 10);
			section.addRelBody(10, DIR.S, s);
		}
		
		if (sExtra.body().width() > 0)
		section.addRelBody(8, DIR.E, sExtra);

	}

	private GuiSection table(int y1, UIRoom gui, UIRoomModule... appliers) {
		GTableBuilder builder = new GTableBuilder() {

			@Override
			public int nrOFEntries() {
				return tableSort.size();
			}

			@Override
			public void hover(int index) {

			}

			@Override
			public void click(int index) {

			}

			@Override
			public boolean selectedIs(int index) {
				RoomInstance t = tableSort.get(index);
				return gui.detailIns() == t && VIEW.s().panels.added(gui.detail(t));
			}
		};

		// for (UIRoomModule m : appliers)
		// m.appendTableRow(builder);

		RENDEROBJ o = makeRow(gui, new GETTER_IMP<Integer>(), appliers);
		
		builder.column(null, o.body().width(), new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return makeRow(gui, ier, appliers);
			}
		}, DIR.NW);

		return builder.createHeight(HEIGHT - y1 - C.SG * 16, false);
	}

	private GuiSection makeRow(UIRoom gui, GETTER<Integer> ier, UIRoomModule... appliers) {
		
		GuiSection s = new GButt.BSection(100, 0) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (!isHoveringAHoverElement()) {
					RoomInstance t = tableSort.get(ier.get());
					gui.hover(VIEW.hoverBox(), t, t.mX(), t.mY());
				} else
					super.hoverInfoGet(text);
			}
			
			@Override
			public boolean click() {
				if (super.click())
					return true;
				RoomInstance t = tableSort.get(ier.get());
				ISidePanel d = gui.detail(t);
				VIEW.s().panels.add(d, false);
				VIEW.s().getWindow().centererTile.set(t.body().cX(), t.body().cY());
				oldC.set(VIEW.s().getWindow().pixels().cX(), VIEW.s().getWindow().pixels().cY());
				return true;
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				selectOnlythis(VIEW.s().panels.added(VIEW.s().ui.rooms.rooms[blueprint.index()].detail) && VIEW.s().ui.rooms.rooms[blueprint.index()].detailIns() == tableSort.get(ier.get()));
				super.render(r, ds);
			}

		};



		final int mW = UI.FONT().S.height()*12;
		
		s.add(new GStat() {
			@Override
			public void update(GText text) {
				if (tableSort.currentSort() != null)
					tableSort.currentSort().format(get(ier), text);
				text.setMaxWidth(s.body().width()-24);
				text.setMultipleLines(false);
				text.lablifySub();
			}
		}.decrease(), 0, 0);
		s.body().incrW(mW);

		
		
		GuiSection pButts = new GuiSection();
		
		if (blueprint.employment() != null) {
			pButts.add(new GButt.Checkbox() {
				@Override
				protected void renAction() {
					selectedSet(get(ier).active());
				}
	
				@Override
				protected void clickA() {
					if (get(ier).active()) {
						get(ier).deactivate();
					} else {
						get(ier).activate();
					}
				}
			});
		}
		
		pButts.addRightC(8, new GButt.Glow(SPRITES.icons().s.crossheir) {

			@Override
			public boolean hover(COORDINATE mCoo) {
				if (super.hover(mCoo)) {
					int index = ier.get();
					if (index < 0)
						return true;
					RoomInstance t = tableSort.get(index);
					if (!wasHovering) {
						oldC.set(VIEW.s().getWindow().pixels().cX(), VIEW.s().getWindow().pixels().cY());
						wasHovering = true;
					}

					hovered = t;
					return true;
				}
				return false;
			}
		});

		pButts.addRightC(4, new GButt.Glow(SPRITES.icons().s.cancel) {
			ACTION a = new ACTION() {

				@Override
				public void exe() {
					RoomInstance t = get(ier);
					TmpArea a = get(ier).remove(t.mX(), t.mY(), true, this, false);
					if (a != null)
						a.clear();
					tableSort.sortForced();
				}
			};

			@Override
			protected void clickA() {
				dCount--;
				if (dCount < 0) {
					VIEW.inters().yesNo.activate(¤¤ReallyDelete, a, ACTION.NOP, true);
					dCount = 10;
				} else {
					a.exe();
				}

			}
		}.hoverInfoSet(¤¤DeleteRoom));

		GuiSection butts = new GuiSection();
		
		GETTER<RoomInstance> getter = new GETTER<RoomInstance>() {

			@Override
			public RoomInstance get() {
				return UIRoomTable.this.get(ier);
			}
			
		};
		
		for (UIRoomModule m : appliers)
			m.appendButt(butts, getter);
		
		butts.body().moveX1(0);
		pButts.body().moveY1(butts.body().y1());
		if (butts.body().width() + pButts.body().width() > s.body().width()) {
			pButts.body().moveX1(butts.body().x2()+16);
		}else {
			pButts.body().moveX2(s.body().width());
		}
		for (RENDEROBJ o : pButts.elements())
			butts.add(o);
		
		butts.body().centerY(s);
		butts.body().moveY1(s.body().y2()+2);
		
		for (RENDEROBJ o : butts.elements())
			s.add(o);
		
		s.pad(8, 8);

		return s;

	}

	private int dCount = 0;

	protected RoomInstance get(GETTER<Integer> ier) {
		return tableSort.get(ier.get());
	}

}

class TableSorter extends GTableSorter<RoomInstance> {

	private RoomBlueprintIns<?> b;

	public TableSorter() {
		super(ROOMS.ROOM_MAX);
	}

	@Override
	protected RoomInstance getUnsorted(int index) {
		if (b == null)
			return null;
		if (index < b.all().size()) {
			return b.getInstance(index);
		}
		return null;
	}

	public void set(RoomBlueprintIns<?> b, GTFilter<RoomInstance> filterCurrent, GTSort<RoomInstance> sortCurrent) {
		if (this.b != b || filterCurrent != filter || sort != sortCurrent) {
			this.b = b;
			this.sort = sortCurrent;
			this.filter = filterCurrent;
			sortForced();
		}
	}

}
