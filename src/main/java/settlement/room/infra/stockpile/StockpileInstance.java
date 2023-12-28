package settlement.room.infra.stockpile;

import static settlement.main.SETT.*;

import game.GAME;
import game.boosting.Boostable;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finder.SPath;
import settlement.room.infra.transport.ROOM_DELIVERY_INSTANCE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.job.StorageCrate;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomState;
import settlement.room.main.util.RoomState.RoomStateInstance;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayCooShort;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class StockpileInstance extends RoomInstance implements StorageCrate.STORAGE_CRATE_HASSER, ROOM_RADIUS_INSTANCE, ROOM_DELIVERY_INSTANCE {

	private final static long serialVersionUID = -7063521835843676015l;
	private int usedCrates = 0;
	final short[] allocated = new short[RESOURCES.ALL().size()];
	final int[] amountTotal = new int[RESOURCES.ALL().size()];
	final int[] amountUnreserved = new int[RESOURCES.ALL().size()];
	final int[] spaceReserved = new int[RESOURCES.ALL().size()];
	
	private RBITImp fetchMaximums = new RBITImp();
	private final ArrayCooShort crates;
	final StorageCrate.StorageData[] sdata;
	private RBITImp fetchMaskBig = new RBITImp();
	private RBITImp fetchMask = new RBITImp();
	private RBITImp hasMask = new RBITImp();
	private RBITImp fetchOther = new RBITImp();
	private byte searchStatus = 0;
	byte radius = 100;
	private StockpileInstance emptyTo;
	boolean autoE;
	

	StockpileInstance(ROOM_STOCKPILE p, TmpArea area, RoomInit init) {
		super(p, area, init);

		sdata = p.crate.make(this);
		
		int crateI = 0;
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			StorageCrate cr = p.crate.get(c.x(), c.y(), this, sdata);
			if (cr != null) {
				crateI++;
			}
		}
		
		crates = new ArrayCooShort(crateI);
	
		crateI = 0;
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			if (p.crate.get(c.x(), c.y(), this, sdata) != null) {
				crates.set(crateI++).set(c.x(), c.y());
			}
		}
		
		crates.shuffle(crates.size());
		
		crates.set(0);
		
		while(crates.hasNext()) {
			ScatteredResource s = SETT.THINGS().resources.tGet.get(crates.get());
			StorageCrate c = p.crate.get(crates.get().x(), crates.get().y(), this, sdata);
			
			if (s != null) {
				
				c.resourceSet(s.resource());
				int am = CLAMP.i(s.amount(), 0, crateSize() - c.amount());
				c.amountSet(c.amount() + am);
				while(am-- > 0) {
					if (!s.findableReservedIs())
						s.findableReserve();
					s.resourcePickup();
				}
			}
			crates.next();
		}
		crates.set(0);
		
		employees().maxSet(crates.size()*2);
		activate();
	}
	
	private void updateMasks() {
		fetchMask.clear();
		fetchMaskBig.clear();
		for (RESOURCE r : RESOURCES.ALL()) {
			int am = allocated[r.bIndex()]*crateSize();
			am -= amountTotal[r.bIndex()];
			am -= spaceReserved[r.bIndex()];
			if (am > 0) {
				fetchMask.or(r);
			}
			if (am > 64)
				fetchMaskBig.or(r);
		}
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		super.render(r, shadowBatch, it);
		it.lit();
		return false;
	}
	
	
	private static boolean debugi = false;
	void fixiFix() {
		
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			amountTotal[i] = 0;
			amountUnreserved[i] = 0;
			spaceReserved[i] = 0;
			allocated[i] = 0;
		}
		usedCrates = 0;
		boolean oo = debugi;
		debugi = false;
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			StorageCrate crate = blueprintI().crate.get(c.x(), c.y(), this, sdata);
			if (crate != null && crate.resource() != null) {
				crate.fix();
				
			}
		}
		debugi = oo;
	}
	
	void addCrate(int res, int crates, int amountTot, int amountUnres, int spaceRes) {
		amountTotal[res] += amountTot;
		amountUnreserved[res] += amountUnres;
		spaceReserved[res] += spaceRes;
		usedCrates -= allocated[res];
		allocated[res]+=crates;
		usedCrates += allocated[res];
		
		
		
		if (debugi && crates > 0) {
			int xx = blueprintI().crate.x();
			int yy = blueprintI().crate.y();
			RESOURCE rr = RESOURCES.ALL().get(res);
			debug(rr); 
			blueprintI().crate.get(xx, yy, this, sdata);
		}
		
		if (amountTotal[res] < 0)
			throw new RuntimeException();
		if (amountUnreserved[res] < 0)
			throw new RuntimeException();
		if (spaceReserved[res] < 0)
			throw new RuntimeException();
		if (allocated[res] < 0)
			throw new RuntimeException();

		ROOMS().STOCKPILE.tally(res, crates, amountTot, amountUnres, spaceRes, crateSize(), getsMaximum(RESOURCES.ALL().get(res)));
		
		RESOURCE bit = RESOURCES.ALL().get(res);
		
		fetchMask.clear(bit);
		fetchMaskBig.and(fetchMask);
		int am = allocated[res]*crateSize() - amountTotal[res] - spaceReserved[res];
		//GAME.Notify(RESOURCES.ALL().get(res).name + " " + am + " " + allocated[res]*ROOM_STOCKPILE.CRATE_MAX + " " + amountTotal[res] + " " + spaceReserved[res] + " " + spaceRes);

		if (am > 5) {
			fetchMask.or(bit);
		}
		if (am > 64)
			fetchMaskBig.or(bit);
		if (amountUnreserved[res] > 0)
			hasMask.or(bit);
		else
			hasMask.clear(bit);
	}
	
	void debug(RESOURCE rr) {
		int space = 0;
		int amTot = 0;
		int res = rr.index();
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			StorageCrate crate = blueprintI().crate.get(c.x(), c.y(), this, sdata);
			if (crate != null && crate.resource() == rr) {
				space += crate.storageReserved();
				amTot += crate.amount();
			}
		}
		if ((space-spaceReserved[res]) != 0 || (amTot -amountTotal[res]) != 0) {
			GAME.Notify(mX() + " " + mY() + " " + rr + " " + (space-spaceReserved[res]) + " " + (amTot -amountTotal[res]) + " ");
		}
	}

	void allocateCrate(RESOURCE res, int amount){
		
		
		if (amount < allocated[res.bIndex()]) {
			for (COORDINATE c : body()) {
				if (!is(c))
					continue;
				StorageCrate crate = blueprintI().crate.get(c.x(), c.y(), this, sdata);
				if (crate == null)
					continue;
				if (crate.resource() != res)
					continue;
				if (crate.amount() != 0)
					continue;
				crate.clear();
				if (allocated[res.bIndex()] == amount)
					break;
			}
			if (amount < allocated[res.bIndex()]) {
				for (COORDINATE c : body()) {
					if (!is(c))
						continue;
					StorageCrate crate = blueprintI().crate.get(c.x(), c.y(), this, sdata);
					if (crate == null)
						continue;
					if (crate.resource() != res)
						continue;
					crate.clear();
					
					if (allocated[res.bIndex()] == amount)
						break;
				}
			}
				
			
		}else if(amount > allocated[res.bIndex()]) {
			for (COORDINATE c : body()) {
				if (!is(c))
					continue;
				StorageCrate crate = blueprintI().crate.get(c.x(), c.y(), this, sdata);
				
				if (crate == null)
					continue;
				if (crate.resource() != null)
					continue;
				crate.resourceSet(res);
				
				if (allocated[res.bIndex()] == amount)
					break;
			}
		}

	}
	
	int usedCrates() {
		return usedCrates;
	}

	int totalCrates() {
		return crates.size();
	}

	public int amountGet(RESOURCE r) {
		return amountTotal[r.bIndex()];
	}
	
	public int amountUnreservedGet(RESOURCE r) {
		return amountUnreserved[r.bIndex()];
	}

	public int storageGet(RESOURCE r) {
		return allocated[r.bIndex()] * crateSize();
	}
	
	public int cratesGet(RESOURCE r) {
		return allocated[r.bIndex()];
	}
	
	public int storageUnreserved(RESOURCE r) {
		return storageGet(r) - spaceReserved[r.bIndex()];
	}

	void setGetMaximum(RESOURCE r, boolean get) {
		boolean now = fetchMaximums.has(r);
		
		
		if (now != get) {
			
			for (int i = 0; i < crates.size(); i++) {
				blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata).remove();
				crates.inc();
			}
			
			if (get)
				fetchMaximums.or(r);
			else
				fetchMaximums.clear(r);
			
			for (int i = 0; i < crates.size(); i++) {
				blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata).add();
				crates.inc();
			}
		}
		
		updateMasks();
	}
	
	@Override
	public void upgradeSet(int upgrade) {
		for (int i = 0; i < crates.size(); i++) {
			blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata).remove();
			crates.inc();
		}
		super.upgradeSet(upgrade);
		for (int i = 0; i < crates.size(); i++) {
			blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata).add();
			crates.inc();
		}
	}

	@Override
	public boolean getsMaximum(RESOURCE r) {
		return fetchMaximums.has(r);
	}
	
	public boolean getsMaximum(int r) {
		return getsMaximum(RESOURCES.ALL().get(r));
	}

	@Override
	protected void updateAction(double ds, boolean day, int daycount) {

		if (!active() || employees().employed() <= 0)
			return;
		searchStatus = 0;
	}

	private static final RBITImp btmp1 = new RBITImp();
	private static final RBITImp btmp2 = new RBITImp();
	
	@Override
	public TILE_STORAGE job(COORDINATE start, SPath path) {
		
		
		if (searchStatus == 2)
			return null;
		
		
		RESOURCE res = null;
		if (fetchOther.isClear())
			fetchOther.setAll();
		btmp1.clearSet(fetchOther).and(fetchMask);
		if (searchStatus == 0 && !btmp1.isClear()) {
			btmp2.clearSet(btmp1).and(fetchMaximums);
			res = PATH().finders.resource.find(btmp1, btmp2, RBIT.NONE, start, path, radius());
			if (res != null)
				fetchOther.clear(res);
			else
				fetchOther.setAll();
		}else {
			fetchOther.setAll();
		}
		
		if (res == null && searchStatus == 0 && !fetchMaskBig.isClear()) {
			btmp1.clearSet(fetchMaskBig).and(fetchMaximums);
			res = PATH().finders.resource.find(fetchMaskBig, btmp1, RBIT.NONE, start, path, radius());
			if (res == null)
				searchStatus = 1;
		}
		
		if (res == null) {
			btmp1.clearSet(fetchMask).and(fetchMaximums);
			res = PATH().finders.resource.find(fetchMask, btmp1, RBIT.NONE, start,  path, radius());
		}
		
		if (res == null) {
			updateMasks();
			searchStatus = 2;
			return null;
		}
		
		for (int i = 0; i < crates.size(); i++) {
			crates.inc();
			TILE_STORAGE c = blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata);
			if (c.resource() == res && c.storageReservable() > 0) {
				return c; 
			}
			
		}
		

		GAME.Notify("weird!");
		
		PATH().finders.resource.unreserve(res, path.destX(), path.destY(), 1);
		updateMasks();
		searchStatus = 2;
		return null;
		
	}
	
	@Override
	public TILE_STORAGE job(int tx, int ty) {
		if (is(tx, ty))
			return blueprintI().crate.get(tx, ty, this, sdata);
		return null;
	}
	
	public TILE_STORAGE emptyJob(COORDINATE start, SPath path) {
		
		StockpileInstance e = emptyTo();
		
		if (e == null)
			return null;
		
		
		RBIT m = btmp1.clearSet(hasMask).and(fetchMaskBig).clear(fetchMaximums);
		
		if (m.isClear())
			return null;
		
		RESOURCE_TILE c = null;
		for (int i = 0; i < crates.size(); i++) {
			crates.inc();
			RESOURCE_TILE c2 = blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata);
			if (c2.resource() != null && m.has(c2.resource()) && c2.findableReservedCanBe()) {
				c = c2;
				break;
			}
		}
		
		
		
		if (c == null) {
			GAME.Notify("weird!");
			return null;
		}
		
		RESOURCE r = c.resource();
		
		if (!path.request(start, c.x(), c.y()))
			return null;
		
		for (int i = 0; i < e.crates.size(); i++) {
			e.crates.inc();
			TILE_STORAGE s = blueprintI().crate.get(e.crates.get().x(), e.crates.get().y(), e, e.sdata);
			if (s.resource() == r && s.storageReservable() > 0) {
				return s;
			}
		}
		
		GAME.Notify("weird!");
		return null;
		
	}
	
	@Override
	protected void dispose() {
		
		for (int i = 0; i < crates.size(); i++) {
			StorageCrate crate = blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata);
			crate.dispose();
			crates.inc();
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (this.amountTotal[res.bIndex()] != 0)
				GAME.Notify(res.name + " "+ this.amountTotal[res.bIndex()]);
			if (this.amountUnreserved[res.bIndex()] != 0)
				GAME.Notify(res.name + " "+ this.amountTotal[res.bIndex()]);
		}
	}
	
	@Override
	public ROOM_STOCKPILE blueprintI() {
		return ROOMS().STOCKPILE;
	}
	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return blueprintI().crate.get(tx, ty, this, sdata);
	}
	
	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return blueprintI().crate.get(tx, ty, this, sdata);
	}
	
	public double getUsedSpace() {
		double d = 0;
		double c = 0;
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			d += (double)amountTotal[i];
			c += allocated[i];
		}
		if (c == 0)
			return 0;
		return d/(c*crateSize());
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
	public int radius() {
		return (radius & 0x0FF)*16;
	}

	@Override
	public boolean searching() {
		return searchStatus != 2;
	}
	
	public StockpileInstance emptyTo() {
		if (emptyTo == null || !emptyTo.exists()) {
			emptyTo = null;
		}
		return emptyTo;
	}
	
	public void emptyTo(StockpileInstance ins) {
		emptyTo = ins;
	}
	
	public RBIT getMask() {
		return fetchMaskBig;
	}
	
	public RBIT hasMask() {
		return fetchMaskBig;
	}

	@Override
	public int deliverCapacity() {
		return crateSize();
	}

	@Override
	public TILE_STORAGE getDeliveryCrate(RBIT okMask, int minAmount) {
		if (!fetchMask.has(okMask))
			return null;
		
		for (int i = 0; i < crates.size(); i++) {
			crates.inc();
			TILE_STORAGE s = blueprintI().crate.get(crates.get().x(), crates.get().y(), this, sdata);
			if (s.resource() != null && okMask.has(s.resource()) && s.storageReservable() >= minAmount) {
				return s;
			}
		}
		
		
		return null;
	}
	
	@Override
	public RoomState makeState(int tx, int ty) {
		return new State(this);
	}
	
	private static class State extends RoomStateInstance {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final short[] crates = new short[RESOURCES.ALL().size()];
		private boolean[] fetch = new boolean[RESOURCES.ALL().size()];
		
		public State(StockpileInstance ins) {
			super(ins);
			for (RESOURCE r : RESOURCES.ALL()) {
				crates[r.index()] = ins.allocated[r.index()];
				fetch[r.index()] = ins.getsMaximum(r);
			}
		}
		
		@Override
		public void applyIns(RoomInstance ins) {
			if (ins instanceof StockpileInstance) {
				
				StockpileInstance s = (StockpileInstance) ins;
				for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
					s.allocateCrate(RESOURCES.ALL().get(ri), crates[ri]);
					s.setGetMaximum(RESOURCES.ALL().get(ri), fetch[ri]);
				}
				
				
			}
			
		}
		
		
	}

	@Override
	public Boostable carryBonus() {
		return blueprintI().bonus();
	}
	
	public int crateSize() {
		return (int) (blueprintI().upgrades().boost(upgrade())-1);
	}

}