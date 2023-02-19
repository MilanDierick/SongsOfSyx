package settlement.room.law.stocks;

import settlement.main.SETT;
import settlement.room.main.*;
import snake2d.util.datatypes.COORDINATE;

final class Instance extends RoomSingleton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Instance(ROOMS m, RoomBlueprint p) {
		super(m, p);

	}

	protected Object readResolve() {
		return blueprintI().instance;
	}

	@Override
	public ROOM_STOCKS blueprintI() {
		return (ROOM_STOCKS) blueprint();
	}

	@Override
	protected void addAction(ROOMA ins) {
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && blueprintI().constructor().service(c.x(), c.y())) {
				SETT.ROOMS().data.set(ins, c.x(), c.y(), 0);
				blueprintI().finder.report(blueprintI().finder.get(c), 1);
				blueprintI().total ++;
			}
		}
	}

	@Override
	protected void removeAction(ROOMA ins) {
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && blueprintI().constructor().service(c.x(), c.y())) {
				if (SETT.ROOMS().data.get(c) == 1)
					blueprintI().used --;
				else
					blueprintI().finder.report(blueprintI().finder.get(c), -1);
				blueprintI().total --;
			}
		}
		super.removeAction(ins);
	}

	@Override
	protected void degradeChange(double oldD, double newD) {

	}

}