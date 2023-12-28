package settlement.room.service.market;

import init.race.RACES;
import init.race.RaceResources.RaceResource;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import settlement.main.SETT;
import settlement.misc.job.*;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class MarketInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_SERVICER{

	private final static long serialVersionUID = -7063521835843676015l;
	
	boolean autoE;
	private final int[] amounts = new int[RACES.res().ALL.size()];
	private final int[] jobReserved = new int[RACES.res().ALL.size()];
	private int amountTotal = 0;
	final int maxAmount;
	private final JobIterator jobs;
	private final RBITImp fetchMask = new RBITImp().clearSet(RACES.res().BIT);
	private final RBITImp useMask = new RBITImp().clearSet(RACES.res().BIT);
	final RoomServiceInstance service;

	MarketInstance(ROOM_MARKET p, TmpArea area, RoomInit init) {
		super(p, area, init);

		maxAmount = (int) blueprintI().constructor.storage.get(this);
		jobs = new JobIterator(this) {
			private static final long serialVersionUID = 1L;

			@Override
			protected SETT_JOB init(int tx, int ty) {
				return blueprintI().crate.job(tx, ty);
			}
		};
		jobs.setAlwaysNewJob();
		
		int m = 0;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().constructor.isCrate(c.x(), c.y())) {
				m++;
			}
		}
		service = new RoomServiceInstance(m, blueprintI().service);
		employees().maxSet(m);
		employees().neededSet((int) Math.ceil(blueprintI().constructor.workers.get(this)));
		activate();
		
		for (RaceResource rr : RACES.res().ALL) {
			setMask(rr);
		}
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().crate.init(c.x(), c.y());
			}
		}
		
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		super.render(r, shadowBatch, it);
		it.lit();
		return false;
	}

	@Override
	protected void updateAction(double ds, boolean day, int daycount) {
		if (day)
			service.updateDay();
		jobs.searchAgain();
		if (!active() || employees().employed() <= 0)
			return;
		
	}
	
	public int amount(RaceResource e) {
		return amounts[e.index()];
	}
	
	public int amountTotal() {
		return amountTotal;
	}
	
	public int jobReserved(RaceResource e) {
		return jobReserved[e.index()];
	}
	
	
	public RBIT fetchMask() {
		return fetchMask;
	}
	
	public boolean uses(RaceResource e) {
		return useMask.has(e.res);
	}
	
	public void usesToggle(RaceResource e) {
		useMask.toggle(e.res);
		dump(e);
		setMask(e);
		
	}
	
	private void dump(RaceResource e) {
		if (!useMask.has(e.res)) {
			int am = amounts[e.index()];
			amounts[e.index()] = 0;
			amountTotal -= am;
			blueprintI().total -= am;
			blueprintI().amounts[e.res.index()] -= am;
			if (am > 0) {
				SETT.THINGS().resources.create(mX(), mY(), e.res, am);
			}
		}
	}
	
	void jobTally(RaceResource e, int dReserved, int dAmount) {
		amounts[e.index()] += dAmount;
		amountTotal += dAmount;
		blueprintI().total += dAmount;
		blueprintI().amounts[e.index()] += dAmount;
		jobReserved[e.index()] += dReserved;
		dump(e);
		setMask(e);
	}
	
	void consume(RaceResource e, int amount, int tx, int ty) {
		jobTally(e, 0, -amount);
	}
	
	private void setMask(RaceResource e) {
		if (amounts[e.index()] + jobReserved[e.index()]*4 <= maxAmount-4) {
			fetchMask.or(e.res);
		}else {
			fetchMask.clear(e.res);
		}
		fetchMask.and(useMask);
		if (fetchMask.isClear()) {
			jobs.dontSearch();
		}else {
			jobs.searchAgainWithoutResources();
		}
	}
	
	@Override
	protected void dispose() {
		
		int amI = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				int a = amounts[amI];
				if (a > 0)
					SETT.THINGS().resources.create(c, RACES.res().ALL.get(amI).res, a);
				amI++;
				if (amI == amounts.length)
					return;
			}
		}
		
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().crate.dispose(c.x(), c.y());
			}
		}
		service.dispose(blueprintI().service);
		
	}
	
	@Override
	public ROOM_MARKET blueprintI() {
		return (ROOM_MARKET) blueprint();
	}
	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return null;
	}
	
	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return null;
	}

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 1);
	}

}