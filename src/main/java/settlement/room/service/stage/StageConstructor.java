package settlement.room.service.stage;

import java.io.IOException;

import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.furnisher.FurnisherStat.FurnisherStatI;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSpriteBoxN;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;

final class StageConstructor extends Furnisher{

	private final ROOM_STAGE blue;
	

	static final int STATION = 1;
	final FurnisherStatI workers;
	final FurnisherStat spectators;
	final FurnisherStat quality;
	
	protected StageConstructor(ROOM_STAGE blue, RoomInitData init)
			throws IOException {
		super(init, 3, 3, 88, 44);
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
		
		Json sp = init.data().json("SPRITES");
		
		RoomSpriteBoxN first = new RoomSpriteBoxN(sp, "A_BOX") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() >= 0;
			}
		};
		first.sData(0);
		
		RoomSpriteBoxN second = new RoomSpriteBoxN(sp, "B_BOX") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() >= 1;
			}
		};
		second.sData(1);
		
		RoomSpriteBoxN third = new RoomSpriteBoxN(sp, "C_BOX") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null && item.sprite(rx, ry).sData() >= 2;
			}
		};
		third.sData(2);
		
		FurnisherItemTile AA = new FurnisherItemTile(
				this,
				first,
				AVAILABILITY.PENALTY4, 
				false);
		AA.setData(STATION);
		final FurnisherItemTile aa = new FurnisherItemTile(
				this,
				false,
				first,
				AVAILABILITY.ROOM, 
				false);
		final FurnisherItemTile ai = new FurnisherItemTile(
				this,
				first,
				AVAILABILITY.SOLID, 
				true);
		
		FurnisherItemTile BB = new FurnisherItemTile(
				this,
				second,
				AVAILABILITY.PENALTY4, 
				false);
		BB.setData(STATION);
		final FurnisherItemTile bb = new FurnisherItemTile(
				this,
				second,
				AVAILABILITY.ROOM, 
				false);

		FurnisherItemTile CC = new FurnisherItemTile(
				this,
				third,
				AVAILABILITY.PENALTY4, 
				false);
		CC.setData(STATION);
		final FurnisherItemTile cc = new FurnisherItemTile(
				this,
				third,
				AVAILABILITY.ROOM, 
				false);

		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ai,aa,aa,ai,}, 
			{aa,AA,AA,aa,},
			{aa,AA,AA,aa,},
			{ai,aa,aa,ai,}, 
		}, 1);
		
		flush(1, 0);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ai,aa,aa,aa,aa,aa,ai},
			{aa,bb,bb,bb,bb,bb,aa},
			{aa,bb,BB,BB,BB,bb,aa},
			{aa,bb,BB,bb,BB,bb,aa},
			{aa,bb,BB,BB,BB,bb,aa},
			{aa,bb,bb,bb,bb,bb,aa},
			{ai,aa,aa,aa,aa,aa,ai},
		}, 1);
		
		flush(1, 0);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ai,aa,aa,aa,aa,aa,aa,aa,aa,ai},
			{aa,bb,bb,bb,bb,bb,bb,bb,bb,aa},
			{aa,bb,cc,cc,cc,cc,cc,cc,bb,aa},
			{aa,bb,cc,CC,CC,CC,CC,cc,bb,aa},
			{aa,bb,cc,CC,cc,cc,CC,cc,bb,aa},
			{aa,bb,cc,CC,cc,cc,CC,cc,bb,aa},
			{aa,bb,cc,CC,CC,CC,CC,cc,bb,aa},
			{aa,bb,cc,cc,cc,cc,cc,cc,bb,aa},
			{aa,bb,bb,bb,bb,bb,bb,bb,bb,aa},
			{ai,aa,aa,aa,aa,aa,aa,aa,aa,ai},
		}, 1);
		
		flush(1, 0);
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
		return new StageInstance(blue, area, init);
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
