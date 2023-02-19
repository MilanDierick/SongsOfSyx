package settlement.room.service.arena;

import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import util.rendering.ShadowBatch;

final class ArenaInstance extends RoomInstance implements ROOM_SERVICER{

	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	final byte off = (byte) RND.rInt(64);
	
	public final short gladiatorsMax;
	short gladiators = 0;
	private short gladiatorsTop = 0;
	private float gladiatorsValue = 0.1f;
	public final byte ax,ay;
	
	int cheerTime;
	boolean cheer;
	
	static final int CHEER_TIME = TIME.secondsPerDay/128;
	
	
	protected ArenaInstance(ROOM_ARENA b, TmpArea area, RoomInit init) {
		super(b, area, init);

		int ss = 0;
		for (COORDINATE c : body()) {
			if (is(c) && b.work.init(c.x(), c.y())) {
				ss++;
			}
		}
		
		int ax = 0,ay = 0;
		outer:
		for (int y = 0; y < body().height(); y++) {
			for (int x = 0; x < body().width(); x++) {
				if (SETT.ROOMS().fData.tileData.get(body().x1()+x, body().y1()+y) == ArenaConstructor.ARENA) {
					ax = x;
					ay = y;
					break outer;
				}
			}
		}
		this.ax = (byte) ax;
		this.ay = (byte) ay;
		
		service = new RoomServiceInstance(ss, blueprintI().data);
		gladiatorsMax = (short) b.constructor.workers.get(this);
		
		activate();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}
	
	@Override
	protected void activateAction() {
		blueprintI().gladiatorMax += gladiatorsMax;
		if (gladiators > 0) {
			for (COORDINATE c : body()) {
				if (is(c)) {
					blueprintI().work.activate(c.x(), c.y());
				}
			}
		}
	}

	@Override
	protected void deactivateAction() {
		blueprintI().gladiatorMax -= gladiatorsMax;
		dact();
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e != null && e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				HEvent.Handler.removeRoom(h, this);
			}
			
		}
		blueprintI().gladiators -= gladiators;
		gladiators = 0;
	}
	
	private void act(){
		for (COORDINATE c : body()) {
			cheerTime = (int) (TIME.currentSecond()) - CHEER_TIME;
			if (is(c)) {
				blueprintI().work.activate(c.x(), c.y());
			}
		}
	}
	
	private void dact(){
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().work.deactivate(c.x(), c.y());
			}
		}
	}

	@Override
	protected void updateAction(double updateInterval, boolean day, int daycount) {
		if (day) {
			gladiatorsValue = (float) ((double)gladiatorsTop/gladiatorsMax);
			gladiatorsTop = 0;
			service.updateDay();
		}
	}
	
	@Override
	protected void dispose() {
		
	}

	@Override
	public ROOM_ARENA blueprintI() {
		return (ROOM_ARENA) blueprint();
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, gladiatorsValue*blueprintI().constructor.quality.get(this));
	}
	
	public int gladiatorsNeeded() {
		if (active())
			return gladiatorsMax - gladiators;
		return 0;
	}
	
	public double gladiatorValue() {
		return gladiatorsValue;
	}
	
	public double gladiatorValueNext() {
		return ((double)gladiatorsTop/gladiatorsMax);
	}
	
	public void reserveGladiator(int delta) {
		blueprintI().gladiators -= gladiators;
		if (active() && delta > 0 && gladiators == 0) {
			act();
		}else if (delta < 0 && gladiators == 1) {
			dact();
		}
		gladiators += delta;
		blueprintI().gladiators += gladiators;
		gladiatorsTop = (short) Math.max(gladiatorsTop, gladiators);
		
	}

}
