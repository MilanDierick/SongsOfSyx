package view.ui;

import static util.dic.DicRes.*;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.PlayerPrices.TradeHolder;
import game.faction.trade.TradeManager;
import init.C;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.DicRes;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.gui.table.GStaples;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.statistics.HistoryResource;
import view.main.VIEW;

public final class UIGoodsTrade extends GuiSection {

	private final Faction current = FACTIONS.player();
	private static CharSequence ¤¤clickForInfo = "¤Click for info";
	private static CharSequence ¤¤ExportLevelDesc = "¤Your export level controls how much will be exported, based on your current warehouse stock. Wares must first be moved to an Export Depot, where they will eventually be sold to the highest bidder, if one exist.";
	private static CharSequence ¤¤ExportEverything = "¤Export everything, even resources not in warehouses.";
	private static CharSequence ¤¤ExportNothing = "¤Export nothing.";
	private static CharSequence ¤¤ExportLevelCurrent = "¤Export {0}% of warehouse stock (when above {1} items). Current stock is {2} items. You will currently sell {3} items.";
	private static CharSequence ¤¤ExportProblem = "¤You don't have any export depots set to this resource. No exporting can be done.";
	
	private static CharSequence ¤¤ImportLevelDesc = "¤Your import level dictates how much that will be imported based on your warehouse stock. Wares will be delivered to import depots from selling factions, which might take time. If you are short on money, or lacking depot space, nothing will be imported. If you have no warehouse space, yet still want to import, put this setting on maximum.";
	private static CharSequence ¤¤ImportEverything = "¤Import maximum to fill both warehouses and import depots.";
	private static CharSequence ¤¤ImportNothing = "¤Never import.";
	private static CharSequence ¤¤ImportLevelCurrent = "¤Import to maintain warehouse stock at {0}% of total capacity ({1} items). Current stock is {2} items. You will currently import {3} items.";
	private static CharSequence ¤¤ImportProblem = "¤You don't have any import depots set to this resource. No importing can be done.";
	static {
		D.ts(UIGoodsTrade.class);
	}
	
	private static RESOURCE lRes = RESOURCES.ALL().get(0);
	private static final UIPanelTraders buyers = new UIPanelTraders(¤¤Buyers) {

		@Override
		protected LIST<TradeHolder> list() {
			return FACTIONS.tradeUtil().getBuyers(GAME.player(), lRes);
		}

		@Override
		protected HistoryResource prices() {
			return GAME.player().credits().pricesSell;
		}
	};
	private static final UIPanelTraders sellers = new UIPanelTraders(¤¤Sellers) {

		@Override
		protected LIST<TradeHolder> list() {
			// TODO Auto-generated method stub
			return FACTIONS.tradeUtil().getSellers(GAME.player(), lRes);
		}

		@Override
		protected HistoryResource prices() {
			return GAME.player().credits().pricesBuy;
		}
	};
	
	UIGoodsTrade(RESOURCE r) {

		addRelBody(16, DIR.E, sell(r));
		addRelBody(16, DIR.E, buy(r));
		if (r != null) {
			GButt.ButtPanel c = new GButt.ButtPanel(SPRITES.icons().s.money) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(VIEW.UI().goods.specialTrade(r), this);
				}
				
			};
			c.hoverTitleSet(UIGoodsTradeSpecial.¤¤Title).hoverInfoSet(UIGoodsTradeSpecial.¤¤Desc);
			c.pad(10, 10);
			addRelBody(16, DIR.E, c);
		}
		
	}

	
	public static RENDEROBJ sliderImport(GETTER<RESOURCE> g, int width) {

		INTE limit = new INTE() {
			
			@Override
			public int get() {
				return SETT.ROOMS().IMPORT.tally.importWhenBelow.get(g.get());
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public int max() {
				return SETT.ROOMS().IMPORT.tally.importWhenBelow.max(g.get());
			}

			@Override
			public void set(int t) {
				SETT.ROOMS().IMPORT.tally.importWhenBelow.set(g.get(), t);
			}
		};
		
		return new GSliderInt(limit, width, false) {
			@Override
			protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
				ColorImp.TMP.interpolate(GCOLOR.UI().BAD.normal, GCOLOR.UI().GOOD.normal, (double)width/widthFull);
				if (width == widthFull)
					GCOLOR.UI().GREAT.normal.render(r, x1, x1+width, y1, y2);
				else
					GCOLOR.UI().GOOD.normal.render(r, x1, x1+width, y1, y2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(¤¤ImportLevel);
				
				if (limit.getD() == 1) {
					b.textL(¤¤ImportEverything);
					b.NL(4);
				}else if (limit.getD() == 0) {
					b.textL(¤¤ImportNothing);
					b.NL(4);
				}else {
					GText t = b.text();
					t.add(¤¤ImportLevelCurrent);
					double lim = limit.get()/(limit.max()-1.0);
					t.insert(0, (int)(Math.round(100*lim)));
					
					int space = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(g.get());
					int amount = (int) SETT.ROOMS().STOCKPILE.tally().amountReservable(g.get());
					
					int imp = (int)CLAMP.d(lim*space-amount, 0, lim*space);
					
					t.insert(1, (int)(lim*space));
					t.insert(2, amount);
					t.insert(3, imp);
					
					b.add(t);
				}
				
				
				if (SETT.ROOMS().IMPORT.tally.capacity.get(g.get()) == 0) {
					b.error(¤¤ImportProblem);
					b.NL(4);
				}
				
				b.NL(8);
				b.text(¤¤ImportLevelDesc);
				
				
			}
		};

	}

	public static RENDEROBJ sliderExport(GETTER<RESOURCE> g, int width) {

		INTE limit = new INTE() {
			
			@Override
			public int get() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.get(g.get());
			}

			@Override
			public int min() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.min(g.get());
			}

			@Override
			public int max() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.max(g.get());
			}

			@Override
			public void set(int t) {
				SETT.ROOMS().EXPORT.tally.exportWhenUnder.set(g.get(), t);
			}
		};

		return new GSliderInt(limit, width, false) {
			@Override
			protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
				ColorImp.TMP.interpolate(GCOLOR.UI().BAD.normal, GCOLOR.UI().GOOD.normal, (double)width/widthFull);
				if (width == widthFull)
					GCOLOR.UI().GREAT.normal.render(r, x1, x1+width, y1, y2);
				else
					GCOLOR.UI().GOOD.normal.render(r, x1, x1+width, y1, y2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(¤¤ExportLevel);

				if (limit.getD() == 1) {
					b.textL(¤¤ExportEverything);
				}else if (limit.getD() == 0) {
					b.textL(¤¤ExportNothing);
				}else {
					GText t = b.text();
					t.add(¤¤ExportLevelCurrent);
					double lim = limit.get()/(limit.max()-1.0);
					t.insert(0, (int)(Math.round(100*lim)));
					
					int space = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(g.get());
					int amount = (int) SETT.ROOMS().STOCKPILE.tally().amountReservable(g.get());
					
					lim = 1.0-lim;
					int export = (int)CLAMP.d(amount - lim*space, 0, amount);
					
					t.insert(1, (int)(lim*space));
					t.insert(2, amount);
					t.insert(3, export);
					
					b.add(t);
				}
				b.NL(4);
				
				if (SETT.ROOMS().EXPORT.tally.capacity.get(g.get()) == 0) {
					b.error(¤¤ExportProblem);
					b.NL(4);
				}
				
				b.NL(8);
				b.text(¤¤ExportLevelDesc);
				
				
				
			}
		};

	}
	

	
	RENDEROBJ sell(RESOURCE res) {
		
		GuiSection s = new GuiSection();
		
		s.addRightC(C.SG * 32, new HOVERABLE.HoverableAbs(C.SG * 32, C.SG * 24) {

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double d = (double) (SETT.ROOMS().IMPORT.tally.incoming.get(res)
						+ SETT.ROOMS().IMPORT.tally.amount.get(res)) / SETT.ROOMS().IMPORT.tally.capacity.get(res);
				GMeter.render(r, GMeter.C_ORANGE, d, body());
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤ImportCapacity);
				text.text(¤¤ImportCapacityDesc);
				text.NL(8);
				int in = SETT.ROOMS().IMPORT.tally.incoming.get(res);
				int am = SETT.ROOMS().IMPORT.tally.amount.get(res);
				int ca = SETT.ROOMS().IMPORT.tally.capacity.get(res);
				GBox b = (GBox) text;
				b.textL(DicRes.¤¤Storage);
				
				
				b.add(b.text().lablifySub().add(¤¤Inbound));
				b.add(GFORMAT.i(b.text(), in));
				b.NL();
				b.add(b.text().lablifySub().add(¤¤Stored));
				b.add(GFORMAT.i(b.text(), am));
				b.NL();
				b.add(b.text().lablifySub().add(¤¤Capacity));
				b.add(GFORMAT.i(b.text(), ca));
				b.NL(8);
				b.add(b.text().lablifySub().add(¤¤Importable));
				b.add(GFORMAT.i(b.text(), ca - am - in));

			}

		});
		
		
		s.addRightC(0, new GButt.ButtPanel(new GStat() {

			@Override
			public void update(GText text) {
				
				if (res == null)
					return;
				
				int s = FACTIONS.tradeUtil().getSellPriceBest(GAME.player(), res);
				GFORMAT.i(text, s);
				
				if (s*TradeManager.MIN_LOAD > GAME.player().credits().credits())
					text.errorify();
				else if (SETT.ROOMS().IMPORT.tally.spaceForTribute(res) < TradeManager.MIN_LOAD)
					text.lablifySub();
				else
					text.normalify();
				
			}
		}) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (res == null)
					return;
				GBox b = (GBox) text;
				b.title(¤¤buyPrice);
				b.text(¤¤clickForInfo).NL();
				int s = (int) Math.ceil(TradeManager.MIN_LOAD * FACTIONS.tradeUtil().getSellPriceBest(current, res));
				if (s > GAME.player().credits().credits()) {
					GText t = b.text();
					t.errorify().clear().add(¤¤cantAfford).insert(0, s);
					b.add(t);
					b.NL(4);
				}
				
				if (SETT.ROOMS().IMPORT.tally.spaceForTribute(res) < TradeManager.MIN_LOAD) {
					GText t = b.text();
					t.errorify().clear().add(¤¤cantStore);
					b.add(t);
					b.NL(4);
				}
			}
			
			@Override
			protected void clickA() {
				if (res == null)
					return;
				lRes = res;
				VIEW.inters().popup.show(sellers, this);
			}
		}.align(DIR.E).setDim(64, 24).hoverInfoSet(¤¤buyPrice));
		
		s.addRelBody(0, DIR.S, sliderImport(new GETTER.GETTER_IMP<RESOURCE>(res), 98));
		
		return s;
		
	}
	
	RENDEROBJ buy(RESOURCE res) {
		
		GuiSection s = new GuiSection();
		
		s.addRightC(C.SG * 32, new HOVERABLE.HoverableAbs(C.SG * 32, C.SG * 24) {

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				int in = SETT.ROOMS().EXPORT.tally.promised.get(res);
				int am = SETT.ROOMS().EXPORT.tally.amount.get(res);
				int ca = SETT.ROOMS().EXPORT.tally.capacity.get(res);
				double d = (double) (am - in) / ca;
				GMeter.render(r, GMeter.C_ORANGE, d, body());
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤ExportCapacity);
				text.text(¤¤ExportCapacityDesc);
				text.NL(8);
				int in = SETT.ROOMS().EXPORT.tally.promised.get(res);
				int am = SETT.ROOMS().EXPORT.tally.amount.get(res);
				int ca = SETT.ROOMS().EXPORT.tally.capacity.get(res);
				GBox b = (GBox) text;
				b.add(b.text().lablifySub().add(¤¤Outbound));
				b.add(GFORMAT.i(b.text(), in));
				b.NL();
				b.add(b.text().lablifySub().add(¤¤Amount));
				b.add(GFORMAT.i(b.text(), am));
				b.NL();
				b.add(b.text().lablifySub().add(¤¤Capacity));
				b.add(GFORMAT.i(b.text(), ca));
				b.NL(8);
				b.add(b.text().lablifySub().add(¤¤Exportable));
				b.add(GFORMAT.i(b.text(), (am - in)));
			}

		});
		
		s.addRightC(0, new GButt.ButtPanel(new GStat() {

			@Override
			public void update(GText text) {
				if (res == null)
					return;
				int s =  FACTIONS.tradeUtil().getBuyPriceBest(current, res);
				GFORMAT.i(text, s);
				if (s == 0)
					text.errorify();
			}

		}) {
			@Override
			protected void clickA() {
				if (res == null)
					return;
				lRes = res;
				VIEW.inters().popup.show(buyers, this);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (res == null)
					return;
				GBox b = (GBox)text;
				b.title(¤¤sellPrice);
				b.text(¤¤clickForInfo).NL();
				if ( FACTIONS.tradeUtil().getBuyPriceBest(current, res) == 0)
					b.add(b.text().errorify().add(¤¤noBuyers));
				
				if (SETT.ROOMS().EXPORT.tally.capacity.get(res) < TradeManager.MIN_LOAD) {
					GText t = b.text();
					t.errorify().clear().add(¤¤noStorage);
					b.add(t);
					b.NL(4);
				}
			}
		}.align(DIR.E).setDim(64, C.SG * 24).hoverInfoSet(¤¤sellPrice));

		s.addRelBody(0, DIR.S, sliderExport(new GETTER.GETTER_IMP<RESOURCE>(res), 98));
		
		return s;
		
	}
	
	static abstract class UIPanelTraders extends GuiSection{
		

		
		protected abstract LIST<TradeHolder> list();
		
		public UIPanelTraders(CharSequence title) {
			int width = 300;
			
			GStaples s = new GStaples(16) {
				
				double low;
				
				@Override
				protected void hover(GBox box, int stapleI) {
					GText t = box.text();
					t.lablify();
					int si = 16- stapleI - 1;
					DicTime.setAgo(t, si*GAME.player().credits().time.bitSeconds());
					box.add(t);
					
					box.add(GFORMAT.i(box.text(), prices().history(lRes).get(si)));
					box.text(DicRes.¤¤Curr);
					
					//box.add(TIME.setAgo(box.text().lablify(), 21));
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					low = Integer.MAX_VALUE;
					for (int si = 0; si < 16; si++) {
						low =  Math.min(low, prices().history(lRes).get(si));
					}
					low -= 1;
					super.render(r, ds, isHovered);
				}
				
				@Override
				protected double getValue(int stapleI) {
					int si = 16- stapleI - 1;
					return prices().history(lRes).get(si)-low;
				}
			};
			
			s.body().setWidth(width);
			s.body().setHeight(32);
			
			add(s);
			
			GTableBuilder builder = new GTableBuilder() {
				@Override
				public int nrOFEntries() {
					return list().size();
				}
			};
			
			builder.column(title, width, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					GButt.BSection s = new GButt.BSection(width, 40) {
						@Override
						protected void clickA() {
							VIEW.world().UI.faction.openList(list().get(ier.get()).faction());
							VIEW.world().activate();
						};
					};
					
					
					s.add(SPRITES.icons().s.money, C.SG*4, C.SG*4);
					s.addRightC(C.SG*4, new GStat() {
						@Override
						public void update(GText text) {
							double d = list().get(ier.get()).price();
							if (d == Integer.MAX_VALUE)
								d = Double.NaN;
							GFORMAT.i(text, (int)list().get(ier.get()).price());
						}
					});
					
					s.addRightC(C.SG*50, SPRITES.icons().s.clock);
					s.addRightC(C.SG*1, new GStat() {
						@Override
						public void update(GText text) {
							double d = list().get(ier.get()).toll();
							GFORMAT.f(text, d);
						}
					});
					
					s.addRightC(C.SG*50, SPRITES.icons().s.crate);
					s.addRightC(C.SG*4, new GStat() {
						@Override
						public void update(GText text) {
							GFORMAT.i(text, list().get(ier.get()).stored());
						}
					});
					
					
					s.add(new GStat() {
						
						@Override
						public void update(GText text) {
							Faction f = list().get(ier.get()).faction();
							text.color(f.banner().colorBG());
							text.set(f.appearence().name());
						}
					}, C.SG*4, s.getLastY2()+C.SG*4);
					
					return s;
				}
			});
			
			addDownC(8, builder.create(5, false));
			
		}
		
		protected abstract HistoryResource prices();
		
	}


}
