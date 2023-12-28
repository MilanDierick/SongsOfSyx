package settlement.room.infra.embassy;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	public final FurnisherStat workers = new FurnisherStat.FurnisherStatEmployees(this);
	private final ROOM_EMBASSY blue;
	static final int IWORK = 3;
	
	protected Constructor(ROOM_EMBASSY blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1);
		this.blue = blue;
		
		final Json sp = init.data().json("SPRITES");
		
		final RoomSprite sDec = new RoomSprite1x1(sp, "TABLE_WORK_1X1") {
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return d.orthoID() == item.rotation;
			}
		};
		
		final RoomSprite sWork = new RoomSprite1x1(sp, "TABLE_DECOR_1X1") {
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return d.orthoID() == item.rotation;
			}
		};
		final RoomSprite sTable = new RoomSpriteCombo(sp, "TABLE_COMBO");
		
		final RoomSprite sTableDec = new RoomSpriteCombo(sTable) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				sDec.render(r, s, getData2(it), it, degrade, false);
			};
			

			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sDec.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		
		final RoomSprite sTableSide = new RoomSpriteCombo(sTable) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (SETT.ROOMS().fData.candle.is(it.tile()))
					return;
				sWork.render(r, s, getData2(it), it, degrade, false);
			};
			

			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sWork.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		
		final RoomSprite sStool = new RoomSprite1x1(sp, "STOOL_1X1") {
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) instanceof RoomSpriteCombo;
			}
		};
		
		final FurnisherItemTile ss = new FurnisherItemTile(this,false, sTableSide, AVAILABILITY.SOLID, true);
		final FurnisherItemTile ww = new FurnisherItemTile(this,false, sTable, AVAILABILITY.SOLID, false);
		final FurnisherItemTile dd = new FurnisherItemTile(this,false, sTableDec, AVAILABILITY.SOLID, false);
		final FurnisherItemTile ch = new FurnisherItemTile(this,false, sStool, AVAILABILITY.AVOID_PASS, true);
		ww.setData(IWORK);
		final FurnisherItemTile __ = null;
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, ch, __, },
			{ ss, ww, ss, },
			{ dd, dd, dd, }, 
		}, 1);

		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, ch, __, __, ch, __},
			{ ss, ww, ss, ss, ww, ss},
			{ dd, dd, dd, dd, dd, dd}, 
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, ch, __, __, ch, __, __, ch, __},
			{ ss, ww, ss, ss, ww, ss, ss, ww, ss},
			{ dd, dd, dd, dd, dd, dd, dd, dd, dd}, 
		}, 3);
		
		
		
		flush(3);

	}

	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return true;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new EmbassyInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}

}
