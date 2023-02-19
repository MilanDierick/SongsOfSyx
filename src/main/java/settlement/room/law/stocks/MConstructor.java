package settlement.room.law.stocks;

import java.io.IOException;

import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import settlement.tilemap.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.rendering.ShadowBatch;

final class MConstructor extends Furnisher {
	
	private final ROOM_STOCKS blue;
	private final FurnisherItemTile ss;
	
	MConstructor(ROOM_STOCKS blue, RoomInitData init)
			throws IOException {
		super(init, 1, 0, 88, 44);
		
		this.blue = blue;
	
		
		Json sData = init.data().json("SPRITES");
		
		RoomSprite ssprite = new RoomSpriteBoxN(sData, "BOX") {
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			
		};
		
		RoomSprite sservice = new RoomSpriteBoxN(ssprite) {
			
			RoomSprite ssmall = new RoomSprite1x1(sData, "STOCK_BELOW_1X1") {
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return d == DIR.ORTHO.get(item.rotation).next(2);
				}
			};
			
			RoomSprite stop = new RoomSprite1x1(sData, "STOCK_TOP_1X1");
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return ssmall.render(r, s, getData2(it), it, degrade, isCandle);
			}
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (SETT.ROOMS().data.get(it.tx(), it.ty()) == 2) {
					stop.render(r, s, getData2(it), it, degrade, false);
				}
			}
			
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				ssmall.renderPlaceholder(r, x, y, (item.rotation+1)%4, tx, ty, rx, ry, item);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return ssmall.getData(tx, ty, rx, ry, item, itemRan);
			}
			
			
			
		};
		
		ss = new FurnisherItemTile(
				this,
				true,
				sservice,
				AVAILABILITY.AVOID_LIKE_FUCK,
				false
				);
		
		FurnisherItemTile tt = new FurnisherItemTile(
				this,
				true,
				ssprite,
				AVAILABILITY.ROOM,
				false
				);
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,},
			{tt,ss,tt,},
			{tt,tt,tt,},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,tt,tt,},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,tt,tt,},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,tt,tt,},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,ss,tt,},
			{tt,tt,tt,},
		}, 5);

//		new SettDebugClick() {
//			
//			@Override
//			public boolean debug(int px, int py, int tx, int ty) {
//				LOG.ln(tx + " " + ty + " " + service(tx, ty));
//				return true;
//			}
//		}.add();;
		
		
		flush(3);
		
	}
	
	public boolean service(int tx, int ty) {
		return blue.is(tx, ty) && SETT.ROOMS().fData.tile.get(tx, ty) == ss;
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
	public Room create(TmpArea area, RoomInit init) {
		return blue.instance.place(area);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}

	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {

		super.putFloor(tx, ty, upgrade, area);
		floor(tx, ty);
	}
	
	private void floor(int tx, int ty) {
		Floor res = floor.get(0);
		int am = 1;
		for (DIR d : DIR.ORTHO) {
			if (SETT.ROOMS().map.is(tx, ty, d))
				continue;
			Floor f = SETT.FLOOR().getter.get(tx, ty, d);
			if (f != null && f != res) {
				int a = testFloor(tx, ty, f);
				if (a > am) {
					am = a;
					res = f;
				}
			}
		}
		
		if (SETT.FLOOR().getter.get(tx, ty) != res)
			res.placeFixed(tx, ty);
	}
	
	private int testFloor(int tx, int ty, Floor f) {
		int am = 0;
		for (DIR d : DIR.ALL) {
			if (SETT.ROOMS().map.is(tx, ty, d))
				continue;
			Floor f2 = SETT.FLOOR().getter.get(tx, ty, d);
			if (f2 == f) {
				am++;
			}
		}
		return am;
	}
	
}