package settlement.army.ai.general;

import static settlement.main.SETT.*;

import init.C;
import settlement.army.Div;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import settlement.room.main.ROOMS;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.thing.projectiles.Trajectory;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.*;

class ArtilleryTargets {


	private final Context c;
	final AbsMap targeted = new AbsMap(1);
	private Bitmap1D mark = new Bitmap1D(SETT.TAREA, false);
	private final ArrayListResize<ArtilleryInstance> ins = new ArrayListResize<>(256, ROOMS.ROOM_MAX);
	private final Trajectory traj = new Trajectory();
	private final int radius = 20*C.TILE_SIZE;
	private final ArrayList<Div> list = new ArrayList<>(1);
	
	public ArtilleryTargets(Context c) {
		this.c = c;

	}
	
	boolean bombard() {
		
		int enemy = fillRoomsAndGetEnemyTraj();
		if (ins.size() == 0)
			return enemy < 0;
		
		Flooder f = c.flooder.getFlooder();
		f.init(f);
		mark.clear();
		targeted.clear();
		for (ArtilleryInstance i : ins) {
			mark.set(i.mX()+i.mY()*TWIDTH, true);
		}
		
		f.pushSloppy(c.getDestCoo().x(), c.getDestCoo().y(), 0);
		
		int am = 0;
		
		while(f.hasMore() && ins.size() > 0) {
			PathTile t = f.pollSmallest();
			if (mark.get(t.x()+t.y()*SETT.TWIDTH)) {
				if (pushTarget(t))
					am++;
				continue;
			}
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					if (!SETT.PATH().availability.get(dx, dy).isSolid(c.army) || SETT.ARMIES().map.attackable.is(dx, dy, c.army))
						f.pushSmaller(dx, dy, t.getValue()+d.tileDistance(), t);
				}
			}
			
		}
		
		f.done();
		
		return am > 0 && am>=enemy;
	}
	
	private boolean pushTarget(PathTile t) {
		
		for(int i = 0; i < ins.size(); i++) {
			ArtilleryInstance ii = ins.get(i);
			if (ii.mX() == t.x() && ii.mY() == t.y()) {
				ins.remove(ii);
				if (pushTarget(ii, t))
					return true;
			}
		}
		return false;
	}
	
	private boolean pushTarget(ArtilleryInstance ins, PathTile t) {
		
		while(t != null) {
			PathTile coo = t;
			t = t.getParent();
			
			if (targeted.get(AbsMap.getI(coo)) == 1)
				continue;
			
			if (SETT.ENV().environment.SPACE.get(coo) < 0.5) {
				list.clearSloppy();
				ArmyAIUtil.quads().getNearest(list, coo.x()*C.TILE_SIZE, coo.y()*C.TILE_SIZE, radius, c.army, null);
				if (list.size() > 0) {
					targeted.set(AbsMap.getI(coo), 1);
					continue;
				}
				
				
				int i = 0;
				while (c.circle.radius(i) <= 5) {
					int tx = coo.x()+c.circle.get(i).x();
					int ty = coo.y()+c.circle.get(i).y();
					i++;
					if (isTarget(tx, ty)) {
						int x = tx*C.TILE_SIZE + C.TILE_SIZEH;
						int y = ty*C.TILE_SIZE+C.TILE_SIZEH;
						if (ins.testTarget(x, y, traj, false) == null) {
							ins.targetCooSet(x,y, false, false);
							targeted.set(AbsMap.getI(tx, ty), 1);
							return true;
						}
						
					}
				}
				
			}
		}
		return false;
	}
	
	private boolean isTarget(int x, int y) {
		if (!SETT.IN_BOUNDS(x, y))
			return false;
		if (targeted.get(AbsMap.getI(x, y)) == 1)
			return false;
		if (!SETT.PATH().availability.get(x, y).tileCollide)
			return false;
		if (!SETT.ARMIES().map.attackable.is(x, y, c.army))
			return false;
		return true;
	}
	

	
	private int fillRoomsAndGetEnemyTraj() {
		ins.clear();
		int am = 1;
		for (int ai = 0; ai < SETT.ROOMS().ARTILLERY.size(); ai++) {
			SETT.ROOMS().ARTILLERY.get(ai).threadInstances(ins);
		}
		
		for (int ii = 0; ii < ins.size(); ii++) {
			ArtilleryInstance i = ins.get(ii);
			
			if (i.army() != c.army ) {
				if (i.isFiring())
					am++;
				ins.remove(ii);
				ii--;
				continue;
			}
			
			if (i.menMustering() == 0) {
				ins.remove(ii);
				ii--;
				continue;
			}
			if (i.targetDivGet() != null) {
				ins.remove(ii);
				ii--;
				am--;
				continue;
			}

			COORDINATE c = i.targetCooGet();
			if (c != null) {
				int tx = c.x()>>C.T_SCROLL;
				int ty = c.y()>>C.T_SCROLL;
				if (ArmyAIUtil.quads().ART.is(tx, ty, this.c.army.enemy())) {
					ins.remove(ii);
					ii--;
					am--;
					continue;
				}
			}
		}
		return am;
	}
	

	


	


	
}
