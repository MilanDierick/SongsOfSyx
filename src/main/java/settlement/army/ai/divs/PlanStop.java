package settlement.army.ai.divs;

import init.C;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DivFormation;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class PlanStop extends Plan{
	
	private final VectorImp vec = new VectorImp();
	public PlanStop(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
	}
	
	@Override
	void init() {
		
		wait.set();
		
		path.clear();
		m.order.path.set(path);
		
		int xx = 0;
		int yy = 0;
		for (int i = 0; i < current.deployed(); i++) {
			xx += current.pixel(i).x();
			yy += current.pixel(i).y();
		}
		
		xx /= current.deployed();
		yy /= current.deployed();
		
		if (next.deployed() > 0) {
			
			int dist = getDistance(next, xx, yy);
			if (dist <= C.TILE_SIZE*2) {
				dest.copy(next);
				m.order.dest.set(dest);
				m.order.next.set(next);
				return;
			}
			
			
			int bestI = 0;
			vec.set(next.centrePixel().x(), next.centrePixel().y(), xx, yy);
			
			for (int dd = 1; dd < 10; dd++) {
				int sx = (int) (next.start().x()+vec.nX()*dd*C.TILE_SIZE);
				int sy = (int) (next.start().y()+vec.nY()*dd*C.TILE_SIZE);
				
				DivFormation ff = t.deployer.deployArroundCentre(info.men, settings.formation, sx, sy, next.dx(), next.dy(), next.width(), a);
				if (ff != null && ff.deployed() >= next.deployed()) {
					int ddd = getDistance(ff, xx, yy);
					if (ddd < dist) {
						bestI = dd;
						dist = ddd;
					}else {
						break;
					}
				}
				dd+= 1;
			}

			if (bestI == 0) {
				dest.copy(next);
				m.order.dest.set(dest);
				m.order.next.set(next);
				return;
			}
			if (dist < C.TILE_SIZE*5) {
				int sx = (int) (next.start().x()+vec.nX()*bestI*C.TILE_SIZE);
				int sy = (int) (next.start().y()+vec.nY()*bestI*C.TILE_SIZE);
				DivFormation ff = t.deployer.deployArroundCentre(info.men, settings.formation, sx, sy, next.dx(), next.dy(), next.width(), a);
				if (ff != null && ff.deployed() > 0) {
					m.order.current.get(current);
					t.mover.moveFromIntoTo(current, next, ff);
					dest.copy(next);
					m.order.dest.set(dest);
					m.order.next.set(next);
					return;
				}
			}
			
			
		}
		
		if (current.deployed() > 0){
			
			int min = Integer.MAX_VALUE;
			int f = 0;
			for (int i = 0; i < current.deployed(); i++) {
				int d = Math.abs(current.pixel(i).x()-xx) + Math.abs(current.pixel(i).y()-yy);
				if (d < min) {
					min = d;
					f = i;
				}
			}
			
			DivFormation ff =  t.deployer.deployCentre(current.deployed(), settings.formation, current.pixel(f).x(), current.pixel(f).y(), 1.0, 0.0, 5, a);
			
			if (ff != null) {
				next.copy(ff);
				dest.copy(next);
				m.order.dest.set(dest);
				m.order.next.set(next);
			}
			
			
		}
		
		
	}
	
	private int getDistance(DivFormation f, int xx, int yy) {
		int dist = 0;
		int am = 0;
		for (int i = 0; i < f.deployed(); i++) {
			dist += f.pixel(i).tileDistanceTo(xx, yy);
			am++;
		}
		if (am == 0)
			return Integer.MAX_VALUE;
		return dist/am;
	}
	
	@Override
	void update(int upI, int gamemillis) {
		state(m).update(upI, gamemillis);
	}
	
	private STATE wait = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {

			
			if (t.div.needsFixing(next, info.men, a, settings.formation)) {
				if (t.deployer.fixFormation(next, settings.formation, info.men, a)) {
					m.order.next.set(next);
					m.order.dest.set(next);
				}
			}
			
			shouldBreak = true;
			shouldFire = true;
			
		}
		
		@Override
		boolean setAction() {
			return true;
		}
	};



}
