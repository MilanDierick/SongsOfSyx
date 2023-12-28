package settlement.room.infra.export;

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
import snake2d.util.file.*;
import util.data.BOOLEANO;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.statistics.IntResource;

public final class ExportTally implements FACTION_EXPORTER{
	
	double timer;
	final IntResource attempting = new IntResource();
	public final IntResource promised = new IntResource();
	private final IntResource pAmount = new IntResource();
	private final IntResource pCapacity = new IntResource();
	public final INT_O<RESOURCE> amount = pAmount;
	public final INT_O<RESOURCE> capacity = pCapacity;
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
				set(r, 1);
		};
		
	};
	
	public final INT_OE<RESOURCE> exportWhenUnder = new IntResource() {
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
	
	public boolean shouldWork(RESOURCE res) {
		
		double tot = SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
		double am = SETT.ROOMS().STOCKPILE.tally().amountReservable(res);
		
		if (exportWhenUnder.getD(res) == 1) {
			return true;
		}else {
			double exp = exportWhenUnder.get(res)/(exportWhenUnder.max(res)-1.0);
			exp = 1.0-exp;
			double to = am - exp*tot;
			return to > 0;
		}
		
	}
	
	public final BOOLEANO<RESOURCE> exporting = new BOOLEANO<RESOURCE>() {

		
		@Override
		public boolean is(RESOURCE res) {
			int am = (amount.get(res)-promised.get(res));
			return am/TradeManager.MIN_LOAD > 0;
		}
	};
	
	ExportTally() {
		for (RESOURCE r : RESOURCES.ALL())
			exportWhenUnder.set(r, 1);
	}
	
	final SAVABLE saver = new SAVABLE() {
		@Override
		public void save(FilePutter file) {
			promised.save(file);
			pAmount.save(file);
			pCapacity.save(file);
			attempting.save(file);
			priceCapsI.save(file);
			file.d(timer);
			
			((IntResource) exportWhenUnder).save(file);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			promised.load(file);
			pAmount.load(file);
			pCapacity.load(file);
			attempting.load(file);
			priceCapsI.load(file);
			timer = file.d();
			((IntResource) exportWhenUnder).load(file);
			
		}

		@Override
		public void clear() {
			promised.clear();
			pAmount.clear();
			pCapacity.clear();
			attempting.clear();
			priceCapsI.clear();
			timer = 0;
			((IntResource) exportWhenUnder).clear();
			for (RESOURCE r : RESOURCES.ALL())
				exportWhenUnder.set(r, 1);
		}
	};
	
	void inc(RESOURCE r, int amount, int capacity) {
		this.pAmount.inc(r, amount);
		this.pCapacity.inc(r, capacity);
	}


	@Override
	public int priceSell(RESOURCE res, int amount) {
		return 1;
	}
	
	@Override
	public void sell(RESOURCE res, int amount, int price, Faction buyer) {
		sellFake(res, amount, price);
	}
	
	@Override
	public void remove(RESOURCE res, int amount, ITYPE type) {
		FACTIONS.player().res().inc(res, type.rtype, -amount);
		promised.inc(res, amount);
	}
	
	public void sellFake(RESOURCE res, int amount, int price) {
		FACTIONS.player().credits().inc(price, CTYPE.TRADE, res);
		remove(res, amount, ITYPE.trade);
		GAME.count().TRADE_SALES.inc(price);
	}
	
	@Override
	public int forSale(RESOURCE res) {
		int aa = (amount.get(res) - (promised.get(res)));
		if (aa < 0)
			return aa;
		if (SETT.ENTRY().isClosed())
			return 0;
		
		return aa;
			
	}
	
	public void debug() {
		String s = "";
		for (RESOURCE r : RESOURCES.ALL()) {
			s += r.name + " ";
			s += "att: " + attempting.get(r) + " ";
			s += "pro: " + promised.get(r) + " ";
			s += "am: " + amount.get(r) + " ";
			s += "ca: " + capacity.get(r);
			s += "\r";
			
		}
		GAME.Notify(s);
	}





	public double prio(RESOURCE r) {
		if (pCapacity.get(r) == 0)
			return 0;
		
		
		double am = amount.get(r)-promised.get(r);
		return am/pCapacity.get(r);
	}
	
	public boolean okPrice(RESOURCE r, int price) {
		if (price >= priceCapsI.get(r))
			return true;
		return false;
	}

	
	private static CharSequence ¤¤ExportProblem = "¤You don't have any export depots set to this resource. No exporting can be done.";
	private static CharSequence ¤¤ExportFull = "¤Our export depots are full. We must increase their space if we are to export at full capacity";
	private static CharSequence ¤¤priceCapProblem = "The current price is below your price cap. To trade, you must disable or decrease the price cap.";
	private static CharSequence ¤¤NoPrice = "¤There is no one willing to buy this resource. You must increase either the tariff or the toll.";
	static {
		D.ts(ExportTally.class);
	}
	
	public CharSequence problem(RESOURCE res, boolean storage){
		
		if (storage && capacity.get(res) == 0) {
			return ¤¤ExportProblem;
		}
		
		if (FACTIONS.pRel().neighs().size() == 0) {
			return ¤¤noTrade;
		}

		if (FACTIONS.pRel().traders().size() == 0)
			return ¤¤noTradePartners;
		
		if (FACTIONS.pRel().pricesSell.get(res) <= 0) {
			return ¤¤NoPrice;
		}
		
		return null;
	}

	public CharSequence warning(RESOURCE res) {
		
		if (capacity.get(res) > 0 && amount.get(res) >= capacity.get(res)) {
			return ¤¤ExportFull;
		}
		
		if (FACTIONS.pRel().pricesSell.get(res) > 0 && FACTIONS.pRel().pricesSell.get(res) < priceCapsI.get(res)) {
			return ¤¤priceCapProblem;
		}
			
		
		return null;
	}
	

}
