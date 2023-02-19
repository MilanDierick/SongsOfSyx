package settlement.army.ai.divs;

import init.C;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPlacability;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;

final class PlanAttackTile extends PlanWalkAbs{
	
	private final VectorImp vec = new VectorImp();
	private final INT_OE<AIManager> setI;
	
	public PlanAttackTile(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
		setI = data. new DataByte();
	}
	
	@Override
	void init() {

		
		setI.set(m, m.order.task.setI()&0x0FF);
		int tx = task.targetTile().x();
		int ty = task.targetTile().y();
		

		if (!breakable(tx, ty)) {
			task.stop();
			m.order.task.set(task);
			return;
		}
		
		wait.set();
			
	}
	
	private final STATE wait = new STATE() {
		
		private int destX,destY;
		
		@Override
		void update(int updateI, int gameMillis) {
			if (PlanWalkAbs.amountOfPaths > 10)
				return;
			int tx = task.targetTile().x();
			int ty = task.targetTile().y();
			if (!breakable(tx, ty) || !setStart()) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			
			
			
			Flooder f = t.pather.getFlooder();
			f.init(this);
			PathTile best = f.pushSloppy(tx, ty, 0);
			double bestDistance = Double.MAX_VALUE;
			f.setValue2(tx, ty, 0);
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				double dist = COORDINATE.tileDistance(t.x(), t.y(), destX, destY);
				if (dist < bestDistance) {
					best = t;
					bestDistance = dist;
				}
				if (dist == 0) {
					break;
				}
				
				boolean solid = availability(t.x(), t.y()) <= 0;
				
				for (int i = 0; i < DIR.ALL.size(); i++) {
					int dx = t.x()+DIR.ALL.get(i).x();
					int dy = t.y()+DIR.ALL.get(i).y();
					double di = COORDINATE.tileDistance(dx, dy, destX, destY);
					if (SETT.IN_BOUNDS(dx, dy)) { 
						
						if (availability(dx, dy) <= 0) {
							if (solid && breakable(dx, dy)) {
								double cost = 200;
								if (f.pushSmaller(dx,  dy, t.getValue2()+cost+di, t) != null)
									f.setValue2(dx, dy, t.getValue2()+cost);
								
							}
							
							continue;
						}else if(solid) {
							if (!DIR.ALL.get(i).isOrtho())
								continue;
							if (f.pushSmaller(dx,  dy, t.getValue2()+di, t) != null)
								f.setValue2(dx, dy, t.getValue2());
						}else {
							if (!DivPlacability.checkStep(t.x(), t.y(), dx, dy, a))
								continue;
							
							double cost = DIR.ALL.get(i).tileDistance();
							
							if (f.pushSmaller(dx,  dy, t.getValue2()+cost+di, t) != null)
								f.setValue2(dx, dy, t.getValue2()+cost);
						}
						
						
						
					}
					
				}
					
			}
			f.done();
			
			if (availability(best.x(), best.y()) <= 0 || best.getParent() == null) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			
			while(best.getParent() != null && availability(best.getParent().x(), best.getParent().y()) > 0) {
				best = best.getParent();
			}

			int dx = best.x()*C.TILE_SIZE+C.TILE_SIZEH;
			int dy = best.y()*C.TILE_SIZE+C.TILE_SIZEH;
			
			if (best.getParent() == null) {
				DIR d = DIR.get(next.centrePixel().x(), next.centrePixel().y(), dx, dy);
				vec.set(d.xN(), d.yN());
				vec.rotate90();
			}
			else {
				vec.set(best.x(), best.y(), best.getParent().x(), best.getParent().y());
				vec.rotate90();
			}
			
			int rm = info.men/3;
			if (rm % 1 == 0)
				rm ++;
			
			
			DivFormation res = t.deployer.deployCentre(info.men, settings.formation, dx, dy, vec.nX(), vec.nY(), rm, a);
			if (res == null || res.deployed() == 0) {
				task.stop();
				m.order.task.set(task);
				return;
			}
			m.order.dest.set(res);
			setWalkToDest();
		}
		
		boolean setStart() {
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
			if (useNext) {
				destX = next.centreTile().x();
				destY = next.centreTile().y();
				return true;
			}
			
			int xx = 0;
			int yy = 0;
			int am = 0;
			for (int i = 0; i < current.deployed(); i++) {
				xx+= current.tile(i).x();
				yy+= current.tile(i).y();
				am++;
			}
			if (am == 0)
				return false;
			xx /= am;
			yy /= am;
			
			double bestD = Double.MAX_VALUE;
			int best = 0;
			for (int i = 0; i < current.deployed(); i++) {
				double d = current.tile(i).tileDistanceTo(xx, yy);
				if (d < bestD) {
					bestD = d;
					best = i;
				}
			}
			
			destX = current.tile(best).x();
			destY = current.tile(best).y();
			return true;
			
		}
		
		@Override
		boolean setAction() {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private double availability(int tx, int ty) {
		AVAILABILITY av = SETT.PATH().availability.get(tx, ty);
		if (a == SETT.ARMIES().player())
			return av.player;
		return av.enemy;
	}
	
	private boolean breakable(int tx, int ty) {
		return SETT.ARMIES().map.attackable.is(tx, ty, a);
	}
	
	@Override
	void update(int upI, int gamemillis) {
		if (task.targetTile() == null) {
			task.stop();
			m.order.task.set(task);
			return;
		}
		
		
		if (setI.get(m) != (m.order.task.setI()&0x0FF)) {
			setI.set(m, m.order.task.setI()&0x0FF);
			wait.set();
		}
		
		state(m).update(upI, gamemillis);
		
	}

	@Override
	void finished() {
		attack.set();
	}
	
	private STATE attack = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			
			int tx = task.targetTile().x();
			int ty = task.targetTile().y();
			if (!breakable(tx, ty)) {
				DIR d = DIR.get(dest.dx(), dest.dy()).next(-2);
				int dx = tx + d.x();
				int dy = ty + d.y();
				
				if (breakable(dx, dy)) {
					task.attack(dx, dy);
					m.order.task.set(task);
					setI.set(m, m.order.task.setI()&0x0FF);
				}
			}
			
			for (int i = 0; i < dest.deployed(); i++) {
				if (availability(dest.tile(i).x(), dest.tile(i).y()) <0 && SETT.ARMIES().map.attackable.is(dest.tile(i), a)) {
					return;
				}
			}
			
			wait.set();
			return;
			
		}
		
		@Override
		boolean setAction() {
			vec.set(dest.dx(), dest.dy());
			vec.rotate90().rotate90().rotate90();
			int dx = (int) (vec.nX()*(C.TILE_SIZEH+4));
			int dy = (int) (vec.nY()*(C.TILE_SIZEH+4));
			dest.copy(t.deployer.move(dest, dx, dy, a));
			m.order.next.set(dest);
			m.order.dest.set(dest);
			return true;
		}
	};



}
