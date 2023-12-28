package settlement.army.ai.general;

import game.time.TIME;
import init.C;
import settlement.army.ai.general.MDivs.MDiv;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;

final class StepMoveToThrone {
	
	private final Context context;
	
	public StepMoveToThrone(Context context) {
		this.context = context;
	}
	
	public boolean setToThrone(ArrayList<MDiv> toDeploy, Bitmap2D blocked, MDivs data) {
		
		
		context.finder.clear();
		
		int am = 0;
		for (MDiv d : data.activeDivs) {
			if (!d.isDeployed && d.busyUntil < TIME.currentSecond() && !d.div.settings.isFighting()) {
				context.finder.add(d);
				am++;
			}
			
		}
		
		if (am == 0)
			return false;
		
		Flooder f = context.flooder.getFlooder();
		f.init(this);
		f.pushSloppy(context.getDestCoo(), 0);
		

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			
			for (MDiv m : context.finder.get(t.x(), t.y())) {
				m.isDeployed = true;
				f.done();
				
				m.distance = t.getValue();
				PathTile dest = setDest(m, blocked, t);					
				block(t, dest, blocked);

				toDeploy.add(m);
				return true;
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(context, dx, dy);
					
					if (cost > 0) {
						if (!dir.isOrtho()) {
							cost = Math.max(cost, cost(context, dx, t.y()));
							cost = Math.max(cost, cost(context, t.x(), dy));
						}
						if (blocked.is(dx, dy)) {
							cost *= 4;
						}
						f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t);
					}
				}
				
			}
		}
		f.done();
		return false;
	}
	
	
	public static double cost(Context context, int dx, int dy) {
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.army) || SETT.TERRAIN().get(dx, dy) instanceof TFortification) {
			return 3 + SETT.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().environment.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}
	
	private static void block(PathTile t, PathTile dest, Bitmap2D toBlock) {
		while(t != dest) {
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				toBlock.set(t, DIR.ORTHO.get(i), true);
			}
			t = t.getParent();
		}
	}

	
	private PathTile setDest(MDiv div, Bitmap2D blocked, PathTile t) {
		
		if (initBlocked(div, DIR.C, t.x(), t.y(), t))
			return t;
		
		if (t.getParent() == null) {
			div.destX = t.x();
			div.destY = t.y();
			div.destDir = DIR.C;
			return t;
		}
		
		PathTile prev = null;
		boolean b = true;
		while(t.getParent() != null) {	
			if (initBlocked(div, blocked, t))
				return t.getParent();
			if (!b && blocked.is(t.getParent())) {
				div.destX = t.x();
				div.destY = t.y();
				div.destDir = DIR.get(t, t.getParent());
				return t;
			}
			prev = t;
			b = false;
			t = t.getParent();
		}
		div.destX = t.x();
		div.destY = t.y();
		div.destDir = DIR.get(prev, t);
		return t;
	}
	
	private boolean initBlocked(MDiv div, Bitmap2D blocked, PathTile block) {
		if (initBlocked(div, DIR.get(block.getParent(), block), block.getParent().x(), block.getParent().y(), block))
			return true;
		if (initBlocked(div, DIR.get(block.getParent(), block), block.getParent().x(), block.y(), block))
			return true;
		if (initBlocked(div, DIR.get(block.getParent(), block), block.x(), block.getParent().y(), block))
			return true;
		return false;
	}
	
	private boolean initBlocked(MDiv div, DIR d, int dx, int dy, PathTile block) {
		if (SETT.PATH().availability.get(dx,dy).isSolid(div.div.army()) || ArmyAIUtil.map().hasEnemy.is(dx, dy, context.army)) {
			div.destX = dx;
			div.destY = dy;
			div.destDir = d;
			return true;
		}
		return false;
	}

	
		
}
