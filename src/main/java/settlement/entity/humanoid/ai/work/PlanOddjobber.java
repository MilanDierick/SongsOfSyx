package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.*;

import game.GAME;
import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.entity.humanoid.ai.work.SubWork.SubWorkTool;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.TILE_STORAGE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;

class PlanOddjobber extends PlanWork {
	
	private final Multi clear = new Multi();
	private final Regular regular = new Regular();
	private final Crate crate = new Crate();
	
	PlanOddjobber() {
		
	}

	private static int distance,crateX,crateY;
	private static Job sjob;
	
	public static boolean hasOddjob(Humanoid a) {
	
		if (PATH().finders.job.hasAny(a.tc().x(), a.tc().y()))
			return true;
		if (PATH().finders.jobStore.hasStoreJob(a.tc().x(), a.tc().y()))
			return true;
		return false;
	}
	
	protected AiPlanActivation activateOddjobber(Humanoid a, AIManager d) {
		distance = Integer.MAX_VALUE;
		sjob = ajacent(a, d);
		if (sjob == null)
			sjob = PATH().finders.job.find(a.tc().x(), a.tc().y(), distance, d.path);
		crateX = a.tc().x();
		crateY = a.tc().y();
		return super.activate(a, d);
	}
	
	protected AiPlanActivation activateOddjobber(Humanoid a, AIManager d, int dist) {
		distance = dist;
		sjob = ajacent(a, d);
		if (sjob == null)
			sjob = PATH().finders.job.find(a.tc().x(), a.tc().y(), distance, d.path);
		crateX = a.tc().x();
		crateY = a.tc().y();
		return super.activate(a, d);
	}
	
	
	protected AiPlanActivation activateHelpOut(Humanoid a, AIManager d) {
		int wx = work(a).mX();
		int wy = work(a).mY();
		crateX = wx;
		crateY = wy;
		distance = 100;
		d.path.clear();
		if (PATH().connectivity.is(wx, wy)) {
			sjob = PATH().finders.job.find(wx, wy, distance, null);
			return super.activate(a, d);
		}
		for (DIR dir : DIR.ORTHO) {
			if (PATH().connectivity.is(wx, wy, dir)) {
				sjob = PATH().finders.job.find(wx+dir.x(), wy+dir.y(), distance, null);
				return super.activate(a, d);
			}
		}
		GAME.Notify(work(a).mX() + " " + work(a).mY());
		return null;
	}
	
	private Job ajacent(Humanoid a, AIManager d) {
		
		if (d.planTile.tileDistanceTo(a.tc()) == 1) {
			Job j = SETT.JOBS().getter.get(d.planTile);
			if (j != null && j.jobReserveCanBe() && j.jobResourceBitToFetch() == 0)
				return j;
		}
		
		for (DIR dd : DIR.ORTHO) {
			Job j = SETT.JOBS().getter.get(a.tc(), dd);
			if (j != null && j.jobReserveCanBe() && j.jobResourceBitToFetch() == 0)
				return j;
		}
		return null;
		
	}
	
	protected AiPlanActivation activateWorker(Humanoid a, AIManager d, int sx, int sy, int radius) {

		distance = Integer.MAX_VALUE;
		crateX = sx;
		crateY = sy;
		sjob = PATH().finders.job.findWeird(sx, sy,radius);
		return super.activate(a, d);
	}
	
	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {
		throw new RuntimeException();
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		Job j = sjob;
		
		
		if (j != null) {
			d.planTile.set(j.jobCoo());
			AISubActivation s = clear.init(a, d, j);
			if (s != null) {
				return s;
			}
			j = SETT.JOBS().getter.get(d.planTile);
			if (j != null && j.jobReserveCanBe()) {
				s = regular.init(a, d, j);
				if (s != null)
					return s;
			}
		}
		TILE_STORAGE c = PATH().finders.jobStore.find(crateX, crateY, 100);
		if (c != null)
			return crate.init(a, d, c);
		
		return null;

	}
	

	
	private boolean isClearJob(Job j, Humanoid a) {
		if (j == null)
			return false;
		if (j.jobPerformTime(a) == 0) {
			if (j.jobResourceBitToFetch() == 0)
				return false;
		}
		return j.res() != null && j.jobResourcesNeeded() > 0;
	}
	
	private final class Multi {
		
		protected AISubActivation init(Humanoid a, AIManager d, Job j) {
		
			if (!isClearJob(j, a))
				return null;
			d.planTile.set(j.jobCoo());
			RESOURCE res = j.jobResourcesNeeded() > 0 ? j.res() : null;
			boolean needsNow = true;
			
			d.planByte1 = 0;
			int extraRes = 0;
			
			for (int di = 0; di < dirs.length; di++) {
				DIR dir = dirs[di];
				j = SETT.JOBS().getter.get(d.planTile, dir);
				if (isClearJob(j,a) && j.jobReserveCanBe() && j.res() == res) {
					d.planByte1 |= 1 << di;
					extraRes += j.jobResourcesNeeded();
					needsNow &= j.jobResourceBitToFetch() != 0;
					j.jobReserve(j.jobResourceBitToFetch() > 0 ? j.res() : null);
					if (extraRes >= WorkAbs.maxCarry)
						break;
				}
			}
			if (res != null) {
				AISubActivation s = fetch.activate(a, d, res.bit, CLAMP.i(extraRes, 0, WorkAbs.maxCarry), needsNow ? Integer.MAX_VALUE : distance, true, true);
				if (s != null)
					return s;
			}
			d.planByte2 = res == null ? -1 : res.bIndex();
			if (needsNow) {
				cancel(a, d, res);
				return null;
			}
			cancelResourceJobs(a, d);
			d.planByte2 = -1;
			return walk2Job.set(a, d);

		}
		
		private final DIR[] dirs = new DIR[] {
			DIR.C,
			DIR.N,
			DIR.W,
			DIR.S,
			DIR.E,
			DIR.SW,
			DIR.SE,
		};
		
		private final AIPlanResourceMany fetch = new AIPlanResourceMany(PlanOddjobber.this, 32) {
			
			@Override
			public AISubActivation next(Humanoid a, AIManager d) {
				d.planByte2 = d.resourceCarried().bIndex();
				return walk2Job.set(a, d);
			}
			
			@Override
			public void cancel(Humanoid a, AIManager d) {
				d.planByte2 = this.resource(a, d).bIndex();
				Multi.this.cancel(a, d, this.resource(a, d));
			}
		};
		
		private RESOURCE resource(AIManager d) {
			return d.planByte2 >= 0 ? RESOURCES.ALL().get(d.planByte2) : null;
		}
		
		private Job nextJob(Humanoid a, AIManager d) {
			if (d.planByte1 == 0)
				return null;
			for (int di = 0; di < dirs.length; di++) {
				DIR dir = dirs[di];
				if ((d.planByte1 & (1 << di)) != 0) {
					Job j = SETT.JOBS().getter.get(d.planTile, dir);
					if (canDoJob(a, d, j)) {
						return j;
					}
					cancelJob(a, d, j, resource(d));
					j = SETT.JOBS().getter.get(d.planTile, dir);
					d.planByte1 &= ~(1 << di);
				}
			}
			return null;
		}
		
		private Job cancelResourceJobs(Humanoid a, AIManager d) {
			if (d.planByte1 == 0)
				return null;
			for (int di = 0; di < dirs.length; di++) {
				DIR dir = dirs[di];
				if ((d.planByte1 & (1 << di)) != 0) {
					Job j = SETT.JOBS().getter.get(d.planTile, dir);
					if (j != null && j.jobResourceBitToFetch() != 0) {
						cancelJob(a, d, j, resource(d));
						d.planByte1 &= ~(1 << di);
					}
					
				}
			}
			return null;
		}
		
		private boolean canDoJob(Humanoid a, AIManager d, Job j) {
			if (j == null)
				return false;
			if (isClearJob(j, a)) {
				if (j.jobReservedIs(resource(d)) && d.resourceCarried() == resource(d)) {
					return true;
				}else if (j.jobReservedIs(null))
					return true;
			}
			return false;
		}
		
		private void cancelJob(Humanoid a, AIManager d, Job j, RESOURCE res) {
			
			if (j == null)
				return;
			if (j.jobReservedIs(res))
				j.jobReserveCancel(res);
			else if (j.jobReservedIs(null))
				j.jobReserveCancel(null);
			else
				j.jobReserveCancel(null);
		}
		
		private void cancel(Humanoid a, AIManager d, RESOURCE res) {
			Job j = nextJob(a, d);
			
			while(j != null) {
				cancelJob(a, d, j, res);
				j = nextJob(a, d);
			}
			
			d.resourceDrop(a);
		}
		
		
		private final Resumer walk2Job = new Resumer("") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s;
				Job j = nextJob(a, d);
				
				if (j == null) {
					cancel(a, d, resource(d));
					return null;
				}
				
				int dx = j.jobCoo().x();
				int dy = j.jobCoo().y();
				
				if (d.path.isSuccessful() && d.path.destX() == dx && d.path.destY() == dy &&
						a.physics.tileC().isSameAs(d.path)) {
					s = AI.SUBS().walkTo.path(a, d);
				} else {
					s = AI.SUBS().walkTo.coo(a, d, dx, dy);
				}

				if (s == null) {
					cancel(a, d, resource(d));
					return null;
				}
				return s;
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				
				Job j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (!canDoJob(a, d, j)) {
					Multi.this.cancel(a, d, resource(d));
					return null; 
				}

				if (j.jobPerformTime(a) == 0) {
					
					
					
					int am = CLAMP.i(d.resourceA(), 0, j.jobResourcesNeeded());
					RESOURCE res = j.jobPerform(a, d.resourceCarried(), am);
					d.resourceAInc(-am);
					if (res != null) {
						cancel(a, d, resource(d));
						d.resourceDrop(a);
						d.resourceCarriedSet(res);
						return dumpResource.set(a, d);
					}
					j = JOBS().getter.get(d.path.destX(), d.path.destY());
					if (isClearJob(j, a) && j.jobReserveCanBe()) {
						
						if (j.jobResourceBitToFetch() == 0)
							j.jobReserve(null);
						else if (d.resourceCarried() == resource(d) && resource(d) != null && (j.jobResourceBitToFetch() & d.resourceCarried().bit) != 0)
							j.jobReserve(d.resourceCarried());
					}

					return setAction(a, d);
					
				}
				AISubActivation s = work.set(a, d);
				return s;
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				Job j = JOBS().getter.get(d.path.destX(), d.path.destY());
				return j != null;
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				cancel(a, d, resource(d));
			}
			
			@Override
			public void name(Humanoid a, AIManager d, Str string) {
				SETT_JOB j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (j == null || j.jobName() == null) {
					super.name(a, d, string);
				} else
					string.add(j.jobName());

			}
			
			

		};

		
		private final Resumer work = new Resumer("") {

			private SubWorkTool sub = new SubWorkTool() {

				@Override
				public SETT_JOB getJob(Humanoid a, AIManager d) {
					return JOBS().getter.get(d.path.destX(), d.path.destY());
				}
			};

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				Job j = JOBS().getter.get(d.path.destX(), d.path.destY());
				j.jobStartPerforming();
				debug(a, d);
				return sub.activate(a, d, j);
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Job j = JOBS().getter.get(d.path.destX(), d.path.destY());
				RESOURCE produced = null;
				if (j.jobReservedIs(null)) {
					produced = j.jobPerform(a, null, 0);
				}else if(j.jobReservedIs(d.resourceCarried())) {
					int am = CLAMP.i(d.resourceA(), 0, j.jobResourcesNeeded());
					produced = j.jobPerform(a, d.resourceCarried(), am);
					d.resourceAInc(-am);
				}else {
					cancel(a, d, resource(d));
					return null;
				}
				
				j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (isClearJob(j, a) && j.jobReserveCanBe()) {
					if (j.jobResourceBitToFetch() == 0)
						j.jobReserve(null);
					else if (d.resourceCarried() != null && (j.jobResourceBitToFetch() & d.resourceCarried().bit) != 0) {
						j.jobReserve(d.resourceCarried());
					}
				}
				
				if (produced != null) {
					THINGS().resources.create(a.physics.tileC(), produced, 1);
				}
				
				return walk2Job.set(a, d);
			}

			
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return walk2Job.con(a, d);
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				cancel(a, d, resource(d));
			}

			@Override
			public void name(Humanoid a, AIManager d, Str string) {
				SETT_JOB j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (j == null || j.jobName() == null) {
					super.name(a, d, string);
				} else
					string.add(j.jobName());

			}
		};
		

		
	}
	
	private final class Regular {
		
		protected AISubActivation init(Humanoid a, AIManager d, Job j) {
		
			d.planTile.set(j.jobCoo());
			d.planByte1 = -1;
			d.resourceDrop(a);
			if (j.jobResourceBitToFetch() != 0) {
				AISubActivation s = fetch.activate(a, d, j.jobResourceBitToFetch(), CLAMP.i(j.jobResourcesNeeded(), 0, WorkAbs.maxCarry), Integer.MAX_VALUE, true, true);
				if (s != null) {
					j = SETT.JOBS().getter.get(d.planTile);
					j.jobReserve(fetch.resource(a, d));
					return s;
				}
				return null;
			}else {
				j = SETT.JOBS().getter.get(d.planTile);
				j.jobReserve(null);
				return walk2Job.set(a, d);
			}
		}
		
		private RESOURCE resource(AIManager d) {
			return d.planByte1 >= 0 ? RESOURCES.ALL().get(d.planByte1) : null;
		}
		
		private final AIPlanResourceMany fetch = new AIPlanResourceMany(PlanOddjobber.this, 32) {
			
			@Override
			public AISubActivation next(Humanoid a, AIManager d) {
				return walk2Job.set(a, d);
			}
			
			@Override
			public void cancel(Humanoid a, AIManager d) {
				d.planByte1 = resource(a, d).bIndex();
				Regular.this.cancel(a, d, resource(a, d));
			}
		};
		
		private final Resumer walk2Job = new Resumer("") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s;
				Job j = SETT.JOBS().getter.get(d.planTile);
				
				if (j == null) {
					cancel(a, d, resource(d));
					return null;
				}
				
				int dx = j.jobCoo().x();
				int dy = j.jobCoo().y();
				
				if (d.path.isSuccessful() && d.path.destX() == dx && d.path.destY() == dy &&
						a.physics.tileC().isSameAs(d.path)) {
					s = AI.SUBS().walkTo.path(a, d);
				} else {
					s = AI.SUBS().walkTo.coo(a, d, dx, dy);
				}

				if (s == null) {
					cancel(a, d, resource(d));
					return null;
				}
				return s;
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				
				Job j = SETT.JOBS().getter.get(d.planTile);
				
				if (isDumpJob(a, d, j)) {
					if (handleDumpJob(a, d, j)) {
						return dumpResource.set(a, d);
					}
					
					return null;
				}
				
				if (j.jobPerformTime(a) == 0) {
					
						
					int am = CLAMP.i(d.resourceA(), 0, j.jobResourcesNeeded());
					RESOURCE res = j.jobPerform(a, d.resourceCarried(), am);
					d.resourceAInc(-am);
					if (res != null) {
						d.resourceDrop(a);
						d.resourceCarriedSet(res);
						return dumpResource.set(a, d);
					}
					return null;
				}
				return work.set(a, d);
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				Job j = SETT.JOBS().getter.get(d.planTile);
				if (j == null)
					return false;
				if (!j.jobReservedIs(null) && !j.jobReservedIs(d.resourceCarried()))
					return false;
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				cancel(a, d, resource(d));
			}
			
			@Override
			public void name(Humanoid a, AIManager d, Str string) {
				SETT_JOB j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (j == null || j.jobName() == null) {
					super.name(a, d, string);
				} else
					string.add(j.jobName());

			}

		};

		
		private final Resumer work = new Resumer("") {

			private SubWorkTool sub = new SubWorkTool() {

				@Override
				public SETT_JOB getJob(Humanoid a, AIManager d) {
					return SETT.JOBS().getter.get(d.planTile);
				}
			};

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				SETT_JOB j = SETT.JOBS().getter.get(d.planTile);
				j.jobStartPerforming();
				return sub.activate(a, d, j);
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				Job j = SETT.JOBS().getter.get(d.planTile);
				RESOURCE produced = null;
				if (j.jobReservedIs(null)) {
					produced = j.jobPerform(a, null, 0);
				}else if(j.jobReservedIs(d.resourceCarried())) {
					int am = CLAMP.i(d.resourceA(), 0, j.jobResourcesNeeded());
					produced = j.jobPerform(a, d.resourceCarried(), am);
					d.resourceAInc(-am);
				}else {
					cancel(a, d, resource(d));
					return null;
				}
				if (produced != null) {
					d.resourceCarriedSet(produced);
					return dumpResource.set(a, d);
				}

				if (AI.modules().work.moduleCanContinue(a, d)) {
					j = SETT.JOBS().getter.get(d.planTile);
					if (j != null && j.jobReserveCanBe()) {
						if (j.jobResourcesNeeded() == 0)
							j.jobReserve(null);
						else if (d.resourceCarried() != null && d.resourceCarried().isInMask(j.jobResourceBitToFetch()))
							j.jobReserve(d.resourceCarried());
						else
							return null;
						return walk2Job.set(a, d);
					}
				}
				
				
				return null;
			}

			
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return walk2Job.con(a, d);
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				cancel(a, d, resource(d));
			}

			@Override
			public void name(Humanoid a, AIManager d, Str string) {
				SETT_JOB j = JOBS().getter.get(d.path.destX(), d.path.destY());
				if (j == null || j.jobName() == null) {
					super.name(a, d, string);
				} else
					string.add(j.jobName());

			}
		};
		
		private void cancel(Humanoid a, AIManager d, RESOURCE res) {
			Job j = SETT.JOBS().getter.get(d.planTile);
			
			if(j != null) {
				if (j.jobReservedIs(res))
					j.jobReserveCancel(res);
				if (j.jobReservedIs(null))
					j.jobReserveCancel(null);
			}
			
			d.resourceDrop(a);
		}
		
	}
	
	private final class Crate {
		
		protected AISubActivation init(Humanoid a, AIManager d, TILE_STORAGE crate) {
			d.planTile.set(crate);
			RESOURCE r = crate.resource();
			
			int am = CLAMP.i(WorkAbs.maxCarry, 0, crate.storageReservable());
			
			AISubActivation sub = fetch.activate(a, d, r.bit, am, Integer.MAX_VALUE, false, false);
			
			if (sub != null) {
				crate = ROOMS().map.get(d.planTile).storage(d.planTile.x(), d.planTile.y());
				crate.storageReserve(am);

				return sub;
			}
			

			return null;

		}
		

		
		private final AIPlanResourceMany fetch = new AIPlanResourceMany(PlanOddjobber.this, 32) {
			
			@Override
			public AISubActivation next(Humanoid a, AIManager d) {
				return stockpileReturn.set(a, d);
			}
			
			@Override
			public void cancel(Humanoid a, AIManager d) {
				stockpileReturn.can(a, d);
			}
		};

		private final Resumer stockpileReturn = new Resumer("bringing resource to stockpile") {

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.coo(a, d, d.planTile);
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				TILE_STORAGE c = PATH().finders.storage.getter.get(d.planTile);
				int am = CLAMP.i(d.resourceA(), 0, c.storageReserved());
				c.storageDeposit(am);
				d.resourceAInc(-am);
				d.resourceDrop(a);
				int extra = fetch.target(a, d) - am;
				extra = CLAMP.i(extra, 0, c.storageReserved());
				if (extra > 0)
					c.storageUnreserve(extra);
				return null;
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				TILE_STORAGE c = PATH().finders.storage.getter.get(d.planTile);
				if (c == null)
					return false;
				if (c.storageReserved() <= 0 || c.resource() != fetch.resource(a, d))
					return false;
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				TILE_STORAGE c = PATH().finders.storage.getter.get(d.planTile);
				if (c != null && c.resource() == fetch.resource(a, d)) {
					int am = CLAMP.i(fetch.target(a, d), 0, c.storageReserved());
					c.storageUnreserve(am);
					
				}
				d.resourceDrop(a);
			}
		};
		

		
	}
	
	private boolean isDumpJob(Humanoid a, AIManager d, Job j) {
		if (j == null)
			return false;
		if (j.jobPerformTime(a) != 0)
			return false;
		if (j.jobResourceBitToFetch() != 0)
			return false;
		return true;
	}
	
	private boolean handleDumpJob(Humanoid a, AIManager d, Job j) {
		
		if (!isDumpJob(a, d, j))
			return false;
			
		d.resourceDrop(a);
		
		while(d.resourceA() < WorkAbs.maxCarry && isDumpJob(a, d, j)) {
			if (!j.jobReservedIs(null) && j.jobReserveCanBe()) {
				j.jobReserve(null);
			}
			if (j.jobReservedIs(null)) {
				RESOURCE r = j.jobPerform(a, null, 0);
				if (d.resourceCarried() == null) {
					d.resourceCarriedSet(r);
				}else if (d.resourceCarried() != r) {
					THINGS().resources.create(j.jobCoo(), r, 1);
					return d.resourceA() > 0;
				}else {
					d.resourceAInc(1);
				}
			}else {
				return d.resourceA() > 0;
			}
			j = JOBS().getter.get(j.jobCoo());
		}
		
		return d.resourceA() > 0;
	}
	
	private final Resumer dumpResource = new Resumer("dumping resource") {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (d.resourceCarried() == null)
				throw new RuntimeException();
			if (PATH().finders.resourceDump.isTile(a.tc().x(), a.tc().y())) {
				SETT.THINGS().resources.create(a.tc(), d.resourceCarried(), 1);
				d.resourceCarriedSet(null);
				return null;
			}
			
			if (PATH().finders.resourceDump.find(a.physics.tileC(), d.path)) {
				return AI.SUBS().walkTo.path(a, d);
			} else {
				FACTIONS.player().res().outSpoilt.inc(d.resourceCarried(), d.resourceA());
				//THINGS().resources.createPrecise(a.physics.tileC().x(), a.physics.tileC().y(), d.resourceCarried(), 1);
				d.resourceCarriedSet(null);
				return null;
			}
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			THINGS().resources.createPrecise(a.physics.tileC().x(), a.physics.tileC().y(), d.resourceCarried(), d.resourceA());
			d.resourceCarriedSet(null);
			return null;
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceDrop(a);
		}
	};
	
	
	

	
	
}