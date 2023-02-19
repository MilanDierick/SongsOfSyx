package settlement.room.home.house;

import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.path.AVAILABILITY;
import settlement.room.home.HOME_TYPE;
import settlement.room.main.*;
import settlement.room.main.util.RoomState;
import snake2d.Renderer;
import util.rendering.ShadowBatch;

final class InstanceHome extends RoomSingleton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	InstanceHome(ROOMS m, ROOM_HOME p) {
		super(m, p);
		
	}

	protected Object readResolve() {
		return blueprintI().instance;
	}

	@Override
	public ROOM_HOME blueprintI() {
		return (ROOM_HOME) blueprint();
	}

	@Override
	protected void removeAction(ROOMA ins) {
		HomeHouse h = blueprintI().houses.get(ins.mX(), ins.mY(), this);
		h.dispose();
		h.done();
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		SETT.ROOMS().HOMES.HOME.odd.update(tx, ty);
	}
	
	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		return null;
	}
	
	@Override
	protected double degradeResNeeded() {
		return 0;
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		return super.render(r, shadowBatch, i);
	}
	
	@Override
	protected boolean renderAbove(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		return super.renderAbove(r, shadowBatch, i);
	}
	
	@Override
	protected boolean renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		return super.renderBelow(r, shadowBatch, i);
	}
	
	@Override
	protected AVAILABILITY getAvailability(int tile) {
		AVAILABILITY a = super.getAvailability(tile);
		if (a == AVAILABILITY.ROOM) {
			int tx = tile%SETT.TWIDTH;
			int ty = tile/SETT.TWIDTH;
			HomeHouse h = blueprintI().houses.get(tx, ty, this);
			Sprite s = h.sprite.get(tx, ty);
			h.done();
			if (s != null && s.solid)
				return AVAILABILITY.NOT_ACCESSIBLE;
		}
		return a;
		
		
	}
	
	@Override
	public boolean wallJoiner() {
		return true;
	}
	
	@Override
	public double isolation(int tx, int ty) {
		HomeHouse h = blueprintI().houses.get(tx, ty, this);
		double i = h.isolation();
		h.done();
		return i;
	}
	
	@Override
	public void isolationSet(int tx, int ty, double isolation) {
		blueprintI().houses.get(tx, ty, this).isolationSet(isolation).done();;
	}
	
	@Override
	public RoomState makeState(int tx, int ty) {
		return new State(this, tx, ty);
	};
	
	private static class State extends RoomState {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final int i;
		final int rx,ry;
		
		State(InstanceHome h, int rx, int ry){
			this.rx = rx;
			this.ry = ry;
			HomeHouse hh = SETT.ROOMS().HOMES.HOME.houses.get(rx, ry, this);
			i = hh.setting().index();
			hh.done();
			
		}
		
		@Override
		public void apply(Room r) {
			SETT.ROOMS().HOMES.HOME.houses.get(rx, ry, this).settingSet(HOME_TYPE.ALL().get(i)).done();
		}
		
	}
	

}