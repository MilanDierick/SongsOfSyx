package settlement.room.infra.transport;

import static settlement.main.SETT.*;

import game.boosting.Boostable;
import init.resources.*;
import settlement.main.SETT;
import settlement.misc.job.*;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finder.SPath;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.job.StorageCrate.STORAGE_CRATE_HASSER;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.misc.CLAMP;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class TransportInstance extends RoomInstance implements JOBMANAGER_HASER, STORAGE_CRATE_HASSER, ROOM_RADIUS_INSTANCE{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte resource = -1;
	private final Jobs jobs;
	private short si = 0;
	boolean resourceHas = true;
	private short destX,destY;
	boolean fetch;
	byte radius = 25;
	
	boolean searchedAllCarts;
	boolean searchedAllDests;
	boolean auto;
	
	TransportInstance(ROOM_TRANSPORT p, TmpArea area, RoomInit init) {
		super(p, area, init);

		jobs = new Jobs(this);
		
		employees().maxSet(jobs.size()*5);
		employees().neededSet(jobs.size());

		activate();
	
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}
	
	@Override
	protected void dispose() {
		resourceSet(null);
		
	}
	
	@Override
	protected void updateAction(double ds, boolean day, int daycount) {
		if (si == -1)
			si = 0;
		resourceHas = true;
		searchedAllCarts = false;
		searchedAllDests = false;
		jobs.searchAgain();
	}

	@Override
	protected void activateAction() {
	
	}

	@Override
	protected void deactivateAction() {
		
	}

	@Override
	public ROOM_TRANSPORT blueprintI() {
		return (ROOM_TRANSPORT) blueprint();
	}
	
	public RESOURCE resource() {
		if (resource < 0)
			return null;
		return RESOURCES.ALL().get(resource);
	}
	
	void resourceSet(RESOURCE res) {
		int ri = res == null ? -1 : res.index();
		if (ri == resource) {
			return;
		}
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			blueprintI().cart.clear(c.x(), c.y());
		}
		resource = (byte) ri;
		if (resource() != null) {
			for (COORDINATE c : body()) {
				if (!is(c))
					continue;
				if (blueprintI().cart.getStorage(c.x(), c.y()) != null && blueprintI().cart.getStorage(c.x(), c.y()).storageReservable() > 0)
					SETT.PATH().finders.storage.reportPresence(blueprintI().cart.getStorage(c.x(), c.y()));
			}
		}
		
		
	}
	
	public ROOM_DELIVERY_INSTANCE destination() {
		RoomInstance r = SETT.ROOMS().map.instance.get(destX, destY);
		if (r != null && r instanceof ROOM_DELIVERY_INSTANCE && SETT.PATH().reachability.is(r.mX(), r.mY()))
			return (ROOM_DELIVERY_INSTANCE) r;
		return null;

	}
	
	void destinationSet(int tx, int ty) {
		this.destX = (short) tx;
		this.destY = (short) ty;
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}

	@Override
	public TILE_STORAGE job(COORDINATE start, SPath path) {
		
		if (hasStorage()) {
			
			RESOURCE r = SETT.PATH().finders.resource.find(resource().bit, getsMaximum(resource()) ? resource().bit : RBIT.NONE, RBIT.NONE, start, path, radius());
			if (r == null) {
				resourceHas = false;
				return null;
			}
			TILE_STORAGE ss = blueprintI().cart.getStorageRel(jobs.get(si).x(), jobs.get(si).y());
			if (ss == null)
				PATH().finders.resource.unreserve(r, path.destX(), path.destY(), 1);
			return ss;
		}

		si = -1;
		
		return null;
	}
	
	public boolean hasStorage() {
		if (si == -1)
			return false;
		if (resource() == null)
			return false;
		if (!resourceHas)
			return false;
		
		int best = Cart.LOAD+1;
		int bi = -1;
		
		for (si = 0; si < jobs.size(); si++) {
			
			TILE_STORAGE t = blueprintI().cart.getStorageRel(jobs.get(si).x(), jobs.get(si).y());
			if (t != null && t.storageReservable() > 0 && t.storageReservable() < best) {
				best = t.storageReservable();
				bi = si;
			}
		}
		
		si = (short) bi;
		return si >= 0;
	}
	
	private static Coo[] scoos = new Coo[] {
		new Coo(),
		new Coo()
	};
	
	public Coo[] getDeliveryJob() {
		
		
		
		if (destination() == null)
			return null;
		if (searchedAllCarts)
			return null;
		if (searchedAllDests)
			return null;
		
		for (int di = 0; di < jobs.size(); di++) {
			
			if (blueprintI().cart.isReadyToGo(jobs.get(di).x(), jobs.get(di).y())) {
				scoos[0].set(jobs.get(di));
				TILE_STORAGE s = destination().getDeliveryCrate(resource().bit, CLAMP.i(32, 0, destination().deliverCapacity()));
				if (s == null) {
					searchedAllDests = true;
					return null;
				}
				scoos[0].set(jobs.get(di));
				scoos[1].set(s);
				blueprintI().cart.reserveGo(jobs.get(di).x(), jobs.get(di).y());
				return scoos;
			}
		}
		
		searchedAllCarts = true;
		return null;
		
	}
	
	public int deliveryAmount(int tx, int ty) {
		return blueprintI().cart.goAmount(tx, ty);
	}
	
	public void deliveryJobCancel(int tx, int ty) {
		blueprintI().cart.cancelGo(tx, ty);
	}
	
	public boolean doDeliveryJob(int tx, int ty) {
		return blueprintI().cart.go(this, tx, ty);
	}
	
	public void finishDeliveryJob(int tx, int ty) {
		blueprintI().cart.gofinish(this, tx, ty);
	}

	@Override
	public TILE_STORAGE job(int tx, int ty) {
		return blueprintI().cart.getStorage(tx, ty);
	}

	@Override
	public boolean getsMaximum(RESOURCE res) {
		return fetch;
	}

	@Override
	public int radius() {
		return radius*3;
	}

	@Override
	public boolean searching() {
		return resourceHas;
	}
	
	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return blueprintI().cart.getStorage(tx, ty);
	};
	
	private static class Jobs extends JobPositions<TransportInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(TransportInstance ins) {
			super(ins);
		}
		
		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().cart.getJob(tx, ty) != null;
		}
		
		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().cart.getJob(tx, ty);
		}
	}

	@Override
	public Boostable carryBonus() {
		return SETT.ROOMS().STOCKPILE.bonus();
	}

}