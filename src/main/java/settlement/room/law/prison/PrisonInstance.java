package settlement.room.law.prison;

import init.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.*;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.*;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class PrisonInstance extends RoomInstance implements JOBMANAGER_HASER {

	private static final long serialVersionUID = 1L;
	private final Jobs jobs;
	final static double WORKER_PER_BED = 1d/8d;
	
	private short prisoners = 0;
	
	private static final Bits bprisoners = new Bits(0b01111);
	private final short[] cellsXY;
	private short cellI = 0;
	boolean autoEmploy = false;
	
	protected PrisonInstance(ROOM_PRISON b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		int cells = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				candle(c.x(), c.y());
				if (SETT.ROOMS().fData.tileData.get(c) == Constructor.CODE_ENTRANCE) {
					cells++;
				}
			}
		}
		
		cellsXY = new short[cells*2];
		cells = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				if (SETT.ROOMS().fData.tileData.get(c) == Constructor.CODE_ENTRANCE) {
					cellsXY[cells++] = (short) c.x();
					cellsXY[cells++] = (short) c.y();
				}
			}
		}
			
		jobs = new Jobs(this);
		
		employees().maxSet(jobs.size());
		employees().neededSet((int)Math.ceil(b.constructor.guards.get(this)));
		activate();
		
	}
	
	void candle(int tx, int ty) {
		
		if (SETT.LIGHTS().is(tx, ty)) {
			SETT.LIGHTS().remove(tx, ty);
			FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
					
			for (DIR d : DIR.ORTHO) {
				if (SETT.ROOMS().fData.item.is(tx, ty, d, it)) {
					SETT.LIGHTS().candle(tx, ty, d.x()*(C.TILE_SIZEH-4), d.y()*(C.TILE_SIZEH-4));
					return;
				}
			}
		}
	}
	
	public int prisoners() {
		return prisoners;
	}
	
	public int prisonersMax() {
		return blueprintI().constructor.PRISONERS_PER_CELL*cellsXY.length/2;
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}

	@Override
	protected void activateAction() {
		blueprintI().incPrisoners(prisoners, prisonersMax());
	}

	@Override
	protected void deactivateAction() {
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e != null && e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				HEvent.Handler.removeRoom(h, this);
			}
		}
		
		for (int i = 0; i < cellsXY.length; i+=2) {
			cellI += 2;
			if (cellI >= cellsXY.length)
				cellI = 0;
			int tx = cellsXY[cellI];
			int ty = cellsXY[cellI+1];
			int data = SETT.ROOMS().data.get(tx, ty);
			data = bprisoners.set(data, 0);
			SETT.ROOMS().data.set(this, tx, ty, data);
		}
		
		blueprintI().incPrisoners(-prisoners, -prisonersMax());
		prisoners = 0;
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		jobs.searchAgain();
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	protected void dispose() {
		
		
	}

	@Override
	public ROOM_PRISON blueprintI() {
		return (ROOM_PRISON) blueprint();
	}
	
	COORDINATE registerPrisoner() {
		if (prisoners == prisonersMax())
			throw new RuntimeException();
		if (!active())
			throw new RuntimeException();
		for (int i = 0; i < cellsXY.length; i+=2) {
			cellI += 2;
			if (cellI >= cellsXY.length)
				cellI = 0;
			int tx = cellsXY[cellI];
			int ty = cellsXY[cellI+1];
			int data = SETT.ROOMS().data.get(tx, ty);
			if (bprisoners.get(data) < blueprintI().constructor.PRISONERS_PER_CELL) {
				data = bprisoners.inc(data, 1);
				prisoners ++;
				blueprintI().incPrisoners(1, 0);
				SETT.ROOMS().data.set(this, tx, ty, data);
				Coo.TMP.set(tx, ty);
				return Coo.TMP;
			}
		}
		throw new RuntimeException();
	}
	
	void removePrisoner(int tx, int ty) {
		if (!is(tx, ty))
			return;
		if (SETT.ROOMS().fData.tileData.get(tx, ty) != Constructor.CODE_ENTRANCE)
			return;
		int data = SETT.ROOMS().data.get(tx, ty);
		if (bprisoners.get(data) == 0) {
			return;
		}
		data = bprisoners.inc(data, -1);
		prisoners --;
		blueprintI().incPrisoners(-1, 0);
		SETT.ROOMS().data.set(this, tx, ty, data);
	}
	
	boolean isReserved(int tx, int ty) {
		if (SETT.ROOMS().fData.tileData.get(tx, ty) != Constructor.CODE_ENTRANCE)
			return false;
		int data = SETT.ROOMS().data.get(tx, ty);
		if (bprisoners.get(data) == 0) {
			return false;
		}
		return true;
	}
	
	static class Jobs extends JobPositions<PrisonInstance> {

		private static final long serialVersionUID = 1L;

		public Jobs(PrisonInstance ins) {
			super(ins);
		}

		@Override
		protected SETT_JOB get(int tx, int ty) {
			if (Food.init(tx, ty) != null)
				return Food.init(tx, ty);
			if (Latrine.init(tx, ty) != null)
				return Latrine.init(tx, ty);
			return null;
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			if (Food.init(tx, ty) != null)
				return true;
			if (Latrine.init(tx, ty) != null)
				return true;
			return false;
			
			
		}
	}

}
