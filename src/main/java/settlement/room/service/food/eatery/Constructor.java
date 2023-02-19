package settlement.room.service.food.eatery;

import java.io.IOException;

import init.resources.*;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSprite1x1;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	static final int MAX = 16;
	
	final FurnisherStat storage;
	
	final FurnisherStat workers;
	
	private final ROOM_EATERY blue;
	
	private final FurnisherItemTile cr;
	
	boolean isCrate(int tx, int ty) {
		return SETT.ROOMS().fData.tile.is(tx, ty, cr);
	}
	
	protected Constructor(ROOM_EATERY blue, RoomInitData init)
			throws IOException {
		super(init, 1, 2, 88, 44);
		this.blue = blue;
		storage = new FurnisherStat.FurnisherStatServices(this, blue, 1);
		workers = new FurnisherStat.FurnisherStatEmployees(this, 0.01);
		
		Json sp = init.data().json("SPRITES");
		
		final RoomSprite spriteCrate = new RoomSprite1x1(sp, "CRATE_BOTTOM_A_1X1") {
			
			RoomSprite mid = new RoomSprite1x1(sp, "CRATE_BOTTOM_B_1X1");
			RoomSprite top = new RoomSprite1x1(sp, "CRATE_TOP_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				top.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, settlement.main.RenderData.RenderIterator it, double degrade, boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				EateryInstance i = blue.getter.get(it.tile());
				if (i != null) {
					int ran = it.ran();
					Edibles es = RESOURCES.EDI();
					for (int ri = 1; ri <= 2; ri++) {
						Edible res = es.all().get((ran&0x0F)%es.all().size());
						ran = ran >> 4;
						double d = i.amount(res);
						d/=i.maxAmount*ri;
						d*= 16;
						ran = ran >> 4;
						res.resource.renderLaying(r, it.x(), it.y(), ran, d);
					}
				}
				mid.render(r, s, data, it, degrade, isCandle);
				return false;
				
			}
		};
		final RoomSprite spriteMisc = new RoomSprite1x1(sp, "MISC_BOTTOM_1X1") {
			RoomSprite1x1 top = new RoomSprite1x1(sp, "MISC_1X1");
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile()))
					top.renderRandom(r, s, it, it.ran(), degrade);
			};
		};
		
		cr = new FurnisherItemTile(
				this,
				true,
				spriteCrate, 
				AVAILABILITY.SOLID, 
				false);
		
		FurnisherItemTile mm = new FurnisherItemTile(
				this,
				false,
				spriteMisc, 
				AVAILABILITY.SOLID, 
				true);
		
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,mm},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,mm},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,cr,mm},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,cr,cr,mm},
		}, 7);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,mm},
			{mm,cr,cr,cr,cr},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,mm},
			{mm,cr,cr,cr,cr,cr},
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,cr,mm},
			{mm,cr,cr,cr,cr,cr,cr},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,cr,cr,mm},
			{mm,cr,cr,cr,cr,cr,cr,cr},
		}, 14);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{cr,cr,cr,cr,cr,cr,cr,cr,mm},
			{mm,cr,cr,cr,cr,cr,cr,cr,cr},
		}, 16);
	
		flush(1, 3);
		
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
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,1,0,1,0,1,0},
//		{0,0,1,0,1,0,1,0},
//		{0,0,1,1,1,1,1,0},
//		{0,0,0,1,1,1,0,0},
//		{0,0,0,0,1,0,0,0},
//		{0,0,0,0,1,0,0,0},
//		{0,0,0,0,1,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		},
//		miniColor
//	);
//	
//	@Override
//	public COLOR miniColor(int tx, int ty) {
//		return miniC.get(tx, ty);
//	}
	
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new EateryInstance(blue, area, init);
	}

}
