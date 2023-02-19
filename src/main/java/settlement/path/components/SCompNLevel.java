package settlement.path.components;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;

public final class SCompNLevel extends SComponentLevel{

	private final int level;
	private final SCompNFactory factory;
	private final SCompNUpdater updater;
	private final int size;
	
	
	public SCompNLevel(SComponentLevel prev, int level, int size){
		this.level = level;
		factory = new SCompNFactory(level, size);
		this.size = size;
		updater = new SCompNUpdater(this, factory, prev, size);
	}
	
	
	public void update(SComponent oldSuper, SComponent newSubComponent) {
		
		if (oldSuper != null) {
			SCompN o = (SCompN) oldSuper;
			if (o.superComp() != null) {
				PATH().comps.levels.get(level).update(o.superComp(), null);
			}
			RES.filler().init(this);
			RES.filler().fill(oldSuper.centreX(), oldSuper.centreY());
			while(RES.filler().hasMore()) {
				COORDINATE c = RES.filler().poll();
				SComponent ss = PATH().comps.all.get(level-1).get(c.x(), c.y());
				ss.superCompSet(null);
				SComponentEdge e = ss.edgefirst();
				while(e != null) {
					if (e.to().superComp() == oldSuper) {
						RES.filler().fill(e.to().centreX(), e.to().centreY());
					}
					e = e.next();
				}
			}
			RES.filler().done();
			
			factory.retire(o);
		}
		if (newSubComponent != null) {
			
			updater.add(newSubComponent);
		}
	}
	
	
	@Override
	public SComponent get(int tile) {
		SComponent c = SETT.PATH().comps.all.get(level-1).get(tile);
		if (c == null)
			return null;
		return c.superComp();
	}

	@Override
	public SComponent get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH);
		return null;
	}

	@Override
	public int componentsMax() {
		return factory.maxAmount();
	}


	@Override
	public SComponent getByIndex(int index) {
		return factory.get(index);
	}


	@Override
	protected void update() {
		updater.update();
	}


	@Override
	public int level() {
		return level;
	}


	@Override
	public int size() {
		return size;
	}


	@Override
	protected void init() {
		factory.clear();
	}

}
