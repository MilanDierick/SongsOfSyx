package settlement.room.infra.importt;

import static util.dic.DicRes.*;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.trade.*;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.file.*;
import snake2d.util.sprite.text.Str;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.IntResource;

public final class ImportTally implements FACTION_IMPORTER{

	
	private final IntResource reserved = new IntResource();
	final IntResource delivering = new IntResource();
	public final IntResource priceCapsI = new IntResource() {
		
		@Override
		public int max(RESOURCE t) {
			return 1000000;
		};
		
		@Override
		public int min(RESOURCE t) {
			return 1;
		};
		
		@Override
		public void clear() {
			for (RESOURCE r : RESOURCES.ALL())
				set(r, max(r));
		};
		
	};
	
	final IntResource[] iAll = new IntResource[ITYPE.all.size()];
	{
		for (int i = 0; i < ITYPE.all.size(); i++) {
			iAll[i] = new IntResource();
		}
		priceCapsI.clear();
	}

	public final INT_O<RESOURCE> incoming = new INT_O<RESOURCE>() {
		@Override
		public int get(RESOURCE t) {
			int am = reserved.get(t);
			for (IntResource ii : iAll)
				am += ii.get(t);
			return am;
		}
		@Override
		public int min(RESOURCE t) {
			return 0;
		};
		@Override
		public int max(RESOURCE t) {
			return 100;
		};
	};
	
	private final IntResource pAmount = new IntResource();
	private final IntResource pCapacity = new IntResource();
	public final INT_O<RESOURCE> amount = pAmount;
	public final INT_O<RESOURCE> capacity = pCapacity;
	

	
	
	public final INT_OE<RESOURCE> importWhenBelow = new IntResource() {
		@Override
		public int min(RESOURCE t) {
			return 0;
		};
		@Override
		public int max(RESOURCE t) {
			return 21;
		};
		
		@Override
		public int get(RESOURCE t) {
			if (t != null) {
				return super.get(t);
			}
			int am = 0;
			for (int i = 0; i < RESOURCES.ALL().size(); i++) {
				am = Math.max(am, get(RESOURCES.ALL().get(i)));
			}
			return am;
		}
		
		@Override
		public void set(RESOURCE t, int am) {
			if (t != null) {
				super.set(t, am);
				return;
			}
			for (int i = 0; i < RESOURCES.ALL().size(); i++) {
				set(RESOURCES.ALL().get(i), am);
			}
		};
	};
	
	public void debug(RESOURCE res) {
		LOG.ln(res.name);
		for (ITYPE t : ITYPE.all)
			LOG.ln(" t " + t.name + " " + iAll[t.index].get(res));
		LOG.ln("del: " + reserved.get(res));
		LOG.ln("am " + pAmount.get(res));
		LOG.ln("ca " + capacity.get(res));
		LOG.ln(spaceForTribute(res));
		LOG.ln();
	}
	
	ImportTally() {
		for (RESOURCE r : RESOURCES.ALL())
			importWhenBelow.set(r, 1);
	}
	
	final SAVABLE saver = new SAVABLE() {
		@Override
		public void save(FilePutter file) {
			for (IntResource ii : iAll)
				ii.save(file);
			pAmount.save(file);
			pCapacity.save(file);
			reserved.save(file);
			delivering.save(file);
			priceCapsI.save(file);
			((IntResource) importWhenBelow).save(file);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (IntResource ii : iAll)
				ii.load(file);
			pAmount.load(file);
			pCapacity.load(file);
			reserved.load(file);
			delivering.load(file);
			priceCapsI.load(file);
			((IntResource) importWhenBelow).load(file);
		}

		@Override
		public void clear() {
			for (IntResource ii : iAll)
				ii.clear();
			pAmount.clear();
			pCapacity.clear();
			reserved.clear();
			delivering.clear();
			priceCapsI.clear();
			((IntResource) importWhenBelow).clear();
			for (RESOURCE r : RESOURCES.ALL())
				importWhenBelow.set(r, 1);
		}
	};
	
	void inc(RESOURCE r, int amount, int capacity) {
		this.pAmount.inc(r, amount);
		this.pCapacity.inc(r, capacity);
	}

	@Override
	public void buy(RESOURCE res, int amount, int price, Faction seller) {
		//FACTIONS.player().res().inImported.inc(res, amount);
		FACTIONS.player().credits().inc(-price, CTYPE.TRADE, res);
		GAME.count().TRADE_PURCHASES.inc(price);
		reserve(res, amount, ITYPE.trade);
	}
	
	@Override
	public int buyPrice(RESOURCE res, int amount) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean wantsToBuy(RESOURCE res, int a) {
		if (FACTIONS.player().credits().credits() <= 0)
			return false;
		if (spaceForTribute(res) < a)
			return false;
		if (importWhenBelow.get(res) == 0)
			return false;
		int amount = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
		amount += pAmount.get(res);
		amount += incoming.get(res);
		double cap = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
		if (cap == 0)
			return true;
		if (importWhenBelow.isMax(res))
			return amount < capacity.get(res) + cap - 32;
		double per = 20*amount/cap;
		if (per < importWhenBelow.get(res))
			return true;
		return false;
	}
	
	@Override
	public void reserve(RESOURCE res, int am, ITYPE type) {
		reserved.inc(res, am);
	}
	
	@Override
	public void deliverAndUnreserve(RESOURCE res, int am, ITYPE type) {
		reserved.inc(res, -am);
		iAll[type.index].inc(res, am);
	}

//	@Override
//	public double buyValue(RESOURCE res, int amount, double price) {
//		if (price/amount >= priceCapsI.get(res))
//			return 0;
//		if (GAME.player().credits().credits() < price) {
//			return 0;
//		}
//		if (wantsToBuy(res, amount)) {
//			return 1 + 1.0/price;
//		}
//		return 0;
//	}
	
	@Override
	public double buyPriority(RESOURCE res, int amount, double price) {
		if (price/amount >= priceCapsI.get(res))
			return 0;
		if (GAME.player().credits().credits() < price) {
			return 0;
		}
		if (wantsToBuy(res, amount)) {
			double a = incoming.get(res);
			if (a > 0) {
				a /= capacity.get(res);
			}
			
			return 1 + 1.0/price + 1.0-a;
		}
		return 0;
	}



	@Override
	public int spaceForTribute(RESOURCE res) {
		int am = capacity.get(res)-(amount.get(res)+incoming.get(res));
		if (am < 0) {
			return 0;
		}
		return am;
	}

//	@Override
//	public void setBestBuyValue(double value) {
//		// TODO Auto-generated method stub
//		
//	}
	
	
	private static CharSequence ¤¤ImportProblem = "¤You don't have any import depots set to this resource. No automated importing can be done.";
	private static CharSequence ¤¤priceCapProblem = "The current cheapest buy price exceeds your set price cap. To trade, you must disable or increase the price cap.";
	private static CharSequence ¤¤ImportFull = "¤Our import depots are full. We must increase their space, or improve logistics, if we are to import more.";
	private static CharSequence ¤¤cantAfford = "¤You don't have enough credits. Trades are done in increments of 32. You need at least {0} credits to purchase a batch of this resource.";
	static {
		D.ts(ImportTally.class);
	}
	
	
	public CharSequence problem(RESOURCE res, boolean storage) {

		if (storage && SETT.ROOMS().IMPORT.tally.capacity.get(res) == 0) {
			return ¤¤ImportProblem;
		}

		if (FACTIONS.pRel().neighs().size() == 0)
			return ¤¤noTrade;
		
		if (FACTIONS.pRel().traders().size() == 0)
			return ¤¤noTradePartners;
		
		if (FACTIONS.pRel().pricesBuy.get(res) == 0)
			return ¤¤noTrade;
		
		return null;
	}

	
	public CharSequence warning(RESOURCE res) {
		
		if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0 && SETT.ROOMS().IMPORT.tally.spaceForTribute(res) < TradeManager.MIN_LOAD) {
			return ¤¤ImportFull;
		}
		
		int pr = FACTIONS.pRel().pricesBuy.get(res);
		
		if (pr > 0 &&  pr > FACTIONS.player().credits().getD()) {
			return Str.TMP.clear().add(¤¤cantAfford).insert(0, pr*TradeManager.MIN_LOAD);
		}
		
		if (pr != Integer.MAX_VALUE && pr > priceCapsI.get(res))
			return ¤¤priceCapProblem;
		
		return null;
	}
	
	


	

}
