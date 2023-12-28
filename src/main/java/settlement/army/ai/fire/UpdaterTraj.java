package settlement.army.ai.fire;


import init.C;
import settlement.army.Div;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.order.DivTDataInfo;
import settlement.army.order.DivTDataTask;
import settlement.army.order.DivTDataTask.DIVTASK;
import settlement.main.SETT;
import settlement.thing.projectiles.SProjectiles;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class UpdaterTraj {

	private final DivTDataInfo info = new DivTDataInfo();
	private final DivTDataTask task = new DivTDataTask();
	private final DivTrajectory traj = new DivTrajectory();
	private final DivTDataStatus otherStatus = new DivTDataStatus();
	private final DivTDataStatus status = new DivTDataStatus();
	private final DivFormation next = new DivFormation();
	private final Trajectory trajLow = new Trajectory();
	private final VectorImp vec = new VectorImp();
	private final VectorImp vec1 = new VectorImp();
	private final VectorImp vec2 = new VectorImp();
	
	private final ArrayList<Div> targets = new ArrayList<Div>(16);
	
	public void update(Div div, long time) {
		
		traj.clear();
		if (div.order().active()) {
			div.order().info.get(info);
			if (info.projVel > 0) {
				div.order().task.get(task);
				div.order().next.get(next);
				if (next.deployed() > 0) {
					
					if (task.task() == DIVTASK.ATTACK_RANGED) {
						if (setTrajectory(div, task.targetDiv())) {
							
						}
						
					}else if (div.settings.fireAtWill()){
						targets.clear();
						div.order().status.get(status);
						status.enemiesClosest(targets);
						for (Div d : targets) {
							if (setTrajectory(div, d))
								break;
						}
						
					}
				}
				
			}
				
			

		}
		
		div.order().trajectory.set(traj);
		
	}
	
//	private boolean test(Div div, int fx, int fy, int tx, int ty) {
//		if (!testTrajectory(div, fx, fy, tx, ty, 0))
//			return false;
//		if (SProjectiles.test(div.army(), trajtmp, fx, fy) != null)
//			return false;
//		return true;
//	}
	
	private boolean setTrajectory(Div div, Div target) {
		
		if (target == null || !target.order().active())
			return false;
		target.order().status.get(otherStatus);
		
		int cx = otherStatus.currentPixelCX();
		int cy = otherStatus.currentPixelCY();
		
		if (SProjectiles.problem(trajLow, div, cx, cy) != null)
			return false;
			
		int sfx = next.centrePixel().x();
		int sfy = next.centrePixel().y();
		
		double l = vec.set(sfx, sfy, cx, cy);

		
		for (int i = 0; i < next.deployed(); i++) {
			
			int fx = next.pixel(i).x();
			int fy = next.pixel(i).y();
			
			int tx = (int) (fx+vec.nX()*l);
			int ty = (int) (fy+vec.nY()*l);
			
			if (isEnemy(div, tx, ty)) {
				if (SProjectiles.problem(div.army(), trajLow, fx, fy, tx, ty, info.projAngle, info.projVel) == null) {
					traj.set(i, trajLow);
				}
			}else {
				vec1.set(tx, ty, cx, cy);
				vec2.set(vec1);
				vec2.rotate90();
				
				int dist = next.body().width() > next.body().height() ? next.body().width() : next.body().height();
				dist *= C.ITILE_SIZE;
				outer:
				for (int v1 = 1; v1 <= dist; v1++) {
					for (int v2 = 0; v2 <= dist; v2++) {
						
						
						
						int dx = (int) (tx + (vec1.nX()*v1+vec2.nX()*v2)*C.TILE_SIZE);
						int dy = (int) (ty + (vec1.nY()*v1+vec2.nY()*v2)*C.TILE_SIZE);
						if (isEnemy(div, dx, dy) && SProjectiles.problem(div.army(), trajLow, fx, fy, dx, dy, info.projAngle, info.projVel) == null) {
							traj.set(i, trajLow);
							break outer;
						}
						dx = (int) (tx + (vec1.nX()*v1-vec2.nX()*v2)*C.TILE_SIZE);
						dy = (int) (ty + (vec1.nY()*v1-vec2.nY()*v2)*C.TILE_SIZE);
						if (isEnemy(div, dx, dy) && SProjectiles.problem(div.army(), trajLow, fx, fy, dx, dy, info.projAngle, info.projVel) == null) {
							traj.set(i, trajLow);
							break outer;
						}
					}
					
				}
			}
			
			
			
			
		}
		
		return traj.hasAny();
		
	}
	
	private static boolean isEnemy(Div div, int x, int y) {
		int tx = x >> C.T_SCROLL;
		int ty = y >> C.T_SCROLL;
		if (SETT.IN_BOUNDS(tx, ty)) {
			if (ArmyAIUtil.map().hasEnemy.is(tx, ty, div.army()))
				return true;
		}
		return false;
	}
	
	
}
