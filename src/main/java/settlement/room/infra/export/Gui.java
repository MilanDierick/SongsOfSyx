


package settlement.room.infra.export;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickWrap;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.DicRes;
import util.gui.common.UIPickerRes;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import view.ui.goods.UIGoodsExport;

class Gui extends UIRoomModuleImp<ExportInstance, ROOM_EXPORT> {


	private static CharSequence ¤¤NoResource = "¤No resource has been selected for export.";
	private static CharSequence ¤¤NoSell = "¤There are no resources to sell!";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_EXPORT s) {
		super(s);
		
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<ExportInstance> g, int x1, int y1) {
		
		
		
		RENDEROBJ r = null;
		

		
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().amount, g.get().crates*ExportInstance.crateMax);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.textLL(DicRes.¤¤Inbound);
				b.add(GFORMAT.i(b.text(), g.get().spaceReserved));
			};
		}.hh(DicRes.¤¤Stored);
		grid.add(r);
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().amountReserved);
				
			}
		}.hh(DicRes.¤¤Sold);
		grid.add(r);
		
		final UIPickerRes pop = new UIPickerRes(true) {
			
			@Override
			protected void select(RESOURCE r, int li) {
				g.get().resourceSet(r);
				VIEW.inters().popup.close();
			}
			
			@Override
			protected RESOURCE getResource() {
				return g.get().resource();
			}
			
			@Override
			protected void hoverResource(RESOURCE res, GBox b) {
				b.title(res.name);
				
				CharSequence p = blueprint.tally.problem(res, false);
				if (p != null) {
					b.error(p);
				}else {
					p = blueprint.tally.warning(res);
					if (p != null)
						b.add(b.text().warnify().add(p));
				}
				b.NL();
				
			
				
				b.textL(DicRes.¤¤Buyers);
				b.tab(6);
				b.textL(DicRes.¤¤buyPrice);
				b.tab(9);
				b.textL(DicRes.¤¤Stored);
				b.NL();
				for (FactionNPC h : FACTIONS.pRel().traders()) {
					if (h.buyer().priceBuyP(res) <= 0)
						continue;
					b.add(b.text().lablify().add(h.name));
					b.tab(6);
					b.add(GFORMAT.f(b.text(), h.buyer().priceBuyP(res)));
					b.tab(9);
					b.add(GFORMAT.i(b.text(), h.stockpile.amount(res)));
					b.NL();
				}
				
			}
		};
		
		SPRITE la = new SPRITE.Imp(Icon.M) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (g.get().resource() != null)
					g.get().resource().icon().render(r, X1, X2, Y1, Y2);
				else
					SPRITES.icons().m.cancel.render(r, X1, X2, Y1, Y2);
			}
		};
		
		section.addRelBody(2, DIR.S, new GButt.ButtPanel(la) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(pop, this, true);
			}
			
		});
		
		{
			UIGoodsExport ex = new UIGoodsExport();
			ClickWrap s = new ClickWrap(ex) {
				
				@Override
				protected RENDEROBJ pget() {
					if (g.get().resource() == null)
						return null;
					ex.res.set(g.get().resource());
					return ex;
				}
			};
			
			section.add(s, section.body().x1()+8, section.getLastY2()+8);
			
		}
		
	}
	
	@Override
	protected void appendTableButt(GuiSection s, GETTER<RoomInstance> ins) {

		s.add(new SPRITE.Imp(Icon.S) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				RESOURCE ro = ((ExportInstance) ins.get()).resource();
				SPRITE s = ro == null ? SPRITES.icons().s.cancel : ro.icon().small;
				s.render(r, X1, Y1);
			}
		}, 0, s.body().y2());

		s.addRightC(8, new SPRITE.Imp(s.body().width() - 8 - s.getLastX2(), 12) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				ExportInstance in = (ExportInstance) ins.get();

				double t = in.crates*ExportInstance.crateMax;
				double n = in.amount;
				double i = in.amountReserved;
				GMeter.renderDelta(r, (n-i) / t, (n) / 2, X1, X2, Y1, Y2);
			}
		});

	}
	
	@Override
	protected void problem(ExportInstance i, GBox box) {
		if (i.resource() == null) {
			box.error(¤¤NoResource);
		}
		else if (!blueprint.tally.shouldWork(i.resource()))
			box.error(¤¤NoSell);
		box.NL();
	}
	
	@Override
	protected void hover(GBox box, ExportInstance i) {
		if (i.resource() != null) {
			box.setResource(i.resource(), i.amount, i.crates*ExportInstance.crateMax);
		}
	}

	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				double am = 0;
				double cap = 0;
				
				for (RESOURCE r : RESOURCES.ALL()) {
					am += blueprint.tally.amount.get(r);
					cap += blueprint.tally.capacity.get(r);
				}
				GFORMAT.percInv(text, am/cap);
				
			}
		}.hh(DicRes.¤¤Capacity);
		text.add(r);
		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (RESOURCE r : RESOURCES.ALL()) {
					am += blueprint.tally.amount.get(r);
				}
				GFORMAT.i(text, am);
				
			}
		}.hh(DicRes.¤¤Stored);
		text.add(r);
		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (RESOURCE r : RESOURCES.ALL()) {
					am += blueprint.tally.promised.get(r);
				}
				GFORMAT.i(text, am);
				
			}
		}.hh(DicRes.¤¤Outbound);
		
		text.add(r);
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		final CharSequence none = "--";
		GTSort<RoomInstance> s = new GTSort<RoomInstance>(DicRes.¤¤Resource) {

			@Override
			public int cmp(RoomInstance current, RoomInstance cmp) {
				return Dictionary.compare(name(current), name(cmp));
			}

			@Override
			public void format(RoomInstance h, GText text) {
				text.add(name(h));
			}
			
			private CharSequence name(RoomInstance ins) {
				if (ins != null && ins instanceof ExportInstance) {
					ExportInstance i = (ExportInstance) ins;
					if (i.resource() == null)
						return none;
					return i.resource().name;
				}
				return none;
			}
			
		};
		sorts.add(s);
	}
}
