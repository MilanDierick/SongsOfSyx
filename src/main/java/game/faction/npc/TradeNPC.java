package game.faction.npc;

import java.io.IOException;

import game.faction.Faction;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.FACTION_IMPORTER;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.*;

public class TradeNPC implements FACTION_IMPORTER, FACTION_EXPORTER{

	private final Stockpile stockpile;
	private final Faction s;
	private double bestBuyValue = 1;
	
	public TradeNPC(FactionNPC s) {
		this.s = s;
		stockpile = new Stockpile(RESOURCES.ALL().size(), s);
	}
	
	public int amount(RESOURCE res) {
		return (int) stockpile.amount(res.bIndex());
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			stockpile.save(file);
			file.d(bestBuyValue);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			stockpile.load(file);
			bestBuyValue = file.d();
		}

		@Override
		public void clear() {
			stockpile.clear();
			bestBuyValue = 1;
		}
		
	};
	

	
	static final double CONSUMPTION_DIVIDER = 150;
	
	public int consume(RESOURCE r) {
		double div = (double)stockpile.amount(r.bIndex())/Integer.MAX_VALUE;
		if (r.isEdible())
			div *= 2;
		stockpile.inc(r.bIndex(), -stockpile.amount(r.bIndex())/(1 + CONSUMPTION_DIVIDER - CONSUMPTION_DIVIDER*div), 0);
		return (int) stockpile.amount(r.bIndex());
	}
	

//	
//	void inc(RESOURCE r, int amount) {
//		stockpile.inc(r.bIndex(), amount);
//	}
	
	@Override
	public int priceSell(RESOURCE res, int amount) {
		return (int) Math.ceil(stockpile.priceSell(res.bIndex(), amount));
	}

	@Override
	public void sell(RESOURCE res, int amount, int price) {
		s.credits().inExported.inc(res, price);
		stockpile.inc(res.index(), -amount, 0);
	}

	@Override
	public int forSale(RESOURCE res) {
		return (int)(stockpile.amount(res.bIndex()));
	}

	@Override
	public void buy(RESOURCE res, int amount, int price) {
		s.credits().outImported.inc(res, price);
		stockpile.inc(res.index(), 0, amount);
	}

	void add(RESOURCE res, int amount) {
		stockpile.inc(res.bIndex(), amount, 0);
	}
	

	@Override
	public void reserveSpace(RESOURCE res, int amount) {
		stockpile.inc(res.index(), 0, amount);
	}

	@Override
	public void addImport(RESOURCE res, int amount) {
		s.res().inImported.inc(res, amount);
		stockpile.inc(res.index(), amount, 0);
	}
	
	@Override
	public void addTaxes(RESOURCE res, int amount) {

		s.res().inTaxes.inc(res, amount);
		stockpile.inc(res.index(), amount, 0);
	}
	
	@Override
	public void addSpoils(RESOURCE res, int amount) {

		s.res().in.inc(res, amount);
		stockpile.inc(res.index(), amount, 0);
	}

	@Override
	public double buyValue(RESOURCE res, int amount, double price) {
		if (stockpile.amount(res.index()) + stockpile.incoming(res.index()) >= Integer.MAX_VALUE)
			return 0;
		return stockpile.priceBuy(res.bIndex(), amount)/price;
	}
	
	@Override
	public double buyValueResource(RESOURCE res, int amount, double price) {
		if (stockpile.amount(res.index()) + stockpile.incoming(res.index()) + amount >= Integer.MAX_VALUE)
			return 0;
		return stockpile.priceBuy(res.bIndex(), amount)/price;
	}

	@Override
	public int buyPrice(RESOURCE res, int amount) {
		return (int) Math.ceil(stockpile.priceBuy(res.bIndex(), amount)*this.bestBuyValue);
	}
	
	@Override
	public void setBestBuyValue(double value) {
		this.bestBuyValue = 1.0/value;
	}


	@Override
	public int spaceForTribute(RESOURCE res) {
		return Integer.MAX_VALUE;
	}

	public int credits() {
		return (int) stockpile.credit();
	}


//	public double credits2() {
//		return stockpile.credits2();
//	}




}
