package game.faction.trade;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.trade.TradeShipper.Partner;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Tree;

final class TradeSorter {

	private final ResTree[] resTrees = new ResTree[RESOURCES.ALL().size()];
	private final Tree<ResTree> tree = new Tree<TradeSorter.ResTree>(RESOURCES.ALL().size()) {

		@Override
		protected boolean isGreaterThan(ResTree current, ResTree cmp) {
			return current.value > cmp.value;
		}
	
	};
	private Holder[] holders = new Holder[FACTIONS.MAX*RESOURCES.ALL().size()];
	
	public TradeSorter() {
		for (int i = 0; i < resTrees.length; i++)
			resTrees[i] = new ResTree(RESOURCES.ALL().get(i));
		for (int i = 0; i < holders.length; i++)
			holders[i] = new Holder();
	}
	
	public void sellPlayer(TradeShipper shipper) {

		Faction player = FACTIONS.player();
		tree.clear();
		int hI = 0;
		
		for (RESOURCE r : RESOURCES.ALL()) {
			
			ResTree t = resTrees[r.index()];
			t.traders.clear();
			
			if (player.seller().forSale(r) <= 0)
				continue;
			
			
			
			for (int i = 0; i < shipper.partners(); i++) {
				
				Partner buyer = shipper.partner(i);
				
				double toll = buyer.toll()/TradeManager.MIN_LOAD;
				double price = playerSellPrice(r, toll, buyer.faction());
				
				if (price <= 0)
					continue;
				
				Holder h = holders[hI++];
				
				
				h.p = buyer;
				h.value = price;
				h.price = (int) price;
				t.traders.add(h);
				
			}
			
			if (resTrees[r.index()].traders.size() > 0) {
				t.value = SETT.ROOMS().EXPORT.tally.prio(r);
				tree.add(t);
			}
		}
		
		while(tree.hasMore()) {
			ResTree t = tree.pollSmallest();
			Holder h = t.traders.pollGreatest();
			
			int am = CLAMP.i(player.seller().forSale(t.res), 0, TradeManager.MIN_LOAD);
			
			if (am <= 0)
				continue;
			
			
			
			h.p.trade(t.res, am);
			
			h.p.faction().buyer().buy(t.res, am, h.price*am);
			player.seller().sell(t.res, am, h.price*am);
			//LOG.ln(h.res.name + " " + MIN_LOAD + " " + (h.price-h.toll) + " " + h.f.appearence().name());
			
			//LOG.ln("sold " + h.res.name + " to " + h.f.appearance.name() + " for: " + (h.price-h.toll));
			
			double price = playerSellPrice(t.res, h.p.toll(), h.p.faction());
			
			if (price > 0) {
				h.value = price;
				h.price = (int) Math.ceil(price);
				t.traders.add(h);
			}
			
			if (t.traders.size() > 0) {
				t.value = SETT.ROOMS().EXPORT.tally.prio(t.res);
				tree.add(t);
			}
		}
		
	}
	
	void buy(Faction buyer, TradeShipper shipper) {

		long traded = 0;
		tree.clear();
		int hI = 0;

		buyer.buyer().setBestBuyValue(1.0);
		for (RESOURCE r : RESOURCES.ALL()) {
			
			ResTree t = resTrees[r.index()];
			t.traders.clear();
			
			if (buyer != GAME.player() && buyer.buyer().buyPrice(r, TradeManager.MIN_LOAD) <= 0)
				continue;
			
			
			for (int i = 0; i < shipper.partners(); i++) {
				
				Partner seller = shipper.partner(i);
				
				if (seller.faction() == buyer)
					continue;
				
				if (seller.faction() == FACTIONS.player())
					continue;
				
				if (seller.faction().seller().forSale(r) <= TradeManager.MIN_LOAD)
					continue;
				
				double toll = (int) seller.toll();
				int sellPrice = seller.faction().seller().priceSell(r, TradeManager.MIN_LOAD);
				double v = buyer.buyer().buyValue(r, TradeManager.MIN_LOAD, Math.ceil(sellPrice+toll));
				
				
				if (v > 1) {
					Holder h = holders[hI++];
					h.p = seller;
					h.value = v;
					h.price = sellPrice;
					t.traders.add(h);
				}
				
			}
			
			if (t.traders.size() > 0) {
				Holder h = t.traders.greatest();
				t.value = buyer.buyer().buyValueResource(r, TradeManager.MIN_LOAD, h.price + h.p.toll());
				tree.add(t);
			}
		}
		
		if (tree.hasMore()) {
			ResTree t = tree.greatest();
			buyer.buyer().setBestBuyValue(t.traders.greatest().value);
		}
		
		while(tree.hasMore()) {
			
			ResTree t = tree.pollGreatest();
			
			Holder h = t.traders.pollGreatest();
			int sellPrice = h.price;
			double toll = h.p.toll();
			sellPrice = (int) Math.ceil(sellPrice+toll);

			double v = buyer.buyer().buyValue(t.res, TradeManager.MIN_LOAD, sellPrice);
			
			if (v > 1) {
				h.p.trade(t.res, TradeManager.MIN_LOAD);
				traded ++;
				
				buyer.buyer().buy(t.res, TradeManager.MIN_LOAD,  sellPrice);
				h.p.faction().seller().sell(t.res, TradeManager.MIN_LOAD, sellPrice);

				if (h.p.faction().seller().forSale(t.res) > TradeManager.MIN_LOAD) {
					h.price = (int) Math.ceil(h.p.faction().seller().priceSell(t.res, TradeManager.MIN_LOAD));
					h.value = buyer.buyer().buyValue(t.res, TradeManager.MIN_LOAD, h.price+toll);
					if (h.value > 1) {
						t.traders.add(h);
					}
				}
				
				
			}else {
				t.traders.clear();
			}
			
			if (traded > 100000)
				break;
			
			if (t.traders.size() > 0) {
				h = t.traders.greatest();
				t.value = buyer.buyer().buyValueResource(t.res, TradeManager.MIN_LOAD, h.price + h.p.toll());
				tree.add(t);
			}
			
		}
		
		

	}
	
	private double playerSellPrice(RESOURCE r, double toll, Faction buyer) {
		double price = buyer.buyer().buyPrice(r, 1) - toll;
		return price - FACTIONS.player().credits().tradePenalty(price);
	}

	private static class Holder {
		Partner p;
		int price;
		private double value;
	}
	
	private static class ResTree {
		
		double value;
		final RESOURCE res;
		
		ResTree(RESOURCE res){
			this.res = res;
		}
		
		final Tree<Holder> traders = new Tree<TradeSorter.Holder>(FACTIONS.MAX) {

			@Override
			protected boolean isGreaterThan(Holder current, Holder cmp) {
				return current.value > cmp.value;
			}
			
		};
		
	}
	
	
}
