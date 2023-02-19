package settlement.room.military.barracks;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.GAME;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSpriteBoxN;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;


abstract class Constructor extends Furnisher{
	
	final FurnisherItemTile manikin;
	final FurnisherItemTile work;
	
	final FurnisherStat men = new FurnisherStat(this, 0) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	
	protected Constructor(RoomInitData init) throws IOException {
		super(init, 1, 1, 88, 44);

		Json js = init.data().json("SPRITES");
		RoomSpriteBoxN sPedi = new RoomSpriteBoxN(js, "PODEUM_BOX") {
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null;
			}
			
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
		
		RoomSprite1x1 sMani = new RoomSprite1x1(js, "MANAKIN_A_1X1") {
			RoomSprite1x1 sMani2 = new RoomSprite1x1(js, "MANAKIN_B_1X1");
			
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				sPedi.renderBelow(r, s, getData2(it), it, degrade);
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				int rot = (it.ran()>>4);
				if (SETT.ROOMS().BARRACKS.is(it.tile()) && BarracksThing.used.is(ROOMS().data.get(it.tile())))
					rot += GAME.intervals().get05();
				rot &= 0x07;
				if ((rot & 1) == 1) {
					return sMani2.render(r, s, rot>>1, it, degrade, false);
				}else 
					return super.render(r, s, rot>>1, it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sPedi.getData(tx, ty, rx, ry, item, itemRan);
			}
			
		};
		
		RoomSprite1x1 sCandle = new RoomSprite1x1(js, "TABLE_1X1") {
			@Override
			public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				sPedi.renderBelow(r, s, getData2(it), it, degrade);
			}

			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sPedi.getData(tx, ty, rx, ry, item, itemRan);
			}
			
		};
		
		FurnisherItemTile ma = new FurnisherItemTile(this, false, sMani, AVAILABILITY.SOLID, false);
		FurnisherItemTile ca = new FurnisherItemTile(this, false, sCandle, AVAILABILITY.SOLID, true);
		FurnisherItemTile st = new FurnisherItemTile(this, true, sPedi, AVAILABILITY.AVOID_PASS, false);
		FurnisherItemTile ee = new FurnisherItemTile(this, false, sPedi, AVAILABILITY.AVOID_PASS, false);
		
		manikin = ma;
		work = st;
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ca},
					{st,ee}
				},
				1.0, 1.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ca,ma},
					{st,ee,st}
				},
				1.5, 2);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ca,ma,ma},
					{st,ee,st,st}
				},
				2.0, 3.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ma,ca,ma,ma},
					{st,st,ee,st,st}
				},
				2.5, 4.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ma,ma,ca,ma,ma},
					{st,st,st,ee,st,st}
				},
				3, 5.0);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{ma,ma,ma,ca,ma,ma,ma},
					{st,st,st,ee,st,st,st}
				},
				3.5, 6);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,ee},
					{ma,ca},
					{ma,ca},
					{st,ee}
				},
				2.0, 2);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,ee,st},
					{ma,ca,ma},
					{ma,ca,ma},
					{st,ee,st}
				},
				3, 4);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,ee,st,st},
					{ma,ca,ma,ma},
					{ma,ca,ma,ma},
					{st,ee,st,st}
				},
				4, 6);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,st,ee,st,st},
					{ma,ma,ca,ma,ma},
					{ma,ma,ca,ma,ma},
					{st,st,ee,st,st}
				},
				5, 8);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,st,st,ee,st,st},
					{ma,ma,ma,ca,ma,ma},
					{ma,ma,ma,ca,ma,ma},
					{st,st,st,ee,st,st}
				},
				6, 10);
		
		new FurnisherItem(
				new FurnisherItemTile[][] {
					{st,st,st,ee,st,st,st},
					{ma,ma,ma,ca,ma,ma,ma},
					{ma,ma,ma,ca,ma,ma,ma},
					{st,st,st,ee,st,st,st}
				},
				7, 12);
		
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
	public ROOM_BARRACKS blue() {
		return SETT.ROOMS().BARRACKS;
	}

}
