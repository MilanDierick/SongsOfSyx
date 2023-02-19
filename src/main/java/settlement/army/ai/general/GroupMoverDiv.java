package settlement.army.ai.general;

import init.C;
import settlement.army.ai.general.Groups.GroupLine;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import settlement.army.order.DivTDataTask;
import settlement.army.order.DivTDataTask.DIVTASK;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.VectorImp;

final class GroupMoverDiv {

	private final Context c;
	private final DivTDataTask task = new DivTDataTask();
	private final DivPosition current = new DivPosition();

	GroupMoverDiv(Context context){
		this.c = context;
	}
	
	public boolean moreIntoFormation(GDiv div, GroupLine l, int tx1, int ty1, int tw, PathTile abs, boolean firstLine) {
		
		
		PathTile dest = null;
		PathTile old = null;
		
		if (abs != null) {

			PathTile t = c.pathing.getDetailPath(abs, div.tx, div.ty, c.pmap.cost, tx1, ty1);
			if (t == null) {
				stop(div);
				return false;
			}
			
			
			dest = t;
			old = t;
			while(t.getParent() != null) {
				
				if (SETT.PATH().availability.get(t).isSolid(c.army))
					dest = t;
				if (SETT.PATH().availability.get(t.getParent().x(), t.y()).isSolid(c.army))
					dest = t;
				if (SETT.PATH().availability.get(t.x(), t.getParent().y()).isSolid(c.army))
					dest = t;
//				if (c.pmap.path.is(t))
//					dest = t;
				
				t = t.getParent();
			}
			
	
		
			
			c.pmap.markPath(div, dest);
		}
		
		
		if (old == dest) {
			int x1 = tx1*C.TILE_SIZE + C.TILE_SIZEH;
			int y1 = ty1*C.TILE_SIZE + C.TILE_SIZEH;
			if (checkForBreakable(l, tx1, ty1, div, tw)) {
				div.updated = true;
				return true;
			}
			
			if (!firstLine && checkForBreakableExtra(tx1, ty1, div)) {
				div.updated = true;
				return true;
			}
			
			
			DivFormation f = c.deployer.deploy(div.div().menNrOf(), div.div().settings.formation, x1, y1, l.v.nX(), l.v.nY(), tw*C.TILE_SIZE, c.army);
			if (f == null) {
				stop(div);
				return false;
			}
			c.pmap.markPos(f);
			div.div().order().dest.set(f);
			task.move();
			div.div().order().task.set(task);
			div.updated = true;
			return true;
		}
		
		div.updated = true;
		if (SETT.PATH().availability.get(dest).isSolid(c.army)) {
			task.attack(dest.x(), dest.y());
			div.div().order().task.set(task);
			
			
		}else {
			moveToDest(div, dest);
			
		}
		return true;
	}
	private final VectorImp vec = new VectorImp();
	
	private boolean checkForBreakable(GroupLine l, int tx, int ty, GDiv d, int width) {
	
		vec.set(l.v);
		vec.rotate90();
		int hi = (int) Math.ceil(d.div().menNrOf()/width);
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < hi; h++) {
				int x = (int) (tx+l.v.nX()*w + h*vec.nX());
				int y = (int) (ty+l.v.nY()*w + h*vec.nY());
				if (!SETT.IN_BOUNDS(x, y))
					break;
				if (SETT.PATH().availability.get(x, y).isSolid(c.army)) {
					task.attack(x, y);
					d.div().order().task.set(task);
					return true;
				}
			}
			
		}
		return false;
		
	}
	
	private boolean checkForBreakableExtra(int tx, int ty, GDiv d) {
		
		int i = 0;
		while(c.circle.radius(i) < 10) {
			int x = c.circle.get(i).x()+tx;
			int y = c.circle.get(i).y()+ty;
			if (SETT.IN_BOUNDS(x, y) && SETT.PATH().availability.get(x, y).isSolid(c.army)) {
				d.timeout(80);
				task.attack(x, y);
				d.div().order().task.set(task);
				return true;
			}
			i++;
			
		}
		return false;
		
	}

	private void stop(GDiv d) {
		task.stop();
		d.div().order().task.set(task);
		d.div().order().current.get(current);
		c.pmap.markPos(current);
	}
	
	private void moveToDest(GDiv d, PathTile dest) {
		
		DivFormation f = c.pUtil.getDest(d.tx, d.ty, d, dest);
		
		if (f == null) {
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

	

	
}
