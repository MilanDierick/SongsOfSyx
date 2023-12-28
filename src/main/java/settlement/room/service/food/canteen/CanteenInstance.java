package settlement.room.service.food.canteen;

import init.resources.*;
import init.resources.RBIT.RBITImp;
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
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class CanteenInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER, ROOM_SERVICER{

	private final static long serialVersionUID = -7063521835843676015l;
	
	boolean autoE;
	private long[] pdata;
	private final int[] amounts = new int[RESOURCES.EDI().all().size()];
	private final int[] amountIncoming = new int[RESOURCES.EDI().all().size()];
	private int amountTotal = 0;
	final int maxAmount;
	private int serviceReserved = 0;

	
	private final JobIterator jobs;
	private final RBITImp fetchMask = new RBITImp().or(RESOURCES.EDI().mask);
	private final RBITImp useMask = new RBITImp();
	final RoomServiceInstance service;
	short tableX = -1;
	short tableY = -1;

	CanteenInstance(ROOM_CANTEEN p, TmpArea area, RoomInit init) {
		super(p, area, init);

		
		jobs = new JobIterator(this) {
			private static final long serialVersionUID = 1L;

			@Override
			protected SETT_JOB init(int tx, int ty) {
				return blueprintI().job.get(tx, ty);
			}
		};
		//jobs.setAlwaysNewJob();
		
		int m = 0;
		for (COORDINATE c : body()) {
			if (tableX == -1 && is(c)) {
				tableX = (short) c.x();
				tableY = (short) c.y();
			}
			if (is(c) && p.food.get(c.x(), c.y()) != null)
				m++;
		}
		pdata = blueprintI().industryFuel.makeData();
		service = new RoomServiceInstance(m*SService.MAX, blueprintI().service);
		maxAmount = (int) m*2*RESOURCES.EDI().all().size();
		
		employees().maxSet((int) Math.ceil(blueprintI().constructor.workers.get(this)*2));
		employees().neededSet((int) Math.ceil(blueprintI().constructor.workers.get(this)));
		activate();
		
		for (ResG e : RESOURCES.EDI().all()) {
			if (e.resource.edibleServe) {
				useMask.or(e.resource);
			}
		}
		
		fetchMask.and(useMask);
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
		blueprintI().industryFuel.updateRoom(this);
		jobs.searchAgain();
		if (tableX == -1) {
			tableX = (short) body().x1();
			tableY = (short) body().y1();
		}

		
	}
	
	public int amount(ResG e) {
		return amounts[e.index()];
	}
	
	public int amountReserved(ResG e) {
		return amountIncoming[e.index()];
	}
	
	public int amountTotal() {
		return amountTotal;
	}
	
	public int serviceReserved() {
		return serviceReserved;
	}
	
	public RBIT fetchMask() {
		return fetchMask;
	}
	
	public boolean uses(ResG e) {
		return useMask.has(e.resource.bit);
	}
	
	public void usesToggle(ResG e) {
		useMask.toggle(e.resource);
		setMask(e);
	}
	
	void tally(ResG e, int dAmount, int amountReserved) {
		amounts[e.index()] += dAmount;
		this.amountIncoming[e.index()] += amountReserved;
		amountTotal += dAmount;
		blueprintI().total += dAmount;
		blueprintI().amounts[e.index()] += dAmount;
		setMask(e);
	}
	
	void serviceTally(int dReserved) {
		serviceReserved += dReserved;
	}
	
	void consume(ResG e, int amount, int tx, int ty) {
		tally(e, -amount, 0);
		blueprintI().food.get(tx, ty).check();
	}
	
	private void setMask(ResG e) {
		if (amounts[e.index()] + amountIncoming[e.index()] < maxAmount) {
			fetchMask.or(e.resource);
			
		}else {
			fetchMask.clear(e.resource);
			
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
					SETT.THINGS().resources.create(c, RESOURCES.EDI().all().get(amI).resource, a);
				amI++;
				if (amI == amounts.length)
					return;
			}
		}
		
		for (ResG e : RESOURCES.EDI().all())
			tally(e, -amounts[amI], -amountIncoming[amI]);
		
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().food.dispose(c.x(), c.y());
				blueprintI().job.dispose(c.x(), c.y());
			}
		}
		service.dispose(blueprintI().service);
		
	}
	
	@Override
	public ROOM_CANTEEN blueprintI() {
		return (ROOM_CANTEEN) blueprint();
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
		return blueprintI().industries().get(0);
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 0.2 + 0.8 * blueprintI().constructor.tables.get(this));
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateTileDay(int tx, int ty) {
		SOven o = blueprintI().job.get(tx, ty);
		if(o != null && o.coal.get() > 0) {
			int i = blueprintI().industryFuel.ins().get(0).incDay(this);
			if (i > 0) {
				o.coalWithDraw.inc(i);
				
			}
		}
		super.updateTileDay(tx, ty);
	}
	

}