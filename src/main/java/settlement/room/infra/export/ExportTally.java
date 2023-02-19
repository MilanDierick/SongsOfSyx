package settlement.room.infra.export;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.TradeManager;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.file.*;
import util.data.BOOLEAN_OBJECT;
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
			double to = tot*(1-exp);
			am = am - to;
		}
		if (am > 0) {
			return true;
		}
		return false;
		
	}
	
	public final BOOLEAN_OBJECT<RESOURCE> exporting = new BOOLEAN_OBJECT<RESOURCE>() {

		
		@Override
		public boolean is(RESOURCE res) {
			int am = (amount.get(res)-promised.get(res));
			return am/TradeManager.MIN_LOAD > 0;
		}
	};
	
	ExportTally() {
		for (RESOURCE r : RESOURCES.ALL())
			exportWhenUnder.set(r, exportWhenUnder.max(r));
	}
	
	final SAVABLE saver = new SAVABLE() {
		@Override
		public void save(FilePutter file) {
			promised.save(file);
			pAmount.save(file);
			pCapacity.save(file);
			attempting.save(file);
			file.d(timer);
			((IntResource) exportWhenUnder).save(file);

		}

		@Override
		public void load(FileGetter file) throws IOException {
			promised.load(file);
			pAmount.load(file);
			pCapacity.load(file);
			attempting.load(file);
			timer = file.d();
			((IntResource) exportWhenUnder).load(file);
			
		}

		@Override
		public void clear() {
			promised.clear();
			pAmount.clear();
			pCapacity.clear();
			attempting.clear();
			timer = 0;
			((IntResource) exportWhenUnder).clear();
			for (RESOURCE r : RESOURCES.ALL())
				exportWhenUnder.set(r, exportWhenUnder.max(null));
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
	public void sell(RESOURCE res, int amount, int price) {
		promised.inc(res, amount);
		sellFake(res, amount, price);
	}
	
	public void sellFake(RESOURCE res, int amount, int price) {
		FACTIONS.player().credits().inExported.inc(res, (int)price);
		FACTIONS.player().res().outExported.inc(res, amount);
		GAME.stats().TRADE_SALES.inc(price);
	}
	
	@Override
	public int forSale(RESOURCE res) {
		if (SETT.ENTRY().isClosed())
			return 0;
		
		return (amount.get(res) - (promised.get(res)));
			
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






	

}
