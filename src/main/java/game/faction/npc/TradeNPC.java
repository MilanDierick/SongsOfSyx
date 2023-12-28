package game.faction.npc;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.npc.ruler.ROpinions;
import game.faction.trade.*;
import init.resources.RESOURCE;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import world.regions.data.RD;

public class TradeNPC implements FACTION_IMPORTER, FACTION_EXPORTER{

	private final FactionNPC s;
	private double bestBuyValue = 1;
	
	public TradeNPC(FactionNPC s) {
		this.s = s;
	}
	
	public int amount(RESOURCE res) {
		return s.stockpile.amount(res);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.d(bestBuyValue);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			bestBuyValue = file.d();
		}

		@Override
		public void clear() {
			bestBuyValue = 1;
		}
		
	};
	
	@Override
	public int priceSell(RESOURCE res, int amount) {
		return (int) CLAMP.d(Math.ceil(s.stockpile.priceSell(res.bIndex(), amount)), 0, Integer.MAX_VALUE);
	}

	@Override
	public void sell(RESOURCE res, int amount, int price, Faction buyer) {
		s.credits().inc(price, CTYPE.TRADE, res);
		remove(res, amount, ITYPE.trade);

		if (buyer == FACTIONS.player()) {
			ROpinions.trade(s, price);
		}
	}
	
	@Override
	public void remove(RESOURCE res, int amount, ITYPE type) {
		reserve(res, -amount, type);
	}

	@Override
	public int forSale(RESOURCE res) {
		return (int)(s.stockpile.amount(res.bIndex()));
	}

	@Override
	public void buy(RESOURCE res, int amount, int price, Faction seller) {
		s.credits().inc(-price, CTYPE.TRADE, res);
		reserve(res, amount, ITYPE.trade);
		if (seller == FACTIONS.player()) {
			ROpinions.trade(s, price);
		}
		
	}

//	@Override
//	public double buyValue(RESOURCE res, int amount, double price) {
//		if (Integer.MAX_VALUE - s.stockpile.amount(res.index()) < amount)
//			return 0;
//		return s.stockpile.priceBuy(res.bIndex(), amount)/price;
//	}
	
	@Override
	public double buyPriority(RESOURCE res, int amount, double price) {
		if (Integer.MAX_VALUE - s.stockpile.amount(res.index()) < amount)
			return 0;
		return s.stockpile.priceBuy(res.bIndex(), amount)/price - 1.0;
	}

	@Override
	public int buyPrice(RESOURCE res, int amount) {
		return (int) Math.floor(s.stockpile.priceBuy(res.bIndex(), amount));
	}
	
//	@Override
//	public void setBestBuyValue(double value) {
//		this.bestBuyValue = 1.0/value;
//	}


	@Override
	public int spaceForTribute(RESOURCE res) {
		return Integer.MAX_VALUE;
	}

	public int credits() {
		return (int) s.stockpile.credit();
	}

	@Override
	public void reserve(RESOURCE res, int am, ITYPE type) {
		
		s.stockpile.inc(res, am);
		s.res().inc(res, type.rtype, am);
		if (type == ITYPE.tax)
			s.stockpile.inc(res, -am);
	}

	@Override
	public void deliverAndUnreserve(RESOURCE res, int am, ITYPE type) {
		
	}

	public int priceSellP(RESOURCE res) {
		int p = priceSell(res, 1);
		if (p < 0)
			return 0;
		p += TradeManager.toll(FACTIONS.player(), s, RD.DIST().distance(s), p);
		return (int) Math.ceil((double)p);
	}
	
	public int priceBuyP(RESOURCE res) {
		int p = buyPrice(res, 1);
		
		p -= TradeManager.toll(FACTIONS.player(), s, RD.DIST().distance(s), p);
		return p;
	}
	

//	public double credits2() {
//		return stockpile.credits2();
//	}




}
