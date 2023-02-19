package settlement.path.components;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.util.datatypes.*;

final class SComp0 extends SComponent{

	private final int index;
	private short cx,cy;
	private byte edgeMask;

	SComp0(int index){
		this.index = index;
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public int centreX() {
		return cx;
	}

	@Override
	public int centreY() {
		return cy;
	}

	@Override
	public byte edgeMask() {
		return (byte) (edgeMask & 0x0F);
	}
	
	@Override
	protected void retire(){
		retire(true);
		cx = -1;
		cy = -1;
		super.retire();
	}
	
	@Override
	public boolean retired() {
		return (edgeMask & 0b010000) != 0;
	}
	
	void retire(boolean b) {
		if (b)
			edgeMask |= 0b010000;
		else
			edgeMask &= ~0b010000;
	}
	
	boolean checked() {
		return (edgeMask & 0b0100000) != 0;
	}
	
	void checked(boolean b) {
		if (b)
			edgeMask |= 0b0100000;
		else
			edgeMask &= ~0b0100000;
	}
	
	void init(RECTANGLE bounds, int size, SComponentChecker neighbours){
		
		edgeMask = 0;
		if (bounds.x1() == 0) {
			edgeMask |= DIR.W.mask();
		}else if(bounds.x2() == TWIDTH) {
			edgeMask |= DIR.E.mask();
		}
		if (bounds.y1() == 0) {
			edgeMask |= DIR.N.mask();
		}else if(bounds.y2() == THEIGHT) {
			edgeMask |= DIR.S.mask();
		}
		
		
		
		int smallest = -1;
		double smallestValue = 100000000;
		for (COORDINATE c : bounds) {
			if (is(c)) {
				AVAILABILITY a = PATH().availability.get(c.x(), c.x());
				double v = (a.player + a.from);
				if ( v < smallestValue) {
					smallest = 1;
					smallestValue = v;
					this.cx = (short) c.x();
					this.cy = (short) c.y();
				}
			}
		}
		
//		int smallest = -1;
//		double smallestValue = 100000000;
//		final int cx = bounds.cX();
//		final int cy = bounds.cY();
//		int i = 0;
//		while(RES.circle().radius(i) < smallestValue) {
//			COORDINATE c = RES.circle().get(i);
//			int x = c.x()+cx;
//			int y = c.y()+cy;
//			if (is(x, y)) {
//				AVAILABILITY a = PATH().availability.get(x, y);
//				double v = (a.player + a.from)+(RES.circle().radius(i)+1);
//				if ( v < smallestValue) {
//					smallest = i;
//					smallestValue = v;
//				}
//			}
//			
//			i++;
//		}
//		this.cx = (short) (RES.circle().get(smallest).x() + cx);
//		this.cy = (short) (RES.circle().get(smallest).y() + cy);
		
		if (smallest == -1)
			throw new RuntimeException("shitty component");	
		
		
		
		
		setEdges(neighbours);
		
		return;
		
	}
	
	
	
	void setEdges(SComponentChecker neighbours) {
		
		super.retire();
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(centreX(), centreY(), 0);
		RES.flooder().setValue2(centreX(), centreY(), 0);
		
		neighbours.init();

		while (RES.flooder().hasMore()) {

			PathTile t = RES.flooder().pollSmallest();
			int x = t.x();
			int y = t.y();

			SComp0 n = PATH().comps.zero.get(x, y);
			if (neighbours.is(n))
				continue;

			if (n != this) {
				if (SETT.PATH().comps.zero.updating().is(x, y)) {
					neighbours.isSetAndSet(n);
					continue;
				}

				if ((n.centreX() == x && n.centreY() == y)) {
					
					pushEdge(n, t.getValue2(), t.getValue());
					n.pushEdge(this, t.getValue2(), t.getValue());
					neighbours.isSetAndSet(n);
					
				}
			}
			
			

			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				double v2 = PATH().coster.player.getCost(x, y, x + d.x(), y + d.y())*d.tileDistance();
				if (v2 < 0)
					continue;
				SComponent next =  PATH().comps.zero.get(x, y, d);
				if (next == null)
					continue;
				if (next != this && n != this && next != n)
					continue;
				if (neighbours.is(next))
					continue;
				double v = PATH().availability.get(x + d.x(), y + d.y()).movementSpeedI;
				if (RES.flooder().pushSloppy(x, y, d, t.getValue() + v * d.tileDistance(), t) != null) {
					RES.flooder().setValue2(x, y,d,v2+t.getValue2());
				}
			}
		}

		RES.flooder().done();
		
		pruneEdges();
	}

	@Override
	public SComponentLevel level() {
		return PATH().comps.zero;
	}

	
}
