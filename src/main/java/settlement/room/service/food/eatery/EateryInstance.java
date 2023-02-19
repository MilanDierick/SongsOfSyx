package settlement.room.service.food.eatery;

import init.resources.Edible;
import init.resources.RESOURCES;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.misc.job.*;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

final class EateryInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER, ROOM_SERVICER{

	private final static long serialVersionUID = -7063521835843676015l;
	
	boolean autoE;
	private long[] pdata;
	private final int[] amounts = new int[RESOURCES.EDI().all().size()];
	private final int[] jobReserved = new int[RESOURCES.EDI().all().size()];
	private int serviceReserved = 0;
	private int amountTotal = 0;
	final int maxAmount;
	private final JobIterator jobs;
	private long fetchMask = RESOURCES.EDI().mask;
	private long useMask = 0;
	final RoomServiceInstance service;

	EateryInstance(ROOM_EATERY p, TmpArea area, RoomInit init) {
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
			if (is(c) && blueprintI().constructor.isCrate(c.x(), c.y()))
				m++;
		}
		pdata = blueprintI().industry.makeData();
		service = new RoomServiceInstance(m, blueprintI().service);
		employees().maxSet(m);
		employees().neededSet((int) Math.ceil(blueprintI().constructor.workers.get(this)));
		activate();
		
		for (Edible e : RESOURCES.EDI().all()) {
			if (e.resource.edibleServe)
				useMask |= e.resource.bit;
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
	
	public int amount(Edible e) {
		return amounts[e.index()];
	}
	
	public int amountTotal() {
		return amountTotal;
	}
	
	public int jobReserved(Edible e) {
		return jobReserved[e.index()];
	}
	
	public int serviceReserved() {
		return serviceReserved;
	}
	
	public long fetchMask() {
		return fetchMask & useMask;
	}
	
	public boolean uses(Edible e) {
		return (useMask & e.resource.bit) != 0;
	}
	
	public void usesToggle(Edible e) {
		useMask ^= e.resource.bit;
	}
	
	void jobTally(Edible e, int dReserved, int dAmount) {
		amounts[e.index()] += dAmount;
		amountTotal += dAmount;
		blueprintI().total += dAmount;
		blueprintI().amounts[e.index()] += dAmount;
		jobReserved[e.index()] += dReserved;
		setMask(e);
	}
	
	void serviceTally(int dReserved) {
		serviceReserved += dReserved;
	}
	
	void consume(Edible e, int amount, int tx, int ty) {
		jobTally(e, 0, -amount);
		blueprintI().crate.service(tx, ty).check();
	}
	
	private void setMask(Edible e) {
		if (amounts[e.index()] + jobReserved[e.index()]*4 <= maxAmount-4) {
			fetchMask |= e.resource.bit;
			jobs.searchAgainWithoutResources();
		}else {
			fetchMask &= ~e.resource.bit;
			if (fetchMask == 0) {
				jobs.dontSearch();
			}
		}
	}
	
	@Override
	protected void dispose() {
		
		int amI = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				int a = amounts[amI];
				if (a > 0)
					SETT.THINGS().resources.create(c, RESOURCES.EDI().all().get(amI).resource, a);
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
	public ROOM_EATERY blueprintI() {
		return (ROOM_EATERY) blueprint();
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
	public long[] productionData() {
		return pdata;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industry;
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 1);
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}

}