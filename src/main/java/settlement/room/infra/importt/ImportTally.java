package settlement.room.infra.importt;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.trade.FACTION_IMPORTER;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.file.*;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.IntResource;

public final class ImportTally implements FACTION_IMPORTER{
	
	final IntResource onItsWay = new IntResource();
	final IntResource toBeImport = new IntResource();
	final IntResource toBeTaxes = new IntResource();
	final IntResource toBeSpoils = new IntResource();
	
	final IntResource delivering = new IntResource();
	public final INT_O<RESOURCE> incoming = new INT_O<RESOURCE>() {
		@Override
		public int get(RESOURCE t) {
			return onItsWay.get(t) + toBeImport.get(t) + toBeTaxes.get(t) + toBeSpoils.get(t) + delivering.get(t);
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
		LOG.ln("on: " + onItsWay.get(res));
		LOG.ln("toBe: " + toBeImport.get(res));
		LOG.ln("del: " + delivering.get(res));
		LOG.ln("am " + pAmount.get(res));
		LOG.ln("ca " + capacity.get(res));
		LOG.ln(spaceForTribute(res));
		LOG.ln();
	}
	
	ImportTally() {
		for (RESOURCE r : RESOURCES.ALL())
			importWhenBelow.set(r, importWhenBelow.max(null));
	}
	
	final SAVABLE saver = new SAVABLE() {
		@Override
		public void save(FilePutter file) {
			onItsWay.save(file);
			pAmount.save(file);
			pCapacity.save(file);
			toBeImport.save(file);
			toBeSpoils.save(file);
			toBeTaxes.save(file);
			delivering.save(file);
			((IntResource) importWhenBelow).save(file);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			onItsWay.load(file);
			pAmount.load(file);
			pCapacity.load(file);
			toBeImport.load(file);
			toBeSpoils.load(file);
			toBeTaxes.load(file);
			delivering.load(file);
			((IntResource) importWhenBelow).load(file);
		}

		@Override
		public void clear() {
			onItsWay.clear();
			pAmount.clear();
			pCapacity.clear();
			toBeImport.clear();
			toBeSpoils.clear();
			toBeTaxes.clear();
			delivering.clear();
			((IntResource) importWhenBelow).clear();
			for (RESOURCE r : RESOURCES.ALL())
				importWhenBelow.set(r, importWhenBelow.max(null));
		}
	};
	
	void inc(RESOURCE r, int amount, int capacity) {
		this.pAmount.inc(r, amount);
		this.pCapacity.inc(r, capacity);
	}

	@Override
	public void buy(RESOURCE res, int amount, int price) {
		//FACTIONS.player().res().inImported.inc(res, amount);
		FACTIONS.player().credits().outImported.inc(res, (int)price);
		GAME.stats().TRADE_PURCHASES.inc(price);
		onItsWay.inc(res, amount);
	}
	
	@Override
	public int buyPrice(RESOURCE res, int amount) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean wantsToBuy(RESOURCE res, int a) {
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
	public void addTaxes(RESOURCE res, int amount) {
		toBeTaxes.inc(res, amount);
	}
	
	@Override
	public void addImport(RESOURCE res, int amount) {
		toBeImport.inc(res, amount);
	}
	
	@Override
	public void addSpoils(RESOURCE res, int amount) {
		toBeSpoils.inc(res, amount);
	}

	@Override
	public void reserveSpace(RESOURCE res, int amount) {
		onItsWay.inc(res, amount);
	}

	@Override
	public double buyValue(RESOURCE res, int amount, double price) {
		if (GAME.player().credits().credits() < price) {
			return 0;
		}
		if (wantsToBuy(res, amount)) {
			return 1 + 1.0/price;
		}
		return 0;
	}
	
	@Override
	public double buyValueResource(RESOURCE res, int amount, double price) {
		double a = incoming.get(res);
		if (a > 0) {
			a /= capacity.get(res);
		}
		return 1.0 + 1.0-a;
	}



	@Override
	public int spaceForTribute(RESOURCE res) {
		int am = capacity.get(res)-(amount.get(res)+incoming.get(res));
		if (am < 0) {
			return 0;
		}
		return am;
	}

	@Override
	public void setBestBuyValue(double value) {
		// TODO Auto-generated method stub
		
	}
	


	
	


	

}
