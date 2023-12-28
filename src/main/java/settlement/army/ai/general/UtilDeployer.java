package settlement.army.ai.general;

import game.time.TIME;
import init.C;
import settlement.army.Div;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.army.ai.general.UtilLines.Line;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.formation.DIV_FORMATION;
import settlement.army.formation.DivFormation;
import settlement.army.order.DivTDataTask;
import settlement.main.SETT;
import snake2d.util.datatypes.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;

/**
 * uses the prelines, and sets them to divs. 
 * @author Jake
 *
 */
class UtilDeployer {
	
	private final Context context;

	public UtilDeployer(Context context) {
		this.context = context;
	}
		
	public boolean deploy(ArrayList<MDiv> toFill, Bitmap2D blocked) {
		
		if (toFill.size() == 0)
			return false;
		
		for (MDiv d : toFill) {
			deploy(d, blocked);
		}
		
		toFill.clearSloppy();
		return true;
	}
	private final DivTDataTask task = new DivTDataTask();
	
	private final Rec blocker = new Rec();
	private final DivFormation current = new DivFormation();
	
	private void deploy(MDiv d, Bitmap2D blocked) {
		
		int w = (int) Math.sqrt(d.men);
		blocker.setDim(w);
		blocker.moveX1Y1(d.tx-w/2, d.ty-w/2);
		
		for (COORDINATE c : blocker)
			blocked.set(c, true);
		
		
		if (d.destX == -1)
			deployLine(d, blocked);
		else {
			deployTile(d, blocked);
		}
			
		
		
	}
	
	private void deployLine(MDiv d, Bitmap2D blocked) {
		
		Line l = d.line(context.preLines);
		
		int x1 = l.tx1;
		int y1 = l.ty1;
		int wi = l.tileLength;
		DIR dir = l.dir;
		x1 -= dir.x() * l.back;
		y1 -= dir.y() * l.back;
		
		dir = dir.next(2);
		DivFormation f = context.deployer.deploy(d.men, DIV_FORMATION.LOOSE, (int)(x1*C.TILE_SIZE), (int) (y1*C.TILE_SIZE), dir.xN(), dir.yN(), wi*C.TILE_SIZE, context.army);
		if (f != null) {

			for (int i = 0; i < f.deployed(); i++) {
				blocked.set(f.tile(i), true);
			}
			l.back += Math.ceil((double)d.men/wi) + 1;
			d.isDeployed = true;
			if (setInPosition(d, f)) {
				;
				
			}else {
				d.div.order().dest.set(f);
				task.move();
				d.div.order().task.set(task);
			}

		}else {
			task.stop();
			d.div.order().task.set(task);
			l.active = false;
		}
		
	}
	
	
	
	private void deployTile(MDiv d, Bitmap2D blocked) {

		if (attackTile(d.destX, d.destY, d)) {
			;
		}else if (attackDiv(d.destX, d.destY, d)) {
			;
		}else {
			moveToDest(d);
		}
		
		
	}
	
	private void moveToDest(MDiv d) {
		
		DIR dir = d.destDir.next(2);
		int rm = (int) (1 + Math.sqrt(d.div.menNrOf()/2.0))*d.div.settings.formation.size;
		DivFormation f = context.deployer.deployArroundCentre(d.div.menNrOf(), d.div.settings.formation, (d.destX<<C.T_SCROLL) + C.TILE_SIZEH, (d.destY<<C.T_SCROLL) + C.TILE_SIZEH, dir.xN(), dir.yN(), rm, d.div.army());
		
		
		if (f == null) {
			task.stop();
			d.div.order().task.set(task);
			d.isDeployed = true;
			return;
		}
		
		if (setInPosition(d, f)) {
			checkForExtra(d.destX, d.destY, d);
		}else {
			d.div.order().dest.set(f);
			task.move();
			d.div.order().task.set(task);
		}
		


	}
	

	
	private boolean checkForExtra(int tx, int ty, MDiv d) {
		
		if (attackTile(tx+d.destDir.x(), ty+d.destDir.y(), d))
			return true;
		
		if (attackTile(tx+d.destDir.next(1).x(), ty+d.destDir.y(), d))
			return true;
		
		if (attackTile(tx+d.destDir.x(), ty+d.destDir.next(1).y(), d))
			return true;
		
//		for (Ray ray : context.tracer.rays()) {
//			
//			for (int i = 0; i < ray.size(); i++) {
//				int x = tx+ray.get(i).x();
//				int y = ty+ray.get(i).y();
//				if (!SETT.IN_BOUNDS(x, y))
//					break;
//				if (attackTile(x, y, d)) {
//					return true;
//				}else if (ArmyAIUtil.map().hasEnemy.is(x, y, context.army)) {
//					Div enemy = ArmyAIUtil.map().get(x, y, context.army.enemy());
//					task.attackMelee(enemy);
//					d.busyUntil = TIME.currentSecond()+20;
//					d.div.order().task.set(task);
//					return true;
//				}
//			}
//			
//		}
		return false;
		
	}
	
	private boolean attackDiv(int tx, int ty, MDiv d) {
		
		Div enemy = ArmyAIUtil.map().get(tx, ty, context.army.enemy());
		
		if (enemy != null) {
			
			task.attackMelee(enemy);
			d.div.order().task.set(task);
			return true;
		}
		return false;
	}
	
	private boolean attackTile(int tx, int ty, MDiv d) {
		
		
		
		if (SETT.IN_BOUNDS(tx, ty) && SETT.PATH().availability.get(tx, ty).isSolid(context.army)) {
			
			d.div.order().task.get(task);
			
			if (task.targetTile() != null) {
				if (COORDINATE.tileDistance(task.targetTile().x(), task.targetTile().y(), tx, ty) < 3)
					return true;
			}
			
			d.busyUntil = TIME.currentSecond()+20;
			task.attack(tx, ty);
			d.div.order().task.set(task);
			return true;
		}
		return false;
	}
	
	private boolean setInPosition(MDiv d, DivFormation f) {
		d.div.order().next.get(current);
		boolean pos = current.dx() == f.dx() && current.dy() == f.dy() && current.start().isSameAs(f.start()) && current.width() == f.width();
		if (pos) {
			if (TIME.currentSecond() < d.inPositionSince)
				d.inPositionSince = TIME.currentSecond();
			return true;
		}else {
			d.inPositionSince = Integer.MAX_VALUE;
			return false;
		}
	}

}