package settlement.room.infra.stockpile;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.D;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.*;
import util.data.DOUBLE_O;
import util.info.INFO;
import util.statistics.HistoryResource;

public final class StockpileTally{
	
	{
		D.gInit(this);
	}
	
	private final HistoryResource amounts = new HistoryResource(64, TIME.seasons(), true) {
		
		private final INFO info = new INFO(
			D.g("Stored"),
			D.g("StoredD", "How much is stored in your warehouses")
				);
		
		@Override
		public INFO info() {
			return info;
		};
	};
	private final HistoryResource amountDay = new HistoryResource(STATS.DAYS_SAVED, TIME.days(), true) {
		
		@Override
		public INFO info() {
			return amounts.info();
		};
	};
	private final Data crateDesignations = new Data();
	private final Data amountTotal = new Data();
	private final Data amountReservable = new Data();
	private final Data amountFetch = new Data();
//	private final int[] spaceTotal = new int[RESOURCES.ALL().size()];
//	private final int[] spaceReservable = new int[RESOURCES.ALL().size()];
	private final Data spaceReserved = new Data();
	private final Data space = new Data();
	private final RBITImp spaceMask = new RBITImp();
	private long totalAmount = 0;
	private long totalSpace = 0;
	
	public static final DOUBLE_O<RESOURCE> usage = new DOUBLE_O<RESOURCE>() {

		@Override
		public double getD(RESOURCE t) {
			int space = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(t);
			if (space == 0)
				return 1.0;
			double used = (int) SETT.ROOMS().STOCKPILE.tally().amountReservable(t);
			
			return used/space;
		}		
	};
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			amounts.save(file);
			amountDay.save(file);
			crateDesignations.save(file);;
			amountTotal.save(file);
			amountReservable.save(file);
			amountFetch.save(file);
			spaceReserved.save(file);
			space.save(file);
			spaceMask.save(file);
			file.l(totalAmount);
			file.l(totalSpace);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			amounts.load(file);
			amountDay.load(file);
			crateDesignations.load(file);;
			amountTotal.load(file);
			amountReservable.load(file);
			amountFetch.load(file);
			spaceReserved.load(file);
			space.load(file);
			spaceMask.load(file);
			totalAmount = file.l();
			totalSpace = file.l();
		}
		
		@Override
		public void clear() {
			amounts.clear();
			amountDay.clear();
			crateDesignations.clear();
			amountTotal.clear();
			amountReservable.clear();
			amountFetch.clear();
			spaceReserved.clear();
			space.clear();
			spaceMask.clear();
			totalAmount = 0;
			totalSpace = 0;
		}
	};
	
	
	public StockpileTally() {
		// TODO Auto-generated constructor stub
	}
	
	void tally(int res, int crates, int amountTot, int amountUnres, int spaceRes, int space, boolean fetch) {
		
		if (check(RESOURCES.ALL().get(res)) != 0)
			debug(RESOURCES.ALL().get(res));
		
		crateDesignations.inc(res, crates);
		amountTotal.inc(res, amountTot);
		totalAmount += amountTot;
		amountReservable.inc(res, amountUnres);
		spaceReserved.inc(res, spaceRes);
		this.space.inc(res, space);
		if (fetch) {
			amountFetch.inc(res, amountTot);
		}
		
		
		
		
		if (spaceReservable(res) == 0) {
			spaceMask.clear(RESOURCES.ALL().get(res));
		}else if(spaceReservable(res) > 0) {
			spaceMask.or(RESOURCES.ALL().get(res));
		}
		
		amounts.set(RESOURCES.ALL().get(res), amountTotal(res));
		amountDay.set(RESOURCES.ALL().get(res), amountTotal(res));
		
		if (check(RESOURCES.ALL().get(res)) != 0) {
			System.err.println(crates + " " + amountTot + " " + amountUnres + " " + spaceRes + " " + space + " " + fetch);
			debug(RESOURCES.ALL().get(res));
		}
		//debug(res);
		
	}
	
	int check(RESOURCE res) {
		if (amountTotal(res) > spaceTotal(res))
			return 1;
		if (amountReservable(res) > amountTotal(res))
			return 2;
		if (amountNGReservable(res) > amountTotal(res))
			return 3;
		if (spaceReservable(res) > spaceTotal(res))
			return 4;
		if (spaceReserved(res) >  spaceTotal(res))
			return 5;
		if (spaceReservable(res) + spaceReserved(res) + amountTotal(res) != spaceTotal(res))
			return 6;
		return 0;
		
	}
	
	void debug(RESOURCE res) {
		System.err.println(res);
		System.err.println("capacity " + spaceTotal(res));
		System.err.println("stored:  "  + amountTotal(res));
		System.err.println("reservalble " + amountReservable(res));
		System.err.println("space reservable " + spaceReservable(res));
		System.err.println("space reserved " + spaceReserved(res));
		
		throw new RuntimeException("" + res + " " + check(res));
	}
	
	public HistoryResource amountsSeason() {
		return amounts;
	}
	
	public HistoryResource amountsDay() {
		return amountDay;
	}
	
	public double load(RESOURCE res) {
		if (spaceTotal(res) == 0)
			return 1;
		return (double)amountTotal(res)/spaceTotal(res);
	}
	
	long crateDesignations(int res) {
		return crateDesignations.get(res);
	}

	public long crateDesignations(RESOURCE res) {
		return crateDesignations.get(res);
	}
	
	int amountTotal(int res) {
		return amountTotal.get(res);
	}
	
	public int amountTotal(RESOURCE res) {
		return amountTotal.get(res);
	}
	
	long amountReservable(int res) {
		return amountReservable.get(res);
	}
	
	/**
	 * Amount stored that is not set to fetch maximum
	 * @param res
	 * @return
	 */
	public long amountNGReservable(RESOURCE res) {
		return amountReservable.get(res) - amountFetch.get(res);
	}
	
	public long amountReservable(RESOURCE res) {
		return amountReservable.get(res);
	}
	
	long spaceTotal(int res) {
		return space.get(res);
	}
	
	public long spaceTotal(RESOURCE res) {
		return space.get(res);
	}
	
	long spaceReservable(int res) {
		return spaceTotal(res)-amountTotal(res)-spaceReserved(res);
	}
	
	public long spaceReservable(RESOURCE res) {
		return spaceReservable(res.bIndex());
	}
	
	public boolean spaceReservable(RBIT mask) {
		return spaceMask.has(mask);
	}
	
	long spaceReserved(int res) {
		return spaceReserved.get(res);
	}
	
	public long spaceReserved(RESOURCE res) {
		return spaceReserved.get(res);
	}
	
	public long totalSpace() {
		return totalSpace;
	}
	
	public long totalAmount() {
		return totalAmount;
	}
	
	private static class Data implements SAVABLE{
		
		private final int[] ams = new int[RESOURCES.ALL().size()+1];
		
		public int get(int ri) {
			return ams[ri];
		}
		
		public int get(RESOURCE res) {
			if (res == null)
				return ams[RESOURCES.ALL().size()];
			return ams[res.index()];
		}
		
		
		public void inc(int ri, int am) {
			ams[ri]+= am;
			ams[RESOURCES.ALL().size()] += am;
		}

		@Override
		public void save(FilePutter file) {
			file.is(ams);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.is(ams);
		}

		@Override
		public void clear() {
			Arrays.fill(ams, 0);
		}
		
	}
	
}
