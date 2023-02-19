package settlement.room.spirit.dump;

import java.io.IOException;

import init.D;
import init.RES;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{

	private final ROOM_DUMP p;
	
	final FurnisherStat total = new FurnisherStat(this, 1) {
		
		@Override
		public double get(AREA area, double acc) {
			int a = 0;
			for (COORDINATE c : area.body()) {
				if (area.is(c)) {
					if (!isEdge(c.x(), c.y(), area))
						a++;
				}
			}
			
			return a;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int) value);
		}
	};
	
	protected Constructor(ROOM_DUMP p, RoomInitData init) throws IOException {
		super(init, 0, 1, 384, 108);
		this.p = p;
	}

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		s.house2.init(0, y1, 2, 1, d.s16);
		s.house2.setVar(0).paste(1, true);
		s.house2.setVar(1).paste(1, true);
		return d.s16.saveGame();
	}

	@Override
	public boolean usesArea() {
		return true;
	}
	
	private static CharSequence ¤¤TooThin = "¤Area is too thin at places. Expand the area to at least 3x3";
	
	static {
		D.ts(Constructor.class);
	}
	
	@Override
	public CharSequence constructionProblem(AREA area) {
		for (COORDINATE c : area.body()){
			if (area.is(c)) {
				boolean ok = false;
				for (DIR d : DIR.ALL) {
					if (isFull(c, area, d)) {
						ok = true;
						break;
					}	
				}
				if (!ok) {
					RES.filler().done();
					return ¤¤TooThin;
				}
				
				
			}
			
		}
		return null;
		
	}
	
	private boolean isFull(COORDINATE c, AREA a, DIR d) {
		int tx = c.x()+d.x();
		int ty = c.y()+d.y();
		if (!a.is(tx, ty))
			return false;
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR dd = DIR.ALL.get(i);
			if (!a.is(tx, ty, dd))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean mustBeIndoors() {
		return false;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return false;
	}

	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new DumpInstance(p, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return p;
	}
	
	@Override
	public void renderTileBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, boolean floored) {
		int m = SETT.ROOMS().fData.spriteData.get(it.tile());
		if ((m & 0b010000) != 0) {
			sheet.render(r, (m&0x0F)+16*(it.ran()&0b011), it.x(), it.y());
		}
	}
	
	@Override
	public void doBeforePlanning(int tx, int ty) {
		SETT.ROOMS().fData.spriteData.set(tx, ty, 0);
		super.doBeforePlanning(tx, ty);
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		if (isEdge(tx, ty, area)) {
			set(tx, ty, area);
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (isEdge(tx+d.x(), ty+d.y(), area))
					set(tx+d.x(), ty+d.y(), area);
			}
		}else {
			super.putFloor(tx, ty, upgrade, area);
			SETT.ROOMS().fData.spriteData.set(tx, ty, 0);
		}
	}
	
	private void set(int tx, int ty, AREA area) {
		int m = 0;
		for (DIR d: DIR.NORTHO) {
			if (joins(tx, ty, d, area) && joins(tx, ty, d.next(-1), area) && joins(tx, ty, d.next(1), area))
				m |= d.mask();
		}
		SETT.ROOMS().fData.spriteData.set(tx, ty, 0b10000|m);
	}
	
	private boolean joins(int tx, int ty, DIR d, AREA area) {
		tx += d.x();
		ty += d.y();
		if (!area.is(tx, ty))
			return true;
		if (isEdge(tx, ty, area))
			return true;
		return false;
	}

	public boolean isEdge(int tx, int ty, AREA area) {
		if (!area.is(tx, ty))
			return false;
		for (DIR d: DIR.ALL)
			if (!area.is(tx, ty, d))
				return true;
		return false;
	}
	
}
