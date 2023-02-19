package settlement.room.infra.importt;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSpriteSimple;
import settlement.tilemap.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{
	
	private final ROOM_IMPORT blue;
	final FurnisherStat crates;
	private final FurnisherItemTile cr;
	private final Floor floor2;
	
	
	boolean isCrate(int tx, int ty) {
		return SETT.ROOMS().fData.tIndex.get(tx, ty) == cr.index(); 	
	}

	


	
	protected Constructor(ROOM_IMPORT blue, RoomInitData init)
			throws IOException {
		super(init, 1, 1, 152, 100);
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
		
		RoomSprite spriteCrate = new RoomSpriteSimple(sheet, 2*4, 4) {
			
			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return (byte) (4*(item.rotation& 0b01));
			};
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade, boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				ImportInstance ins = ROOMS().IMPORT.getter.get(it.tile());
				if (ins != null){
					if (ins.resource() != null) {
						int d = ROOMS().data.get(it.tile());
						int a = ImportThingy.bAmount.get(d);
						ins.resource().renderLaying(r, it.x(), it.y(), it.ran(), a);
					}
				}
				return true;
			};
		};
		
		RoomSprite.Imp marker = new RoomSprite.Imp() {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade, boolean isCandle) {
				return false;
			};
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				ImportInstance ins = ROOMS().IMPORT.getter.get(it.tile());
				if (ins == null)
					return;
				ICON.MEDIUM i = ins.resource() == null ? SPRITES.icons().m.cancel : ins.resource().icon();
				OPACITY.O99.bind();
				i.render(r, it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE);
				OPACITY.unbind();
				
				super.renderBelow(r, s, data, it, degrade);
			}

			@Override
			public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		cr = new FurnisherItemTile(
				this,
				true,
				spriteCrate, 
				AVAILABILITY.SOLID, 
				false);
		
		FurnisherItemTile ca = new FurnisherItemTile(
				this,
				false,
				spriteCrate, 
				AVAILABILITY.SOLID, 
				true);
		
		FurnisherItemTile ee = new FurnisherItemTile(
				this,
				new SpriteThingy(0), 
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile cc = new FurnisherItemTile(
				this,
				new SpriteThingy(1), 
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile __ = new FurnisherItemTile(
				this,
				null, 
				AVAILABILITY.ROOM, 
				false);
		
		FurnisherItemTile mm = new FurnisherItemTile(
				this,
				marker, 
				AVAILABILITY.ROOM, 
				false);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,ee},
			{mm,cr,cr,ca,mm},
			{__,__,__,__,__},
			{mm,cr,cr,ca,mm},
			{ee,cc,cc,cc,ee},
		}, 5, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,ee},
			{mm,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__},
			{mm,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,ee},
		}, 6, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,cc,ee},
			{mm,cr,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__,__},
			{mm,cr,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,cc,ee},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,cc,cc,ee},
			{mm,cr,cr,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__,__,__},
			{mm,cr,cr,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,cc,cc,ee},
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
			{mm,cr,cr,cr,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__,__,__,__},
			{mm,cr,cr,cr,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
			{mm,ca,cr,cr,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__,__,__,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,__,__,__,__,__,__,__,__},
			{mm,ca,cr,cr,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
		}, 26);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
			{mm,ca,cr,cr,cr,cr,cr,ca,mm},
			{__,__,__,__,__,__,__,__,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,__,__,__,__,__,__,__,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,cr,cr,cr,cr,cr,cr,cr,__},
			{__,__,__,__,__,__,__,__,__},
			{mm,ca,cr,cr,cr,cr,cr,ca,mm},
			{ee,cc,cc,cc,cc,cc,cc,cc,ee},
		}, 40);
		
		flush(1);
		
	}

	@Override
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		s.full.init(0, y1, 1, 1, 2, 1, d.s16);
		for (int i = 0; i < 2; i++)
			s.full.setSkip(1, i).paste(3, true);
		
		s.full.init(0, s.full.body().y2(), 1, 1, 4, 1, d.s16);
		s.full.paste(true);
		s.full.pasteRotated(1, true);
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
		for (DIR d : DIR.ALL) {
			if (!area.is(tx, ty, d)) {
				super.putFloor(tx, ty, upgrade, area);
				return;
			}
		}
		floor2.placeFixed(tx, ty);
	}
	

//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,1,1,0},
//		{0,0,0,1,1,1,0,0},
//		{0,1,1,1,1,0,0,0},
//		{0,1,1,1,1,0,0,0},
//		{0,0,0,1,1,1,0,0},
//		{0,0,0,0,0,1,1,0},
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
		return new ImportInstance(blue, area, init);
		
	}
	
	private class SpriteThingy extends RoomSpriteSimple{

		private int offset;
		
		public SpriteThingy(int offset) {
			super(sheet, 0, 1);
			if (offset > 0) {
				setShadow(8, 0);
			}else {
				setShadow(0, 8);
			}
			this.offset = offset*4;
		}
		
//		@Override
//		protected boolean joinsWith(RoomSprite s, boolean outof, int dir, DIR test) {
//			return s instanceof SpriteThingy;
//		};
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			return false;
		}
		
		@Override
		public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
			super.render(r, s, data, it, degrade, false);
		}

	
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			int i = 0;
			for (DIR d : DIR.ORTHO) {
				if (item.sprite(rx+d.x(), ry+d.y()) instanceof SpriteThingy) {
					return (byte) (offset+i);
				}
				i++;
			}
			return 0;
		}
	}
}
