package settlement.path.finder;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.components.*;
import settlement.path.components.SCompFinder.SCompPatherExister;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.Bitmap1D;
import util.updating.IUpdater;

public final class SFinderJob{

	private final Updater updater = new Updater();

	SFinderJob() {
		
		new TestPath("job", null) {
			@Override
			protected void place(int sx, int sy, SPath p) {
				Job j = find(sx, sy, Integer.MAX_VALUE, p);
				if (j != null && j.resourceCurrentlyNeeded() != null) {
					p.request(sx,  sy, j.jobCoo());
				}
			}
		};
		
	}
	
	void update(float ds) {
		updater.update(ds);
		
		
	}
	
	private FindableDatas d() {
		return SETT.PATH().comps.data;
	}
	
	long resMask = 0;
	long jobMask = 0;
	private Job result;
	
	private final SCompPatherExister wierd = new SCompPatherExister() {
		
		
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			if (d().job.get(c) > 0)
				return true;
			if (SETT.WEATHER().growthRipe.cropsAreRipe() && d().jobHarvest.get(c) > 0)
				return true;
			jobMask |= d().jobs.bits(c);
			resMask |= d().resScattered.bits(c);
			resMask |= d().resCrate.bits(c);
			resMask |= d().resCrateGet.bits(c);
			return (jobMask & resMask) != 0;
		}
		
		@Override
		public void init(SComponentLevel l) {
			resMask = 0;
			jobMask = 0;
		}
	};
	
	private SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			if (d().job.get(c) > 0)
				return true;
			if ((resMask & d().jobs.bits(c)) != 0)
				return true;
			if (SETT.WEATHER().growthRipe.cropsAreRipe() && d().jobHarvest.get(c) > 0)
				return true;
			return false;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			result = JOBS().getter.get(tx, ty);

			if (result == null)
				return false;
			if (!result.jobReserveCanBe())
				return false;
			if (result.resourceCurrentlyNeeded() == null)
				return true;
			if ((result.resourceCurrentlyNeeded().bit & resMask) != 0)
				return true;
			return false;
		}
	};
	
	public Job findWeird(int sx, int sy, int maxDistance) {
		
		if (maxDistance == Integer.MAX_VALUE) {
			resMask = d().resScattered.bits(sx, sy) | d().resCrate.bits(sx, sy) | d().resCrateGet.bits(sx, sy);
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance);
			if (c != null) {
				return result;
			}
			return null;
		}
		
		if (SETT.PATH().comps.pather.exists(sx, sy, wierd, maxDistance)) {
			if (SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance) != null)
				return result;
		}
		return null;
		
	}
	
	public boolean hasAny(int tx, int ty) {
//		return d().job.has(tx, ty) || d().jobs.has(tx, ty, -1l);
		SComponent c = SETT.PATH().comps.superComp.get(tx, ty);
		if (c != null)
			return d().job.has(c) || (d().jobHarvest.has(c) && SETT.WEATHER().growthRipe.cropsAreRipe()) || d().jobs.has(c, d().resScattered.bits(c) | d().resCrate.bits(c) | d().resCrateGet.bits(c));
		return false;
	}
	
	/**
	 * 
	 * @param sx
	 * @param sy
	 * @param maxDistance
	 * @param path
	 * @return null if nothing was found. Else a job. If job resource == null, then the path is not set. Otherwise it is set.
	 */
	public Job find(int sx, int sy, int maxDistance, SPath path) {
		
		if (SETT.PATH().comps.zero.get(sx, sy) == null || !updater.shouldSearch(SETT.PATH().comps.levels.get(1).get(sx, sy) , maxDistance)) {
			return null;
		}
		
		Job j = findWeird(sx, sy, maxDistance);

		if (j != null) {
			if (j.resourceCurrentlyNeeded() == null && path != null) {
				int jx = j.jobCoo().x();
				int jy = j.jobCoo().y();
				if (path.request(sx, sy, jx, jy, false))
					return JOBS().getter.get(jx, jy);
				updater.reportFailure(SETT.PATH().comps.zero.get(sx, sy), maxDistance);
				
				return null;
			}
			return j;
		}
		updater.reportFailure(SETT.PATH().comps.zero.get(sx, sy), maxDistance);
		return null;
	}
	
	public final void report(Job job, int delta) {

		try {
			RESOURCE r = job.resourceCurrentlyNeeded();
			
			
			if (r != null) {
				if (delta == 1) {
					d().jobs.reportPresence(job.jobCoo().x(), job.jobCoo().y(), r);
					
				}else if(delta == -1) {
					d().jobs.reportAbsence(job.jobCoo().x(), job.jobCoo().y(), r);
				}
				return;
			}
			
			FindableDataSingle s = d().job;
			if (job.needsRipe())
				s = d().jobHarvest;
			
			if (delta == 1) {
				s.reportPresence(job.jobCoo().x(), job.jobCoo().y());
			}else if(delta == -1) {
				s.reportAbsence(job.jobCoo().x(), job.jobCoo().y());
			}
		}catch(RuntimeException e) {
			System.err.print(job.jobName() + " " + job.getClass().getSimpleName() + " " + job.jobCoo().x() + " " + job.jobCoo().y());
			throw e;
		}
	
	}
	
	private static class Updater extends IUpdater{

		private byte[] failData = new byte[Short.MAX_VALUE];
		private final Bitmap1D tryMore = new Bitmap1D(Short.MAX_VALUE, false);
		private final Bits failDistance = new Bits(0b11110000);
		private final Bits maxDistance = new Bits(0b00001111);
		
		public Updater() {
			super(Short.MAX_VALUE, 10);
			
		}

		@Override
		protected void update(int i, double timeSinceLast) {
			
			int d = failData[i];
			d = failDistance.set(d, 0);
			if (tryMore.get(i)) {
				
				tryMore.setFalse(i);
				d = maxDistance.inc(d, 1);
				
			}
			failData[i] = (byte) d;
		}
		
		private boolean shouldSearch(SComponent s, int distance) {
			if (s == null || s.index() >= failData.length)
				return true;
			int d = failData[s.index()];
			int fd = (64 << failDistance.get(d));
			if (distance >= fd) {
				return true;
			}
			return false;
		}
		
//		int getDistance(Component s, int distance) {
//			int d = (64<< maxDistance.get(failData[s.index]));
//			if (distance >= d) {
//				return d;
//			}
//			return distance;
//		}
		
		void reportFailure(SComponent s, int distance) {
			if (s.index() >= failData.length)
				return;
			
			int d = failData[s.index()];
			int di = (64 << maxDistance.get(d));
			if (distance >= di) {
				tryMore.set(s.index(), true);
			}
			
			int fd = (64 << failDistance.get(d));
			if (distance > fd) {
				d = failDistance.inc(d, 1);
				failData[s.index()] = (byte) d;
			}
			
			
		}
		
		
	}


}
