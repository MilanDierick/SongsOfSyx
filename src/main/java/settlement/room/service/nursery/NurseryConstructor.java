package settlement.room.service.nursery;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class NurseryConstructor extends Furnisher{

	private final ROOM_NURSERY blue;
	static final int MARK = 1;
	final FurnisherStat workers;
	final FurnisherStat beds;
	final FurnisherStat coziness;
	private final RoomSprite sHead;
	private final RoomSprite1xN sTail;
	
	protected NurseryConstructor(ROOM_NURSERY blue, RoomInitData init)
			throws IOException {
		super(init, 2, 3, 188, 94);
		this.blue = blue;
		
		
		beds = new FurnisherStat.FurnisherStatI(this);
		workers = new FurnisherStat.FurnisherStatEmployeesR(this, beds, 0.3);
		coziness = new FurnisherStat.FurnisherStatRelative(this, beds);
		
		Json sData = init.data().json("SPRITES");
		
		
		sHead = new RoomSprite1xN(sData,"1x1_CRIB_TOP", true) {
			
			private final RoomSprite1xN fence = new RoomSprite1xN(sData,"1x1_CRIB_TOP_FENCE", true);
			private final RoomSprite1x1 baby = new RoomSprite1x1(sData,"1x1_BABY");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				
				
				DIR d = DIR.ORTHO.get(getRot(data));
				if (blue.ss.init(it.tx()+d.x(), it.ty()+d.y())) {
					if (blue.ss.age.get() > 0) {
						baby.render(r, s, data, it, degrade, false);
					}
				}
				
				fence.render(r, s, data, it, degrade, rotates);
			}
		};
		
		sTail = new RoomSprite1xN(sData,"1x1_CRIB_BOTTOM", false) {
			
			private final RoomSprite1xN fence = new RoomSprite1xN(sData,"1x1_CRIB_BOTTOM_FENCE", true);
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				
				if (blue.ss.init(it.tx(), it.ty())) {
					int i = 0;
					for (IndustryResource rr : blue.productionData.ins()) {
						if (blue.ss.resources[i++].get() != 0) {
							rr.resource.renderLaying(r, it.x(), it.y(), it.ran(), 1);
						}
					}
					
				}
				
				fence.render(r, s, data, it, degrade, rotates);
				
			}
		};

		RoomSprite sExtra = new RoomSpriteXxX(sData, "2x2_DECOR", 2);
		
		
		RoomSprite sTable = new RoomSprite1x1(sData, "1x1_TABLE") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) == sHead;
			}
		};

		
		final FurnisherItemTile h1 = new FurnisherItemTile(
				this,
				sHead,
				AVAILABILITY.AVOID_PASS, 
				false);
		final FurnisherItemTile t1 = new FurnisherItemTile(
				this,
				true,
				sTail, 
				AVAILABILITY.AVOID_PASS, 
				false).setData(MARK);
		final FurnisherItemTile ta = new FurnisherItemTile(
				this,
				sTable, 
				AVAILABILITY.SOLID, true);
		
		final FurnisherItemTile ex = new FurnisherItemTile(
				this,
				sExtra, 
				AVAILABILITY.SOLID, 
				false);
		
		
		final FurnisherItemTile __ = null;
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta}, 
			{h1},
			{t1},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta, h1}, 
			{ __,t1}, 
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta,h1,h1,ta}, 
			{__,t1,t1,__},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta,h1,h1,h1,ta}, 
			{__,t1,t1,t1,__},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta,h1,h1,h1,h1,ta}, 
			{__,t1,t1,t1,t1,__},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta,h1,h1,h1,h1,h1,ta}, 
			{__,t1,t1,t1,t1,t1,__},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ta,h1,h1,h1,h1,h1,h1,ta}, 
			{__,t1,t1,t1,t1,t1,t1,__},
		}, 6);
		
		flush(1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ex,ex},
			{ex,ex},
		}, 1);
		
		flush(3);
		
	}
	
	boolean isHead(COORDINATE c, DIR d) {
		RoomSprite s = SETT.ROOMS().fData.sprite.get(c, d);
		return s == sHead;
	}

	boolean isTail(int tx, int ty) {
		RoomSprite s = SETT.ROOMS().fData.sprite.get(tx, ty);
		return s == sTail;
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
		return new NurseryInstance(blue, area, init);
	}
	
	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0},
//		{0,1,0,0,0,0,0,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,1,1,1,1,1,0},
//		{0,1,0,0,0,0,1,0},
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
