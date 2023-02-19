
package settlement.room.infra.importt;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.trade.PlayerPrices.TradeHolder;
import game.faction.trade.TradeManager;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.DicGeo;
import util.dic.DicRes;
import util.gui.common.GResSelector;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import view.ui.UIGoodsTrade;
import world.World;
import world.entity.WEntity;
import world.entity.caravan.Shipment;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.regions.RegionTaxes.RegionResource;

class Gui extends UIRoomModuleImp<ImportInstance, ROOM_IMPORT> {

	private static CharSequence ¤¤TotalSpace = "¤Total Space";
	private static CharSequence ¤¤UsedSpace = "¤Used Space";
	private static CharSequence ¤¤Incoming = "¤Incoming Wares";
	private static CharSequence ¤¤Accepting = "¤Accepting";

	static {
		D.ts(Gui.class);
	}

	Gui(ROOM_IMPORT s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<ImportInstance> g, int x1, int y1) {

		RENDEROBJ r = null;

		grid = new GGrid(section, section.body().width() + 100, 2, 0, section.getLastY2() + 8);

		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().allocated);
			}
		}.hh(¤¤TotalSpace);
		grid.add(r);

		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = g.get().amount;
				GFORMAT.i(text, am);
			}
		}.hh(¤¤UsedSpace);
		grid.add(r);

		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().spaceReserved);
			}
		}.hh(¤¤Incoming);
		grid.add(r);

		if (S.get().developer) {
			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					int res = 0;
					for (COORDINATE c : i.body()) {
						if (i.is(c)) {
							res += blueprint.UNLOADER.reserved(i.resource(), c);
						}
					}

					GFORMAT.iBig(text, res);
				}
			}.hh("reserved crates");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.amount.get(r));
					}
				}
			}.hh("t amount");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.capacity.get(r));
					}
				}
			}.hh("t capacity");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.delivering.get(r));
					}
				}
			}.hh("t delivering");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.incoming.get(r));
					}
				}
			}.hh("t incoming");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.onItsWay.get(r));
					}
				}
			}.hh("t on way");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					if (r != null) {
						GFORMAT.iBig(text, blueprint.tally.toBeImport.get(r) + blueprint.tally.toBeSpoils.get(r)
								+ blueprint.tally.toBeTaxes.get(r));
					}
				}
			}.hh("to be delivered");
			grid.add(r);

			r = new GStat() {

				@Override
				public void update(GText text) {
					ImportInstance i = g.get();
					RESOURCE r = i.resource();
					int a = 0;
					if (r != null) {
						for (WEntity e : World.ENTITIES().all()) {
							if (e instanceof world.entity.caravan.Shipment) {
								Shipment s = (Shipment) e;
								if (s.destination() == FACTIONS.player().capitolRegion()) {
									a += s.loadGet(r);
								}
							}
						}
						GFORMAT.iBig(text, a);
					}
				}
			}.hh("caravans");
			grid.add(r);

		}

		section.body().incrW(48);

		section.addRelBody(8, DIR.S, new GHeader(¤¤Accepting));

		section.addRelBody(2, DIR.S, new GResSelector(true) {

			@Override
			protected void select(RESOURCE r, int li) {
				g.get().allocate(r);
			}

			@Override
			protected RESOURCE getResource() {
				return g.get().resource();
			}

			@Override
			protected void hoverResource(RESOURCE res, GBox b) {
				b.title(res.name);

				b.textL(DicRes.¤¤Sellers);
				b.tab(6);
				b.textL(DicRes.¤¤sellPrice);
				b.tab(9);
				b.textL(DicRes.¤¤Stored);
				b.NL();
				for (TradeHolder h : FACTIONS.tradeUtil().getSellers(FACTIONS.player(), res)) {
					b.add(b.text().lablify().add(h.faction().appearence().name()));
					b.tab(6);
					b.add(GFORMAT.f(b.text(), h.price()));
					b.tab(9);
					b.add(GFORMAT.i(b.text(), h.stored()));
					b.NL();
				}

				b.NL(8);

				b.textL(DicRes.¤¤Taxes);

				for (Region reg : FACTIONS.player().kingdom().realm().regions()) {
					for (RegionResource r : REGIOND.RES().res) {
						if (r.resource == res && r.current_output.get(reg) > 0) {
							b.add(SPRITES.icons().s.arrow_left);
							b.add(b.text().lablify().add(reg.name()));
							b.add(GFORMAT.i(b.text(), r.current_output.get(reg)));
							b.NL();
						}
					}

				}
				b.NL(6);
				int p = FACTIONS.tradeUtil().getSellPriceBest(FACTIONS.player(), res) * TradeManager.MIN_LOAD;
				if (p > GAME.player().credits().credits()) {
					GText t = b.text();
					t.errorify().add(DicRes.¤¤cantAfford).insert(0, p);
					b.add(t);

				}
			}
		});

		{
			GuiSection s = new GuiSection() {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					visableSet(g.get().resource() != null);
					if (visableIs())
						super.render(r, ds);
				}

			};
			s.add(new RENDEROBJ.Sprite(ICON.MEDIUM.SIZE*2) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					setSprite(g.get().resource().icon().huge);
					super.render(r, ds);
				}

			});
			s.addRightC(32, new GHeader(DicGeo.¤¤Global));
			
			s.add(new GHeader(DicRes.¤¤ImportLevel), 0, s.getLastY2()+4);
			
			s.addDown(2, UIGoodsTrade.sliderImport(new GETTER<RESOURCE>() {

				@Override
				public RESOURCE get() {
					return g.get().resource();
				}
				
			}, 180));

			s.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, SETT.ROOMS().STOCKPILE.tally().amountTotal(g.get().resource()));
					text.s();
					GFORMAT.perc(text, SETT.ROOMS().STOCKPILE.tally().load(g.get().resource()));
				}
			}.hh(SETT.ROOMS().STOCKPILE.info.names, 120), 0, s.getLastY2() + 2);

			s.addDown(8, new GStat() {
				@Override
				public void update(GText text) {
					GFORMAT.i(text, blueprint.tally.incoming.get(g.get().resource()));
				}
			}.hh(DicRes.¤¤Inbound, 120));

			s.addDown(8, new GStat() {
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.tradeUtil().getSellPriceBest(FACTIONS.player(), g.get().resource()));
				}
			}.hh(DicRes.¤¤buyPrice, 120));

			section.addRelBody(8, DIR.S, s);

		}

		//
		// makeTable(g, section, section.getLastY2()+20);

	}

	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		RENDEROBJ r = null;

		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (RESOURCE r : RESOURCES.ALL())
					am += blueprint.tally.capacity.get(r);
				GFORMAT.i(text, am);
			}
		}.hh(¤¤TotalSpace);
		text.add(r);

		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (RESOURCE r : RESOURCES.ALL())
					am += blueprint.tally.amount.get(r);
				GFORMAT.i(text, am);
			}
		}.hh(¤¤UsedSpace);
		text.add(r);

		r = new GStat() {

			@Override
			public void update(GText text) {
				int am = 0;
				for (RESOURCE r : RESOURCES.ALL())
					am += blueprint.tally.incoming.get(r);
				GFORMAT.i(text, am);
			}
		}.hh(¤¤Incoming);
		text.add(r);

	}

	@Override
	protected void appendTableButt(GuiSection s, GETTER<RoomInstance> ins) {

		s.add(new SPRITE.Imp(ICON.SMALL.SIZE) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				RESOURCE ro = ((ImportInstance) ins.get()).resource();
				SPRITE s = ro == null ? SPRITES.icons().s.cancel : ro.icon().small;
				s.render(r, X1, Y1);
			}
		}, 0, s.body().y2());

		s.addRightC(8, new SPRITE.Imp(s.body().width() - 8 - s.getLastX2(), 12) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				ImportInstance in = (ImportInstance) ins.get();

				double t = in.allocated;
				double n = in.amount;
				double i = in.spaceReserved;
				GMeter.renderDelta(r, n / t, (n + i) / 2, X1, X2, Y1, Y2);
			}
		});

	}

	@Override
	protected void hover(GBox box, ImportInstance i) {
		super.hover(box, i);
		if (i.resource() != null) {
			box.setResource(i.resource(), i.amount, i.allocated);
		}
	}

	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters,
			LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {
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
				if (ins != null && ins instanceof ImportInstance) {
					ImportInstance i = (ImportInstance) ins;
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
