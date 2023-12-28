package settlement.army.invasion;

import java.io.IOException;

import init.C;
import init.RES;
import settlement.entry.EntryPoints.EntryPoint;
import settlement.main.SETT;
import settlement.thing.projectiles.Projectile;
import settlement.thing.projectiles.Trajectory;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.LinkedList;
import world.map.pathing.WTRAV;

final class SpotMaker {

	public static InvasionSpot get(int men, int wx, int wy) {
		
		EntryPoint p = getEntry(wx, wy);
		InvasionSpot sp = make(men, p);
		
		return sp;
	}
	
	private static EntryPoint getEntry(int wx, int wy) {
		EntryPoint start = SETT.ENTRY().points.active(wx, wy);
		if (start == null)
			return SETT.ENTRY().points.all(wx, wy);
		
		wx = start.wx()+start.dirOut.x();
		wy = start.wy()+start.dirOut.y();
		
		LinkedList<COORDINATE> all = new LinkedList<>();
		
		final RECTANGLE inner = SETT.WORLD_AREA().tiles();
		final Rec outer = new Rec(inner.width()+2, inner.height()+2);
		outer.moveX1Y1(inner).incrX(-1).incrY(-1);
		RES.flooder().init(SpotMaker.class);
		RES.flooder().pushSloppy(wx, wy, 0);
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			all.add(t);
			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (!SETT.IN_BOUNDS(dx, dy))
					continue;
				if (!outer.isOnEdge(dx, dy))
					continue;
				if (WTRAV.canLand(t.x(), t.y(), d, false)) {
					RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance());
				}
			}
			
		}
		
		RES.flooder().done();
		
		COORDINATE cc = all.rnd();
		
		EntryPoint best = null;
		double bestV = Double.MAX_VALUE;
		for (EntryPoint p : SETT.ENTRY().points.all()) {
			if (!WTRAV.isGoodLandTile(p.wx(), p.wy()))
				continue;
			double v = p.distanceValue(cc.x(), cc.y())+RND.rFloat();
			if (v < bestV) {
				bestV = v;
				best = p;
			}
		}
		return best;
	}
	
	private static InvasionSpot make(int men, EntryPoint p) {
		Rec c = new Rec(1);
		
		int sx = p.body.x1()+RND.rInt(p.body.width());
		int sy = p.body.y1()+RND.rInt(p.body.height());
		
		c.moveX1Y1(sx, sy);
		DIR d = p.dirOut.next(2);
		
		int w = Math.max(men/10, 8);
		int i = 1;
		
		
		while (w > 0) {
			
			int x = sx+i*d.x();
			int y = sy+i*d.y();
			
			
			if (p.body.holdsPoint(x, y)) {
				w--;
				c.unify(x, y);
			}
			int x2 = sx-i*d.x();
			int y2 = sy-i*d.y();
			if (p.body.holdsPoint(x2, y2)) {
				w--;
				c.unify(x2, y2);
				
			}
			i++;
			if (!SETT.IN_BOUNDS(x, y) && !SETT.IN_BOUNDS(x2, y2))
				break;
				
		}
		return new InvasionSpot(c, p.dirOut.perpendicular());
	}
	
	static class InvasionSpot{
		
		private static Coo bomb = new Coo();
		public final Rec body;
		private final ArrayCooShort coos; 
		public final int size;
		public final DIR dir;
		private int lastBombarded;
		private static Trajectory traj = new Trajectory();
		private boolean any = false;
		
		public InvasionSpot(FileGetter f) throws IOException {
			this.body = new Rec();
			this.body.load(f);
			size = Math.max(body.width(), body.height());
			coos = new ArrayCooShort(size);
			coos.load(f);
			dir = DIR.ALL.get(f.i());
			lastBombarded = f.i();
			any = f.bool();
		}
		
		public InvasionSpot(Rec rec, DIR d) {
			this.body = rec;
			size = Math.max(rec.width(), rec.height());
			coos = new ArrayCooShort(size);
			int i = 0;
			for (COORDINATE c : rec) {
				
				coos.set(i).set(c);
				i++;
			}
			coos.shuffle(i-1);
			coos.set(0);
			dir = d;
			lastBombarded = 0;
		}
		
		public void save(FilePutter p) {
			this.body.save(p);
			coos.save(p);
			p.i(dir.id());
			p.i(lastBombarded);
			p.bool(any);
		}
		
		private COORDINATE getNextBombardStart() {
				
			int dist = (int) 12;
			
			while(lastBombarded < dist) {
				
				if (coos.getI() >= coos.size()-1) {
					coos.set(0);
					if (!any)
						lastBombarded ++;
					any = false;
				}
				
				int sx = coos.get().x();
				int sy = coos.get().y();
				
				int x = sx+dir.x()*lastBombarded;
				int y = sy+dir.y()*lastBombarded;
				coos.inc();
				if (SETT.PATH().availability.get(x, y).isSolid(SETT.ARMIES().enemy())) {
					any = true;
					bomb.set(sx, sy);
					
					return bomb;
				}
			}
			return null;
		}
		
		public boolean launchProj() {
			COORDINATE coo = getNextBombardStart();
			if (coo == null)
				return false;
			int sx = coo.x()*C.TILE_SIZE + C.TILE_SIZEH;
			int sy = coo.y()*C.TILE_SIZE + C.TILE_SIZEH;
			int x = sx + (lastBombarded)*dir.x()*C.TILE_SIZE;
			int y = sy + (lastBombarded)*dir.y()*C.TILE_SIZE;
			Projectile proj = SETT.INVADOR().proj;
			
			if (lastBombarded == 0 || !traj.calcLow(16, sx, sy, x, y, proj.maxAngle(1.0), proj.velocity(1.0)))
				SETT.ARMIES().map.breakIt(x/C.TILE_SIZE, y/C.TILE_SIZE);
			else {
				SETT.PROJS().launch(sx, sy, 16, traj, proj, 0, 1.0);
			}
			return true;
		}
		
	}
	
}
