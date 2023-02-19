package settlement.room.food.farm;

import java.io.IOException;

import init.D;
import init.sprite.SPRITES;
import init.sprite.game.*;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.spritecomposer.*;

final class Constructor extends Furnisher{

	private static CharSequence ¤¤warning = "Fertility for this farm is very low, which will result in low yields. It can be improved by digging water around the farm. Proceed anyway?";
	static {
		D.ts(Constructor.class);
	}
	
	final boolean isIndoors;
	private final ROOM_FARM blue;
	final FurnisherStat fertility = new FurnisherStat(this, 1/10000.0) {

		@Override
		public GText format(GText t, double value) {
			return GFORMAT.perc(t, value);
		}

		@Override
		public double get(AREA area, double fromItems) {
			if (mustBeIndoors())
				return 1;
			double v = 0;
			for (COORDINATE c : area.body()) {
				
				if (area.is(c)) {
					v += SETT.FERTILITY().target.get(c);
				}
			}
			return v/area.area();
		}
		
	};
	final FurnisherStat workers = new FurnisherStat(this, 1) {

		@Override
		public GText format(GText t, double value) {
			return GFORMAT.f(t, value);
		}

		@Override
		public double get(AREA area, double fromItems) {
			return area.area()*ROOM_FARM.WORKERPERTILEI;
		}
		
	};
	
	final FurnisherStat output;
	
	private final LIST<Sheet> sheets;
	
	protected Constructor(ROOM_FARM blue, RoomInitData init)
			throws IOException {
		super(init, 0, 3, 88, 44);
		
		output = new FurnisherStat.FurnisherStatProduction2(this, blue, 0.01) {
			
			@Override
			protected double getBase(AREA area, double[] acc) {
				double f = area.area();
				if (!isIndoors) {
					f = 0;
					for (COORDINATE c : area.body()) {
						if (area.is(c)) {
							f += SETT.FERTILITY().target.get(c);
						}
					}
				}
				return ROOM_FARM.WORKERPERTILEI*f;
			}
			
		};
		
		isIndoors = init.data().bool("INDOORS");
		this.blue = blue;
		
		sheets = SPRITES.GAME().get(SheetType.s1x1, "_FARM_DIRT");
//		for (Sheet s : sheets) {
//			s.hasRotation = true;
//			s.hasShadow = false;
//		}

	}

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		return null;
	}

	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return isIndoors;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return !isIndoors;
	}


	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		int m = 0;
		for (DIR d : DIR.ORTHO)
			if (area.is(tx, ty, d))
				m |= d.mask();
		SETT.ROOMS().fData.spriteData.set(tx, ty, m);
		SETT.FLOOR().clearer.clear(tx, ty);
	}
	
//	@Override
//	public CharSequence placable(int tx, int ty) {
//		if (SETT.MINERALS().amountD.get(tx, ty) > 0)
//			return PLACABLE.E;
//		return super.placable(tx, ty);
//	}
	
	@Override
	public void renderEmbryo(SPRITE_RENDERER r, int mask, RenderIterator it, boolean isFloored, AREA area) {
		
		if (isFloored) {
			COLOR c = CORE.renderer().colorGet();
			COLOR.unbind();
			renderTill(r, it, area, 0);
			c.bind();
		}
		super.renderEmbryo(r, mask, it, isFloored, area);
	}
	
	@Override
	public void renderExtra() {
		SETT.OVERLAY().FERTILITY.add();
	}
	
	void renderTill(SPRITE_RENDERER r, RenderIterator it, AREA area, double till) {
		
		
		int d = direction(it, area);
		
		int sheet = 0;
		int rot = 0;
		
		if (area.is(it.tx(), it.ty(), DIR.ORTHO.get(d))) {
			if (area.is(it.tx(), it.ty(), DIR.ORTHO.get(d+2))) {
				sheet = 2;
				rot = d + 2*(it.ran()&1);
			}else {
				sheet = 1;
				rot = d;
			}
		}else if (area.is(it.tx(), it.ty(), DIR.ORTHO.get(d+2))) {
			sheet = 1;
			rot = d +2;
		}else {
			rot = d + 2*(it.ran()&1);
		}
		
		renderTill(r, it, till, sheet, rot);
		
	}
	
	private void renderTill(SPRITE_RENDERER r, RenderIterator it, double till, int t, int rot) {
		
		till = 1.0-till;
		int aa = (int) (till*(sheets.size()/3-1));
		
		t = (int) (3*aa) + t;
		int data = SheetType.s1x1.tile(sheets.get(0), SheetData.DUMMY, 0, it.ran(), rot);
		sheets.get(t).render(SheetData.DUMMY, it.x(), it.y(), it, r, data, it.ran(), 0);
	}
	
	int direction(RenderIterator it, AREA area) {
		return (it.ran(area.body().x1(), area.body().y1())& 1);
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new FarmInstance(blue, area, init);
	}
	
	@Override
	public CharSequence warning(AREA area) {
		double d = fertility.get(area, 0);
		if (d < 0.5)
			return ¤¤warning;
		return null;
	}

}
