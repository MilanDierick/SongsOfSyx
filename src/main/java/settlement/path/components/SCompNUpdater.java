package settlement.path.components;

import init.RES;
import settlement.main.SETT;
import snake2d.PathUtilOnline.Filler;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayListResize;

final class SCompNUpdater {

	private final SCompNFactory factory;
	private final Rec bounds;
	private final Rec boundsC = new Rec();
	private final SComponentChecker checkerUnderlings;
	private final SComponentChecker checkerSelf;
	private final SComponentLevel lower;
	private final ArrayListResize<SComponent> upUnderlings;
	private final int size;
	
	SCompNUpdater(SCompNLevel map, SCompNFactory f, SComponentLevel prev, int size){
		this.lower = prev;
		this.factory = f;
		checkerUnderlings = new SComponentChecker(prev); 
		checkerSelf = new SComponentChecker(map); 
		upUnderlings = new ArrayListResize<>(SComp0Level.startSize >> map.level(), Integer.MAX_VALUE);
		bounds = new Rec(size, size);
		this.size = size;
	}

	void update() {
		
		checkerUnderlings.init();
		for (int i = 0; i < upUnderlings.size(); i++) {
			SComponent c = upUnderlings.get(i);
			if (!checkerUnderlings.isSetAndSet(c)) {
				update(c);
			}
		}
		checkerUnderlings.init();
		for (int i = 0; i < upUnderlings.size(); i++) {
			SComponent c = upUnderlings.get(i);
			if (!checkerUnderlings.isSetAndSet(c)) {
				update(c);
			}
		}
		
		upUnderlings.clearSoft();
	}
	
	void add(SComponent c) {
		upUnderlings.add(c);
	}
	
	private void update(SComponent underling) {
		{
			int qx1 = size*(underling.centreX()/size);
			int qy1 = size*(underling.centreY()/size);
			bounds.moveX1Y1(qx1, qy1);
			boundsC.clear();
		}
		
		if (underling.superComp() != null && !underling.superComp().retired()){
			SCompN n = (SCompN) underling.superComp();
			SETT.PATH().comps.data.initComponentN(n, checkerUnderlings, bounds);
			if (n.superComp() != null)
				((SCompNLevel) n.superComp().level()).update(null, n);
			return;
		}
		
		if (underling.edgefirst() == null) {
			underling.superCompSet(null);
			return;
		}

		
		final SCompN comp = factory.create();
		
		
		Filler f = RES.filler();
		f.init(this);
		f.fill(underling.centreX(),underling.centreY());
		while(f.hasMore()) {
			COORDINATE coo = f.poll();
			SComponent c = lower.get(coo);
			if (c == null)
				throw new RuntimeException();
			checkerUnderlings.isSetAndSet(c);
			c.superCompSet(comp);
			boundsC.unify(coo.x(), coo.y());
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				if (bounds.holdsPoint(e.to().centreX(), e.to().centreY()) && !checkerUnderlings.is(e.to())) {
					f.fill(e.to().centreX(), e.to().centreY());
				}
				e = e.next();
			}
		}
		f.done();
		
		comp.init(underling, boundsC, lower, checkerSelf);
		
		SETT.PATH().comps.data.initComponentN((SCompN) underling.superComp(), checkerUnderlings, boundsC);
		
	}

}
