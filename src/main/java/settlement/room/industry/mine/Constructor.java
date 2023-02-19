package settlement.room.industry.mine;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.D;
import init.sprite.ICON;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import settlement.tilemap.Floors.Floor;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.*;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	final static int B_WORK = 1;
	final static int B_STORAGE = 2;
	
	private static CharSequence ¤¤problemMore = "¤This room must be partially placed on {0} in order to function.";
	private static CharSequence ¤¤mustBe = "¤Must not be placed on {0}";
	
	static {
		D.ts(Constructor.class);
	}
	
	final FurnisherStat workers = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			double am = 0;
			for (COORDINATE c : area.body()) {
				if (area.is(c) && SETT.MINERALS().getter.get(c) == blue.minable && SETT.MINERALS().amountD.get(c) > 0)
					am++;
			}
			return Math.floor(am/2); 
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.i(t, (int)value);
		}
	};
	final FurnisherStat deposits = new FurnisherStat(this) {
		
		@Override
		public double get(AREA area, double fromItems) {
			double am = 0;
			double v = 0;
			for (COORDINATE c : area.body()) {
				if (area.is(c) &&  SETT.MINERALS().getter.get(c) == blue.minable && SETT.MINERALS().amountD.get(c) > 0 && SETT.ROOMS().fData.item.get(c) == null) {
					am ++;
					v += SETT.MINERALS().amountD.get(c);
				}
			}
			if (am == 0)
				return 0;
			return v/am;
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.perc(t, value);
		}
	};
	final FurnisherStat efficiency = new FurnisherStat.FurnisherStatEfficiency(this, workers);
	
	final FurnisherStat output;
	private final ROOM_MINE blue;
	private Floor floor;
	
	protected Constructor(RoomInitData init, ROOM_MINE blue)
			throws IOException {
		super(init, 2, 4, 88, 44);
		
		output = new FurnisherStat.FurnisherStatProduction2(this, blue) {
			@Override
			protected double getBase(AREA area, double[] fromItems) {
				
				return workers.get(area, fromItems)*deposits.get(area, fromItems)*efficiency.get(area, fromItems);
			}
		};
		
		floor = SETT.FLOOR().get(init.data());
		this.blue = blue;

		
		Json sp = init.data().json("SPRITES");
		
		final RoomSprite sConveyor = new RoomSprite1x1(sp, "CONVEYOR_1X1") {
			
			@Override
			protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
				return item.sprite(rx, ry) != null && item.sprite(rx, ry) != this;
			}
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = aniSpeed(it.tile());
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		final RoomSprite sStorageTop = new RoomSprite1xN(sp, "STORAGE_1X1_TOP", true) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER ren, ShadowBatch shadowBatch, int data, RenderIterator it, double degrade) {
				Room r = SETT.ROOMS().map.get(it.tile());
				if (r != null && r instanceof MineInstance) {
					((MineInstance)r).blueprintI().job.storage.render(ren, shadowBatch, it.tx(), it.ty(), it.x(), it.y(), it.ran());
				}
			};
			
		};
		final RoomSprite sStorageMid = new RoomSprite1xN(sp, "STORAGE_1X1_MID", false) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER ren, ShadowBatch shadowBatch, int data, RenderIterator it, double degrade) {
				Room r = SETT.ROOMS().map.get(it.tile());
				if (r != null && r instanceof MineInstance) {
					((MineInstance)r).blueprintI().job.storage.render(ren, shadowBatch, it.tx(), it.ty(), it.x(), it.y(), it.ran());
				}
			};
			
		};
		final RoomSprite sStorageEnd = new RoomSprite1xN(sp, "STORAGE_1X1_END", false) {
			
			@Override
			public void renderAbove(SPRITE_RENDERER ren, ShadowBatch shadowBatch, int data, RenderIterator it, double degrade) {
				Room r = SETT.ROOMS().map.get(it.tile());
				if (r != null && r instanceof MineInstance) {
					((MineInstance)r).blueprintI().job.storage.render(ren, shadowBatch, it.tx(), it.ty(), it.x(), it.y(), it.ran());
				}
			};
			
		};
		final RoomSprite sStorageSingle = new RoomSprite1x1(sp, "SLAB_1X1") {
			
			@Override
			public void renderAbove(SPRITE_RENDERER ren, ShadowBatch shadowBatch, int data, RenderIterator it, double degrade) {
				Room r = SETT.ROOMS().map.get(it.tile());
				if (r != null && r instanceof MineInstance) {
					((MineInstance)r).blueprintI().job.storage.render(ren, shadowBatch, it.tx(), it.ty(), it.x(), it.y(), it.ran());
				}
			};
			
		};
		
		final RoomSprite sSlab = new RoomSprite1x1(sStorageSingle) {
			
			
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				super.render(r, s, data, it, degrade, isCandle);
				if (!isCandle) {
					ICON.MEDIUM i = blue.minable.resource.icon();
					OPACITY.O99.bind();
					i.render(r, it.x()+8, it.x()+C.TILE_SIZE-8, it.y()+8, it.y()+C.TILE_SIZE-8);
					OPACITY.unbind();
				}
				return false;
			}
			
		};
		
		final RoomSprite sAuxSmall = new RoomSprite1x1(sp, "AUX_1X1") {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = aniSpeed(it.tile());
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		
		final RoomSprite sAuxMediumA = new RoomSprite1xN(sp, "AUX_MEDIUM_A_1X1", true) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = aniSpeed(it.tile());
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		
		final RoomSprite sAuxMediumB = new RoomSprite1xN(sp, "AUX_MEDIUM_B_1X1", false) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = aniSpeed(it.tile());
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		
		final RoomSprite sAuxBig = new RoomSpriteXxX(sp, "AUX_BIG_2X2", 2) {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = aniSpeed(it.tile());
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		
		final RoomSprite sWork = new RoomSprite1x1(sp, "WORK_1X1") {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				animationSpeed = 0;
				Room ro = SETT.ROOMS().map.get(it.tile());
				
				if (r != null && ro instanceof MineInstance && Job.working(SETT.ROOMS().data.get(it.tile()))) {
					animationSpeed = 1.0;
				}
				return super.render(r, s, data, it, degrade, isCandle);
			}
		};
		
		
		
		final FurnisherItemTile ss = new Aux(this, true, sStorageSingle, AVAILABILITY.SOLID, false).setData(B_STORAGE);
		final FurnisherItemTile sA = new Aux(this, true, sStorageTop,  AVAILABILITY.SOLID, false).setData(B_STORAGE);
		final FurnisherItemTile sm = new Aux(this, true, sStorageMid, AVAILABILITY.SOLID, false).setData(B_STORAGE);
		final FurnisherItemTile sc = new Aux(this, true, sStorageEnd,  AVAILABILITY.SOLID, false).setData(B_STORAGE);
		final FurnisherItemTile co = new Aux(this, false, sConveyor, AVAILABILITY.SOLID, false);
		
		final FurnisherItemTile cc = new Aux(this, false, sSlab, AVAILABILITY.SOLID, true);
		
		final FurnisherItemTile ms = new Aux(this, false, sAuxSmall, AVAILABILITY.SOLID, false);
		final FurnisherItemTile mA = new Aux(this, false, sAuxMediumA, AVAILABILITY.SOLID, false);
		final FurnisherItemTile mB = new Aux(this, false, sAuxMediumB, AVAILABILITY.SOLID, false);
		final FurnisherItemTile ml = new Aux(this, false, sAuxBig, AVAILABILITY.SOLID, false);
		
		final FurnisherItemTile ww = new Aux(this, true, sWork, AVAILABILITY.SOLID, false).setData(B_WORK);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,ss,cc},
			{co,ss,cc},
		}, 6, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sc,cc},
			{co,sA,sc,cc},
		}, 8, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sc,cc},
			{co,sA,sm,sc,cc},
		}, 10, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sc,cc},
			{co,sA,sm,sm,sc,cc},
		}, 12, 8);
		

		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sc,cc},
		}, 14, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sm,sc,cc},
		}, 16, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sm,sm,sc,cc},
		}, 18, 14);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sm,sm,sm,sc,cc},
		}, 20, 16);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sm,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sm,sm,sm,sm,sc,cc},
		}, 22, 18);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{co,sA,sm,sm,sm,sm,sm,sm,sm,sm,sc,cc},
			{co,sA,sm,sm,sm,sm,sm,sm,sm,sm,sc,cc},
		}, 24, 20);
		
		flush(1, 1, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww,cc},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww,cc,ms},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww},
			{cc,ms},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww,cc},
			{ww,ms,ms},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ms,ww,cc,ms},
			{ms,mA,mB,ww},
		}, 8);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ww,ml,ml,ww,cc},
			{ww,ml,ml,ww,ms},
		}, 10);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ww,ml,ml,mA,ww,cc},
			{ww,ml,ml,mB,ww,ms},
		}, 12);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{ww,ml,ml,mA,ww,cc,mA},
			{ww,ml,ml,mB,ww,ms,mB},
		}, 14);
		
		flush(3);
		
		
		
	}
	
	private class Aux extends FurnisherItemTile {

		public Aux(Furnisher p, boolean mustBeReachable, RoomSprite sprite, AVAILABILITY availability,
				boolean canGoCandle) {
			super(p, mustBeReachable, sprite, availability, canGoCandle);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
//			if (SETT.MINERALS().amountD.get(tx, ty) > 0) {
//				return ¤¤itProblem;
//			}
			return null;
		}
		
		
	}

	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		floor.placeFixed(tx, ty);
	}
	
	@Override
	public CharSequence constructionProblem(AREA area) {
		if (workers.get(area, 0) <= 0)
			return Str.TMP.clear().add(¤¤problemMore).insert(0, blue.minable.name);
		return null;
	}

	@Override
	public void renderEmbryo(SPRITE_RENDERER r, int mask, RenderIterator it, boolean isFloored, AREA area) {
		super.renderEmbryo(r, mask, it, isFloored, area);
		GROUND().renderMinerals(CORE.renderer(), it.tile(), it.ran(), it.x(), it.y());
	}
	
	@Override
	public void renderExtra() {
		SETT.OVERLAY().MINERALS.add();
//		t.m = blue.minable;
//		t.add();
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
		return false;
	}
	
	@Override
	public CharSequence placable(int tx, int ty) {
		if (SETT.MINERALS().getter.get(tx, ty) != null && SETT.MINERALS().getter.get(tx, ty) != blue.minable)
			return Str.TMP.clear().add(¤¤mustBe).insert(0, SETT.MINERALS().getter.get(tx, ty).name);
		return super.placable(tx, ty);
	}

	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new MineInstance(blue, area, init);
	}

	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,0,1,1,1,1,0,0},
//		{0,1,1,0,0,1,1,0},
//		{0,0,0,1,1,0,0,0},
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
	public boolean canBeCopied() {
		return false;
	}
	
	private double aniSpeed(int tile) {
		Room r = SETT.ROOMS().map.get(tile);
		if (r != null && r instanceof MineInstance) {
			MineInstance ins = (MineInstance)r;
			return (double)ins.workage / ins.jobs.size();

		}
		return 0;
	}
	
	@Override
	public boolean hasShape() {
		return false;
	}


}
