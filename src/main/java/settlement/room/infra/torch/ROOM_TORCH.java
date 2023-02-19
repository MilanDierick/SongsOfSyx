package settlement.room.infra.torch;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.C;
import init.resources.RESOURCES;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.environment.SettEnvMap.SettEnvValue;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.*;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_TORCH extends RoomBlueprintImp {

	private final Constructor2 constructor;
	private final Instance instance;
	
	
	public ROOM_TORCH(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(init, 0, "_TORCH", cat);
		this.constructor = new Constructor2(init);
		this.instance = new Instance(init.m, this);
	}

	@Override
	public Room get(int tx, int ty) {
		if (ROOMS().map.get(tx, ty) == instance)
			return instance;
		return null;
	}

	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}
	
	@Override
	public Constructor2 constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				//SETT.OVERLAY().envThing(SETT.ENV().environment.LIGHT);
			}
		});
		
	}
	


	private final static class Instance extends RoomSingleton{

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Instance(ROOMS m, RoomBlueprint p){
			super(m, p);
			
		}
		
		protected Object readResolve() {
			  return blueprintI().instance;
		}

		@Override
		public ROOM_TORCH blueprintI() {
			return ROOMS().TORCH;
		}
		
		@Override
		protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
			super.render(r, shadowBatch, i);
//			if (body().width() == 2) {
//				
//				if (i.tx() == body().x2()-1 && i.ty() == body().y2()-1)
//					RESOURCES.WOOD().renderLaying(r, i.x()-C.TILE_SIZEH, i.y()-C.TILE_SIZEH, i.ran(), 1 + 8*(1.0-getDegrade()));
//			}else {
//				RESOURCES.WOOD().renderLaying(r, i.x(), i.y(), i.ran(), 1 + 4*(1.0-getDegrade()));
//			}
			return false;
		}
		
		
		
//		@Override
//		protected void removeAction() {
//			if (getDegrade() <= 0.5)
//				map().remove(body().cX(), body().cY(), tracer());
//		}
//		
//		@Override
//		protected void degradeChange(double oldD, double newD) {
//			if (newD > 0.5 && oldD <= 0.5) {
//				map().remove(body().cX(), body().cY(), tracer());
//			}else if (newD <= 0.5 && oldD > 0.5)
//				map().add(body().cX(), body().cY(), tracer());
//		}

	}
	
	public final class Constructor2 extends Furnisher {

		public final RoomSprite small;
		public final RoomSprite medium;
		final FurnisherItemTile ss;
		final FurnisherItemTile sm;
		
		protected Constructor2(RoomInitData init)
				throws IOException {
			super(init, 1, 0, 88,44);
			
			Json js = init.data().json("SPRITES");
			
			small = new RoomSprite1x1(js, "SMALL_1X1") {
				@Override
				public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
						boolean isCandle) {
					super.render(r, s, data, it, degrade, isCandle);
					int am = (int) ((1.0-SETT.ROOMS().map.get(it.tx(), it.ty()).getDegrade(it.tx(), it.ty()))*4);
					RESOURCES.WOOD().renderLaying(r, it.x(), it.y(), it.ran(), am);
					return false;
				}
				
			};
			medium = new RoomSpriteComboN(js, "COMBO") {
				
				@Override
				public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
						boolean isCandle) {
					super.render(r, s, data, it, degrade, isCandle);
					if ((data & DIR.N.mask()) != 0 && (data & DIR.W.mask()) != 0) {
						int am = (int) ((1.0-SETT.ROOMS().map.get(it.tx(), it.ty()).getDegrade(it.tx(), it.ty()))*8);
						RESOURCES.WOOD().renderLaying(r, it.x()-C.TILE_SIZEH, it.y()-C.TILE_SIZEH, it.ran(), am);
					}
					return false;
				}
				
			};
			
			ss = new FurnisherItemTile(
					this,
					small,
					AVAILABILITY.SOLID,
					false
					);
			sm = new FurnisherItemTile(
					this,
					medium,
					AVAILABILITY.SOLID,
					false
					);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{ss},
			}, 1,1);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{sm,sm},
				{sm,sm},
			}, 4,4);
			
			flush(0);
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
			
			if (area.body().width() == 2) {
				SETT.LIGHTS().torchBig(area.body().x1(), area.body().y1(), C.TILE_SIZEH);
			}else {
				SETT.LIGHTS().torch(area.body().x1(), area.body().y1(), 0);
			}
			instance.place(area);
			return instance;
		}

		@Override
		public RoomBlueprintImp blue() {
			return SETT.ROOMS().TORCH;
		}
		
		
		@Override
		public void renderExtra(SPRITE_RENDERER r, int x, int y, int tx, int ty, int rx, int ry, FurnisherItem item) {
			if (rx == 0 && ry == 0) {
				SETT.ENV().environment.LIGHT.addExtraView(1.0, item.width() == 2 ? 0.8 : 0.4, tx, ty, item.width(), item.height());
				//SETT.OVERLAY().envThing(SETT.ENV().environment.LIGHT);
			}
		}
		
		@Override
		public void renderExtra() {
			SETT.OVERLAY().envThing(SETT.ENV().environment.LIGHT).add();
		}
		
		
		@Override
		public boolean envValue(SettEnv e) {
			return (e == SETT.ENV().environment.LIGHT);
		}
		
		@Override
		public boolean envValue(SettEnv e, SettEnvValue v, int tx, int ty) {
			if (e == SETT.ENV().environment.LIGHT) {
				v.radius = lightRadius(tx, ty);
				v.value = lightValue(tx, ty);
				return true;
			}
			return false;
		}
		
		public double lightRadius(int tx, int ty) {
			if (is(tx, ty)) {
				
				if (get(tx, ty).width(tx, ty) == 2)
					return 0.8;
				return 0.4;
			}
			return 0;
		}
		
		public double lightValue(int tx, int ty) {
			if (is(tx, ty)) {
				return 1.0;
			}
			return 0;
		}

		
	}

	@Override
	protected void save(FilePutter saveFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		
	}


	
}
