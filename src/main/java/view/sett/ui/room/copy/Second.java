package view.sett.ui.room.copy;

import init.sprite.ICON.MEDIUM;
import init.sprite.SPRITES;
import settlement.job.Job;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import view.tool.PLACABLE;
import view.tool.PlacableFixed;

final class Second extends PlacableFixed{

	private final Dest dest;
	private final RoomChecker room;
	
	Second(Dest dest){
		this.dest = dest;
		room = new RoomChecker(dest);
	}
	
	@Override
	public void init(int cx, int cy) {
		dest.init(cx, cy, rot());
		room.init();
	}

	@Override
	public int width() {
		return dest.body().width();
	}

	@Override
	public int height() {
		return dest.body().height();
	}
	
	@Override
	public CharSequence placable(int tx, int ty, int rx, int ry) {
		return null;
	}

	@Override
	public void place(int tx, int ty, int rx, int ry) {
		
		if (!dest.is(tx, ty))
			return;
		
		
		if (room.place(tx, ty)) {
			
		}else {
			COORDINATE s = dest.transform(tx, ty);
			Job j = Jobs.get(s.x(), s.y());
			if (j != null && j.placer().isPlacable(tx, ty, null, null) == null) {
				j.placer().place(tx, ty, null, null);
			}
		}
		
		
		
			
		
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, int rx, int ry,
			boolean isPlacable, boolean areaIsPlacable) {
		
		
		if (blocked(tx, ty)) {
			SPRITES.cons().color.blocked.bind();
		}else if(room.isPartOfBlocked(tx, ty))
			SPRITES.cons().color.semiblocked.bind();
		else
			SPRITES.cons().color.ok2.bind();
			
		if (!dest.is(tx, ty))
			return;
		if (dest.blocking(tx, ty)) {
			SPRITES.cons().BIG.filled.render(r, 0, x, y);
		}else {
			SPRITES.cons().BIG.dashed.render(r, 0x0F, x, y);
		}
		mask = 0;
		for (DIR d : DIR.ORTHO) {
			if (dest.is(tx, ty, d)) {
				mask |= d.mask();
			}
		}
		if (mask != 0) {
			SPRITES.cons().BIG.outline.render(r, mask, x, y);
		}
		
	}

	@Override
	public MEDIUM getIcon() {
		return null;
	}

	@Override
	public CharSequence name() {
		return E;
	}
	
	@Override
	public PLACABLE getUndo() {
		return null;
	}

	@Override
	public int rotations() {
		return 4;
	}

	@Override
	public int sizes() {
		return 1;
	}

	@Override
	public CharSequence placableWhole(int tx1, int ty1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private boolean blocked(int tx, int ty) {
		if (room.isBlocked(tx, ty))
			return true;
		COORDINATE s = dest.transform(tx, ty);
		Job j = Jobs.get(s.x(), s.y());
		if (j != null && j.placer() == null)
			LOG.ln(j);
		if (j != null && j.placer().isPlacable(tx, ty, null, null) != null)
			return true;
			
		return false;
	}


	
}
