package settlement.path;

import static settlement.main.SETT.*;

import game.GAME;
import settlement.main.SETT;
import settlement.path.components.SComp0Level;
import settlement.path.components.SCompFinder.SCompPatherFinder;
import settlement.path.components.SComponent;
import settlement.room.main.throne.THRONE;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.LIST;

public class SettEntryPoints {


	
	private final int qSize = SComp0Level.SIZE;

	
	private final Coo tmp = new Coo();
	
	SettEntryPoints(){
		
		
	}
	
	private final SCompPatherFinder fin = new SCompPatherFinder() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return c.edgeMask() != 0;
		}
	};
	
	public COORDINATE rnd() {
		
		if (!hasAny())
			return null;
		
		LIST<SComponent> list = SETT.PATH().comps.pather.fill(THRONE.coo().x(), THRONE.coo().y(), fin, Integer.MAX_VALUE).path();
		if (list.size() == 0) {
			GAME.Notify("ohno");
			return null;
		}
		
		return rnd(list.get((int)RND.rExpo()*list.size()));
	}
	
	private COORDINATE rnd(SComponent s) {
		DIR d = DIR.ORTHO.rnd();
		for (int i = 0; i < 4; i++) {
			if ((s.edgeMask() & d.mask()) != 0)
				return rnd(s, d);
			d = d.next(2);
		}
		throw new RuntimeException();
	}
	
	private COORDINATE rnd(SComponent s, DIR d) {

		int x1 = qSize*(s.centreX()/qSize);
		int y1 = qSize*(s.centreY()/qSize);
		
		int dx = 0;
		int dy = 0;
				
		if (d.x() != 0) {
			if (d.x() > 0)
				x1 += qSize-1;
			dy = 1;
		}else if(d.y() != 0) {
			if (d.y() > 0)
				y1 += qSize-1;
			dx = 1;
		}
			
		for (int i = 0; i <= qSize; i++) {
			int x = dx*i + x1;
			int y = dy*i + y1;
			if (SETT.PATH().connectivity.is(x, y)) {
				tmp.set(x, y);
				return tmp;
			}
		}
		
		GAME.Notify("No entrypoints! " + d + " " + s.level().level() + " " + s.centreX() + " " + s.centreY() + " " + s.index() + " " + x1 + " " + y1 + " " + dx + " " + dy);
		return null;
	}
	
	public int rnd(ArrayCooShort array) {
		array.set(0);
		if (!hasAny())
			return 0;
		
		LIST<SComponent> list = SETT.PATH().comps.pather.fill(THRONE.coo().x(), THRONE.coo().y(), fin, Integer.MAX_VALUE).path();
		if (list.size() == 0) {
			GAME.Notify("ohno");
			return 0;
		}
		
		int li = RND.rInt(list.size());
		for (int i = 0; i < list.size() && array.hasNext(); i++) {
			int ii = (i+li)%list.size();
			rnd(list.get(ii), array);
		}
		return array.getI();
		
	}
	
	public boolean validate(COORDINATE c) {
		return validate(c.x(), c.y());
	}
	
	public boolean validate(int tx, int ty) {
		return SETT.IN_BOUNDS(tx, ty) && SETT.PATH().connectivity.is(tx, ty) && ( tx == 0 || ty == 0 || tx == SETT.TWIDTH-1 || ty == SETT.THEIGHT-1);
	}
	
	private void rnd(SComponent s, ArrayCooShort array) {
		
		DIR d = DIR.ORTHO.rnd();
		for (int i = 0; i <  4 && array.hasNext(); i++) {
			if ((s.edgeMask() & d.mask()) != 0)
				rnd(d, s, array);
			d = d.next(2);
		}
	}
	
	private int rnd(DIR d, SComponent s, ArrayCooShort array) {
		

		final int dx = Math.abs(d.next(2).x());
		final int dy = Math.abs(d.next(2).y());
		
		int sx = s.level().size()*(s.centreX()/s.level().size());
		int sy = s.level().size()*(s.centreY()/s.level().size());
		
		sx += ((d.x()+1)/2)*(s.level().size()-1);
		sy += ((d.y()+1)/2)*(s.level().size()-1);
		
		int amount = 0;
		int x = sx;
		int y = sy;
		
		for (int i = -1; i <= s.level().size(); i++) {
			
			x = dx*i + sx;
			y = dy*i + sy;
			if (IN_BOUNDS(x, y) && SETT.PATH().availability.get(x, y).player >= 0) {
				array.get().set(x, y);
				amount++;
				if (!array.hasNext())
					return amount;
				array.set(array.getI()+1);
			}
		}
		if (amount == 0)
			GAME.Notify(sx + " " + sy + " " + d + " " + dx + " " + dy);
		return amount;
		
	}
	
	public boolean hasAny() {
		SComponent s = SETT.PATH().comps.superComp.get(THRONE.coo());
		if (s == null)
			return false;
		return s.edgeMask() != 0;
	}
	
}
