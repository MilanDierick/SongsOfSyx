package settlement.room.food.hunter;

import java.io.IOException;

import init.C;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	private final ROOM_HUNTER blue;
	
	final FurnisherStat workers = new FurnisherStat(this, 1) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	
	final FurnisherStat efficiency = new FurnisherStat.FurnisherStatEfficiency(this, workers);
	
	final FurnisherItemTile ww;
	final FurnisherItemTile rm;
	final FurnisherItemTile rr;
	
	protected Constructor(ROOM_HUNTER blue, RoomInitData init)
			throws IOException {
		super(init, 2, 2, 88, 44);
		this.blue = blue;
		
		Json j = init.data().json("SPRITES");
		
		final RoomSpriteComboN table = new RoomSpriteComboN(j, "TABLE_COMBO") {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				
				boolean ret = super.render(r, s, data, it, degrade, isCandle);
				if (isCandle)
					return ret;
				Cadaver ca = SETT.THINGS().cadavers.tGet.get(it.tx(), it.ty());
				if (ca != null) {
					ca.spec().blood.bind();
					long ran = it.bigRan();
					int a = Job.gore.get(SETT.ROOMS().data.get(it.tile()));
					int cx = it.x()+C.TILE_SIZEH;
					int cy = it.y()+C.TILE_SIZEH;
					for (int i = 0; i < a; i++) {
						int xx = (int) (cx + (-4 + (ran&0x07))*C.SCALE);
						ran = ran >> 3;
						int yy = (int) (cy + (-4 + (ran&0x07))*C.SCALE);
						ran = ran >> 3;

						SETT.THINGS().sprites.bloodPool.render(r, (int) (ran&0x0F), xx, yy);
						ran = ran >> 4;
					}
					COLOR.unbind();
				}
				return ret;
			}
			
		};
		
		final RoomSprite sNick = new RoomSprite1x1(j, "NICKNACK_1X1");
		
		final RoomSprite stable2 = new RoomSpriteComboN(table) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile()))
					sNick.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sNick.getData2(tx, ty, rx, ry, item, itemRan);
			}
			
		};
		
		final RoomSprite sstorage = new RoomSprite1x1(j, "STORAGE_1X1") {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile()))
					sNick.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sNick.getData2(tx, ty, rx, ry, item, itemRan);
			}
			
		};

		
		ww = new FurnisherItemTile(this, true,table, AVAILABILITY.SOLID, false);
		rm = new FurnisherItemTile(this, true,table, AVAILABILITY.SOLID, false);
		rr = new FurnisherItemTile(this, true,table, AVAILABILITY.SOLID, false);
		final FurnisherItemTile mm = new FurnisherItemTile(this, false,stable2, AVAILABILITY.SOLID, true);
		final FurnisherItemTile nn = new FurnisherItemTile(this, false, sstorage, AVAILABILITY.SOLID, false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{rm,ww,rr,mm},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{rm,ww,rr,mm},
			{rm,ww,rr,mm},
		}, 2);
		
		flush(3);
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,nn},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,nn},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,mm,nn},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm},
			{nn,mm},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,nn},
			{nn,mm,nn},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,nn},
			{nn,mm,mm,nn},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{nn,mm,mm,mm,nn},
			{nn,mm,mm,mm,nn},
		}, 10);
		
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
		return new HunterInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,1,0,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,0,1,0,1,0,0,0},
//		{0,1,1,1,0,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		},
//		miniColor
//	);
//	
//	@Override
//	public COLOR miniColor(int tx, int ty) {
//		return miniC.get(tx, ty);
//	}

}
