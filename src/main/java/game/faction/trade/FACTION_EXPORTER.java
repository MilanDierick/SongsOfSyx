package game.faction.trade;

import init.resources.RESOURCE;

public interface FACTION_EXPORTER {

	public int priceSell(RESOURCE res, int amount);
	public void sell(RESOURCE res, int amount, int price);
	public int forSale(RESOURCE res);
	
}
