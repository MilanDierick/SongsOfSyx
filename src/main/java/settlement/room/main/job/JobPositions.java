package settlement.room.main.job;

import java.io.Serializable;

import init.resources.RESOURCE;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;

public abstract class JobPositions<T extends RoomInstance> implements JOB_MANAGER, Serializable {

	protected final T ins;
	private boolean hasSearchedAll;
	private final ArrayCooShort coos;
	private int searchI = 0;
	private long resNotFound;
	private long resSearchmask;
	private boolean alwaysNew;
	private final static long serialVersionUID = 2358456270243399328l;

	public JobPositions(T ins) {
		this.ins = ins;

		int amount = 0;

		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && isAndInit(c.x(), c.y()))
				amount++;
		}

		coos = new ArrayCooShort(amount);
		int i = 0;

		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && initIs(c.x(), c.y())) {
				coos.set(i).set(c);
				i++;
			}
		}

		if (i != amount)
			throw new RuntimeException(i + " " + amount);

	}

	protected abstract boolean isAndInit(int tx, int ty);

	protected boolean initIs(int tx, int ty) {
		return get(tx, ty) != null;
	}

	protected abstract SETT_JOB get(int tx, int ty);

	@Override
	public SETT_JOB reportResourceMissing(long resMask, int jx, int jy) {
		resNotFound |= resMask;
		resSearchmask |= resMask;
		return getReservableJob();
	}
	
	@Override
	public void reportResourceFound(long res) {
		resNotFound &= ~res;
		resSearchmask &= ~res;
	}
	
	@Override
	public boolean resourceShouldSearch(RESOURCE res) {
		return (resSearchmask & res.bit) == 0;
	}
	
	@Override
	public boolean resourceReachable(RESOURCE res) {
		return (resNotFound & res.bit) == 0;
	}

	private boolean resourceCheck(SETT_JOB j) {
		long f = j.jobResourceBitToFetch();
		if (f == 0) {
			return true;
		}
//		if (!PATH().finders.resource.normal.has(f))
//			return false;

		return (f & ~resSearchmask) != 0;

	}

	private SETT_JOB getReservableJob() {

		if (hasSearchedAll)
			return null;
		
		if (alwaysNew)
			searchI++;
		
		for (int i = 0; i < coos.size(); i++) {
			if (searchI >= coos.size())
				searchI = 0;
			coos.set(searchI);

			SETT_JOB j = reservable(coos.get().x(), coos.get().y());
			if (j != null && resourceCheck(j))
				return j;
			searchI++;
		}
		
		hasSearchedAll = true;
		return null;
	}
	
	private SETT_JOB reservable(int tx, int ty) {
		SETT_JOB j = get(tx, ty);
		if (j == null)
			return null;
		if (!j.jobReserveCanBe())
			return null;
		return j;
	}
	
	@Override
	public SETT_JOB getReservableJob(COORDINATE pref) {
		
		if (pref == null || alwaysNew)
			return getReservableJob();
		
		SETT_JOB j = reservable(pref.x(), pref.y());

		if (j == null)
			return getReservableJob();

		if (!resourceCheck(j)) {
			return getReservableJob();
		}
		return j;
	}

	@Override
	public SETT_JOB getJob(COORDINATE c) {
		if (ins.is(c)) {
			return get(c.x(), c.y());
		}
		return null;
	}

	public void searchAgain() {
		resSearchmask = 0;
		this.hasSearchedAll = false;
	}
	
	@Override
	public void resetResourceSearch() {
		resSearchmask = 0;
		resNotFound = 0;
		
	}
	
	public void searchAgainButDontReset() {
		this.hasSearchedAll = false;
	}

	public void stopSearching() {
		this.hasSearchedAll = true;
	}

	public boolean isSearching() {
		return !hasSearchedAll;
	}

	public int size() {
		return coos.size();
	}

	public COORDINATE get(int i) {
		return coos.set(i);
	}

	public void setAlwaysNew() {
		alwaysNew = true;
	}
	
	public void randomize() {
		for (int i = 0; i < coos.size(); i++) {
			coos.set(i);
			int x = coos.get().x();
			int y = coos.get().y();
			int d = RND.rInt(coos.size());
			coos.set(d);
			int x2 = coos.get().x();
			int y2 = coos.get().y();

			coos.set(i);
			coos.get().set(x2, y2);

			coos.set(d);
			coos.get().set(x, y);

		}
	}
}