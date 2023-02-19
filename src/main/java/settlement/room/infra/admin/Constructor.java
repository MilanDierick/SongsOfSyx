package settlement.room.infra.admin;

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
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	public final FurnisherStat stations = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return fromItems;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value*blue.knowledgePerStation());
		}
	};
	public final FurnisherStat efficiency = new FurnisherStat.FurnisherStatEfficiency(this, stations);
	
	private final ROOM_ADMIN blue;
	final FurnisherItemTile ww;
	static final int ICHAIR = 3;
	
	protected Constructor(ROOM_ADMIN blue, RoomInitData init)
			throws IOException {
		super(init, 3, 2, 88, 44);
		this.blue = blue;
		
		final Json sp = init.data().json("SPRITES");
		
		final RoomSprite sMisc = new RoomSprite1x1(sp, "MISC_1X1");
		
		final RoomSprite sTable = new RoomSpriteComboN(sp, "TABLE_COMBO") {

			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
				if (SETT.ROOMS().fData.candle.is(it.tile()))
					return;
				sMisc.render(r, s, getData2(it), it, degrade, false);
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sMisc.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		final RoomSprite sWork = new RoomSpriteComboN(sTable) {

			final RoomSprite idle = new RoomSprite1x1(sp, "WORK_UNUSED_1X1") {
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.get(rx, ry) != null && item.get(rx, ry).data() == ICHAIR;
				}
			};
			final RoomSprite active = new RoomSprite1x1(sp, "WORK_USED_1X1");
			final RoomSpriteNew ontop = new RoomSprite1x1(sp, "WORK_USED_TOP_1X1");
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {

				if(!blue.is(it.tile()))
					return;
				if(!Job.reserved.is(ROOMS().data.get(it.tile()))) {
					idle.render(r, s, getData2(it), it, degrade, false);
				}else {
					ontop.animate(Job.used.get(ROOMS().data.get(it.tile())) == 0 ? 0 : 1);
					active.render(r, s, getData2(it), it, degrade, false);
					ontop.render(r, s, getData2(it), it, degrade, false);
				}
			}
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return idle.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		
		final RoomSprite sShelfSingle = new RoomSprite1x1(sp, "SHELF_1X1") {
			
			final RoomSpriteNew ontop = new RoomSprite1x1(sp, "SHELF_TOP_1X1");

			@Override
			public void renderAbove(SPRITE_RENDERER re, ShadowBatch s, int data, RenderIterator it, double degrade) {
				Room r = SETT.ROOMS().map.get(it.tile());
				if (r instanceof AdminInstance) {
					int f = blue.data.usedD;
					if (f > (it.ran() & 0x0FF)) {
						ontop.render(re, s, data, it, degrade, false);
					}
				}
				
			}
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				if (item.width() == 1 || item.height() == 1)
					return d.orthoID() == item.rotation;
				if (d.orthoID() == item.rotation || d.perpendicular().orthoID() == item.rotation)
					return item.sprite(rx, ry) == this;
				return false;
			}

		};

		
		final RoomSprite sStool = new RoomSprite1x1(sp, "STOOL_1X1") {
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) instanceof RoomSpriteComboN;
			}
		};
		
		final FurnisherItemTile ss = new FurnisherItemTile(this,true, sShelfSingle, AVAILABILITY.SOLID, false);
		ww = new FurnisherItemTile(this,false, sWork, AVAILABILITY.SOLID, false);
		final FurnisherItemTile tt = new FurnisherItemTile(this,false, sTable, AVAILABILITY.SOLID, true);
		final FurnisherItemTile in = new FurnisherItemTile(this,true, sStool, AVAILABILITY.AVOID_PASS, false);
		in.setData(ICHAIR);
		final FurnisherItemTile __ = null;
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ss, },
			{ __, in, __, }, 
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ss, },
			{ __, in, in, __, }, 
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ww, ss, },
			{ __, in, in, in, __, }, 
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, __, }, 
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, __, }, 
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, in, __, }, 
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ww, ww, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, in, in, __, }, 
		}, 7);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, __, }, 
			{ tt, ww, ss, },
			{ ss, ww, ss, },
			{ __, in, __, }, 
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, __, }, 
			{ tt, ww, ww, ss, },
			{ tt, ww, ww, ss, },
			{ __, in, in, __, }, 
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, in, __, }, 
			{ tt, ww, ww, ww, ss, },
			{ tt, ww, ww, ww, ss, },
			{ __, in, in, in, __, }, 
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, in, in, __, },
			{ tt, ww, ww, ww, ww, ss, },
			{ tt, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, __, }, 
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, in, in, in, __, }, 
			{ tt, ww, ww, ww, ww, ww, ss, },
			{ tt, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, __, }, 
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, in, in, in, in, __, }, 
			{ tt, ww, ww, ww, ww, ww, ww, ss, },
			{ tt, ww, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, in, __, }, 
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ __, in, in, in, in, in, in, in, __, }, 
			{ tt, ww, ww, ww, ww, ww, ww, ww, ss, },
			{ tt, ww, ww, ww, ww, ww, ww, ww, ss, },
			{ __, in, in, in, in, in, in, in, __, }, 
		}, 14);
		
		flush(1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss }, 
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, }, 
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, }, 
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, ss, }, 
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, ss, ss, }, 
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss }, 
			{ tt, ss }, 
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, }, 
			{ tt, ss, ss, }, 
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, }, 
			{ tt, ss, ss, ss, }, 
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, ss, }, 
			{ tt, ss, ss, ss, ss, }, 
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ tt, ss, ss, ss, ss, ss, }, 
			{ tt, ss, ss, ss, ss, ss, }, 
		}, 10);
		
		
		flush(3);
		
		FurnisherItemTools.makeUnder(this, sp, "CARPET_COMBO");

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
		return new AdminInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,1,1,1,1,1,1},
//		{0,1,1,1,1,1,0,1},
//		{0,1,1,1,1,1,0,1},
//		{0,1,1,1,1,1,0,1},
//		{0,1,1,1,1,1,0,1},
//		{0,1,1,1,1,1,1,1},
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
