package settlement.army.ai.divs;

import static settlement.army.ai.divs.Plans.Plan.*;

import init.C;
import settlement.army.Div;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPlacability;
import settlement.main.SETT;
import snake2d.Path;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;

final class ToolsWalk {

	private final VectorImp vec = new VectorImp();
	private final VectorImp vec2 = new VectorImp();
	private final Tools t;
	private final DivFormation other = new DivFormation();
	
	
	public static final int destMoveStart = 30;
	public static final int destMoveResume = 20;
	
	ToolsWalk(Tools t){
		this.t = t;
	}
	
	public boolean setStart(int tilesCheckDest) {
		
		boolean useNext = false;
		
		if (next.deployed() > 0) {
			
			int am = 0;
			
			for (int i = 0; i < next.deployed() && i < current.deployed(); i++) {
				if (next.tile(i).tileDistanceTo(current.tile(i)) <= 3)
					am++;
			}
			
			if (am > (next.deployed()+1)/2) {
				useNext = true;
			}
		}
		
		PathTile c = getStartPixel(useNext);
		if (c == null)
			return false;
		
		int sx = (c.x()<<C.T_SCROLL)+C.TILE_SIZEH;
		int sy = (c.y()<<C.T_SCROLL)+C.TILE_SIZEH;
		
		if (useNext && c.isSameAs(c)) {
			sx = next.centrePixel().x();
			sy = next.centrePixel().y();
		}

		if (c.getParent() != null){
			PathTile p = c.getParent();
			c.parentSet(null);
			c = reverse(c, p);
		}
		
		if (c.getParent() != null)
			c = c.getParent();
		
		pp.clear();
		pp.set(c);
		path.clear();
		COORDINATE dest = t.div.getSafeCentrePixel(Plan.dest);
		int destX = dest.x()>>C.T_SCROLL;
		int destY = dest.y()>>C.T_SCROLL;
		path.init(sx, sy, pp, destX, destY, t.pathCost, a);
		m.order.path.set(path);
		
		if (path.isDest())
			return false;
		
		
		return setStartPosition(tilesCheckDest);
		
	}

	
	private PathTile reverse(PathTile newParent, PathTile t) {
		if (t.getParent() == null) {
			t.parentSet(newParent);
			return t;
		}
		PathTile res = reverse(t, t.getParent());
		t.parentSet(newParent);
		return res;
		
	}
	
	private PathTile getStartPixel(boolean useNext) {
		
		int cx = 0;
		int cy = 0;
		
		if (current.deployed() <= 0)
			return null;
	
		
		if (useNext) {
			cx = next.centreTile().x();
			cy = next.centreTile().y();
		}else {
			COORDINATE c = t.div.getSafeCentreTile(current);
			cx = c.x();
			cy = c.y();
			
		}
		
		int destX = dest.centreTile().x();
		int destY = dest.centreTile().y();
		if (dest.deployed() == 0) {
			destX = cx;
			destY = cy;
		}

		t.pathCost.init(current, destX, destY);
		
		int mid = 0;
		for (int i = 0; i < current.deployed(); i++) {
			mid += current.tile(i).tileDistanceTo(cx, cy);
		}
		
		mid /= current.deployed();
		
		double mag = vec.set(cx, cy, destX, destY);
	
		if (mag >= mid) {
			mag = mid;
		}
		cx += vec.x()*mag;
		cy += vec.y()*mag;
		
		
		Flooder f = t.pather.getFlooder();
		f.init(this);
		
		PathTile best = f.pushSloppy(destX, destY, 0);
		double bestDistance = Double.MAX_VALUE;
		f.setValue2(destX, destY, 0);

		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			double dist = COORDINATE.tileDistance(t.x(), t.y(), cx, cy);
			if (dist < bestDistance) {
				best = t;
				bestDistance = dist;
			}
			if (dist == 0) {
				break;
			}
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) { 
					double cost = this.t.pathCost.cost(t.x(), t.y(), d);
					if (cost < 0)
						continue;
					cost *= DIR.ALL.get(i).tileDistance();
					double di = COORDINATE.tileDistance(dx, dy, cx, cy);
					if (f.pushSmaller(dx,  dy, t.getValue2()+cost+di, t) != null)
						f.setValue2(dx, dy, t.getValue2()+cost);
					
				}
				
			}
				
		}
		
		f.done();
		
		return best;
		
	}


	
	public boolean setStartPosition(int fDistance) {
		
		int prevX = path.x();
		int prevY = path.y();
		path.currentIInc(1);
		int cx = path.x();
		int cy = path.y();
		path.currentIInc(-1);
		
		int w = (int) Math.ceil(Math.sqrt(info.men/2.0));
		if (w % 2 == 0)
			w++;
		{
			Div o = status.enemyClosest();
			if (o != null) {
				o.order().next.get(other);
				if (other.deployed() > 0) {
					if (next.deployed() > 0 && other.centrePixel().tileDistanceTo(next.centrePixel()) < C.TILE_SIZE*20)
						w = dest.width()/(dest.formation().size);
				}
			}
		}
		vec.set(prevX, prevY, cx, cy);
		vec.rotate90();
		
		if (path.tilesToDest() <= fDistance && canMoveAllTheWayToDest()) {
			
			double d =  (fDistance - path.tilesToDest());
			d /= fDistance;
			d*= 2;
			if (d > 1)
				d = 1;
			
			//Spin towards destination
			{
				vec.set(dest.dx(), dest.dy());
			}
//			COORDINATE dp = getDestPixel();
//			double dx = dp.x() - ((dp.x()&~C.T_MASK)+C.TILE_SIZEH);
//			double dy = dp.y() - ((dp.y()&~C.T_MASK)+C.TILE_SIZEH);
//			cx += dx*d;
//			cy += dy*d;
			w = dest.width()/(dest.formation().size);
			
		}
		
		double nX = vec.nX();
		double nY = vec.nY();			
		

		
		DivFormation pos = t.deployer.deployCentre(info.men, dest.formation(), prevX, prevY, nX, nY, w, a);
		
		if (pos == null) {
			return false;
		}
		m.order.current.get(current);
		t.mover.moveFromIntoTo(current, next, pos);
		m.order.next.set(next);
		return true;
	}
	
	public boolean setNextPosition(int fDistance) {
	
		DivFormation pos = getNextPosition(fDistance);
		
		if (pos == null) {
			return false;
		}
		m.order.current.get(current);
		t.mover.moveFromIntoTo(current, next, pos);
		m.order.next.set(next);
		return true;
	}
	
	public DivFormation getNextPosition(int fDistance) {
		//check if collisions ahead
		{
			int ii = path.currentI();
			for (int i = 0; i < 7; i++) {
				if (SETT.PATH().solidity.is(path.x()>>C.T_SCROLL, path.y()>>C.T_SCROLL)) {
					path.setCurrentI(ii);
					return null;
				}
				if (path.isDest())
					break;
				path.currentIInc(1);
			}
			path.setCurrentI(ii);
		}
		
		
		int prevX = path.x();
		int prevY = path.y();
		path.currentIInc(1);
		int cx = path.x();
		int cy = path.y();
		m.order.path.set(path);
		
		int w = (int) Math.ceil(Math.sqrt(info.men/2.0));
		if (w % 2 == 0)
			w++;
		{
			Div o = status.enemyClosest();
			if (o != null) {
				o.order().next.get(other);
				if (other.deployed() > 0) {
					if (other.centrePixel().tileDistanceTo(next.centrePixel()) < C.TILE_SIZE*20)
						w = dest.width()/(dest.formation().size);
				}
			}
		}
		vec.set(prevX, prevY, cx, cy);
		vec.rotate90();
		
		if (path.tilesToDest() <= fDistance && canMoveAllTheWayToDest()) {
			
			double d =  (fDistance - path.tilesToDest());
			d /= fDistance;
			d*= 2;
			if (d > 1)
				d = 1;
			
			//Spin towards destination
			{
				vec.set(dest.dx(), dest.dy());
			}
			
//			COORDINATE dp = getDestPixel();
//			double dx = (dp.x()&C.T_MASK) - (cx&C.T_MASK);
//			double dy =(dp.y()&C.T_MASK) - (cy&C.T_MASK);
//			cx -= dx*d;
//			cy -= dy*d;
//			cx = (int) (cx*(1.0-d)+dp.x()*d);
//			cy = (int) (cy*(1.0-d)+dp.y()*d);
//			if (vec2.set(cx, cy, dp.x(), dp.y()) <= C.TILE_SIZEH) {
//				cx =dp.x();
//				cy = dp.y();
//			}
			
			w = dest.width()/(dest.formation().size);
			
		}
		
		double nX = vec.nX();
		double nY = vec.nY();			
		
		return t.deployer.deployCentre(info.men, dest.formation(), cx, cy, nX, nY, w, a);
	}

	
	public boolean canMoveAllTheWayToDest() {
		
		int pi = path.currentI();
		while(!path.isDest()) {
			if (!checkStep(path.x(), path.y())) {
				path.setCurrentI(pi);
				return false;
			}
			path.currentIInc(1);
		}
		
		if (path.isComplete()) {
			path.setCurrentI(pi);
			return true;
		}
		
		int sx = path.x();
		int sy = path.y();
		
		path.setCurrentI(pi);
		return canMoveAllTheWayToDest(sx, sy);
	}
	
	public boolean canMoveAllTheWayToDest(int sx, int sy) {
		int dx = dest.centrePixel().x();
		int dy = dest.centrePixel().y();
		double m = vec2.set(sx, sy, dx, dy);
		int steps = (int) Math.ceil(m/C.TILE_SIZE);
		for (int i = 0; i < steps; i++) {
			int tx = ((int) (sx+vec2.nX()*i*C.TILE_SIZE));
			int ty = ((int) (sy+vec2.nY()*i*C.TILE_SIZE));
			if (!checkStep(tx, ty))
				return false;
		}
		return true;
	}
	
	private boolean checkStep(int cx, int cy) {
		
		double x1 = cx-dest.dx()*dest.width()/2;
		double y1 = cy-dest.dy()*dest.width()/2;
		int am = dest.width()/dest.formation().size;
		for (int i = 0; i < am; i++) {
			if (DivPlacability.pixelIsBlocked((int)x1, (int)y1, dest.formation().size, a))
				return false;
			x1 += dest.dx()*dest.formation().size;
			y1 += dest.dy()*dest.formation().size;
		}
		return true;
	}
	
	private final Paths pp = new Paths();
	
	public static class Paths extends Path.PathSync {

		public Paths() {
			super(1024*4);

		}
		
		@Override
		public void setOne(int destX, int destY){
			super.setOne(destX, destY);
		}
		
		@Override
		public void setTwo(int x1, int y1, int x2, int y2) {
			super.setTwo(x1, y1, x2, y2);
		}
	}
	
}
