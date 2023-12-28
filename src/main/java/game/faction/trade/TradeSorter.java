package game.faction.trade;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
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
				
				int am = CLAMP.i(player.seller().forSale(r), 0, TradeManager.MIN_LOAD);
				
				if (am <= 0)
					continue;
				
				int price = buyer.faction().buyer().buyPrice(r, am);
				double toll = TradeManager.toll(player, (FactionNPC) buyer.faction(), buyer.distance(), price);

				price -= toll;

				if (price <= 0)
					continue;
				
				if (!SETT.ROOMS().EXPORT.tally.okPrice(r, price))
					continue;
				
				Holder h = holders[hI++];
				
				
				h.p = buyer;
				h.value = price/am;
				h.price = price;
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
			
			h.p.faction().buyer().buy(t.res, am, h.price, player);
			player.seller().sell(t.res, am, h.price, h.p.faction());
			//LOG.ln(h.res.name + " " + MIN_LOAD + " " + (h.price-h.toll) + " " + h.f.appearence().name());
			
			//LOG.ln("sold " + h.res.name + " to " + h.f.appearance.name() + " for: " + (h.price-h.toll));
			
			am = CLAMP.i(player.seller().forSale(t.res), 0, TradeManager.MIN_LOAD);
			
			if (am > 0) {
				int price = h.p.faction().buyer().buyPrice(t.res, am);
				double toll = TradeManager.toll(player, h.p.faction(), h.p.distance(), price);
				price -= toll;
				
				if (price > 0 && SETT.ROOMS().EXPORT.tally.okPrice(t.res, price)) {
					h.value = price/am;
					h.price = price;
					t.traders.add(h);
				}
			}
			
			
			
			if (t.traders.size() > 0) {
				t.value = SETT.ROOMS().EXPORT.tally.prio(t.res);
				tree.add(t);
			}
		}
		
	}
	

	void buy(Faction buyer, TradeShipper shipper) {

		tree.clear();
		int hI = 0;
		
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
				

				int sellPrice = seller.faction().seller().priceSell(r, TradeManager.MIN_LOAD);
				int toll = TradeManager.toll(seller.faction(), buyer, seller.distance(), sellPrice);
				
				int price = sellPrice+toll;
				
				double v = buyer.buyer().buyPriority(r, TradeManager.MIN_LOAD, price);
				//LOG.ln(price + " " + toll + " " + buyer.buyer().buyPrice(r, TradeManager.MIN_LOAD) + " " + v);
				if (v > 0) {
					Holder h = holders[hI++];
					h.p = seller;
					h.value = v;
					h.price = price;
					t.traders.add(h);
					
				}
				
			}
			
			if (t.traders.hasMore()) {
				t.value = t.traders.greatest().value;
				tree.add(t);
			}
			
		}
		
		while(tree.hasMore()) {
			
			ResTree t = tree.pollGreatest();
			
			Holder h = t.traders.pollGreatest();
			int sellPrice = h.price;
			

			double v = buyer.buyer().buyPriority(t.res, TradeManager.MIN_LOAD, sellPrice);
			
			if (v > 0) {
				
				
				h.p.trade(t.res, TradeManager.MIN_LOAD);
				
				buyer.buyer().buy(t.res, TradeManager.MIN_LOAD,  sellPrice, h.p.faction());
				h.p.faction().seller().sell(t.res, TradeManager.MIN_LOAD, sellPrice, buyer);

				//LOG.ln(buyer.name + " " + " <- " + h.p.faction().name + " " + t.res + " " + sellPrice + " " + buyer.seller().priceSell(t.res, TradeManager.MIN_LOAD));
				
				if (h.p.faction().seller().forSale(t.res) > TradeManager.MIN_LOAD) {
					
					int sp = h.p.faction().seller().priceSell(t.res, TradeManager.MIN_LOAD);
					h.price = sp+ TradeManager.toll(h.p.faction(), buyer, h.p.distance(), sp);
					h.value = buyer.buyer().buyPriority(t.res, TradeManager.MIN_LOAD, h.price);
					if (h.value > 0) {
						t.traders.add(h);
					}
				}
				
				
			}else {
				t.traders.clear();
			}
			
			if (t.traders.size() > 0) {
				h = t.traders.greatest();
				t.value = buyer.buyer().buyPriority(t.res, TradeManager.MIN_LOAD, h.price);
				if (t.value > 0)
					tree.add(t);
			}
			
		}
		
		

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
