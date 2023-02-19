package game.faction.trade;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.sets.*;
import world.entity.WPathing;
import world.entity.WPathing.FactionDistance;

public final class PlayerPrices {
	
	private final ResNode[] nodes = new ResNode[RESOURCES.ALL().size()];
	private final ArrayList<TradeNode> holders = new ArrayList<>(FACTIONS.MAX*2);
	private final Tree<TradeNode> sorter = new Tree<TradeNode>(FACTIONS.MAX) {
		@Override
		protected boolean isGreaterThan(TradeNode current, TradeNode cmp) {
			return current.value < cmp.value;
		}
	};
	private int tick = -100;
	private int highestBuyPrice;
	private int highestSellPrice;
	private Faction faction = null;
	
	public PlayerPrices(){
		while(holders.hasRoom())
			holders.add(new TradeNode());
		for (RESOURCE r : RESOURCES.ALL())
			nodes[r.index()] = new ResNode(r);
	}
	
	
	private void init(Faction f) {
		if (Math.abs(tick- GAME.updateI()) < 60 && faction == f)
			return;
		
		tick = GAME.updateI();
		faction = f;
		
		LIST<FactionDistance> ll = WPathing.getFactions(faction);
		
		highestBuyPrice = 0;
		highestSellPrice = 0;
		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			nodes[ri].init(ll);
			
			if (nodes[ri].sellers.size() > 0) {
				highestBuyPrice = Math.max(highestBuyPrice, getBuyPriceBest(f, RESOURCES.ALL().get(0)));
				highestSellPrice = Math.max(highestSellPrice, getSellPriceBest(f, RESOURCES.ALL().get(0)));
			}
		}
	}
	
	public void update(Faction f) {
		tick = -100;
		init(f);
	}
	
	public int getBuyPriceBest(Faction buyer, RESOURCE res) {
		init(buyer);
		if (nodes[res.index()].buyers.size() > 0) {
			return nodes[res.index()].buyers.get(0).price;
		}
		return 0;
	}
	
	public LIST<TradeHolder> getBuyers(Faction buyer, RESOURCE res){
		init(buyer);
		return nodes[res.index()].buyers;
	}
	
	public int getSellPriceBest(Faction seller, RESOURCE res) {
		init(seller);
		if (nodes[res.index()].sellers.size() > 0) {
			return nodes[res.index()].sellers.get(0).price;
		}
		return 0;
	}
	
	public LIST<TradeHolder> getSellers(Faction seller, RESOURCE res){
		init(seller);
		return nodes[res.index()].sellers;
	}
	
	public int highestBuyPrice(Faction seller) {
		init(seller);
		return highestBuyPrice;
	}
	
	public int highestSellPrice(Faction seller) {
		init(seller);
		return highestSellPrice;
	}
	
	public static class TradeHolder {
		private int price;
		private int stored;
		private Faction f;
		private double toll;
		
		public Faction faction() {
			return f;
		}
		
		public double price() {
			return price;
		}
		
		public int stored() {
			return stored;
		}
		
		public double toll() {
			return toll;
		}
	}
	
	public static final class TradeNode {

		private Faction f;
		private double value;
		private double distance;
	}
	
	public final class ResNode {
		
		private final ArrayList<TradeHolder> all = new ArrayList<>(16);
		private final ArrayList<TradeHolder> sellers = new ArrayList<>(16);
		private final ArrayList<TradeHolder> buyers = new ArrayList<>(16);
		private final RESOURCE res;
		
		ResNode(RESOURCE res){
			for (int i = 0; i < 16; i++)
				all.add(new TradeHolder());
			this.res = res;
		}
		
		void init(LIST<FactionDistance> ll) {
			sellers.clear();
			buyers.clear();
			sorter.clear();
			
			if (ll == null) {
				return;
			}
			
			int k = 0;
			
			
			for (int i = 0; i < ll.size(); i++) {
				FactionDistance d = ll.get(i);
				if (d.f == faction)
					continue;
				if (FACTIONS.rel().tradePartner.get(faction, d.f) == 0)
					continue;
				
				TradeNode h = holders.get(i);
				if (h.f == faction)
					continue;
				if (d.f.seller().forSale(res) == 0)
					continue;
				
				double price = d.f.seller().priceSell(res, 1);
				
				double toll = TradeManager.priceToll(d.distance, 1);
				price += toll;
				if (price >= 0) {
					
					h.f = d.f;
					h.distance = d.distance;
					h.value = -price;
					sorter.add(h);
				}
			}
			
			while(sorter.hasMore() && k < 8) {
				TradeNode n = sorter.pollSmallest();
				TradeHolder h = all.get(k++);
				h.f = n.f;
				h.toll = TradeManager.priceToll(n.distance, 1);
				h.price = (int) Math.ceil(h.f.seller().priceSell(res, 1) + h.toll);
				h.stored = h.f.seller().forSale(res);
				sellers.add(h);
			}
			
			sorter.clear();
			for (int i = 0; i < ll.size(); i++) {
				TradeNode h = holders.get(i);
				if (h.f == faction)
					continue;
				if (FACTIONS.rel().tradePartner.get(faction, h.f) == 0)
					continue;
				FactionDistance d = ll.get(i);
				double price = d.f.buyer().buyPrice(res, 1);
				double toll = TradeManager.priceToll(d.distance, 1);
				price -= toll;
				if (price >= 0) {
					h.f = d.f;
					h.value = price;
					h.distance = d.distance;
					sorter.add(h);
				}
			}
			
			while(sorter.hasMore() && k < 16) {
				TradeNode n = sorter.pollSmallest();
				TradeHolder h = all.get(k++);
				h.f = n.f;
				h.toll = TradeManager.priceToll(n.distance, 1);
				h.price = (int) Math.floor(h.f.buyer().buyPrice(res, 1) - h.toll);
				h.price -= FACTIONS.player().credits().tradePenalty(h.price);
				if (h.price < 0)
					continue;
				h.stored = h.f.seller().forSale(res);
				buyers.add(h);
			}
		}
		
		
	}

	public void clear() {
		tick = GAME.updateI() + 100;
		
	}
	
	
}