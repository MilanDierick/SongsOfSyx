package settlement.room.service.arena;

import java.io.IOException;

import init.sprite.SPRITES;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.furnisher.FurnisherStat.FurnisherStatI;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import settlement.tilemap.Floors.Floor;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class ArenaConstructor extends Furnisher{

	private final ROOM_ARENA blue;
	
	final FurnisherStatI workers;
	static final int STATION = 1;
	static final int ARENA = 2;
	
	private final FurnisherItemTile cc;
	private final Floor floormid;
	
	final FurnisherStat spectators;
	final FurnisherStat quality;
	
	protected ArenaConstructor(ROOM_ARENA blue, RoomInitData init)
			throws IOException {
		super(init, 3, 3, 304, 232);
		this.blue = blue;
		workers = new FurnisherStatI(this);
		spectators = new FurnisherStat.FurnisherStatServices(this, blue);
		quality = new FurnisherStat(this) {
			
			@Override
			public double get(AREA area, double acc) {
				return acc;
			}
			
			@Override
			public GText format(GText t, double value) {
				GFORMAT.perc(t, value);
				return t;
			}
		};
		
		floormid = SETT.FLOOR().getByKey("FLOOR2", init.data());
		
		RoomSpriteRot stairsL = new Stairs(0);
		RoomSpriteRot stairsC  = new Stairs(stairsL.tileEnd);
		RoomSpriteRot stairsR = new Stairs(stairsC.tileEnd);
		
		RoomSpriteSimple pillar = new RoomSpriteSimple(sheet, stairsR.tileEnd, 4) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return true;
			}
		};
		pillar.sDataSet(0);
		pillar.setShadow(10, 0);
		RoomSpriteSimple torch = new RoomSpriteSimple(sheet, pillar.tileEnd, 4);
		torch.sDataSet(6);
		torch.setShadow(10, 0);
		
		RoomSpriteBox seats1 = new Seat(torch.tileEnd, true);
		RoomSpriteBox seats2 = new Seat(torch.tileEnd, true);
		
		
		RoomSpriteBox inner = new Seat(seats1.tileEnd+16*3, false);
		inner.setShadow(0, 10);
		
		RoomSpriteRot edge = new RoomSpriteRot(sheet, inner.tileEnd, 4, SPRITES.cons().ROT.north_south) {
			@Override
			protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test, int rx, int ry,
					FurnisherItem item) {
				return s == seats1;
			}
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				return false;
			}
		};
		edge.setShadow(10, 0);
		
		
		
		FurnisherItemTile xx = new FurnisherItemTile(
				this,
				edge,
				AVAILABILITY.SOLID, 
				false);
		
		cc = new FurnisherItemTile(
				this,
				torch,
				AVAILABILITY.SOLID, 
				false);
		
		FurnisherItemTile pp = new FurnisherItemTile(
				this,
				pillar,
				AVAILABILITY.SOLID, 
				false);
		
		
		FurnisherItemTile ee = new FurnisherItemTile(
				this,
				true,
				null,
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile sl = new FurnisherItemTile(
				this,
				stairsL,
				AVAILABILITY.ROOM, 
				false);
		FurnisherItemTile sc = new FurnisherItemTile(
				this,
				stairsC,
				AVAILABILITY.ROOM, 
				false);
		FurnisherItemTile sr = new FurnisherItemTile(
				this,
				stairsR,
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile _1 = new FurnisherItemTile(
				this,
				seats1,
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile _2 = new FurnisherItemTile(
				this,
				seats2,
				AVAILABILITY.PENALTY4, 
				false);
		_2.setData(STATION);
		
		FurnisherItemTile xu = new FurnisherItemTile(
				this,
				inner,
				AVAILABILITY.SOLID, 
				false);
		
		FurnisherItemTile __ = new FurnisherItemTile(
				this,
				null,
				AVAILABILITY.ROOM, 
				false);
		__.setData(ARENA);
		

		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,xx,xx,xx,xx,xx,xx,xx,xx,xx,xx,pp},
			{xx,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,xx},
			{xx,_1,cc,_2,_2,_2,_2,_2,_2,cc,_1,xx},
			{xx,_1,_2,xu,xu,xu,xu,xu,xu,_2,_1,xx},
			{xx,_1,_2,xu,__,__,__,__,xu,_2,_1,xx}, 
			{xx,_1,_2,xu,__,__,__,__,xu,_2,_1,xx}, 
			{xx,_1,_2,xu,__,__,__,__,xu,_2,_1,xx}, 
			{xx,_1,_2,xu,cc,sl,sr,cc,xu,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,sl,sr,_2,_2,_2,_1,xx},
			{xx,_1,_1,_1,_1,sl,sr,_1,_1,_1,_1,xx},
			{pp,xx,xx,xx,pp,ee,ee,pp,xx,xx,xx,pp},
		}, 1);
		flush(1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,xx,xx,xx,xx,pp,xx,xx,xx,xx,xx,pp,xx,xx,xx,xx,pp},
			{xx,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{pp,_1,_2,_2,_2,cc,xu,xu,xu,xu,xu,cc,_2,_2,_2,_1,pp},
			{xx,_1,_2,_2,_2,xu,__,__,__,__,__,xu,_2,_2,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,xu,__,__,__,__,__,xu,_2,_2,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,xu,__,__,__,__,__,xu,_2,_2,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,xu,__,__,__,__,__,xu,_2,_2,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,xu,__,__,__,__,__,xu,_2,_2,_2,_1,xx}, 
			{pp,_1,_2,_2,_2,cc,xu,sl,sc,sr,xu,cc,_2,_2,_2,_1,pp},
			{xx,_1,_2,_2,_2,_2,_2,sl,sc,sr,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,sl,sc,sr,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,sl,sc,sr,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_1,_1,_1,_1,_1,sl,sc,sr,_1,_1,_1,_1,_1,_1,xx},
			{pp,xx,xx,xx,xx,xx,pp,ee,ee,ee,pp,xx,xx,xx,xx,xx,pp},
		}, 1);
		flush(1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{pp,xx,xx,xx,xx,xx,xx,xx,xx,xx,pp,xx,xx,xx,xx,pp,xx,xx,xx,xx,pp,xx,xx,xx,xx,xx,xx,xx,xx,pp,xx,xx,xx,xx,pp,xx,xx,xx,xx,pp,xx,xx,xx,xx,xx,xx,xx,xx,xx,pp},
			{xx,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,cc,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,cc,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{pp,_1,_2,_2,_2,_2,_2,_2,_2,_2,cc,_2,_2,_2,_2,cc,_2,_2,_2,_2,cc,_2,_2,_2,_2,_2,_2,_2,_2,cc,_2,_2,_2,_2,cc,_2,_2,_2,_2,cc,_2,_2,_2,_2,_2,_2,_2,_2,_1,pp},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,cc,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,cc,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,__,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx}, 
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,xu,xu,sl,sr,xu,xu,xu,xu,xu,xu,xu,sl,sc,sc,sc,sc,sr,xu,xu,xu,xu,xu,xu,xu,sl,sr,xu,xu,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{pp,_1,_2,_2,_2,_2,_2,_2,_2,_2,cc,_2,_2,sl,sr,cc,_2,_2,_2,_2,_2,cc,sl,sc,sc,sc,sc,sr,cc,_2,_2,_2,_2,_2,cc,sl,sr,_2,_2,cc,_2,_2,_2,_2,_2,_2,_2,_2,_1,pp},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_2,cc,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,cc,_2,_1,xx},
			{xx,_1,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,sl,sc,sc,sc,sc,sr,_2,_2,_2,_2,_2,_2,_2,sl,sr,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_2,_1,xx},
			{xx,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,sl,sr,_1,_1,_1,_1,_1,_1,_1,sl,sc,sc,sc,sc,sr,_1,_1,_1,_1,_1,_1,_1,sl,sr,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,_1,xx},
			{pp,xx,xx,xx,xx,xx,xx,xx,xx,xx,pp,xx,pp,ee,ee,pp,xx,xx,xx,xx,xx,pp,ee,ee,ee,ee,ee,ee,pp,xx,xx,xx,xx,xx,pp,ee,ee,pp,xx,pp,xx,xx,xx,xx,xx,xx,xx,xx,xx,pp},

		}, 1);
		flush(1, 3);
		
	
	}
	
	private class Seat extends RoomSpriteBox {
		
		private boolean ran;
		
		Seat(int tileEnd, boolean ran){
			super(sheet, tileEnd, false);
			this.ran = ran;
			setShadow(0, 0);
		}
		
		@Override
		protected boolean joins(RoomSprite s, DIR test, int tx, int ty, int rx, int ry, FurnisherItem item,
				int itemRan) {
			
			return getLevel(rx, ry, item) <= getLevel(rx+test.x(), ry+test.y(), item);
		}
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			int cc = (data >> 4) & 0b01111;
			double d = cc/15.0;
			ColorImp.TMP.interpolate(COLOR.WHITE100, COLOR.WHITE65, d);
			ColorImp.TMP.bind();
			super.render(r, s, data & 0b01111, it, degrade, isCandle);
			COLOR.unbind();
			return false;
		}
		
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			byte b = super.getData(tx, ty, rx, ry, item, itemRan);
			b |= getLevel(rx, ry, item)<<4;
			return b;
		}
		
		@Override
		public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
				FurnisherItem item) {
			super.renderPlaceholder(r, x, y, data&0b01111, tx, ty, rx, ry, item);
		}
		
		@Override
		protected int getOffset(int data, RenderIterator it, double degrade, boolean isCandle) {
			if (ran)
				return ((it.ran()>>4)&0b011) *16;
			return 0;
		}
		

		
	}
	
	private class Stairs extends RoomSpriteRot {
		
		Stairs(int tileEnd){
			super(sheet, tileEnd, 1, SPRITES.cons().ROT.full);
			setShadow(0, 0);
			sDataSet(6);
		}
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			return false;
		}
		
		@Override
		public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			return (byte) getLevel(rx, ry, item);
		}
		
		@Override
		protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test, int rx, int ry,
				FurnisherItem item) {
			return dir*2 == test.ordinal();
		}
		@Override
		public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			int cc = getData2(it) & 0b01111;
			double d = cc/15.0;
			ColorImp.TMP.interpolate(COLOR.WHITE100, COLOR.WHITE65, d);
			ColorImp.TMP.bind();
			super.render(r, s, data & 0b01111, it, degrade, false);
			COLOR.unbind();
		}
		

		
	}
	
	private int getLevel(int rx, int ry, FurnisherItem item) {
		for (int i = 1; i < item.width(); i++) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int dx = rx+d.x()*i;
				int dy = ry+d.y()*i;
				if (item.get(dx, dy) == null)
					return i-1;
			}		
		}
		throw new RuntimeException();
	}
	

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		s.full.init(0, y1, 1, 1, 3, 1, d.s16);
		for (int i = 0; i < 3; i++)
			s.full.setSkip(1, i).paste(3, true);
		s.singles.init(0, s.full.body().y2(), 1, 1, 4, 1, d.s16);
		s.singles.paste(true);
		s.singles.init(0, s.singles.body().y2(), 1, 1, 4, 1, d.s16);
		s.singles.paste(true);
		
		s.combo.init(0, s.singles.body().y2(), 2, 1, 4, d.s16);
		s.combo.setVar(0).paste(3, true);
		s.combo.setVar(1).paste(true);
		s.full.init(0, s.combo.body().y2(), 1, 1, 4, 1, d.s16);
		s.full.paste(3, true);
		
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
	public Room create(TmpArea area, RoomInit init) {
		ArenaInstance a =  new ArenaInstance(blue, area, init);
		for (COORDINATE c : a.body()) {
			if (a.is(c) && SETT.ROOMS().fData.tile.get(c) == cc) {
				SETT.LIGHTS().candle(c.x(), c.y(), 0);
			}
		}
		return a;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		if (tx == area.body().x1() || tx == area.body().x2()-1) {
			super.putFloor(tx, ty, upgrade, area);
		}else if (ty == area.body().y1() || ty == area.body().y2()-1) {
			super.putFloor(tx, ty, upgrade, area);
		}else {
			floormid.placeFixed(tx, ty);;
		}
			
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
	
	private static class RoomSpriteBox extends RoomSprite.Imp {

		private final int tileStart;
		public final int tileEnd;
		private final TILE_SHEET sheet;
		private final boolean rotates;
		private final int rotation = 0b010000;

		private static int[][] boxI = new int[16][];
		
		static {
			boxI[DIR.E.mask() | DIR.S.mask()] = new int[] {0};
			boxI[DIR.S.mask() | DIR.E.mask() | DIR.W.mask()] = new int[] {1,2};
			boxI[DIR.W.mask() | DIR.S.mask()] = new int[] {3};
			boxI[DIR.E.mask() | DIR.N.mask() | DIR.S.mask()] = new int[] {4,8};
			boxI[0x0F] = new int[] {5,6,9,10};;
			boxI[DIR.W.mask() | DIR.N.mask() | DIR.S.mask()] = new int[] {7,11};
			boxI[DIR.N.mask() | DIR.E.mask()] = new int[] {12};
			boxI[DIR.N.mask() | DIR.W.mask() | DIR.E.mask()] = new int[] {13,14};;
			boxI[DIR.N.mask() | DIR.W.mask()] = new int[] {15};
		}
		
		private RoomSpriteBox(TILE_SHEET sheet, int tileStart, boolean rotates) {
			this.sheet = sheet;
			this.tileStart = tileStart;
			this.tileEnd = tileStart +  16*(rotates ? 2 : 1);
			this.rotates = rotates;
			
		}

		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade, boolean isCandle) {
			int x = it.x();
			int y = it.y();
			
			int[] ts = boxI[data&0x0F];
			
			if (ts == null) {
				LOG.ln(it.tx() + " " + it.ty());
				return false;
			}
				
			
			int tile = ts[it.ran()%ts.length];
			tile += tileStart;
			tile += getOffset(data, it, degrade, isCandle);
					
					
			if ((data & rotation) != 0) {
				tile += 16;
			}
			
			sheet.render(r, tile, x, y);
			renderDegrade(sheet, r, tile, it, degrade);
			
			if (shadowDist > 0 || shadowHeight > 0) {
				s.setDistance2Ground(shadowHeight).setHeight(shadowDist);
				sheet.render(s, tile, x, y);
			}
			
			return false;
		}
		
		protected int getOffset(int data, RenderIterator it, double degrade, boolean isCandle) {
			return 0;
		}
		
		@Override
		public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
				FurnisherItem item) {
			int t = data&0x0F;
			
			SPRITES.cons().TINY.outline.render(r, t, x, y);
			
		}

		protected boolean joins(RoomSprite s, DIR test, int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			return s != null;
		}
		
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			
			int res = 0;
			
			for (DIR d : DIR.ORTHO) {
				RoomSprite s = item.sprite(rx+d.x(), ry+d.y());
				if (joins(s, d, tx, ty, rx, ry, item, itemRan))
					res |= d.mask();
			}
			
			if (rotates) {
				int r = item.rotation&1;
				res |= r*this.rotation;
			}
			
			return (byte) res;
		}



		
	}
	

}
