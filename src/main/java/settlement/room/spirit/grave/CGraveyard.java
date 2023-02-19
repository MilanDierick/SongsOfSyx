package settlement.room.spirit.grave;

import static settlement.main.SETT.*;

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
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import util.rendering.ShadowBatch;

final class CGraveyard extends Furnisher{

	private final ROOM_GRAVEYARD blue;
	final FurnisherStat workers;
	final FurnisherStat services;
	final FurnisherStat respekk;
	
	final RoomSprite sHead;
	
	private final Floor pathway;
	private final static int PI = 3;
	
	protected CGraveyard(ROOM_GRAVEYARD blue, RoomInitData init)
			throws IOException {
		super(init, 4, 3, 88, 44);
		this.blue = blue;
		
		
		
		workers = new FurnisherStat.FurnisherStatEmployees(this, 0);
		services = new FurnisherStat.FurnisherStatI(this);
		respekk = new FurnisherStat.FurnisherStatRelative(this, services);

		
		
		
		Json sp = init.data().json("SPRITES");
		
		this.sHead = new RoomSprite1xN(sp, "GRAVE_A_TOP_1X1", true) {
			
			final RoomSprite made = new RoomSprite1xN(sp, "GRAVE_B_TOP_1X1", true);
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				if (blue.is(it.tile())) {
					if (Grave.isUsed(it.tx(), it.ty())) {
						return made.render(r, s, data, it, degrade, isCandle);
					}
				}
				return super.render(r, s, data, it, degrade, isCandle);
			}
			
		};
		
		RoomSprite sFoot = new RoomSprite1xN(sp, "GRAVE_A_BOTTOM_1X1", false) {
			
			final RoomSprite made = new RoomSprite1xN(sp, "GRAVE_B_BOTTOM_1X1", false);
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				if (blue.is(it.tile())) {
					if (Grave.isUsed(it.tx()+offX(data), it.ty()+offY(data))) {
						return made.render(r, s, data, it, degrade, isCandle);
					}
				}
				return super.render(r, s, data, it, degrade, isCandle);
			}
			
		};
		
		RoomSprite sStone = new RoomSprite1x1(sp, "TOMBSTONE_1X1") {
			
			final RoomSprite made = new RoomSprite1x1(sp, "TOMBSTONE_RUNE_1X1");
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				OPACITY.O85.bind();
				made.render(r, s, data, it, degrade, isCandle);
				OPACITY.unbind();
				return false;
			}
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) instanceof RoomSprite1xN;
			}
			
		};
		
		final FurnisherItemTile h1 = new FurnisherItemTile(
				this,
				false,
				sHead,
				AVAILABILITY.AVOID_PASS, 
				false).setData(Grave.ITEM_MARK);
		final FurnisherItemTile t1 = new FurnisherItemTile(
				this,
				true,
				sFoot, 
				AVAILABILITY.AVOID_PASS, 
				false);
		final FurnisherItemTile st = new FurnisherItemTile(
				this,
				false,
				sStone, 
				AVAILABILITY.SOLID, true);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st}, 
			{h1}, 
			{t1},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st}, 
			{h1,h1}, 
			{t1,t1},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st,st}, 
			{h1,h1,h1}, 
			{t1,t1,t1},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st,st,st}, 
			{h1,h1,h1,h1}, 
			{t1,t1,t1,t1},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st,st,st,st}, 
			{h1,h1,h1,h1,h1}, 
			{t1,t1,t1,t1,t1},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st,st,st,st,st}, 
			{h1,h1,h1,h1,h1,h1}, 
			{t1,t1,t1,t1,t1,t1},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{st,st,st,st,st,st,st}, 
			{h1,h1,h1,h1,h1,h1,h1}, 
			{t1,t1,t1,t1,t1,t1,t1},
		}, 7);
		
		flush(1,3);
		
		{
			RoomSprite ss = new RoomSprite1x1(sp, "MON_1X1");
			final FurnisherItemTile it = new FurnisherItemTile(
					this,
					false,
					ss, 
					AVAILABILITY.SOLID, false);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{it}, 
			}, 1);
		}
		{
			RoomSprite ss = new RoomSpriteXxX(sp, "MON_2X2", 2);
			final FurnisherItemTile it = new FurnisherItemTile(
					this,
					false,
					ss, 
					AVAILABILITY.SOLID, false);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{it,it},
				{it,it},
			}, 4);
		}
		{
			RoomSprite ss = new RoomSpriteXxX(sp, "MON_3X3", 3);
			final FurnisherItemTile it = new FurnisherItemTile(
					this,
					false,
					ss, 
					AVAILABILITY.SOLID, false);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{it,it,it},
				{it,it,it},
				{it,it,it},
			}, 9);
		}
		flush(1,3);
		
		{
			RoomSprite ss = new RoomSpriteComboN(sp, "FLOWER_COMBO");
			final FurnisherItemTile it = new FurnisherItemTile(
					this,
					false,
					ss, 
					AVAILABILITY.AVOID_LIKE_FUCK, false);
			FurnisherItemTools.makeArea(this, it);
		}
		
		pathway = SETT.FLOOR().getByKey("PATHWAY", init.data());
		
		{
			RoomSprite ss = new RoomSpriteComboN();
			final FurnisherItemTile it = new FurnisherItemTile(
					this,
					false,
					ss, 
					AVAILABILITY.ROAD0, false);
			it.setData(PI);
			FurnisherItemTools.makeArea(this, it);
		}
		
	}
	
	@Override
	public boolean removeFertility() {
		return false;
	}

	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return false;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return true;
	}
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new GraveInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		if (SETT.ROOMS().fData.tileData.get(tx, ty) == PI)
			pathway.placeFixed(tx, ty);
		else {
			double f = CLAMP.d((SETT.FERTILITY().baseD.get(tx, ty)-0.2)*4, 0, 0.5);
			GRASS().current.set(tx, ty, f);
		}
	}

}
