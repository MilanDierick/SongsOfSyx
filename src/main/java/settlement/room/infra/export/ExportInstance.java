package settlement.room.infra.export;

import static settlement.main.SETT.*;

import game.GAME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomState;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class ExportInstance extends RoomInstance implements ROOM_RADIUS_INSTANCE {

	/**
	 * 
	 */
	private byte resourceI;
	private static final long serialVersionUID = 1L;
	static final int crateMax = 128;
	final short crates;
	int amount = 0;
	int amountReserved = 0;
	int spaceReserved = 0;
	byte workFail = 0;
	boolean auto;
	
	private byte searchStatus = 0;
	byte radius = 100;

	ExportInstance(ROOM_EXPORT b, TmpArea area, RoomInit init) {
		super(b, area, init);

		int cc = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				Crate crate = b.crate(c.x(), c.y());
				if (crate != null)
					cc++;
			}
		}
		
		crates = (short) cc;
		
		employees().maxSet(crates);
		employees().neededSet((int) Math.ceil(crates/20.0));
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
		
		workFail --;
		if (workFail < 0)
			workFail = 0;
		
		if (!active() || employees().employed() <= 0)
			return;


	}
	
	public RESOURCE resource() {
		if (resourceI == 0)
			return null;
		return RESOURCES.ALL().get(resourceI-1);
	}
	
	void resourceSet(RESOURCE r) {
		if (r == resource())
			return;
		
		if (resource() != null) {
			for (COORDINATE c : body()) {
				if (!is(c))
					continue;
				Crate crate = blueprintI().crate(c.x(), c.y());
				if (crate == null)
					continue;
				int am = crate.amount();
				crate.clear();
				if (am > 0) {
					for (DIR dd : DIR.ORTHO) {
						if (!PATH().solidity.is(c, dd)) {
							blueprintI().FETCHER.vacate(c.x()+dd.x(), c.y()+dd.y(), resource(), am);
							break;
						}
					}
				}
			}
			blueprintI().tally.inc(resource(), 0, -ExportInstance.crateMax*crates);	
		}
		if (amount != 0) {
			GAME.Notify(resource().name + " "+ this.amount);
		}
		
		resourceI = (byte) (r == null ? 0 : r.index()+1);
		if (resource() != null) {
			blueprintI().tally.inc(resource(), 0, ExportInstance.crateMax*crates);	
		}
		
		
	}

	@Override
	protected void activateAction() {
	
	}

	@Override
	protected void deactivateAction() {
		
	}

	@Override
	public ROOM_EXPORT blueprintI() {
		return ROOMS().EXPORT;
	}

	public ExportWork work() {
		blueprintI().Work.init(this);
		return blueprintI().Work;
	}

	public void workFail() {
		workFail++;
		if (workFail > 5)
			workFail = 5;
	}
	
	public boolean workHas() {
		return workFail == 0;
	}
	
	@Override
	public RoomState makeState(int tx, int ty) {
		return new State(this);
	}
	
	private static class State extends RoomState.RoomStateInstance {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int ri;
		
		public State(ExportInstance ins) {
			super(ins);
			this.ri = ins.resourceI;
		}
		
		@Override
		public void applyIns(RoomInstance ins) {
			super.applyIns(ins);
			if (ri != 0)
				((ExportInstance)ins).resourceSet(RESOURCES.ALL().get(ri-1));
		}
		
		
	}
	
	@Override
	public int radius() {
		return (radius & 0x0FF)*16;
	}

	@Override
	public boolean searching() {
		return searchStatus != 2;
	}

}