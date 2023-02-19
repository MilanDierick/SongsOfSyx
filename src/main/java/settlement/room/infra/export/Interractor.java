package settlement.room.infra.export;

import init.RES;
import init.resources.RESOURCE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract class Interractor {

	protected final ROOM_EXPORT b;
	
	Interractor(ROOM_EXPORT b){
		this.b = b;
	}
	
	COORDINATE getReservableSpot(ExportInstance i, int sx, int sy, RESOURCE res) {
		if (!i.is(sx, sy)) {
			sx = i.mX();
			sy = i.mY();
		}
		RES.filler().init(this);
		RES.filler().filler.set(sx, sy);
		DIR dir = DIR.ORTHO.rnd();
//		int q = 0;
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (reservable(res, c) > 0) {
				RES.filler().done();
				return c;
			}
//			q++;
			DIR d = dir;
			for (int k = 0; k < DIR.ORTHO.size(); k++) {
				if (i.is(c, d))
					RES.filler().fill(c, d);
				d = d.next(2);
			}
			
		}
		RES.filler().done();
		//GAME.Notify("oh no" + " " + q + " " + i.size());
		return null;
	}
	
	public abstract int reservable(RESOURCE res, COORDINATE c);

	public abstract void reserve(RESOURCE res, COORDINATE c, int amount);
	
	public abstract int reserved(RESOURCE res, COORDINATE c);
	
	public abstract void finish(RESOURCE res, COORDINATE c, int amount);

	
}
