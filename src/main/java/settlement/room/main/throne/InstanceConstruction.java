package settlement.room.main.throne;

import static settlement.main.SETT.*;

import init.D;
import init.resources.RESOURCE;
import init.sprite.ICON;
import init.sprite.ICON.BIG;
import init.sprite.SPRITES;
import settlement.job.ROOM_JOBBER;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.room.main.TmpArea;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import util.rendering.ShadowBatch;

final class InstanceConstruction extends Room.RoomInstanceImp implements ROOM_JOBBER{

	private static final long serialVersionUID = 1L;
	private final RECTANGLE body;
	private final int rot;
	private transient ICON.BIG icon;
	
	private boolean active;
	private int jobs;
	
	private static CharSequence ¤¤name = "Throne Construction";
	private static final int WORK = 8;
	static {
		D.ts(InstanceConstruction.class);
	}
	
	InstanceConstruction(int x1, int y1, int rot){
		super(SETT.ROOMS(), SETT.ROOMS().THRONE, false);
		
		THRONE p = SETT.ROOMS().THRONE;
		body = new Rec().moveX1Y1(x1, y1).setDim(Sprite.width(rot), Sprite.height(rot));
		if (SETT.ROOMS().map.get(p.construction) instanceof InstanceConstruction) {
			((InstanceConstruction)SETT.ROOMS().map.get(p.construction)).remove();
		}
		blueprintI().construction.set(body.cX(), body.cY());
		
		this.rot = rot;
		
		for (COORDINATE c : body) {
			setIndex(c.x(), c.y());
			SETT.ROOMS().data.set(this, c, 0);
		}
		SETT.ROOMS().map.init(this);
		jobs = area();
		active = !SETT.JOBS().planMode.is();
		for (COORDINATE c : body) {
			jobSet(c.x(), c.y(), active, null);
		}
		SETT.ROOMS().map.init(this);
	}
	
	@Override
	public boolean is(int tile) {
		return body.holdsPoint(tile%TWIDTH, tile/TWIDTH);
	}
	
	@Override
	public boolean is(int tx, int ty) {
		return body.holdsPoint(tx, ty);
	}

	@Override
	protected void update(double updateInterval, boolean day, int daycount) {
		
	}

	@Override
	public int area() {
		return body().width()*body().height();
	}

	@Override
	public RECTANGLE body() {
		return body;
	}
	
	boolean active() {
		return body.width() > 0;
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		
		if (SETT.ROOMS().data.get(it.tile()) < WORK) {
			COLOR c = active ? SPRITES.cons().color.ok : COLOR.WHITE65;
			c.bind();
			SPRITES.cons().BIG.solid.render(r, 0, it.x(), it.y());
			COLOR.unbind();
		}
		return false;
	}
	
	@Override
	protected boolean renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		if (SETT.ROOMS().data.get(i.tile()) >= WORK) {
			blueprintI().sprite.renderFloor(r, shadowBatch, i);
		}
		return false;
	}
	
	public THRONE blueprintI() {
		return ROOMS().THRONE;
	}
	
	@Override
	protected AVAILABILITY getAvailability(int tile) {
		return AVAILABILITY.ROOM;
	}
	
	@Override
	public int mX() {
		return body.x1();
	}

	@Override
	public int mY() {
		return body.y1();
	}

	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroyTile(int tx, int ty) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		return false;
	}

	@Override
	public CharSequence name(int tx, int ty) {
		return ¤¤name;
	}
	
	@Override
	public BIG icon() {
		if (icon == null)
			icon = new ICON.BIG.Twin(blueprintI().sprite.icon, SPRITES.icons().s.hammer);
		return icon;
	}

	@Override
	public int resAmount(int ri, int upgrade) {
		return 0;
	}

	@Override
	public void jobFinsih(int tx, int ty, RESOURCE r, int ram) {
		SETT.ROOMS().data.inc(this, tx, ty, 1);
		if (SETT.ROOMS().data.get(tx, ty) >= WORK) {
			jobs--;
			if (jobs == 0) {
				remove(body.x1(), body.y1(), false, this, false).clear();
				new Instance(body.x1(), body.y1(), rot);
			}
		}else {
			jobSet(tx, ty, active, null);
		}
	}

	@Override
	public void jobToggle(boolean toggle) {
		active = toggle;
	}

	@Override
	public boolean jobToggleIs() {
		return active;
	}

	@Override
	public boolean needsFertilityToBeCleared(int tx, int ty) {
		return true;
	}

	@Override
	public boolean becomesSolid(int tx, int ty) {
		return false;
	}

	@Override
	public int totalResourcesNeeded(int x, int y) {
		return 0;
	}

	@Override
	public TmpArea remove(int tx, int ty, boolean scatter, Object user, boolean forced) {
		for (COORDINATE c : body) {
			if (is(c)) {
				jobClear(c.x(), c.y());
			}
		}
		TmpArea t = super.delete(tx, ty, user);
		SETT.ROOMS().map.init(t);
		return t;
	}
	
	public void remove() {
		remove(mX(), mY(), false, this, true).clear();
	}


}