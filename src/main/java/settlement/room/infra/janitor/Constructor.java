package settlement.room.infra.janitor;

import java.io.IOException;

import init.resources.RESOURCES;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{

	private final ROOM_JANITOR blue;
	final FurnisherStat workers = new FurnisherStat.FurnisherStatI(this);
	final FurnisherItemTile ta;
	
	
	protected Constructor(ROOM_JANITOR blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1, 88, 44);
		this.blue = blue;
		
		Json sp = init.data().json("SPRITES");
		
		RoomSprite res = new RoomSpriteComboN(sp, "TABLE_COMBO") {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (SETT.ROOMS().fData.candle.is(it.tile()))
					return;
				JanitorInstance ins = blue.getter.get(it.tile());
				if (ins != null) {
					int ri = ins.viewRes >> (8*((it.tx()+it.ty())%3));
					ri &= 0x0FF;
					if (ri != 0)
						RESOURCES.ALL().get(ri-1).renderLaying(r, it.x(), it.y(), it.ran(), blue.res.resources.get(ri-1).get(ins));
				}
			}
			
		};
		
		RoomSprite table = new RoomSpriteComboN(res) {
			final RoomSprite1x1 top = new RoomSprite1x1(sp, "TABLE_MISC_1X1");
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				top.renderRandom(r, s, it, it.ran(), degrade);
			}
			
		};
		
		final RoomSprite1x1 top = new RoomSprite1x1(sp, "MISC_1X1");
		
		RoomSprite nickGround = new RoomSprite1x1(sp, "STORAGE_1X1") {
			
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				if (!isCandle) {
					top.renderRandom(r, s, it, it.ran(), degrade);
				}
				return false;
				
			}
			
		};
		

		

		RoomSprite sDummy = new RoomSprite.Dummy();
		
		final FurnisherItemTile tc = new FurnisherItemTile(
				this,
				table,
				AVAILABILITY.SOLID, 
				true);		
		
		ta = new FurnisherItemTile(
				this,
				res,
				AVAILABILITY.SOLID, 
				false).setData(1);
		
		final FurnisherItemTile ng = new FurnisherItemTile(
				this,
				nickGround,
				AVAILABILITY.SOLID, 
				true);
		
		final FurnisherItemTile nn = new FurnisherItemTile(
				this,
				top,
				AVAILABILITY.SOLID, 
				false);

		final FurnisherItemTile ee = new FurnisherItemTile(
				this,
				true,
				sDummy,
				AVAILABILITY.ROOM, 
				false);
		ee.noWalls = true;
		
		final FurnisherItemTile __ = new FurnisherItemTile(
				this,
				sDummy,
				AVAILABILITY.ROOM, 
				false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tc,ta,ta,ta,tc,}, 
			{nn,__,__,__,nn,},
			{ng,__,__,__,ng,},
			{ng,__,__,__,ng,},
			{nn,__,ee,__,nn,},
		}, 16);
		
		flush(1, 3);
	}
	

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		
		return null;
	}

	@Override
	public boolean usesArea() {
		return false;
	}

	@Override
	public boolean mustBeIndoors() {
		return true;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new JanitorInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,0,0,0,0,0,0},
//		{0,1,0,0,0,0,0,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
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
