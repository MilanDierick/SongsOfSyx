package settlement.room.service.food.tavern;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
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

class Constructor extends Furnisher {

	private final ROOM_TAVERN blue;
	final FurnisherStat tables;
	final FurnisherStat coziness;
	final RoomSprite sTable;
	private final RoomSprite1x1 sJug;
	private final RoomSprite1x1 sFill;
	
	protected Constructor(ROOM_TAVERN blue, RoomInitData init) throws IOException {
		super(init, 3, 2, 88, 44);
		this.blue = blue;
		tables = new FurnisherStat.FurnisherStatServices(this, blue);
		coziness = new FurnisherStat.FurnisherStatRelative(this, tables);
		
		Json sp = init.data().json("SPRITES");
		
		sJug = new RoomSprite1x1(sp, "JUG_1X1");
		sFill = new RoomSprite1x1(sp, "JUG_FILL_1X1");
		
		sTable = new RoomSpriteCombo(sp, "TABLE_COMBO") {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (blue.is(it.tile()))
					renderTable(r, s, data&0b1111, it);
			}
		};
		
		final RoomSprite sTableDec = new RoomSpriteCombo(sTable) {
			
			final RoomSprite1x1 top = new RoomSprite1x1(sp, "TABLE_DECOR_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile()))
					top.renderRandom(r, s, it, it.ran(), degrade);
			}
		};
		
		final RoomSprite sChair = new RoomSprite1x1(sp, "CHAIR_1X1") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null && item.sprite(rx, ry) != this;
			}
		};
		
		final RoomSprite sBarrel = new RoomSprite1x1(sp, "MISC_1X1");
		
		FurnisherItemTile pl = new FurnisherItemTile (
				this,
				false,
				sTable,
				AVAILABILITY.SOLID,
				false
				);
		FurnisherItemTile  td = new FurnisherItemTile (
				this,
				false,
				sTableDec, 
				AVAILABILITY.SOLID,
				true
				);
		FurnisherItemTile  ch = new FurnisherItemTile (
				this,
				true,
				sChair, 
				AVAILABILITY.AVOID_PASS,
				false
				);
		FurnisherItemTile  mm = new FurnisherItemTile (
				this,
				false,
				sBarrel, 
				AVAILABILITY.SOLID,
				false
				);
		FurnisherItemTile __ = null;
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,td},
			{__,ch,__},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,td},
			{__,ch,ch,__},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,td},
			{__,ch,ch,ch,__},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,__},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,__},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,__},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,ch,__},
		}, 7);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,pl,pl,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,ch,ch,__},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,__},
			{td,pl,td},
			{td,pl,td},
			{__,ch,__},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,__},
			{td,pl,pl,td},
			{td,pl,pl,td},
			{__,ch,ch,__},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,__},
			{td,pl,pl,pl,td},
			{td,pl,pl,pl,td},
			{__,ch,ch,ch,__},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,ch,__},
			{td,pl,pl,pl,pl,td},
			{td,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,__},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,ch,ch,__},
			{td,pl,pl,pl,pl,pl,td},
			{td,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,__},
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,ch,ch,ch,__},
			{td,pl,pl,pl,pl,pl,pl,td},
			{td,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,__},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,ch,ch,ch,ch,__},
			{td,pl,pl,pl,pl,pl,pl,pl,td},
			{td,pl,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,ch,__},
		}, 14);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,ch,ch,ch,ch,ch,ch,ch,ch,__},
			{td,pl,pl,pl,pl,pl,pl,pl,pl,td},
			{td,pl,pl,pl,pl,pl,pl,pl,pl,td},
			{__,ch,ch,ch,ch,ch,ch,ch,ch,__},
		}, 16);
		
		flush(3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,mm},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,td,mm},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,td,mm,mm},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm,td},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm,td,mm},
		}, 7);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{td,mm},
			{td,mm},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,td,mm},
			{mm,td,mm},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,td,mm,mm},
			{mm,td,mm,mm},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm},
			{mm,mm,td,mm,mm},
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm,td},
			{mm,mm,td,mm,mm,td},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{mm,mm,td,mm,mm,td,mm},
			{mm,mm,td,mm,mm,td,mm},
		}, 14);
		
		flush(3);
		
		FurnisherItemTools.makeUnder(this, sp, "CARPET_COMBO");
		
	}
	
	void renderTable(SPRITE_RENDERER r, ShadowBatch s, int rotMask, RenderIterator it) {
		
		
		int data = ROOMS().data.get(it.tile());
		
		int af = blue.table.amountFull(data);
		int ae = blue.table.amountEmpty(data);
		if (af + ae == 0)
			return;
		

		
		int cx = 0;
		int cy = 0;
		
		int dd = 4*C.SCALE;
		
		int di = it.ran()% DIR.ALLC.size();
		
		int ran = it.ran();
		
		for (int i = 0; i < 9 && af > 0 || ae > 0; i++) {
			DIR d = DIR.ALLC.get(di%DIR.ALLC.size());
			di++;
			
			it.setOff(cx+d.x()*dd, cy+d.y()*dd);
			
			
			if (af > 0) {
				sJug.renderRandom(r, s, it, ran, 0);
				sFill.renderRandom(r, s, it, ran, 0);
				af--;
			}else if (ae > 0) {
				sJug.renderRandom(r, s, it, ran, 0);
				ae--;
			}
			ran = ran >> 3;
		}
		
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
		return new TavernInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,1,1,1,1,0},
//		{0,0,1,0,0,1,0,0},
//		{0,0,1,0,0,1,0,0},
//		{0,0,1,0,0,1,0,0},
//		{0,0,0,0,0,0,0,0},
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
