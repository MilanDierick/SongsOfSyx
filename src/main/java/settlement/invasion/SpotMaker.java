package settlement.invasion;

import java.io.IOException;

import init.C;
import init.RES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.throne.THRONE;
import settlement.thing.projectiles.Trajectory;
import settlement.tilemap.SettBorder.Border;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayCooShort;

final class SpotMaker {

	public static InvasionSpot get(int men, DIR dir) {
		
		if (dir == DIR.C) {
			dir = DIR.ORTHO.rnd();
		}
		Border border = SETT.BORDERS().get(dir);
		
		RES.flooder().init(border);
		for (Border b : SETT.BORDERS().all()) {
			double v = b == border ? 0 : 100;
			for (COORDINATE c : b.body()) {
				RES.flooder().pushSloppy(c, v);
				RES.flooder().setValue2(c, b.index());
			}
			
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (t.isSameAs(THRONE.coo())) {
				RES.flooder().done();
				return make(men, t);
			}
			
			for (DIR d : DIR.ALL) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double v = 1;
					AVAILABILITY aa = SETT.PATH().availability.get(dx, dy);
					if (aa.isSolid(SETT.ARMIES().enemy())) {
						v += SETT.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
					}else
						v += aa.movementSpeedI + (1.0-SETT.ENV().environment.SPACE.get(dx, dy))*10;
					if (RES.flooder().pushSmaller(dx, dy, v*d.tileDistance(), t) != null) {
						RES.flooder().setValue2(dx, dy, t.getValue2());
					}
				}
				
			}
			
		}
		RES.flooder().done();
		return null;
	}
	
	
	private static InvasionSpot make(int men, PathTile t) {
		RES.flooder().done();
		while(t.getParent() != null) {
			
			t = t.getParent();
		}
		Border border = SETT.BORDERS().all().get((int) t.getValue2());
		
		Rec c = new Rec(1);
		c.moveX1Y1(t.x(), t.y());
		DIR d = border.dir.next(2);
		
		int w = Math.max(men/10, 8);
		int i = 1;
		
		
		while (w > 0) {
			
			int x = t.x()+i*d.x();
			int y = t.y()+i*d.y();
			
			
			if (SETT.BORDERS().is.is(x, y)) {
				w--;
				c.unify(x, y);
			}
			int x2 = t.x()-i*d.x();
			int y2 = t.y()-i*d.y();
			if (SETT.BORDERS().is.is(x2, y2)) {
				w--;
				c.unify(x2, y2);
				
			}
			i++;
			if (!SETT.IN_BOUNDS(x, y) && !SETT.IN_BOUNDS(x2, y2))
				break;
				
		}
		return new InvasionSpot(c, border.dir.perpendicular());
		
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
			if (lastBombarded == 0 || !traj.calcLow(16, sx, sy, x, y, SETT.ROOMS().ARTILLERY.get(0).speed()))
				SETT.ARMIES().map.breakIt(x/C.TILE_SIZE, y/C.TILE_SIZE);
			else {
				SETT.PROJS().launch(sx, sy, 16, traj, SETT.ROOMS().ARTILLERY.get(0).projectile, 0);
			}
			return true;
		}
		
	}
	
}
