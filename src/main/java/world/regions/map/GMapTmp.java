package world.regions.map;

import static world.WORLD.*;

import init.RES;
import init.biomes.TERRAINS;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import world.WORLD;
import world.map.pathing.WPATHING;
import world.map.pathing.WTRAV;
import world.regions.centre.WCentre;
import world.regions.centre.WorldCentrePlacablity;

final class GMapTmp implements MAP_OBJECTE<GMapTmp.TmpRegion>{

	public final TmpRegion[] all;
	private final GMapIds ids;
	public final TmpRegion player;

	GMapTmp(GMapIds ids, int px, int py){
		this.ids = ids;
		
		boolean[] inited = new boolean[ids.MAX+1];
		all = new TmpRegion[ids.MAX+1];
		
		LinkedList<TmpRegion> list = new LinkedList<>();
		

		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (!WTRAV.isGoodLandTile(c.x(), c.y()))
				continue;
			int i = ids.get(c);
			if (i != GMapIds.NOTHING && !inited[i]) {
				TmpRegion r = init(c.x(), c.y());
				all[i] = r;
				inited[i] = true;
				if (r != null) {
					list.add(r);
					if (ids.get(r.c) != r.id)
						throw new RuntimeException();
				}
			}
			
		}
		
		all[ids.MAX] = new TmpRegion(ids.MAX);
		player = all[GMapIds.PLAYER];
	}
	
	
	@Override
	public TmpRegion get(int tile) {
		return all[ids.get(tile)];
	}

	@Override
	public TmpRegion get(int tx, int ty) {
		return all[ids.get(tx, ty)];
	}

	@Override
	public void set(int tile, TmpRegion object) {
		
		int tx = tile%WORLD.TWIDTH();
		int ty = tile/WORLD.TWIDTH();
		TmpRegion r = get(tile);
		if (r != null && r != object)
			throw new RuntimeException();
		if (object == null)
			ids.set(tile, 0);
		else if (r != object){
			ids.set(tile, object.id);
			if (object.area == 0)
				object.bounds.moveX1Y1(tx, ty).setDim(1);
			else
				object.bounds.unify(tx, ty);
			object.area++;
			object.ferArea += 0.2+0.8*WORLD.FERTILITY().map.get(tx, ty);
			
		}
	}

	@Override
	public void set(int tx, int ty, TmpRegion object) {
		if (WORLD.IN_BOUNDS(tx, ty))
			set(tx+ty*TWIDTH(), object);
		
	}

	private TmpRegion init(int x, int y) {
		final int id = ids.get(x, y);
		
		final TmpRegion r = new TmpRegion(id);
		all[id] = r;
		r.bounds.setDim(1).moveX1Y1(x, y);
		RES.filler().init(this);
		RES.filler().fill(x,y);
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			r.terrains[TERRAINS.world.get(c).index()]++;
			r.area++;
			r.ferArea += 0.2+0.8*WORLD.FERTILITY().map.get(c);
			r.bounds.unify(c.x(), c.y());
			for (DIR d : DIR.ALL) {
				int dx = c.x()+d.x();
				int dy = c.y()+d.y();
				if (WORLD.IN_BOUNDS(dx, dy) && ids.get(dx, dy) == id) {
					RES.filler().fill(dx, dy);
				}
			}
		}
		RES.filler().done();
		r.setNewCentre(x, y, this);
		return r;
	}
	
	private boolean assignRiver(int tx, int ty) {

		if (!WORLD.WATER().RIVER.is(tx, ty))
			return false;
		
		if (!can(tx, ty))
			return false;
		
		int di = RND.rInt(4);
		TmpRegion id = null;
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.getC(i+di);
			id = get(tx, ty, d);
			if (id != null && id != player) {
				break;
			}
			
		}
		
		if (id == null)
			return false;
		
		if (id == player)
			return false;
		
		RES.flooder().init(this);
		RES.flooder().pushSmaller(tx, ty, 0);

		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			TmpRegion ii = get(c);
			if (ii == player)
				continue;
			if (!can(c.x(), c.y()) || (ii != null && ii != id)) {
				continue;
			}
			
			
			boolean found = false;
			for (DIR d : DIR.ALL) {
				if (get(c, d) == id && !WORLD.WATER().RIVER.is(c, d)) {
					found = true;
					break;
				}
			}
			set(c, id);
			
			if (found) {
				for (DIR d : DIR.ORTHO) {
					if (WORLD.WATER().RIVER.is(c, d) && !WORLD.MOUNTAIN().coversTile(c.x()+d.x(), c.y()+d.y()))
						RES.flooder().pushSmaller(c, d, c.getValue()+1);
				}
			}
			
		}
		RES.flooder().done();
		
		return true;
	}
	
//	public TmpRegion setPlayer() {
//		
//		Rec body = new Rec(ids.pp);
//		
//		RES.filler().init(this);
//		RES.filler().fill(player.c);
//		for (COORDINATE c : body) {
//			RES.filler().fill(c.x(), c.y());
//			set(c, all[ids.MAX]);
//		}
//		while(RES.filler().hasMore()) {
//			COORDINATE c = RES.filler().poll();
//			if (get(c) != null && !is(c, all[ids.MAX]))
//				continue;
//			if (!WTRAV.isGoodLandTile(c.x(), c.y()))
//				continue;
//			
//			set(c, all[ids.MAX]);
//			body.unify(c.x(), c.y());
//			for (DIR d : DIR.ORTHO) {
//				RES.filler().fill(c, d);
//			}
//		}
//		RES.filler().done();
//		all[ids.MAX].c.set(ids.pp.cX(), ids.pp.cY());
//		return all[ids.MAX];
//	}
	
	public void expand() {
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			assignRiver(c.x(), c.y());
		}
		
		RES.flooder().init(this);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			TmpRegion id = get(c);
			if (id == player)
				RES.flooder().close(c.x(), c.y(), 0);
			else if (id != null) {
				RES.flooder().pushSloppy(c, 0);
				RES.flooder().setValue2(c, id.id);
			}
			
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			TmpRegion id = all[(int)t.getValue2()];
			if (get(t) != id)
				set(t, id);
			
			boolean m = WORLD.MOUNTAIN().coversTile(t.x(), t.y());

			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (m && !WORLD.MOUNTAIN().coversTile(dx, dy))
					continue;
				if (isExpandable(t.x(), t.y(), dx, dy, id)) {
					if (RES.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()*WPATHING.getTerrainCost(dx, dy)) != null)
						RES.flooder().setValue2(dx, dy, t.getValue2());
					
				}
				
			}
			
		}
		RES.flooder().done();
		
		
		
	}
	
	
	private boolean isExpandable(int fromX, int fromY, int tx, int ty, TmpRegion id) {
		if (!WORLD.IN_BOUNDS(tx, ty))
			return false;
		
		if (get(tx, ty) == player)
			return false;
		
		if (get(tx, ty) != null)
			return false;
		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY) && WORLD.MOUNTAIN().coversTile(tx, ty))
			return false;
		
		if (!is(fromX, ty, id) && !is(tx, fromY, id))
			return false;
		
		if (WORLD.MOUNTAIN().coversTile(tx, ty)) {
			for (int di = 0; di< DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (!WORLD.MOUNTAIN().coversTile(tx+d.x(), ty+d.y()))
					return true;
			}
			return false;
		}
		if (WORLD.WATER().has.is(tx, ty)) {
			if (!WORLD.WATER().coversTile.is(tx, ty))
				return true;
			for (int di = 0; di< DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.IN_BOUNDS(tx, ty,d) && !WORLD.WATER().coversTile.is(tx+d.x(), ty+d.y()))
					return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean can(int tx, int ty) {
		if (get(tx, ty) != null)
			return false;
		if (get(tx, ty) == player)
			return false;
		return true;
	}
	
	static class TmpRegion {
		
		Coo  c = new Coo();
		int area;
		double ferArea;
		boolean done = false;
		private boolean isOk = false;
		boolean valid = true;
		public final int id;
		final int[] terrains = new int[TERRAINS.ALL().size()];
		float fertility = 0;
		final Rec bounds = new Rec();
		
		
		TmpRegion(int id){
			this.id = id;
			
			
		}
		
		void absorb(TmpRegion other, GMapTmp ids, boolean nCentre) {
			done |= other.done;
			isOk |= other.isOk;
			area += other.area;
			ferArea += other.ferArea;
			bounds.unify(other.bounds);
			if (!other.valid)
				throw new RuntimeException();
			
			if (other.id == GMapIds.PLAYER)
				throw new RuntimeException();
			
			other.done = true;
			other.isOk = false;
			other.valid = false;
			
			
			
			for (int i = 0; i < terrains.length; i++) {
				terrains[i] += other.terrains[i];
			}
			int aa = other.area;
			for (COORDINATE c : other.bounds) {
				if (ids.ids.get(c) == other.id) {
					ids.ids.set(c, id);
					bounds.unify(c.x(), c.y());
					other.area --;
				}
			}
			
			if (other.area != 0)
				throw new RuntimeException(aa + " "+ other.area + " " + other.id);
			other.bounds.clear();
			
			if (nCentre)
				setNewCentre(c.x(), c.y(), ids);
			
		}
		
		void setNewCentre(int cx, int cy, GMapTmp ids) {
			
			for (int i = 0; i < RES.circle().length(); i++) {
				int dx = cx + RES.circle().get(i).x();
				int dy = cy + RES.circle().get(i).y(); 
				if (ids.is(dx, dy, this) && WTRAV.isGoodLandTile(dx, dy)) {
					c.set(dx, dy);
					return;
				}
			}
			throw new RuntimeException();
		}
		
		private boolean canBeCentre(COORDINATE c, GMapTmp map) {
			if (WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
				for (int y = -1; y < WCentre.TILE_DIM+2; y++) {
					for (int x = -1; x < WCentre.TILE_DIM+2; x++) {
						int dx = c.x()+x;
						int dy = c.y()+y;
						if (!WORLD.IN_BOUNDS(dx, dy))
							return false;
						if (map.get(dx, dy) != null && map.get(dx, dy) != this)
							return false;
						
						
						
					}
				}
				return true;
			}
			return false;
		}
		
		public boolean hasCentre(GMapTmp map) {
			
			if (!valid)
				return false;
			
			if (isOk)
				return true;
			
			RES.filler().init(this);
			RES.filler().fill(c);

			while(RES.filler().hasMore()) {
				
				COORDINATE c = RES.filler().poll();
				if (canBeCentre(c, map)) {
					RES.filler().done();
					isOk = true;
					return true;
				}
				
				for (DIR d : DIR.ORTHO) {
					int dx = c.x()+d.x();
					int dy = c.y()+d.y();
					
					
					if (WORLD.IN_BOUNDS(dx, dy) && map.get(dx, dy) == this) {
						RES.filler().fill(dx, dy);
					}
				}
			}
			RES.filler().done();
			return false;
			
		}
		
		public boolean hasCentre() {
			return valid & isOk;
		}
		
		
	}
	
}