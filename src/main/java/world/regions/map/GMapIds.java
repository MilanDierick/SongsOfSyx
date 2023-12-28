package world.regions.map;

import static world.WORLD.*;

import java.util.LinkedList;

import init.RES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.IntChecker;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.pathing.WTRAV;
import world.overlay.WorldOverlays;
import world.regions.centre.WCentre;

class GMapIds implements MAP_INTE {
	
	public final static int NOTHING = 0;
	public final static int PLAYER = 1;
	
	private final int[] ids = new int[TAREA()];
	public final int MAX;
	
//	public final Rec pp = new Rec(WCentre.TILE_DIM);
	private final Polymap polly;
	private final MAP_BOOLEAN edge;
	
	private static final int cellsize = 8;
	
	private final boolean debug = true;
	
	GMapIds(int px, int py, GMapObstacle edge){
		
		this.edge = edge;
		polly = new Polymap(WORLD.TBOUNDS(), cellsize, 1);
		
		if (debug) {
			WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
				@Override
				protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
					int id = get(it.tx(), it.ty());
					if (id == 0)
						return;
					ColorImp.TMP.set(COLOR.UNIQUE.getC(id)).setBrightnessSelf(2.0);
					for (DIR d : DIR.ORTHO) {
						if (get(it.tx(), it.ty(), d) != id) {
							ColorImp.TMP.setBrightnessSelf(0.5);
							break;
						}
					}
					ColorImp.TMP.bind();
					SPRITES.cons().BIG.outline.render(CORE.renderer(), 0, it.x(), it.y());
					
						
					COLOR.unbind();
					
				}
			};
		}

		Rec pp = new Rec(WCentre.TILE_DIM);
		pp.moveC(px+1, py+1);
		
		boolean pharbour = false;
		for (COORDINATE c : pp) {
			set(c, PLAYER);
			pharbour |= WORLD.WATER().isBig.is(c);
		}
		
		
		if (pharbour) {
			
			RES.flooder().init(this);
			if (WTRAV.isGoodLandTile(px, py)) {
				RES.flooder().pushSloppy(px, py, 0);
			}
			if (WORLD.WATER().isBig.is(px, py)) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR d = DIR.ORTHO.get(di);
					int dx = px + d.x();
					int dy = py + d.y();
					if (WTRAV.isGoodLandTile(dx, dy) && WTRAV.canLand(px, py, d, false)) {
						RES.flooder().pushSloppy(dx, dy, 0);
					}
				}
			}
			Rec cc = new Rec(WCentre.TILE_DIM+2);
			cc.moveC(pp.cX(), pp.cY());
			while(RES.flooder().hasMore()) {
				PathTile t = RES.flooder().pollSmallest();
				if (!pp.holdsPoint(t) && WTRAV.isHarbour(t.x(), t.y())) {
					while(t != null) {
						set(t, PLAYER);
						t = t.getParent();
					}
					break;
				}
				
				for (DIR d : DIR.ORTHO) {
					if (cc.holdsPoint(t, d) && WTRAV.canLand(t.x(), t.y(), d, false))
						RES.flooder().pushSmaller(t, d, t.getValue()+1, t);
				}
			}
			
			RES.flooder().done();
		}
		
		LinkedList<Coo> pMark = new LinkedList<>();
		{
			
			
			for (DIR dir : DIR.NORTHO) {
				
				DIR d = dir.next(RND.rInt0(1));
				int sx = px+RND.rInt0(1);
				int sy = py+RND.rInt0(1);
				
				for (int di = -1; di <= 1; di+= 2) {
					for (int i = 0; i < 10; i++) {
						int dx = sx+d.x()*di*i;
						int dy = sy+d.y()*di*i;
						if (!SETT.IN_BOUNDS(dx, dy))
							break;
						if (!WTRAV.isGoodLandTile(dx, dy))
							break;
						if (get(dx, dy) == PLAYER)
							continue;
						pMark.add(new Coo(dx, dy));
						set(dx, dy, PLAYER);
					}
				}
				
			}
			
		}
		
		
		int id = PLAYER+1;
		
//		for (COORDINATE c : WORLD.TBOUNDS()) {
//			if (assignRiver(c.x(), c.y(), id))
//				id++;
//		}
//		
		LinkedList<Coo> smalls = new LinkedList<>();
		for (COORDINATE c : WORLD.TBOUNDS()) {
			int size = assign(c.x(), c.y(), id);
			if (size > 0) {
				id++;
				if (size < 2) {
					smalls.add(new Coo(c));
				}
				
			}
		}
		
		IntChecker check = new IntChecker(id);
		
		while(!smalls.isEmpty()) {
			join(smalls.removeFirst(), check);
		}
		
		for (Coo c : pMark)
			set(c, NOTHING);
		
		MAX = id;
//		player = id;

//		expand();

		
	}
	

	
	@Override
	public int get(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return 0;
		return get(tx+ty*TWIDTH());
	}
	
	@Override
	public int get(int tile) {
		return ids[tile];
	}

	@Override
	public MAP_INTE set(int tile, int value) {
		ids[tile] = value;
		return this;
	}

	@Override
	public MAP_INTE set(int tx, int ty, int value) {
		if (WORLD.IN_BOUNDS(tx, ty))
			set(tx+ty*TWIDTH(), value);
		return this;
	}
	
	private int assign(int tx, int ty, int id) {

		if (get(tx, ty) != 0)
			return 0;
		if (!WTRAV.isGoodLandTile(tx, ty))
			return 0;
		
		int pi = polly.get(tx, ty);
		
		RES.flooder().init(this);
		RES.flooder().pushSmaller(tx, ty, 0);
		int size = 0;
		
		
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			int ii = get(c);
			
			if (ii != 0)
				continue;
			if (!polly.getter.is(c, pi))
				continue;
			
			size++;
			
			set(c, id);
			if (edge.is(c))
				continue;
			
			for (DIR d : DIR.ORTHO) {
				if (WTRAV.isGoodLandTile(c.x()+d.x(), c.y()+d.y()))
					RES.flooder().pushSmaller(c, d, c.getValue()+1);
			}
			
		}
		RES.flooder().done();
		
		return size;
	}
	
	private void join(COORDINATE start, IntChecker check) {

		final int id = get(start);
		check.init();
		
		RES.flooder().init(this);
		RES.flooder().pushSmaller(start.x(), start.y(), 0);
		
		int bestI = -1;
		double best = 0;
		
		int size = 0;
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			int ii = get(c);
			
			if (ii == NOTHING || ii == PLAYER)
				continue;
			
			if (ii != id) {
				
				int ix = ii%WORLD.TWIDTH();
				int iy = ii/WORLD.TWIDTH();
				
				if (!check.isSetAndSet(ii))
					RES.flooder().setValue2(ix, iy, 0);
				double v = 1;
				if (edge.is(c))
					v = 0.25;
				v += RES.flooder().getValue2(ix, iy);
				if (v > best) {
					best = v;
					bestI = ii;
				}
				continue;
			}
			
			size++;
						
			for (DIR d : DIR.ORTHO) {
				if (WTRAV.isGoodLandTile(c.x()+d.x(), c.y()+d.y()))
					RES.flooder().pushSmaller(c, d, c.getValue()+1);
			}
			
		}
		RES.flooder().done();
		
		if (bestI != -1) {
			if (size < 16)
				assignOther(start.x(), start.y(), bestI);
		}else
			assignOther(start.x(), start.y(), 0);
		
	}
	

	

	
	private int assignOther(int bx, int by, int target) {
		
		
		int id = get(bx, by);
		
		if (id == target || id == PLAYER)
			return 0;
		RES.filler().init(this);
		RES.filler().fill(bx,by);
		int area = 0;
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			area ++;
			set(c, target);
			for (DIR d : DIR.ALL) {
				int dx = c.x()+d.x();
				int dy = c.y()+d.y();
				if (WORLD.IN_BOUNDS(dx, dy) && get(dx, dy) == id) {
					RES.filler().fill(dx, dy);
				}
			}
		}
		
		RES.filler().done();
//		for (COORDINATE c : WORLD.TBOUNDS()) {
//			if (get(c) == id)
//				throw new RuntimeException(bx + " " + by);
//		}
		return area;
		
		
	}

};