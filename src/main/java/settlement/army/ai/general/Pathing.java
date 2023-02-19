package settlement.army.ai.general;

import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_DOUBLE;

final class Pathing {

	private final Context c;
	
	Pathing(Context c){
		this.c = c;
	}
	
	public PathTile getPath(int sx, int sy, PATHCOST cost) {
		
		return getPath(sx, sy, cost, normal);
				
	}
	
	public PathTile getPath(int sx, int sy, PATHCOST cost, PATHDEST dest) {
		
		if (!SETT.IN_BOUNDS(sx, sy))
			return null;
	
		PathTile abs = getAbsPath(sx, sy, cost.abs(), dest.abs());
		if (abs == null)
			return null;
		
		return getDetailPath(abs, sx, sy, cost, dest);
		
	}
	
	public PathTile getPath(int sx, int sy, PATHCOST cost, int dx, int dy) {
		
		dest.set(dx, dy);
		
		return getPath(sx, sy, cost, custom);
		
	}
	
	public PathTile getAbsPath(int sx, int sy, MAP_DOUBLE cost, MAP_BOOLEAN dest) {
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		
		sx = sx >> AbsMap.scroll;
		sy = sy >> AbsMap.scroll;
		
		f.pushSloppy(sx, sy, 0);
		
		
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (dest.is(t)) {
				
				f.done();
				return t;
			}
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				int dx = t.x() + DIR.ORTHO.get(di).x();
				int dy = t.y() + DIR.ORTHO.get(di).y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					double v = cost.get(dx, dy);
					f.pushSmaller(dx, dy, t.getValue()+v, t);
				}
					
				
			}
			
		}
		
		f.done();
		
		return null;
	}
	
	private final Rec Tmp = new Rec.RecThreadSafe().setDim(AbsMap.size+2);
	private final Rec Tmp2 = new Rec.RecThreadSafe().setDim(AbsMap.size);
	
	public PathTile getDetailPath(PathTile abs, int sx, int sy, PATHCOST cost, int dx, int dy){
		dest.set(dx, dy);
		return getDetailPath(abs, sx, sy, cost, custom);
	}
	
	public PathTile getDetailPath(PathTile abs, int sx, int sy, PATHCOST cost, PATHDEST dest){
		
		if (abs == null)
			return null;
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		
		{
			PathTile tmp = abs;
			while(tmp != null) {
				Tmp.moveX1Y1(tmp.x()*AbsMap.size-1, tmp.y()*AbsMap.size-1);
				for (COORDINATE coo : Tmp) {
					if (SETT.IN_BOUNDS(coo)) {
						if (Tmp.isOnEdge(coo.x(), coo.y())) {
							f.setValue2(coo, 1);
						}
					}
					
				}
				tmp = tmp.getParent();
			}
			tmp = abs;
			while(tmp != null) {
				Tmp2.moveX1Y1(tmp.x()*AbsMap.size, tmp.y()*AbsMap.size);
				for (COORDINATE coo : Tmp2) {
					f.setValue2(coo, 0);
				}
				tmp = tmp.getParent();
			}
		}
		
		
		
		
		f.pushSloppy(sx, sy, 0);
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (dest.is(t)) {
				f.done();
				return t;
			}
			
			double c = 0;
			
			if (t.getValue2() != 0)
				continue;
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				int dx = t.x() + DIR.ORTHO.get(di).x();
				int dy = t.y() + DIR.ORTHO.get(di).y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double v = cost.get(dx, dy)*DIR.ORTHO.get(di).tileDistance();
					if (v < 0)
						continue;
					f.pushSmaller(dx, dy, c+v, t);
				}
			}
			
		}
		f.done();
		return null;
		
	}
	
	private Coo dest = new Coo();
	
	private final PATHDEST custom = new PATHDEST() {
		
		@Override
		public boolean is(int tx, int ty) {
			return dest.isSameAs(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}
		
		private final MAP_BOOLEAN abs = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return dest.x() >> AbsMap.scroll == tx && dest.y() >> AbsMap.scroll == ty;
			}
			
			@Override
			public boolean is(int tile) {
				throw new RuntimeException();
			}
		};
		
		@Override
		public MAP_BOOLEAN abs() {
			return abs;
		}
	};
	
	public final PATHDEST normal = new PATHDEST() {
		
		@Override
		public boolean is(int tx, int ty) {
			return c.getDestCoo().isSameAs(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}
		
		private final MAP_BOOLEAN abs = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return c.getDestCoo().x() >> AbsMap.scroll == tx && c.getDestCoo().y() >> AbsMap.scroll == ty;
			}
			
			@Override
			public boolean is(int tile) {
				throw new RuntimeException();
			}
		};
		
		@Override
		public MAP_BOOLEAN abs() {
			return abs;
		}
	};
	
	interface PATHDEST extends MAP_BOOLEAN{
		
		public MAP_BOOLEAN abs();
	}
	
	interface PATHCOST extends MAP_DOUBLE {
		
		public MAP_DOUBLE abs();
	}
	
}
