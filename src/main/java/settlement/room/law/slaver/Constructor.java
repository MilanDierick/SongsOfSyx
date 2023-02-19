package settlement.room.law.slaver;

import java.io.IOException;

import init.sprite.SPRITES;
import settlement.main.RenderData.RenderIterator;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{

	private final ROOM_SLAVER blue;
	
	final FurnisherStat prisoners = new FurnisherStat.FurnisherStatI(this, 1);
	final static int codeService = 1;
	final static int codeHead = 2;
	

	
	private final RoomSprite table;
	
	private final RoomSpriteRot benchA = new RoomSpriteRot(sheet, 0, 2, SPRITES.cons().ROT.north_south) {
		
		@Override
		protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test,int rx, int ry, FurnisherItem item) {
			return s == table;
		};
	};
	
	private final RoomSpriteRot benchB = new RoomSpriteRot(sheet, 0, 2, SPRITES.cons().ROT.north_south) {
		
		@Override
		protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test,int rx, int ry, FurnisherItem item) {
			
			return DIR.ORTHO.get(dir).perpendicular() == test;
		};
	};
	
	protected Constructor(ROOM_SLAVER blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1, 188, 94);
		this.blue = blue;
		
		Json sp = init.data().json("SPRITES");
	
		table = new RoomSpriteComboN(sp, "TABLE_COMBO") {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				if (!isCandle)
					sheet.render(r, 2*4 + (it.ran()&0x07), it.x(), it.y());
				return false;
			}
			
		};
		
		FurnisherItemTile tt = new FurnisherItemTile(this, table, AVAILABILITY.SOLID, true);
		FurnisherItemTile ss = new FurnisherItemTile(this, true, benchB, AVAILABILITY.PENALTY4, false).setData(codeService);
		FurnisherItemTile sh = new FurnisherItemTile(this, benchA, AVAILABILITY.PENALTY4, false).setData(codeHead);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,}, 
			{sh,},
			{ss,},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt}, 
			{sh,sh,},
			{ss,ss,},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,}, 
			{sh,sh,sh,},
			{ss,ss,ss,},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt,}, 
			{sh,sh,sh,sh,},
			{ss,ss,ss,ss,},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{tt,tt,tt,tt,tt,}, 
			{sh,sh,sh,sh,sh,},
			{ss,ss,ss,ss,ss,},
		}, 5);
		
		flush(3);
		
	}
	
	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		s.singles.init(0, y1, 1, 1, 2, 1, d.s16);
		s.singles.paste(3, true);
		
		s.singles.init(0, s.singles.body().y2(), 1, 1, 4, 2, d.s16);
		s.singles.paste(true);
		
		return d.s16.saveGame();
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
	public boolean mustBeOutdoors() {
		return false;
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new ExecutionInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,0,0,1,1,0},
//		{1,0,0,1,1,0,0,1},
//		{1,0,0,1,1,0,0,1},
//		{0,1,1,0,0,1,1,0},
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
