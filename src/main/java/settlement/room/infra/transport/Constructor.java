package settlement.room.infra.transport;

import java.io.IOException;

import game.GAME;
import init.C;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
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
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{
	
	private final ROOM_TRANSPORT blue;
	final FurnisherStat crates;
	private final Floor floor2;
	
	final Sprite cart;
	
	final FurnisherItemTile an;
	final FurnisherItemTile ca;


	
	protected Constructor(ROOM_TRANSPORT blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1, 164, 200);
		cart = new Sprite();
		this.blue = blue;
		floor2 = SETT.FLOOR().getByKey("FLOOR2", init.data());
		
		crates = new FurnisherStat(this, 1) {
			
			@Override
			public double get(AREA area, double fromItems) {
				return fromItems;
			}
			
			@Override
			public GText format(GText t, double value) {
				return GFORMAT.i(t, (int)value);
			}
		};
		
		Json sp = init.data().json("SPRITES");
		
		RoomSprite marker = new RoomSpriteRot(sheet, 0, 1, SPRITES.cons().ROT.join_big) {
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			};
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
			
			@Override
			protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test,int rx, int ry, FurnisherItem item) {
				return s == this;
			}
		};
		
		RoomSprite scart = new RoomSprite.Imp() {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				
				RESOURCE res = null;
				int am = 0;
				
				TransportInstance ins = blue.get(it.tx(), it.ty());
				if (ins != null && blue.cart.storage.init(it.tx(), it.ty()) != null) {
					res = ins.resource();
					am = blue.cart.storage.samount.get();
					if (blue.cart.storage.saway.get() == 0)
						cart.renderBelow(r, s, data*2, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, 0, it.ran(), degrade, res, am);
				}
				
				return false;
			}
			
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (blue.cart.storage.init(it.tx(), it.ty()) != null && blue.cart.storage.saway.get() == 0)
					cart.render(r, s, data*2, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, degrade);
			}
			
			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) item.rotation;
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				SPRITES.cons().ICO.arrows.get(data).render(r, x, y);
			}
		};
		
		RoomSprite animal = new RoomSprite.Imp() {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				
				if (blue.cart.job.init(it.tx(), it.ty()) != null && blue.cart.job.wlivestock.get() > 0 && blue.cart.job.waway.get() == 0) {
					double mov = (GAME.intervals().get05()+it.ran()) & 0x0FF;
					mov /= 0x0FF;
					SETT.ANIMALS().renderCaravan(r, s, mov, it.x()+C.TILE_SIZEH, it.y()+C.TILE_SIZEH, null, 0, false, data*2, it.ran());
				}
				
				return false;
			}
			
			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) item.rotation;
			}
		};
		
		RoomSprite post = new RoomSprite1x1(sp, "TORCH_1X1");
		
		FurnisherItemTile pp = new FurnisherItemTile(
				this,
				false,
				post, 
				AVAILABILITY.SOLID, 
				true);
		
		FurnisherItemTile mm = new FurnisherItemTile(
				this,
				false,
				marker, 
				AVAILABILITY.ROOM, 
				false);

		
		FurnisherItemTile __ = new FurnisherItemTile(
				this,
				false,
				new RoomSprite.Dummy(), 
				AVAILABILITY.ROOM, 
				false);
		
		an = new FurnisherItemTile(
				this,
				true,
				animal, 
				AVAILABILITY.SOLID, 
				false).setData(1);
		
		ca = new FurnisherItemTile(
				this,
				true,
				scart, 
				AVAILABILITY.SOLID, 
				false).setData(1);
		
		FurnisherItemTile xx = new FurnisherItemTile(
				this,
				false,
				new RoomSprite.Dummy(), 
				AVAILABILITY.SOLID, 
				false);
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,pp},
			{mm,__,an,__,mm},
			{mm,__,ca,__,mm},
			{pp,__,xx,__,pp},
		}, 1, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,pp},
			{__,__,an,__,__},
			{__,__,ca,__,__},
			{mm,__,xx,__,mm},
			{mm,__,an,__,mm},
			{__,__,ca,__,__},
			{pp,__,xx,__,pp},
		}, 2, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,pp},
			{__,__,an,__,__},
			{mm,__,ca,__,mm},
			{mm,__,xx,__,mm},
			{__,__,an,__,__},
			{__,__,ca,__,__},
			{mm,__,xx,__,mm},
			{mm,__,an,__,mm},
			{__,__,ca,__,__},
			{pp,__,xx,__,pp},
		}, 3, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,pp},
			{__,__,an,__,__},
			{mm,__,ca,__,mm},
			{mm,__,xx,__,mm},
			{__,__,an,__,__},
			{__,__,ca,__,__},
			{mm,__,xx,__,mm},
			{mm,__,an,__,mm},
			{__,__,ca,__,__},
			{mm,__,xx,__,mm},
			{mm,__,an,__,mm},
			{__,__,ca,__,__},
			{pp,__,xx,__,pp},
		}, 4, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,pp},
			{mm,__,an,__,an,__,mm},
			{mm,__,ca,__,ca,__,mm},
			{pp,__,xx,__,xx,__,pp},
		}, 2, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,pp},
			{__,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,pp},
		}, 4, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,pp},
			{__,__,an,__,an,__,__},
			{mm,__,ca,__,ca,__,mm},
			{mm,__,xx,__,xx,__,mm},
			{__,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,pp},
		}, 6, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,pp},
			{__,__,an,__,an,__,__},
			{mm,__,ca,__,ca,__,mm},
			{mm,__,xx,__,xx,__,mm},
			{__,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,pp},
		}, 8, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,__,__,pp},
			{mm,__,an,__,an,__,an,__,mm},
			{mm,__,ca,__,ca,__,ca,__,mm},
			{pp,__,xx,__,xx,__,xx,__,pp},
		}, 3, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,__,__,pp},
			{__,__,an,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,xx,__,pp},
		}, 6, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,__,__,pp},
			{__,__,an,__,an,__,an,__,__},
			{mm,__,ca,__,ca,__,ca,__,mm},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{__,__,an,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,xx,__,pp},
		}, 9, 9);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,__,__,__,__,__,__,__,pp},
			{__,__,an,__,an,__,an,__,__},
			{mm,__,ca,__,ca,__,ca,__,mm},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{__,__,an,__,an,__,an,__,__},
			{__,__,ca,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,ca,__,__},
			{mm,__,xx,__,xx,__,xx,__,mm},
			{mm,__,an,__,an,__,an,__,mm},
			{__,__,ca,__,ca,__,ca,__,__},
			{pp,__,xx,__,xx,__,xx,__,pp},
		}, 12,12);
		
		flush(3);
		
	}

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		s.singles.init(0, y1, 1, 1, 1, 1, d.s16);
		s.singles.paste(3, true);
		return d.s16.saveGame();
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
	public RoomBlueprintImp blue() {
		// TODO Auto-generated method stub
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		if (SETT.ROOMS().fData.tile.get(tx, ty) != null && SETT.ROOMS().fData.tile.get(tx, ty).data() == 1)
			floor2.placeFixed(tx, ty);
		else
			super.putFloor(tx, ty, upgrade, area);
	}
	

//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,1,1,0,0,0},
//		{0,0,1,1,1,1,0,0},
//		{0,1,0,1,1,0,1,0},
//		{1,0,0,1,1,0,0,1},
//		{0,0,0,1,1,0,0,0},
//		{0,0,0,1,1,0,0,0},
//		{0,0,0,1,1,0,0,0},
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
		return new TransportInstance(blue, area, init);
		
	}
}
