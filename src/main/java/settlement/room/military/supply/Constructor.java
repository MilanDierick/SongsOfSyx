package settlement.room.military.supply;

import java.io.IOException;

import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import settlement.tilemap.Floors;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	public static final int STORAGE = 128;
	private final Floors.Floor floor2;
	
	final FurnisherStat workers = new FurnisherStat.FurnisherStatI(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	
	final FurnisherStat storage = new FurnisherStat(this, 0) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems*STORAGE;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	
	protected Constructor(RoomInitData init)
			throws IOException {
		super(init, 1, 2, 88, 44);
	
		floor2 = SETT.FLOOR().getByKey("FLOOR2", init.data());
		
		Json sp = init.data().json("SPRITES");
		
		RoomSprite sFence = new RoomSpriteComboN(sp, "FENCE_COMBO");
		
		RoomSprite sStone = new RoomSprite1x1(sp, "TORCH_1X1");
		
		RoomSprite sCrate = new RoomSprite1x1(sp, "CRATE_1X1") {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch shadowBatch, int data, RenderIterator it,
					double degrade, boolean isCandle) {
				boolean ret = super.render(r, shadowBatch, data, it, degrade, isCandle);
				int d = SETT.ROOMS().data.get(it.tile());
				if (blue().is(it.tile()) && Crate.resource(d) != null) {
					Crate.resource(d).renderLaying(r, it.x(), it.y(), it.ran(), Crate.amount(d));
				}
				return ret;
			}
			
		};
		
		FurnisherItemTile ff = new FurnisherItemTile(this, false, sFence, AVAILABILITY.SOLID, false);
		FurnisherItemTile ss = new FurnisherItemTile(this, false, sStone, AVAILABILITY.SOLID, true);
		FurnisherItemTile cc = new FurnisherItemTile(this, false, sCrate, AVAILABILITY.SOLID, false).setData(1);
		FurnisherItemTile oo = new FurnisherItemTile(this, true, null, AVAILABILITY.ROOM, false);
		FurnisherItemTile __ = new FurnisherItemTile(this, true, null, AVAILABILITY.ROOM, false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ss,oo,ff,oo,ss},
		}, 3);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ss,oo,ff,oo,ss},
		}, 4);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ss,oo,ff,oo,ss},
		}, 5);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ss,oo,ff,oo,ss},
		}, 6);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ff,__,cc,__,ff},
			{ss,oo,ff,oo,ss},
		}, 7);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ss,oo,ff,ff,oo,ss},
		}, 6);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ss,oo,ff,ff,oo,ss},
		}, 8);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ss,oo,ff,ff,oo,ss},
		}, 10);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,ff},
			{ss,oo,ff,ff,oo,ss},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ss,oo,ff,ss,oo,ss,ff,oo,ss},
		}, 12);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ss,oo,ff,ss,oo,ss,ff,oo,ss},
		}, 16);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ss,oo,ff,ss,oo,ss,ff,oo,ss},
		}, 20);
		new FurnisherItem(new FurnisherItemTile[][] {
			{ff,ff,ff,ff,ff,ff,ff,ff,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ff,__,cc,cc,__,cc,cc,__,ff},
			{ss,oo,ff,ss,oo,ss,ff,oo,ss},
		}, 24);
		
		flush(3);
		
	}

	@Override
	public boolean usesArea() {
		return false;
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
	public void putFloor(int tx, int ty, int upgrade,  AREA area) {
		for (DIR d : DIR.ORTHO) {
			if (!area.is(tx, ty, d)) {
				floor2.placeFixed(tx, ty);
				return;
			}
		}
		super.putFloor(tx, ty, upgrade,  area);
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return  new SupplyInstance(blue(), area, init);
	}

	@Override
	public ROOM_SUPPLY blue() {
		return SETT.ROOMS().SUPPLY;
	}

}
