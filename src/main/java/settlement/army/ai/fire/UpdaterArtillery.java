package settlement.army.ai.fire;

import init.C;
import init.RES;
import settlement.army.Army;
import settlement.army.Div;
import settlement.army.ai.util.*;
import settlement.main.SETT;
import settlement.room.main.ROOMS;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.thing.projectiles.Trajectory;
import snake2d.CircleCooIterator;
import snake2d.util.datatypes.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;

class UpdaterArtillery {
	
	private final ArrayList<Div> res = new ArrayList<>(32);
	private final CircleCooIterator cIt = new CircleCooIterator(4, RES.flooder());
	private final ArrayListResize<ArtilleryInstance> threadSafe = new ArrayListResize<>(256, ROOMS.ROOM_MAX);
	
	void update() {
		threadSafe.clearSoft();
		for (int bi = 0; bi < SETT.ROOMS().ARTILLERY.size(); bi++) {
			ROOM_ARTILLERY b = SETT.ROOMS().ARTILLERY.get(bi);
			
			b.threadInstances(threadSafe);
		}
		for (int i = 0; i < threadSafe.size(); i++) {
			ArtilleryInstance ins = threadSafe.get(i);
			if (!ins.mustered())
				continue;
			if (!setCurrentTarget(ins))
				setTarget(ins);
			

		}
	}
	
	boolean setCurrentTarget(ArtilleryInstance ins) {
		
		
		COORDINATE tar = ins.targetCooGet();
		if (tar != null) {
			int x = tar.x();
			int y = tar.y();
			if (ins.testTarget(x, y, traj, false) == null) {
				
				if (ins.bombarding()) {
					ins.setTrajectory(traj);
					return true;
				}
				int tx = x>>C.T_SCROLL;
				int ty = y>>C.T_SCROLL;
				if (!SETT.ARMIES().map.attackable.is(tx, ty, ins.army())){
					int i = 0;
					while(cIt.radius(i) <= 3) {
						int dx = tx+cIt.get(i).x();
						int dy = ty+cIt.get(i).y();
						if (SETT.ARMIES().map.attackable.is(dx, dy, ins.army())) {
							dx = dx << C.T_SCROLL +C.TILE_SIZEH;
							dy = dy << C.T_SCROLL +C.TILE_SIZEH;
							if (ins.testTarget(dx, dy, traj, false) == null) {
								ins.targetCooSet(dx, dy, false, ins.targetIsUserSet());
								ins.setTrajectory(traj);
								return true;
							}
						}
						i++;
					}
				}else{
					ins.setTrajectory(traj);
					return true;
				}
				
				
			}
			ins.setTrajectory(null);
			if (ins.targetIsUserSet()) {
				return true;
			}
			ins.clearTarget();
			return false;
			
		}
		Div ddd = ins.targetDivGet();
		if (ddd != null) {
			if (!ddd.order().active()) {
				ins.clearTarget();
				ins.setTrajectory(null);
				return false;
			}else {
				ddd.order().status.get(status);
				if (ins.testTarget(status.currentPixelCX(), status.currentPixelCY(), traj, true) == null) {
					ins.setTrajectory(traj);
					return true;
				}
				
				if (ins.targetIsUserSet()) {
					return true;
				}
				ins.clearTarget();
				return false;
			}
			
		}
		return false;
	}
	
	void setTarget(ArtilleryInstance ins) {
		

		
		if (!ins.fireAtWill()) {
			ins.setTrajectory(null);
			return;
		}
		


		
		ox = -1;
		oy = -1;
		int dddd = DivsQuadMap.size*C.TILE_SIZE;
		int fx = ins.body().x1()*C.TILE_SIZE + ins.body().width()*C.TILE_SIZE/2;
		int fy = ins.body().y1()*C.TILE_SIZE + ins.body().height()*C.TILE_SIZE/2;
		fx += C.TILE_SIZE*ins.dir().x();
		fy += C.TILE_SIZE*ins.dir().y();
		
		double min = ins.rangeMin()/C.SQR2;
		double max = ins.rangeMax();
		
	
		double wStart = min + dddd-1;
		int steps = (int) Math.ceil((max-min)/(dddd));
		double dStep = (max-min)/steps;
		
		
		double dist = min;
		
		
		while(steps > 0) {
			double sx = fx+ins.dir().x()*dist;
			double sy = fy+ins.dir().y()*dist;
			
			for (int s = 0; s < wStart; s+= dddd) {
				for (int di = -2; di < 5; di+= 4) {
					DIR d = ins.dir().next(di);
					int tx = ((int)(sx + s*d.x())>>C.T_SCROLL);
					int ty = ((int)(sy + s*d.y())>>C.T_SCROLL);
					if (!SETT.IN_BOUNDS(tx, ty))
						continue;
					
					if (trySet(ins, tx, ty))
						return;
				}
				
			}
			
			dist += DivsQuadMap.size*C.TILE_SIZE;
			wStart += dStep;
			steps--;
		}
		
	}
	
	private int ox,oy;
	private final DivTDataStatus status = new DivTDataStatus();
	private final Rec bounds = new Rec(C.TILE_SIZE*DivsQuadMap.size);
	private final Trajectory traj = new Trajectory();
	
	private boolean trySet(ArtilleryInstance ins, int tx, int ty) {
		Army enemy = ins.army() == SETT.ARMIES().enemy() ? SETT.ARMIES().player() : SETT.ARMIES().enemy();
		int qx = tx/DivsQuadMap.size;
		int qy = ty/DivsQuadMap.size;
		if (ox == qx && oy == qy)
			return false;
		ox = qx;
		oy = qy;
		
		{
			int fx = ins.body().x1()*C.TILE_SIZE + ins.body().width()*C.TILE_SIZE/2;
			int fy = ins.body().y1()*C.TILE_SIZE + ins.body().height()*C.TILE_SIZE/2;
			res.clearSloppy();
			ArmyAIUtil.quads().getInQuad(res, tx, ty, enemy);
			Div best = null;
			int bestDist = Integer.MAX_VALUE;
			for (Div div : res) {
				div.order().status.get(status);
				if (ins.testTarget(status.currentPixelCX(), status.currentPixelCY(), traj, true) == null) {
					int dist = (status.currentPixelCX()-fx)*(status.currentPixelCX()-fx) + (status.currentPixelCY()-fy)*(status.currentPixelCY()-fy);
					if (dist < bestDist) {
						best = div;
						bestDist = dist;
					}
					
				}
			}
			if (best != null) {
				ins.targetDivSet(best, false);
				ins.setTrajectory(traj);
				return true;
			}
		}
		
		
		bounds.moveX1Y1(qx*C.TILE_SIZE*DivsQuadMap.size, qy*C.TILE_SIZE*DivsQuadMap.size);
		
		if (ArmyAIUtil.quads().ART.is(tx, ty, enemy)) {
			
			for (int i = 0; i < threadSafe.size(); i++) {
				ArtilleryInstance other = threadSafe.get(i);
				if (other.army() != ins.army()) {
					int fx = other.body().x1()*C.TILE_SIZE + other.body().width()*C.TILE_SIZE/2;
					int fy = other.body().y1()*C.TILE_SIZE + other.body().height()*C.TILE_SIZE/2;
					if (bounds.touches(fx, fy)) {
						if (ins.testTarget(fx, fy, traj, false) == null) {
							ins.targetCooSet(fx, fy, false, false);
							ins.setTrajectory(traj);
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
}
