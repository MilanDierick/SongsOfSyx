package game.faction.trade;

import init.resources.RESOURCE;

public interface FACTION_IMPORTER {

	/**
	 * resource will be bought. credits payed
	 * @param res
	 * @param price
	 * @param value
	 */
	public void buy(RESOURCE res, int amount, int price);
	
	public void addImport(RESOURCE res, int amount);
	public void addTaxes(RESOURCE res, int amount);
	public void addSpoils(RESOURCE res, int amount);
	
	public void reserveSpace(RESOURCE res, int amount);
	/**
	 * 
	 * @param res
	 * @param price
	 * @return returns < 1 if doesn't want to buy, else a double representing the value of this deal
	 */
	public double buyValue(RESOURCE res, int amount, double price);
	
	public double buyValueResource(RESOURCE res, int amount, double bestPrice);
	
	public int buyPrice(RESOURCE res, int amount);
	
	public int spaceForTribute(RESOURCE res);
	public void setBestBuyValue(double value);
}
