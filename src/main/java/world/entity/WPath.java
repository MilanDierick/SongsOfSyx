package world.entity;

import java.io.IOException;

import init.C;
import init.sprite.SPRITES;
import snake2d.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.World;
import world.entity.WPathing.WorldPathCost;
import world.entity.army.WArmy;

public final class WPath extends Path.PathSync{

	short destX,destY = -1;
	byte dir;
	private float movement;
	private static final double sqrt = Math.sqrt(2);
	private static final WPath tmp = new WPath();
	
	public WPath() {
		super(64);
		dir = (byte) RND.rInt(DIR.ALL.size());
	}
	
	@Override
	public void save(FilePutter file) {
		file.s(destX);
		file.s(destY);
		file.b(dir);
		file.f(movement);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		destX = file.s();
		destY = file.s();
		dir = file.b();
		movement = file.f();
		super.load(file);
	}
	
	@Override
	public void clear() {
		destX = -1;
		destY = -1;
		movement = 0;
		super.clear();
	}
	
	public WPath copyTmp() {
		copyTo(tmp);
		return tmp;
	}
	
	public int destX() {
		return destX;
	}
	
	public int destY() {
		return destY;
	}
	
	public boolean isValid() {
		return destX > 0 && destY > 0;
	}
	
	public DIR dir() {
		return DIR.ALL.get(dir);
	}
	
	public boolean arrived() {
		return destX == x() && destY == y();
	}
	
	public boolean intercept(WArmy e, double speed, WorldPathCost cost, WEntity target) {
		
		
		if (target == null) {
			movement = 0;
			return false;
		}
		
		if (Math.abs(e.body().cX()-target.body().cX()) <= C.TILE_SIZEH && Math.abs(e.body().cY() -target.body().cY()) <= C.TILE_SIZEH) {
			movement = 0;
			return false;
		}
		
		
		
		
		if (destX == -1 || destY == -1) {
			movement = 0;
			return false;
		}
		
		movement += speed;
		

		while(intercept(e.body(), cost, target))
			;
		return true;
		
	}
	
	public boolean move(WEntity e, double speed, WorldPathCost cost) {
		
		if (!moving(e.body()))
			return false;
		
		
		movement += speed*(World.BUILDINGS().roads.is(e.ctx(), e.cty()) ? 1 : 0.8);
		
		while(move(e.body(), cost));
		
		return true;
		
	}
	
	private boolean intercept(RECTANGLEE e, WorldPathCost cost, WEntity target) {
		if (movement < sqrt)
			return false;
		
		int tx = target.ctx();
		int ty = target.cty();
		
		if (Math.abs(ty-destY) + Math.abs(tx-destX) > 3)
			if (!WPathing.intercept(x(), y(), target, this, cost))
				return false;
		
		int cx = e.cX();
		int cy = e.cY();
		
		int dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
		int dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
		
		
		if (cx == dx && cy == dy) {
			if (target.ctx() == cx && target.cty() == cy) {
				movement = 0;
				return false;
			}
			if (!hasNext()) {
				if (!WPathing.intercept(x(), y(), target, this, cost))
					return false;
			}
			if (!hasNext()) {
				return false;
			}
			setNext();
			dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
			dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
		}
		
		int x = dx-cx;
		int y = dy-cy;
		
		
		
		if (movement >= 1 && Math.abs(x) != Math.abs(y)) {
			movement -= 1;
			x = CLAMP.i(x, -1, 1);
			y = CLAMP.i(y, -1, 1);
			dir = (byte) DIR.get(x, y).id();
			e.moveC((cx+x), (cy+y));
			
		}else if (movement >= sqrt) {
			movement -= sqrt;
			x = CLAMP.i(x, -1, 1);
			y = CLAMP.i(y, -1, 1);
			dir = (byte) DIR.get(x, y).id();
			e.moveC((cx+x), (cy+y));
		}

		return movement > sqrt;
	}
	
	private boolean move(RECTANGLEE e, WorldPathCost cost) {
		if (movement < sqrt)
			return false;
		
		int cx = e.cX();
		int cy = e.cY();
		
		int dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
		int dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
		
		if (cx == dx && cy == dy) {
			if (arrived()) {
				movement = 0;
				return false;
			}
			if (!hasNext())
				if (!WPathing.path(x(), y(), destX, destY, this, cost))
					return false;
			if (!hasNext())
				return false;
			setNext();
			dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
			dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
		}
		
		int x = dx-cx;
		int y = dy-cy;
		
		
		
		if (movement >= 1 && Math.abs(x) != Math.abs(y)) {
			movement -= 1;
			x = CLAMP.i(x, -1, 1);
			y = CLAMP.i(y, -1, 1);
			dir = (byte) DIR.get(x, y).id();
			e.moveC((cx+x), (cy+y));
			
		}else if (movement >= sqrt) {
			movement -= sqrt;
			x = CLAMP.i(x, -1, 1);
			y = CLAMP.i(y, -1, 1);
			dir = (byte) DIR.get(x, y).id();
			e.moveC((cx+x), (cy+y));
		}

		return movement > sqrt;
	}
	
	public boolean moving(RECTANGLEE e) {
		
		if (destX == -1 || destY == -1) {
			movement = 0;
			return false;
		}
		
		if (x() == destX && y() == destY) {
			int cx = e.cX();
			int cy = e.cY();
			int dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
			int dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
			if (cx == dx && cy == dy)
				return false;
		}
		return true;
	}
	
	public void render(SPRITE_RENDERER r, int offX, int offY) {
		if (!isValid())
			return;
		
		
		
		copyTo(tmp);
		
		
		while(tmp.hasNext()) {
			render(r, tmp.x(), tmp.y(), -offX, -offY);
			tmp.setNext();
		}
		
		while(tmp.x() != destX && tmp.y() != destY) {
			if (!WPathing.path(tmp.x(), tmp.y(), destX, destY, tmp)) {
				
				LOG.ln("no!");
				return;
			}
			while(tmp.hasNext()) {
				render(r, tmp.x(), tmp.y(), -offX, -offY);
				tmp.setNext();
			}
			
		}
		
	}
	
	private void render(SPRITE_RENDERER r, int tx, int ty, int offX, int offY) {
		SPRITES.icons().s.dot.game.renderC(r, tx*C.TILE_SIZE + C.TILE_SIZEH - offX, ty*C.TILE_SIZE+C.TILE_SIZEH-offY);
	}
	
}
