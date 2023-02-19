package settlement.room.health.hospital;

import java.io.IOException;

import init.sprite.game.SheetType;
import init.sprite.game.Sheets;
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

final class Constructor extends Furnisher {

	private final ROOM_HOSPITAL blue;

	static final int CODE_S = 1;
	
	
	final FurnisherStat patients = new FurnisherStat.FurnisherStatI(this);
	final FurnisherStat workers = new FurnisherStat.FurnisherStatEmployees(this);
	
	protected Constructor(ROOM_HOSPITAL blue, RoomInitData init) throws IOException {
		super(init, 1, 2, 88, 44);
		this.blue = blue;
		
		Json js = init.data().json("SPRITES");
		
		RoomSprite sbedA = new SBedSprite(js, "BED_TOP_1X1", false);
		
		RoomSprite sbedB = new SBedSprite(js, "BED_BOTTOM_1X1", true) {
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (Bed.resource(it.tx(), it.ty()))
					blue.indus.get(0).ins().get(0).resource.renderLaying(r, it.x(), it.y(), it.ran(), 1);
			}
		};
		
		RoomSprite stable = new RoomSpriteComboN(js, "TABLE_COMBO") {
			
			private final RoomSprite top = new RoomSprite1x1(js, "TABLE_ONTOP_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (!SETT.ROOMS().fData.candle.is(it.tile()))
					top.render(r, s, SETT.ROOMS().fData.spriteData2.get(it.tile()), it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return top.getData(tx, ty, rx, ry, item, itemRan);
			};
			
		};
		
		FurnisherItemTile bb = new FurnisherItemTile(this, false, sbedA, AVAILABILITY.NOT_ACCESSIBLE, false);
		FurnisherItemTile ss = new FurnisherItemTile(this, true, sbedB, AVAILABILITY.NOT_ACCESSIBLE, false);
		ss.setData(CODE_S);
		
		FurnisherItemTile tt = new FurnisherItemTile(this, false, stable, AVAILABILITY.SOLID, true);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt},
			{bb}, 
			{ss},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt},
			{bb,bb}, 
			{ss,ss},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt},
			{bb,bb,bb}, 
			{ss,ss,ss},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt},
			{bb,bb,bb,bb}, 
			{ss,ss,ss,ss},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt,tt},
			{bb,bb,bb,bb,bb}, 
			{ss,ss,ss,ss,ss},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss},
			{bb}, 
			{tt},
			{bb}, 
			{ss},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,ss},
			{bb,bb}, 
			{tt,tt},
			{bb,bb}, 
			{ss,ss},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,ss,ss},
			{bb,bb,bb}, 
			{tt,tt,tt},
			{bb,bb,bb}, 
			{ss,ss,ss},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,ss,ss,ss},
			{bb,bb,bb,bb}, 
			{tt,tt,tt,tt},
			{bb,bb,bb,bb}, 
			{ss,ss,ss,ss},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ss,ss,ss,ss,ss},
			{bb,bb,bb,bb,bb}, 
			{tt,tt,tt,tt,tt},
			{bb,bb,bb,bb,bb}, 
			{ss,ss,ss,ss,ss},
		}, 10);
		
		flush(1, 3);
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
		return true;
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new HospitalInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,0,1,1,0,0,0},
//		{0,0,0,1,1,0,0,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
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
	
	private class SBedSprite extends RoomSprite1xN {

		private Sheets made;
		
		public SBedSprite(Json json, String key, boolean master) throws IOException {
			super(json, key, master);
			made = new Sheets(SheetType.s1x1, json.json(key+ "_UNMADE"));
		}	
		
		@Override
		public Sheets sheet(RenderIterator it) {
			int data = SETT.ROOMS().fData.spriteData.get(it.tile());
			int x = it.tx()+offX(data);
			int y = it.ty()+offY(data);

			if (Bed.made(x, y)) {
				return super.sheet(it);
			}
			return made;
		}
		
	}
	

}
