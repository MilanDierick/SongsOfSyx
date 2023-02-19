package settlement.room.food.pasture;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.D;
import init.RES;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import util.colors.GCOLORS_MAP;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;

final class Constructor extends Furnisher{

	private static CharSequence ¤¤Problem = "Must be facing the edge of the room.";
	private static CharSequence ¤¤Problem2 = "Must not be placed in a corner.";
	
	static {
		D.ts(Constructor.class);
	}
	
	private ROOM_PASTURE blue;
	private final boolean isIndoors;
	final FurnisherStat workers = new FurnisherStat(this, 1) {
		
		@Override
		public double get(AREA area, double fromItems) {
			return ROOM_PASTURE.WORKERS_PER_TILE*ferarea.get(area, fromItems);
		}
		
		@Override
		public GText format(GText t, double value) {
			return GFORMAT.f(t, value, 1);
		}
	};
	
	final FurnisherStat ferarea = new FurnisherStat(this, 1) {
		
		@Override
		public double get(AREA area, double fromItems) {
			
			if (mustBeIndoors())
				return area.area()*0.5;
			double f = 0;
			outer: for (COORDINATE c : area.body()) {
				if (!area.is(c))
					continue;
				for (DIR d : DIR.ALL) {
					if (!area.is(c, d)) {
						continue outer;
					}
				}
				f += Math.sqrt(SETT.FERTILITY().baseD.get(c.x(), c.y()));
			}
			return f;
		}
		
		@Override
		public GText format(GText t, double value) {
			double am = 0;
			
			for (IndustryResource o : blue.industries().get(0).outs())
				am += o.rate;
			am *= blue.bonus2.get(HCLASS.CITIZEN, null);
			return GFORMAT.f(t, (ROOM_PASTURE.WORKERS_PER_TILE*value*am), 1);
		}
	};
	
	private final FurnisherItemTile gc;
	final FurnisherItemTile s1;
	final FurnisherItemTile s2;
	final FurnisherItemTile s3;
	
	private final RoomSpriteComboN fence;
	
	protected Constructor(ROOM_PASTURE blue, RoomInitData init)
			throws IOException {
		super(init, 1, 2, 88, 44);
		this.blue = blue;
		
		Json js = init.data().json("SPRITES");
		
		RoomSprite sbelow = new RoomSprite1x1(js, "STORAGE_1X1");
		
		RoomSprite sprite = new RoomSpriteXxX(js, "GATE_TOP_3X3", 3) {
			@Override
			public void renderAbove(snake2d.SPRITE_RENDERER r, util.rendering.ShadowBatch s, int data, settlement.main.RenderData.RenderIterator it, double degrade) {
				super.render(r, s, data, it, degrade, false);
			};
			@Override
			public boolean render(snake2d.SPRITE_RENDERER r, util.rendering.ShadowBatch s, int data, settlement.main.RenderData.RenderIterator it, double degrade, boolean isCandle) {
				
				if (SETT.ROOMS().fData.tile.get(it.tile()).data() == 1) {
					return sbelow.render(r, s, getData2(it), it, degrade, isCandle);
				}
				
				return false;
				
			};
			
			@Override
			public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
				return sbelow.getData(tx, ty, rx, ry, item, itemRan);
			}
		};
		
		fence = new RoomSpriteComboN(js, "FENCE_COMBO");
		
		FurnisherItemTile gl = new FurnisherItemTile(this, sprite, AVAILABILITY.SOLID, false).setData(5);
		gc = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM, false) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				for (DIR d : DIR.ORTHO) {
					if (!roomIs.is(tx, ty, d) && !SETT.PATH().solidity.is(tx, ty, d))
						return null;
					
				}
				return ¤¤Problem;
			};
		}.setData(5);
		FurnisherItemTile du = new FurnisherItemTile(this, sprite, AVAILABILITY.ROOM, false) {
			@Override
			public CharSequence isPlacable(int tx, int ty, MAP_BOOLEAN roomIs, FurnisherItem it, int rx, int ry) {
				
				
				for (DIR d : DIR.ORTHO) {
					if (it.get(rx, ry, d) != null) {
						continue;
					}
					if (!p(tx+d.x(), ty+d.y(), roomIs))
						return ¤¤Problem2;
					
				}
				return super.isPlacable(tx, ty, roomIs, it, rx, ry);
				
			};
			
			private boolean p(int tx, int ty, MAP_BOOLEAN roomIs) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					if (!roomIs.is(tx, ty, DIR.ORTHO.get(i)))
						return false;
				}
				return true;
			}
		};
		s1 = new FurnisherItemTile(this, sprite, AVAILABILITY.SOLID, false).setData(1);
		s2 = new FurnisherItemTile(this, sprite, AVAILABILITY.SOLID, false).setData(1);
		s3 = new FurnisherItemTile(this, sprite, AVAILABILITY.SOLID, false).setData(1);
		
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{gl,gc,gl},
			{du,du,du},
			{s1,s2,s3},
		}, 1, 1);
		isIndoors = init.data().bool("INDOORS", false);
		flush(1, 1, 3);
	}


	@Override
	public boolean usesArea() {
		return true;
	}

	@Override
	public boolean mustBeIndoors() {
		return isIndoors;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return !isIndoors;
	}

	@Override
	public ROOM_PASTURE blue() {
		return blue;
	}

	
//	private final FurnisherMinimapColor miniC = new FurnisherMinimapColor(new byte[][] {
//		{0,0,0,0,0,0,0,0},
//		{0,1,0,0,0,0,1,0},
//		{1,1,1,1,1,1,1,1},
//		{0,1,0,0,0,0,1,0},
//		{1,1,1,1,1,1,1,1},
//		{0,1,0,0,0,0,1,0},
//		{0,1,0,0,0,0,1,0},
//		{0,0,0,0,0,0,0,0},
//		},
//		miniColor
//	);
//	
//	@Override
//	public COLOR miniColor(int tx, int ty) {
//		return miniC.get(tx, ty);
//	}
	
	private final ColorImp color = new ColorImp();
	
	@Override
	public void renderEmbryo(SPRITE_RENDERER r, int mask, RenderIterator it, boolean isFloored, AREA area) {
		double f = SETT.FERTILITY().baseD.get(it.tile());
		COLOR col = CORE.renderer().colorGet();
		
		color.interpolate(GCOLORS_MAP.SOSO, GCOLORS_MAP.GOOD2, 0.75 + 0.25*f);
		color.bind();
		Room room = SETT.ROOMS().map.get(it.tile());
		
		if (isFloored) {
			COLOR.unbind();
			renderFence(r, ShadowBatch.DUMMY, it, f);
			return;
		}
		
		if (mask != 0x0F) {
			SPRITES.cons().BIG.filled.render(r, mask, it.x(), it.y());
			return;
		}
		for (DIR d : DIR.NORTHO) {
			if (!room.isSame(it.tx(), it.ty(), it.tx()+d.x(), it.ty()+d.y())) {
				SPRITES.cons().BIG.filled.render(r, 0x0F, it.x(), it.y());
				return;
			}
		}
		col.bind();
		super.renderEmbryo(r, mask, it, isFloored, area);
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		FurnisherItemTile t = SETT.ROOMS().fData.tile.get(tx, ty);
		if (t != null) {
			super.putFloor(tx, ty, upgrade, area);
			
		}else {
			FLOOR().clearer.clear(tx, ty);
		}
		
	}
	
	@Override
	public CharSequence placable(int tx, int ty) {
		return super.placable(tx, ty);
	}
	
	@Override
	public boolean removeFertility() {
		return false;
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new PastureInstance(blue, area, init);
	}
	
	private static CharSequence ¤¤TooThin = "¤Area is too thin at places. Expand the area to at least 3x3";
	
	static {
		D.ts(Constructor.class);
	}
	
	@Override
	public CharSequence constructionProblem(AREA area) {
		for (COORDINATE c : area.body()){
			if (area.is(c)) {
				boolean ok = false;
				for (DIR d : DIR.ALL) {
					if (isFull(c, area, d)) {
						ok = true;
						break;
					}	
				}
				if (!ok) {
					RES.filler().done();
					return ¤¤TooThin;
				}
				
				
			}
			
		}
		return null;
		
	}
	
	private boolean isFull(COORDINATE c, AREA a, DIR d) {
		int tx = c.x()+d.x();
		int ty = c.y()+d.y();
		if (!a.is(tx, ty))
			return false;
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR dd = DIR.ALL.get(i);
			if (!a.is(tx, ty, dd))
				return false;
		}
		return true;
	}
	
	@Override
	public void renderTileBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, boolean floored) {
		if (floored)
			renderFence(r, s, it, 0);
	}
	
	public boolean fenceJoin(ROOMA ii, int tx, int ty) {
		if (!ii.is(tx, ty))
			return false;
		if (SETT.ROOMS().fData.tile.get(tx, ty) == gc)
			return false;
		
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.get(di);
			if (!ii.is(tx, ty, d) && !SETT.TERRAIN().get(tx, ty, d).isMassiveWall())
				return true;
		}
		return false;
	}
	
	public boolean isFence(ROOMA ii, int tx, int ty) {
		if (!ii.is(tx, ty))
			return false;
		if (SETT.ROOMS().fData.tile.get(tx, ty) != null)
			return false;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (!ii.is(tx, ty, DIR.ORTHO.get(di)))
				return true;
		}
		return false;
	}
	
	public void renderFence(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, double degrade) {
		ROOMA ii = SETT.ROOMS().map.rooma.get(it.tx(), it.ty());
		if (ii == null)
			return;
		if (!fenceJoin(ii, it.tx(), it.ty()))
			return;
		
		int m = 0;
		
		for (DIR d : DIR.ORTHO) {
			if (fenceJoin(ii, it.tx()+d.x(), it.ty()+d.y()) || SETT.TERRAIN().get(it.tx()+d.x(), it.ty()+d.y()).isMassiveWall())
				m|= d.mask();
			
			
		}
		
		
		
		if (m != 0x0F)
			fence.render(r, s, m, it, degrade, false);
	}
	

}
