package settlement.room.food.orchard;

import java.io.IOException;

import init.C;
import init.D;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.food.orchard.OTile.STATE;
import settlement.room.main.*;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;

final class Constructor extends Furnisher{

	private static CharSequence ¤¤warning = "Fertility for this orchard is very low, which will result in low yields. It can be improved by digging water around the farm. Proceed anyway?";
	static {
		D.ts(Constructor.class);
	}
	
	final static int TREE = 1;
	
	final boolean isIndoors;
	private final ROOM_ORCHARD blue;
	final FurnisherStat fertility = new FurnisherStat(this, 1/10000.0) {

		@Override
		public GText format(GText t, double value) {
			return GFORMAT.perc(t, value);
		}

		@Override
		public double get(AREA area, double fromItems) {
			if (mustBeIndoors())
				return 1;
			double v = 0;
			for (COORDINATE c : area.body()) {
				
				if (area.is(c)) {
					v += SETT.FERTILITY().target.get(c);
				}
			}
			return v/area.area();
		}
		
	};
	final FurnisherStat workers = new FurnisherStat(this, 0.01) {

		@Override
		public GText format(GText t, double value) {
			return GFORMAT.f(t, value);
		}

		@Override
		public double get(AREA area, double fromItems) {
			return 4*fromItems/ROOM_ORCHARD.TILES_PER_WORKER;
		}
		
	};
	
	final RoomSpriteComboN sEdge;
	final FurnisherStat output;
	
	protected Constructor(ROOM_ORCHARD blue, RoomInitData init)
			throws IOException {
		super(init, 1, 3, 88, 44);
		
		output = new FurnisherStat.FurnisherStatProduction2(this, blue, 0.01) {
			
			@Override
			protected double getBase(AREA area, double[] acc) {
				double f = area.area();
				if (!isIndoors) {
					f = 0;
					for (COORDINATE c : area.body()) {
						if (area.is(c)) {
							f += SETT.FERTILITY().target.get(c);
						}
					}
				}
				f /= area.area();
				return f*workers.get(area, acc[workers.index()]);
			}
			
		};
		
		isIndoors = init.data().bool("INDOORS");
		this.blue = blue;

		Json sp = init.data().json("SPRITES");
		
		RoomSprite1x1 sfruit = new RoomSprite1x1(sp, "FRUIT_1X1");
		RoomSprite1x1 ssmall = new RoomSprite1x1(sp, "TREE_1X1");
		
		RoomSpriteXxX tree = new RoomSpriteXxX(sp, "TREE_2X2", 2) {
			
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
					boolean isCandle) {
				
				if (type().dx(data) == 0 || type().dy(data) == 0) {
					return false;
				}
				OTile t = blue.tile.get(it.tx(), it.ty());
				if (t != null) {
					STATE state =  t.state();
					if (state == t.ISAPLING) {
						SETT.TERRAIN().BUSH.render(it, r, s, it.x()-C.TILE_SIZEH, it.y()-C.TILE_SIZEH, it.ran());
					}else if (state == t.ISMALL) {
						it.setOff(-C.TILE_SIZEH, -C.TILE_SIZEH);
						ssmall.render(r, s, data, it, degrade, isCandle);
					}
				}else {
					SETT.TERRAIN().BUSH.render(it, r, s, it.x()-C.TILE_SIZEH, it.y()-C.TILE_SIZEH, it.ran());
				}
				
				return false;
			}
			
			@Override
			public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {

				OTile t = blue.tile.get(it.tx(), it.ty());
				if (t == null)
					return;
				STATE state =  t.state();
				if (state != t.IBIG && state != t.IDEAD) {
					//t.renderDebug(r, it);
					return;
				}
				
				degrade = state.deadAmount();
				super.render(r, s, data, it, degrade, false);
				int dx = C.TILE_SIZEH/2 - C.TILE_SIZEH*type().dx(data) + it.oX();
				int dy = C.TILE_SIZEH/2 - C.TILE_SIZEH*type().dy(data) + it.oY();
				it.setOff(dx, dy);
				
				Instance ins = blue.getter.get(it.tile());
				double a = 4*state.fruitAmount()*ins.fertility()*ins.skillPrev()*blue.time.fruit();
				int am = (int) a;
				
				for (int i = 0; i < am; i++) {
					it.ranOffset(i, 0);
					sfruit.render(r, s, data, it, degrade, false);
				}
				
				//t.renderDebug(r, it);
					
				
			}
		};
		
		sEdge = new RoomSpriteComboN(sp, "EDGE_COMBO");

		FurnisherItemTile tt = new FurnisherItemTile(this, tree, AVAILABILITY.AVOID_PASS, false);
		tt.setData(TREE);
		FurnisherItemTile __ = new FurnisherItemTile(this, RoomSprite.DUMMY, AVAILABILITY.ROOM, false);
		__.setData(TREE+2);
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 1);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 2);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 3);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 4);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 5);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 6);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 7);
		
		new FurnisherItem(new FurnisherItemTile[][] {
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
			{__,tt,tt,__},
			{__,tt,tt,__},
			{__,__,__,__},
		}, 8);
		
		flush(3);
		
		

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
		return isIndoors;
	}
	
	@Override
	public boolean mustBeOutdoors() {
		return !isIndoors;
	}


	@Override
	public RoomBlueprintImp blue() {
		return blue;
	}
	
	@Override
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		
		if (SETT.ROOMS().fData.tileData.get(tx, ty) != TREE)
			SETT.GRASS().grow(tx, ty, 16);
		
		
		int m = 0;
		for (DIR d : DIR.ORTHO) {
			if (area.is(tx, ty, d)) {
				m |= d.mask();
			}
		}
		if (m != 0x0F)
			SETT.ROOMS().fData.spriteData2.set(tx, ty, m);
		
		super.putFloor(tx, ty, upgrade, area);
	}
	
	@Override
	public void renderExtra() {
		SETT.OVERLAY().FERTILITY.add();
	}
	
	@Override
	public Room create(TmpArea area, RoomInit init) {
		return new Instance(blue, area, init);
	}
	
	@Override
	public CharSequence warning(AREA area) {
		double d = fertility.get(area, 0);
		if (d < 0.5)
			return ¤¤warning;
		return null;
	}

	@Override
	public void doBeforePlanning(int tx, int ty) {
		// TODO Auto-generated method stub
		super.doBeforePlanning(tx, ty);
	}
	
	@Override
	public boolean removeFertility() {
		return true;
	}
	

}
