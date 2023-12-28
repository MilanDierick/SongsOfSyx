package settlement.room.water.pool;

import java.io.IOException;

import game.time.TIME;
import init.*;
import init.sprite.SPRITES;
import init.sprite.game.SheetType;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.util.FINDABLE;
import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.*;
import settlement.room.sprite.RoomSprite1x1;
import settlement.room.sprite.RoomSpriteCombo;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.gui.misc.GBox;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_POOL extends RoomBlueprintImp {

	public final CanalConstructor constructor;
	public final CanalInstance instance;

	private static CharSequence ¤¤problem = "Has no sweet water access and is not functional. You can create water access with pumps and canals. It can take up to a day before results are shown.";
	private static CharSequence ¤¤ok = "Operational";
	
	private static CharSequence ¤¤available = "Available for a swim";
	private static CharSequence ¤¤unavailable = "Unavailable for a swim";
	
	static {
		D.ts(ROOM_POOL.class);
	}
	


	public ROOM_POOL(int index, RoomInitData init, String key, RoomCategorySub cat) throws IOException {
		super(init, index, key, cat);
		
		this.instance = new CanalInstance(init.m, this);
		constructor = new CanalConstructor(init);
		
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
		
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				ROOM_POOL.hover(box, SETT.ROOMS().data.get(rx, ry) != 0, fservice(rx, ry).findableReservedCanBe());
			}
		});
	}
	
	@Override
	public SFinderFindable service(int tx, int ty) {
		return null;//return SETT.PATH().finders.water;
	}

	public FINDABLE fservice(int tx, int ty) {
		return bath.get(tx, ty);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static void hover(GUI_BOX box, boolean flow, boolean bath) {
		GBox b = (GBox) box;
		b.NL();
		if (!flow) {
			b.add(b.text().warnify().add(¤¤problem));
		}else
			b.add(b.text().normalify2().add(¤¤ok));
		b.NL();
		if (!bath) {
			b.add(b.text().warnify().add(¤¤unavailable));
		}else
			b.add(b.text().normalify2().add(¤¤available));
		
	}

	private final class CanalConstructor extends Furnisher{

		private final boolean clearFer;
		
		protected CanalConstructor(RoomInitData init)
				throws IOException {
			super(init, 1, 0);
			
			final Json sp = init.data().json("SPRITES");
			
			COLOR color = new ColorImp(init.data(), "WATER_COLOR");
			OpacityImp opacity = new OpacityImp((int)(init.data().d("WATER_DEPTH", 0, 1)*255));
			clearFer = init.data().bool("CLEARS_GRASS");
			
			TILE_SHEET bottom = SPRITES.GAME().raw(SheetType.sTex, "BOTTOM_TEXTURE", init.data());
			
			RoomSpriteCombo s = new RoomSpriteCombo(sp, "FRAME_COMBO") {
				
				final RoomSprite1x1 ontop = new RoomSprite1x1(sp, "ON_TOP_1X1");
				final RoomSpriteCombo stencil = new RoomSpriteCombo(sp, "STENCIL_COMBO") {
					
					@Override
					protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
						return SETT.ROOMS().map.get(tx, ty) == instance && (SETT.ROOMS().data.get(tx, ty) & 1) == 1;
					}
					
				};
				
				@Override
				public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
						FurnisherItem item) {
					data = 0;
					for (DIR d : DIR.ORTHO) {
						if (SETT.ROOMS().map.blueprintImp.get(tx, ty, d) == ROOM_POOL.this)
							data |= d.mask();
					}
					SPRITES.cons().BIG.outline.render(r, data, x, y);
				}
				
				@Override
				public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
					data = getData(it.tx(), it.ty(), 0, 0, null, RES.ran2().get(it.tile()));
					super.render(SPRITE_RENDERER.DUMMY, s, data, it, degrade, false);
				}
				
				public void renderB(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
					
					
					data = getData(it.tx(), it.ty(), 0, 0, null, RES.ran2().get(it.tile()))&0x0F;
					TextureCoords tex = stencil.texture(data, it);

					int x2 = it.x()+C.TILE_SIZE;
					int y2 = it.y()+C.TILE_SIZE;
					
					CORE.renderer().renderTextured(it.x(), x2, it.y(), y2, bottom.getTexture(it.ran()%bottom.tiles()), tex);
					//stencil.render(r, s, data, it, degrade, false);
					
					if (SETT.ROOMS().map.get(it.tile()) != instance || (SETT.ROOMS().data.get(it.tile()) & 1) != 1)
						return;
					
					data = stencil.getData(it.tx(), it.ty(), 0, 0, null, RES.ran2().get(it.tile()))&0x0F;
					tex = stencil.texture(data, it);
					color.bind();
					opacity.bind();
					TextureCoords oo = SPRITES.textures().dis_small.get(it.tx()*C.T_PIXELS+SETT.WEATHER().wind.time.getD()*16, it.ty()*C.T_PIXELS+SETT.WEATHER().wind.time.getD()*16);
					CORE.renderer().renderTextured(it.x(), x2, it.y(), y2, oo, tex);
					oo = SPRITES.textures().dis_small.get((it.tx()+1)*C.T_PIXELS-8*TIME.currentSecond(), (it.ty()+1)*C.T_PIXELS-8*TIME.currentSecond());
					CORE.renderer().renderTextured(it.x(), x2, it.y(), y2, oo, tex);
					COLOR.unbind();
					OPACITY.unbind();
					
			
					
				}
				
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return SETT.ROOMS().map.get(tx, ty) == instance;
				}
				
				@Override
				public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
						boolean isCandle) {
					renderB(r, s, data, it, degrade);
					data = 0;
					int iceMask = 0;
					for (DIR d : DIR.ORTHO) {
						if (SETT.ROOMS().map.get(it.tx(), it.ty(), d) == instance) {
							data |= d.mask();
							if ((SETT.ROOMS().data.get(it.tx(), it.ty()) & 1) == 1 && SETT.TERRAIN().WATER.is.is(it.tx()+d.x(), it.ty()+d.y()))
								iceMask |= d.mask();
						}
							
					}
					if (SETT.TERRAIN().WATER.ice.is(it.tx(), it.ty())) {
						SETT.TERRAIN().WATER.renderIce(it, iceMask);
						super.render(r, ShadowBatch.DUMMY, data, it, degrade, isCandle);
					}else {

						super.render(r, ShadowBatch.DUMMY, data, it, degrade, isCandle);
						ontop.render(r, s, getData2(it), it, degrade, false);
					}
					
					{
						data = getData(it.tx(), it.ty(), 0, 0, null, 0);
						TextureCoords tex = stencil.texture(data, it);
						CORE.renderer().setMaxDepth(it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE, tex, CORE.renderer().getDepth()+1);
					}
					
					return false;
				}
				
			};
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{new FurnisherItemTile(this, false,s, AVAILABILITY.AVOID_PASS, false)},
			}, 1);
			
			flush(1, 0);
		}

		
		@Override
		public boolean removeFertility() {
			return clearFer;
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
		public void putFloor(int tx, int ty, int upgrade, AREA area) {
			
		}

		@Override
		public Room create(TmpArea area, RoomInit init) {
			int tx = area.mX();
			int ty = area.my();
			instance.place(area);
			SETT.ROOMS().fData.spriteData2.set(tx, ty, 1);
			for (DIR d : DIR.ORTHO) {
				if (ROOM_POOL.this.is(tx, ty, d)) {
					SETT.ROOMS().fData.spriteData2.set(tx, ty, d, 1);
				}
			}
			
			return SETT.ROOMS().map.get(tx, ty);
		}

		@Override
		public RoomBlueprintImp blue() {
			return ROOM_POOL.this;
		}

	}
	
	private static class CanalInstance extends RoomSingleton {

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected static final transient RoomAreaWrapper w = new RoomAreaWrapper();
		
		CanalInstance(ROOMS m, RoomBlueprint p){
			super(m, p);
			
		}
		
		
		
		protected Object readResolve() {
			  return blueprintI().instance;
		}

		@Override
		public ROOM_POOL blueprintI() {
			return (ROOM_POOL) blueprint();
		}
		
		@Override
		public ROOM_DEGRADER degrader(int tx, int ty) {
			return null;
		}
		
		@Override
		public void updateTileDay(int tx, int ty) {
			
			int i = SETT.ENV().environment.WATER_SWEET.get(tx, ty) <= 0 ? 0 : 1;
			if (i != (SETT.ROOMS().data.get(tx, ty) & 0b01)) {
				remove(tx, ty);
				w.init(this, tx, ty);
				SETT.ROOMS().data.set(w.area(), tx, ty, i);
				w.done();
				add(tx, ty);
				SETT.PATH().availability.updateAvailability(tx, ty);
			}
			
		}
		
		private void remove(int tx, int ty) {
			if (SETT.ROOMS().data.get(tx, ty) == 1) {
				FINDABLE s = blueprintI().fservice(tx, ty);
				if (s != null && s.findableReservedCanBe()) {
					SETT.PATH().finders.water.report(tx, ty, -1);
				}
			}
		}
		
		private void add(int tx, int ty) {
			if (SETT.ROOMS().data.get(tx, ty) == 1) {
				FINDABLE s = blueprintI().fservice(tx, ty);
				if (s != null && s.findableReservedCanBe()) {
					SETT.PATH().finders.water.report(tx, ty, 1);
				}
			}
		}
		
		@Override
		protected void removeAction(ROOMA ins) {
			remove(ins.mX(), ins.mY());
			SETT.ROOMS().data.set(ins, ins.mX(), ins.mY(), 0);
		}

		@Override
		protected void addAction(ROOMA ins) {
			super.addAction(ins);
			int i = SETT.ENV().environment.WATER_SWEET.get(ins.mX(), ins.mY()) <= 0 ? 0 : 1;
			SETT.ROOMS().data.set(ins, ins.mX(), ins.mY(), i);
			add(ins.mX(), ins.mY());
		}
		
		@Override
		protected AVAILABILITY getAvailability(int tile) {
			return AVAILABILITY.PENALTY3;
		}
		
	}
	
	private final Fin bath = new Fin();
	
	private class Fin implements FINDABLE {
		
		private int tx, ty;
		
		public FINDABLE get(int tx, int ty) {
			
			if (ROOM_POOL.this.is(tx, ty)) {
				this.tx = tx;
				this.ty = ty;
			}
			return this;
		}
		
		
		@Override
		public int y() {
			return ty;
		}
		
		@Override
		public int x() {
			return tx;
		}
		
		@Override
		public boolean findableReservedIs() {

			return (SETT.ROOMS().data.get(tx, ty) & 0b11) == 0b11;
		}
		
		@Override
		public boolean findableReservedCanBe() {
			return (SETT.ROOMS().data.get(tx, ty) & 0b11) == 0b01;
		}
		
		@Override
		public void findableReserveCancel() {
			if (findableReservedIs()) {
				int d = SETT.ROOMS().data.get(tx, ty);
				d &= ~0b10;
				SETT.ROOMS().data.set(SETT.ROOMS().map.rooma.get(tx, ty), tx, ty, d);
				if (findableReservedCanBe())
					SETT.PATH().finders.water.report(tx, ty, 1);
			}
			
		}
		
		@Override
		public void findableReserve() {
			if (findableReservedCanBe()) {
				int d = SETT.ROOMS().data.get(tx, ty);
				d |= 0b10;
				
				SETT.ROOMS().data.set(SETT.ROOMS().map.rooma.get(tx, ty), tx, ty, d);
				SETT.PATH().finders.water.report(tx, ty, -1);
			}
			
		}
	}
	
}