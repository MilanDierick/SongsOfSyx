package settlement.army.ai.general;

import init.C;
import settlement.army.Div;
import settlement.army.DivMorale;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import settlement.army.order.DivTDataTask;
import settlement.army.order.DivTDataTask.DIVTASK;
import settlement.main.SETT;
import settlement.misc.util.TileRayTracer.Ray;
import snake2d.PathTile;

final class UpdaterDiv {

	private final Context c;
	private final DivTDataStatus status = new DivTDataStatus();
	private final DivTDataTask task = new DivTDataTask();
	private final DivPosition current = new DivPosition();

	UpdaterDiv(Context context){
		this.c = context;
	}
	
	public void update(GDiv d, PathTile abs) {
		
		d.div().order().status.get(status);
		int sx = status.currentPixelCX() >> C.T_SCROLL;
		int sy = status.currentPixelCY() >> C.T_SCROLL;
		
		if (abs == null) {
			stop(d, sx, sy);
			return;
		}
		
		
		PathTile t = c.pathing.getDetailPath(abs, sx, sy, c.pmap.cost, c.pathing.normal);
		
		
		if (DivMorale.PROJECTILES.getD(d.div()) > 0)
			d.div().settings.run();
		else
			d.div().settings.running = false;
		
		
		if (t == null) {
			stop(d, sx, sy);
			return;
		}
		
		set(d, t, sx, sy);
	}
	

	
	private void set(GDiv d, PathTile t, int sx, int sy) {
		
		d.div().order().current.get(current);
		if (t.getParent() == null) {
			c.pmap.markPath(d, t);
			task.stop();
			d.div().order().task.set(task);
			c.pmap.markPos(current);	
			return;
		}
		
		d.div().order().task.get(task);
		
		
		c.pmap.unmarkPos(current);	
		
		PathTile dest = t;
		PathTile old = t;
		while(t.getParent() != null) {
			
			if (SETT.PATH().availability.get(t).isSolid(c.army))
				dest = t;
			if (c.pmap.path.is(t))
				dest = t;
			
			t = t.getParent();
		}
		

		
//		if (old != dest && c.battleLines.is(old)) {
//			c.battleLines.clear(d, old.x(), old.y());
//		}
		
		
		
		
		dest = c.pUtil.getLastStraighPathTile(sx, sy, dest);
		
//		if (c.battleLines.is(dest)) {
//			c.battleLines.deloy(d, dest.x(), dest.y());
//			
//		}
		
		
		if (SETT.PATH().availability.get(dest).isSolid(c.army)) {			
			task.attack(dest.x(), dest.y());
			d.timeout(60);
			d.div().order().task.set(task);
		}else if(c.pmap.path.is(dest)) {
			
			if (checkForExtra(dest.x(), dest.y(), d))
				;
			else if(old.getValue() < 200 && old == dest && c.army.men() > c.army.enemy().men()*2) {
				d.div().settings.moppingSet(true);
				d.timeout(200);
			}
			else
				moveToDest(sx, sy, d, dest);
		}else {
			moveToDest(sx, sy, d, dest);
			
		}
		
		c.pmap.markPos(current);	
	}
	
	private boolean checkForExtra(int tx, int ty, GDiv d) {
		
		for (Ray ray : c.tracer.rays()) {
			
			for (int i = 0; i < ray.size(); i++) {
				int x = tx+ray.get(i).x();
				int y = ty+ray.get(i).y();
				if (!SETT.IN_BOUNDS(x, y))
					break;
				if (SETT.PATH().availability.get(x, y).isSolid(c.army)) {
					d.timeout(80);
					task.attack(x, y);
					d.div().order().task.set(task);
					return true;
				}else if (ArmyAIUtil.map().hasEnemy.is(x, y, c.army)) {
					Div enemy = ArmyAIUtil.map().get(x, y, c.army.enemy());
					task.attackMelee(enemy);
					d.timeout(60);
					d.div().order().task.set(task);
					return true;
				}
			}
			
		}
		return false;
		
	}

	
	private void stop(GDiv d, int sx, int sy) {
		task.stop();
		d.div().order().task.set(task);
		d.div().order().current.get(current);
		c.pmap.markPos(current);
	}
	
	private void moveToDest(int sx, int sy, GDiv d, PathTile dest) {
		if (!c.getDestCoo().isSameAs(sx, sy) && length(dest) < 6) {
			stop(d, sx, sy);
			return;
		}
		
		c.pmap.markPath(d, dest);
		
		DivFormation f = c.pUtil.getDest(sx, sy, d, dest);
		
		
		
		if (f == null || f.centreTile() == null) {
			task.stop();
			d.div().order().task.set(task);
			return;
		}
		
		d.div().order().task.get(task);
		if (task.task() == DIVTASK.MOVE) {
			d.div().order().dest.get(c.form);
			if (c.form.centreTile().tileDistanceTo(f.centreTile()) < 8)
				return;
		}
		
		d.div().order().dest.set(f);
		task.move();
		d.div().order().task.set(task);
	}
	
	private int length(PathTile t) {
		int l = 1;
		while(t != null) {
			l++;
			t = t.getParent();
		}
		return l;
	}
	

	
}
