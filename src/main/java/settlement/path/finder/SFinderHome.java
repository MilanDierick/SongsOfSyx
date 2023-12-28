package settlement.path.finder;

import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.room.home.HOME;
import settlement.room.home.HOMET;
import settlement.room.home.HomeSettings.HomeSetting;
import settlement.room.main.RoomInstance;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

public final class SFinderHome implements SFINDER {

	private Coo current = new Coo();
	private HOMET type;

	public SFinderHome() {
		
		// TODO Auto-generated constructor stub
	}

	public boolean find(Humanoid h, SPath path) {
		
		if (h.indu().clas() != HCLASS.NOBLE && STATS.WORK().EMPLOYED.get(h) == null) {
			HOME home = STATS.HOME().GETTER.get(h, this);
			if (home != null) {
				int sx = home.service().x();
				int sy = home.service().y();
				home.done();
				return path.requestFull(h.tc(),sx, sy);
			}
			HOME oddJob = SETT.ROOMS().HOMES.HOME.odd.get(h, this);
			if (oddJob == null)
				return false;
			STATS.HOME().GETTER.set(h, oddJob);
			int sx = oddJob.service().x();
			int sy = oddJob.service().y();
			oddJob.done();
			return path.requestFull(h.tc(), sx, sy);
		}
		
		{
			HOME home = STATS.HOME().GETTER.get(h, this);
			if (home != null) {
				if (home.is(h.tc())) {
					home.done();
					return path.requestFull(h.tc(), h.tc());
				}
				int sx = home.service().x();
				int sy = home.service().y();
				if (STATS.WORK().EMPLOYED.get(h) == null) {
					
					home.done();
					return path.requestFull(h.tc(), sx, sy);
				}
				home.done();
				current.set(sx, sy);
				
			}else {
				current.set(-1, -1);
			}
		}
		

		
		type = HOMET.get(h);
		
		if (findP(h, path)) {
			HOME old = STATS.HOME().GETTER.get(h, this);
			HOME n = HOME.getS(path.destX(), path.destY(), this);
			if (old != null) {
				
				if (old.service().isSameAs(path.destX, path.destY)) {
					old.done();
					n.done();
					return true;
				}else {
					STATS.HOME().GETTER.set(h, n);
					old.done();
				}
				
			}else {
				STATS.HOME().GETTER.set(h, n);
			}
			n.done();
			return true;
		}
		
		return false;
		
	}
	
	private boolean findP(Humanoid h, SPath path) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
		if (ins == null) {
			if (h.indu().clas() == HCLASS.NOBLE)
				return path.request(THRONE.coo().x(), THRONE.coo().y(), this, Integer.MAX_VALUE);
			else
				return path.request(h.tc().x(), h.tc().y(), this, Integer.MAX_VALUE);
		}
		COORDINATE c = SETT.PATH().finders.finder().findDest(ins, this, 200);
		if (c != null)
			return path.requestFull(h.tc(), c);
		
		return false;
	}
	

	@Override
	public boolean isInComponent(SComponent c, double distance) {
		if (SETT.PATH().comps.data.home.has(c, type))
			return true;
		if (c.is(current))
			return true;
		return false;
	}

	@Override
	public boolean isTile(int tx, int ty, int tileNr) {
		if (current.isSameAs(tx, ty))
			return true;
		
		HOME home = HOME.getS(tx, ty, this);
		if (home == null)
			return false;
		HomeSetting s = home.availability();
		home.done();
		if (s != null && s.is(type))
			return true;
		return false;
	}




}
