package settlement.army.ai.divs;

import init.C;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPlacability;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

class PlanCharge extends Plan{

	private final VectorImp vec = new VectorImp();
	
	public PlanCharge(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
	}
	
	@Override
	void init() {

		wait.set();
		
	}
	
	private STATE wait = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			if (t.div.needsFixing(next, info.men, a, settings.formation)) {
				if (t.deployer.fixFormation(next, settings.formation, info.men, a)) {
					m.order.next.set(next);
				}
			}
			
			if (inPosition() < (next.deployed()-info.unreachable)/2)
				return;
			
			charge.set();
			return;
			
		}
		
		@Override
		boolean setAction() {
			
			if (next.deployed() > 0) {
				dest.copy(next);
				m.order.dest.set(dest);
			}else if (current.deployed() > 0){
				int xx = 0;
				int yy = 0;
				for (int i = 0; i < current.deployed(); i++) {
					xx += current.tile(i).x();
					yy += current.tile(i).y();
				}
				
				xx /= current.deployed();
				yy /= current.deployed();
				int min = Integer.MAX_VALUE;
				int f = -1;
				for (int i = 0; i < current.deployed(); i++) {
					int d = Math.abs(current.tile(i).x()-xx) + Math.abs(current.tile(i).y()-yy);
					if (d < min) {
						min = d;
						f = i;
					}
				}
				
				if (f == -1) {
					task.stop();
					m.order.task.set(task);
					return false;
				}
					
				
				DivFormation d =  t.deployer.deployCentre(current.deployed(), settings.formation, current.pixel(f).x(), current.pixel(f).y(), 1.0, 0.0, 5, a);
				if (d == null) {
					task.stop();
					m.order.task.set(task);
					return false;
				}
				dest.copy(d);
			}
			
			return false;
		}
	};
	
	private STATE charge = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			
			if (inPosition() == 0)
				return;
			
			if (status.enemyCollisions() > 0) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			
			
			
			vec.set(next.dx(), next.dy());
			vec.rotate90().rotate90().rotate90();
			int sx = (int) (next.start().x()+vec.nX()*C.TILE_SIZE);
			int sy = (int) (next.start().y()+vec.nY()*C.TILE_SIZE);
			
			if (!SETT.PIXEL_IN_BOUNDS(sx, sy) || !SETT.PIXEL_IN_BOUNDS((int) (sx+next.dx()*next.width()), (int) (sy+next.dy()*next.width()))) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			
			vec.set(next.dx(), next.dy());
			
			DivFormation f = t.deployer.deploy(info.men, settings.formation, sx, sy, next.dx(), next.dy(), next.width(), a);
			if (f != null && f.deployed() > 0) {
				m.order.current.get(current);
				t.mover.moveFromIntoTo(current, next, f);
				//next.copy(f);
				m.order.next.set(next);
				return;
			}
			
			double largestGap = -1;
			int largestI1 = -1;
			
			for (int d = 0; d <= next.width(); d+= settings.formation.size){
				int x1 = (int) (sx+d*vec.nX()) + settings.formation.sizeH;
				int y1 = (int) (sy+d*vec.nY()) + settings.formation.sizeH;
				
				if (!DivPlacability.pixelIsBlocked(x1, y1, settings.formation.size, a)) {
					int am = 1;
					int di = d;
					for (; d <= next.width(); d+= settings.formation.size){
						int x = (int) (sx+d*vec.nX());
						int y = (int) (sy+d*vec.nY());
						if (!DivPlacability.pixelIsBlocked(x, y, settings.formation.size, a)) {
							am++;
						}else {
							break;
						}
					}
					if (am > largestGap) {
						largestGap = am;
						largestI1 = di;
					}
					
				}
			}
			
			if (largestI1 == -1) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			int cx = (int) (sx+(largestI1)*vec.nX());
			int cy = (int) (sy+(largestI1)*vec.nY());
			
			f = t.deployer.deploy(info.men, settings.formation, cx, cy, next.dx(), next.dy(), (int) (largestGap*settings.formation.size), a);
			
			if (f != null && f.deployed() > 0) {
				m.order.current.get(current);
				t.mover.moveFromIntoTo(current, next, f);
				//next.copy(f);
				m.order.next.set(next);
			}else {
				task.stop();
				m.order.task.set(task);
				return;
			}
		}
		
		@Override
		boolean setAction() {
			return false;
		}
	};
	
	private int inPosition() {
		
		int am = 0;
		for (int i = 0; i < current.deployed() && i < next.deployed(); i++) {
			
			if (current.pixel(i).tileDistanceTo(next.pixel(i)) < C.TILE_SIZE*3)
				am++;
		}
		
		return am;
	}

	
	@Override
	void update(int upI, int gamemillis) {
		
		charging = true;
		state(m).update(upI, gamemillis);
		
	}





}
