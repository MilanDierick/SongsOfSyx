package settlement.room.infra.importt;

import static settlement.main.SETT.*;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.RenderData;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomState;
import settlement.room.main.util.RoomState.RoomStateInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.ShadowBatch;

public final class ImportInstance extends RoomInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final int crateMax = 250;
	final short allocated;
	short amount;
	short spaceReserved;
	private byte resource = -1;

	ImportInstance(ROOM_IMPORT p, TmpArea area, RoomInit init) {
		super(p, area, init);

		int am = 0;
		for (COORDINATE c : body()) {
			if (is(c) && p.constructor.isCrate(c.x(), c.y()))
				am++;
		}
		
		allocated = (short) (crateMax*am);
		activate();
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}


	void count(int amount, int spaceReserved) {
		if (this.resource() == null)
			throw new RuntimeException();
		blueprintI().tally.inc(this.resource(), -this.amount, -this.allocated);
		this.amount += amount;
		this.spaceReserved += spaceReserved;
		if (this.spaceReserved < 0)
			this.spaceReserved = 0;
		blueprintI().tally.inc(this.resource(), this.amount, this.allocated);
	}
	
	@Override
	protected void dispose() {
		if (this.resource() != null) {
			for (COORDINATE c : body()) {
				if (!is(c))
					continue;
				blueprintI().UNLOADER.clear(c.x(), c.y());
			}
			blueprintI().tally.inc(this.resource(), 0, -this.allocated);
		}
		
		
	}
	
	@Override
	protected void updateAction(double ds, boolean day, int daycount) {
		

	}

	@Override
	protected void activateAction() {
	
	}

	@Override
	protected void deactivateAction() {
		
	}

	@Override
	public ROOM_IMPORT blueprintI() {
		return ROOMS().IMPORT;
	}
	
	void allocate(RESOURCE res){
		if (res == resource())
			return;
		dispose();
		
		if (res != null) {
			this.resource = res.bIndex();
			blueprintI().tally.inc(this.resource(), 0, this.allocated);
		}else {
			this.resource = -1;
		}
		
	}

	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return blueprintI().UNLOADER.resourceTile(tx, ty);
	}
	
	public RESOURCE resource() {
		if (resource < 0)
			return null;
		return RESOURCES.ALL().get(resource);
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
		private final int ri;
		
		public State(ImportInstance ins) {
			super(ins);
			this.ri = ins.resource;
		}
		
		@Override
		protected void applyIns(RoomInstance ins) {
			super.applyIns(ins);
			if (ri != -1)
				((ImportInstance)ins).allocate(RESOURCES.ALL().get(ri));
		}
		
		
	}
	
}