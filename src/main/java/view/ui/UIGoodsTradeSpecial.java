package view.ui;

import game.faction.FACTIONS;
import game.faction.trade.PlayerPrices.TradeHolder;
import init.D;
import init.resources.RESOURCE;
import init.sprite.ICON;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.data.*;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.data.INT.INTE;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import world.World;
import world.entity.caravan.Shipment;

public class UIGoodsTradeSpecial extends GuiSection{

	public static CharSequence ¤¤Title = "¤Special Orders";
	public static CharSequence ¤¤Desc = "¤Make a custom order. A custom import does not need import depots, goods will be delivered to the throne if missing.";
	private static CharSequence ¤¤Cheapest = "¤Cheapest";
	private static CharSequence ¤¤Closest = "¤Closest";
	
	static {
		D.ts(UIGoodsTradeSpecial.class);
	}
	
	private RESOURCE res;
	
	public UIGoodsTradeSpecial get(RESOURCE res) {
		this.res = res;
		return this;
	}
	
	UIGoodsTradeSpecial() {
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.set(res.name);
			}
		}.r(DIR.C));
		addDownC(4, new SPRITE.Imp(ICON.BIG.SIZE, ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				res.icon().huge.render(r, X1, Y1);
			}
		}); 
		
		GuiSection s = new GuiSection();
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int)FACTIONS.player().credits().credits());
			}
		}.hv(DicRes.¤¤Currs));
		
		s.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, SETT.ROOMS().STOCKPILE.tally().amountReservable(res) + SETT.ROOMS().IMPORT.tally.amount.get(res) - SETT.HALFENTS().caravans.tmpSold(res));
			}
		}.hv(DicRes.¤¤Stored));
		
		s.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, SETT.ROOMS().IMPORT.tally.incoming.get(res));
			}
		}.hv(DicRes.¤¤Inbound));
		
		s.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, SETT.ROOMS().EXPORT.tally.promised.get(res));
			}
		}.hv(DicRes.¤¤Outbound));
		
		addDownC(16, s);
		
		addDownC(16, new GHeader(DicRes.¤¤Buy));
		addDownC(4, buyRow());
		
		addDownC(16, new GHeader(DicRes.¤¤Sell));
		addDownC(4, sellRow());
	}
	

	
	private RENDEROBJ buyRow() {
		GuiSection s = new GuiSection();
		
		BOOLEAN_MUTABLE cheapest = new BOOLEAN_MUTABLE() {
			
			private boolean b = true;
			@Override
			public boolean is() {
				return b;
			}
			
			@Override
			public BOOLEAN_MUTABLE set(boolean b) {
				this.b = b;
				return this;
			}
		};
		
		GETTER<TradeHolder> seller = new GETTER<TradeHolder>() {

			@Override
			public TradeHolder get() {
				if (FACTIONS.tradeUtil().getSellers(FACTIONS.player(), res).size() == 0)
					return null;
				if (cheapest.is()) {
					return FACTIONS.tradeUtil().getSellers(FACTIONS.player(), res).get(0);
				}
				double min = Integer.MAX_VALUE;
				TradeHolder m = null;
				for (TradeHolder h : FACTIONS.tradeUtil().getSellers(FACTIONS.player(), res)) {
					if (h.toll() < min) {
						m = h;
						min = h.toll();
					}
				}
				return m;
			}
		
		};
		
		INTE amount = new INTE() {
			
			int am = 1;
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				if (seller.get() == null)
					return 0;
				int m = seller.get().faction().seller().forSale(res);
				int a = (int)FACTIONS.player().credits().credits();
				a /= (seller.get().price());
				return CLAMP.i(a, 0, m);
			}
			
			@Override
			public int get() {

				return CLAMP.i(am, 0, max());
			}
			
			@Override
			public void set(int t) {
				am = t;
			}
		};
		

		

		
		INT price = new INT() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return Integer.MAX_VALUE;
			}
			
			@Override
			public int get() {
				
				FACTIONS.tradeUtil().getBuyers(FACTIONS.player(), res);
				
				if (seller.get() == null)
					return -1;
				
				return (int) Math.ceil(amount.get()*(seller.get().price()));
			}
			
		};
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				int am = price.get();
				if (am < 0) {
					GFORMAT.f(text, Double.NaN);
				}
				GFORMAT.i(text, price.get());
			}
		}.hh(DicRes.¤¤buyPrice, 120), 0, s.body().y2()+2);
		
		s.addRightC(64, new GButt.CheckboxTitle(¤¤Cheapest) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				boolean b = cheapest.is();
				cheapest.set(true);
				TradeHolder h = seller.get();
				if (h != null) {
					GBox bb = (GBox) text;
					bb.title(h.faction().appearence().name());
					bb.textL(DicRes.¤¤Price);
					bb.add(GFORMAT.f(bb.text(), h.price()));
					bb.NL();
					bb.textL(DicRes.¤¤Toll);
					bb.add(GFORMAT.f(bb.text(), h.toll()));
					
				}
				cheapest.set(b);
			}
			
			@Override
			protected void clickA() {
				cheapest.set(true);
			}
			
			@Override
			protected void renAction() {
				selectedSet(cheapest.is());
			}
		});
		
		s.addRightC(64, new GButt.CheckboxTitle(¤¤Closest) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				boolean b = cheapest.is();
				cheapest.set(false);
				TradeHolder h = seller.get();
				if (h != null) {
					GBox bb = (GBox) text;
					bb.title(h.faction().appearence().name());
					bb.textL(DicRes.¤¤Price);
					bb.add(GFORMAT.f(bb.text(), h.price()));
					bb.NL();
					bb.textL(DicRes.¤¤Toll);
					bb.add(GFORMAT.f(bb.text(), h.toll()));
					
				}
				cheapest.set(b);
			}
			
			@Override
			protected void clickA() {
				cheapest.set(false);
			}
			
			@Override
			protected void renAction() {
				selectedSet(!cheapest.is());
			}
		});
		
		s.add(new GHeader(DicRes.¤¤Amount), 0, s.body().y2()+6);
		
		s.addRightC(8, new GGaugeMutable(amount, 220) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.i(text, amount.get());
				return 64;
			}
		});
		
		s.addRightC(8, new GButt.ButtPanel(DicRes.¤¤Buy) {
			
			@Override
			protected void clickA() {
				TradeHolder s = seller.get();
				if (s != null && price.get() <= (int)FACTIONS.player().credits().credits() && amount.get() > 0) {
					int am = amount.get();
					s.faction().seller().sell(res, am, price.get());
					FACTIONS.player().buyer().buy(res, am, price.get());
					FACTIONS.tradeUtil().clear();
					Shipment ss = World.ENTITIES().caravans.createTrade(s.faction().capitolRegion().cx(), s.faction().capitolRegion().cy(), FACTIONS.player().capitolRegion());
					if (ss != null) {
						FACTIONS.player().buyer().reserveSpace(res, -am);
						ss.load(res, am);
					}else {
						FACTIONS.player().buyer().reserveSpace(res, -am);
						//buyer.buyer().reserveSpace(r, -a);
						FACTIONS.player().buyer().addImport(res, am);
					}
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(seller.get() != null && price.get() <= (int)FACTIONS.player().credits().credits());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (seller.get() == null) {
					text.text(DicRes.¤¤noSellers);
				}else if(price.get() > (int)FACTIONS.player().credits().credits()) {
					text.text(DicRes.¤¤NotEnoughCurr);
				}
			}
			
		});
		
		return s;
	}

	
	private RENDEROBJ sellRow() {
		GuiSection s = new GuiSection();
		
		GETTER<TradeHolder> buyer = new GETTER<TradeHolder>() {

			@Override
			public TradeHolder get() {
				if (FACTIONS.tradeUtil().getBuyers(FACTIONS.player(), res).size() == 0)
					return null;
				return FACTIONS.tradeUtil().getBuyers(FACTIONS.player(), res).get(0);
			}
		
		};
		
		INTE amount = new INTE() {
			
			int am = 1;
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				return 200;
			}
			
			@Override
			public int get() {
				int m = CLAMP.i((int) (SETT.ROOMS().STOCKPILE.tally().amountReservable(res) - SETT.HALFENTS().caravans.tmpSold(res)), 0, max());
				return CLAMP.i(am, 0, m);
			}
			
			@Override
			public void set(int t) {
				am = t;
			}
		};
		
		INT price = new INT() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return Integer.MAX_VALUE;
			}
			
			@Override
			public int get() {
				
				if (buyer.get() == null)
					return -1;
				
				return (int) Math.floor(amount.get()*(buyer.get().price()));
			}
			
		};
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				int am = price.get();
				if (am < 0) {
					GFORMAT.f(text, Double.NaN);
				}
				GFORMAT.i(text, price.get());
			}
		}.hh(DicRes.¤¤sellPrice, 120), 0, s.body().y2()+2);
		
		s.add(new GHeader(DicRes.¤¤Amount), 0, s.body().y2()+6);
		
		s.addRightC(8, new GGaugeMutable(amount, 220) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.i(text, amount.get());
				return 64;
			}
		});
		
		s.addRightC(8, new GButt.ButtPanel(DicRes.¤¤Sell) {
			
			@Override
			protected void clickA() {
				TradeHolder s = buyer.get();
				if (s != null && amount.get() > 0) {
					int am = amount.get();
					SETT.HALFENTS().caravans.createTmpExport(res, am, (int)buyer.get().price(), buyer.get().faction());
					FACTIONS.tradeUtil().update(FACTIONS.player());
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(buyer.get() != null && amount.get() > 0);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (buyer.get() == null) {
					text.text(DicRes.¤¤noBuyers);
				}
			}
			
		});
		
		return s;
	}
	
}
