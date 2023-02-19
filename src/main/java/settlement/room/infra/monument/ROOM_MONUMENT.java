package settlement.room.infra.monument;

import static settlement.main.SETT.*;

import java.io.IOException;

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
import settlement.stats.STANDING.StandingDef;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_MONUMENT extends RoomBlueprintImp{

	private final MConstructor constructor;
	private final Instance instance;
	private final AVAILABILITY avail;
	private int area;
	public final StandingDef defaultStanding;
	
	
	public ROOM_MONUMENT(RoomInitData init, int tindex, String key, RoomCategorySub cat) throws IOException {
		super(init, tindex, key, cat);
		avail = init.data().bool("SOLID", true) ? AVAILABILITY.SOLID : AVAILABILITY.ROOM;
		this.constructor = new MConstructor(this, init);
		this.instance = new Instance(init.m, this);
		defaultStanding = new StandingDef(init.data());
	}

	
	@Override
	protected void save(FilePutter f) {
		f.i(area);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		area = f.i();
	}

	@Override
	protected void clear() {
		area = 0;
	}
	
	public int area() {
		return area;
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
	public MConstructor constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
			@Override
			public void hover(GBox box, Room room, int rx, int ry) {
				//SETT.OVERLAY().envThing(constructor.env).add();
			}
		});
		
	}	

	final static class Instance extends RoomSingleton{

		
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
		public ROOM_MONUMENT blueprintI() {
			return (ROOM_MONUMENT) blueprint();
		}
		
		@Override
		protected void addAction(ROOMA ins) {
			blueprintI().area += ins.area();
			blueprintI().area = CLAMP.i(blueprintI().area, 0, SETT.TAREA);
		}
		
		@Override
		protected void removeAction(ROOMA ins) {
			blueprintI().area -= ins.area();
			blueprintI().area = CLAMP.i(blueprintI().area, 0, SETT.TAREA);
			super.removeAction(ins);
		}
		
		@Override
		protected void degradeChange(double oldD, double newD) {
//			if (newD > 0.5 && oldD <= 0.5) {
//				map().remove(body().cX(), body().cY(), tracer());
//			}else if (newD <= 0.5 && oldD > 0.5)
//				map().add(body().cX(), body().cY(), tracer());
		}

	}
	
	private static class MConstructor extends Furnisher {
		
		public final SettEnv env;
		private final ROOM_MONUMENT blue;
		public final RoomSprite floor;
		
		private double vv = 1/16.0;
		
		MConstructor(ROOM_MONUMENT blue, RoomInitData init)
				throws IOException {
			super(init, 1, 0, 88, 44);
			
			this.blue = blue;
			SettEnv env = null;
			for (SettEnv e : SETT.ENV().environment.all()) {
				if (super.envValue(e)) {
					env = e;
					break;
				}
			}
			this.env = env;
			
			Json sData = init.data().json("SPRITES");
			floor = new RoomSpriteComboN(sData, "FLOOR_COMBO") {
				@Override
				protected boolean joins(int tx, int ty, int rx, int ry, DIR d, FurnisherItem item) {
					return item.get(rx, ry) != null;
				}
			};

			
			
			if (sData.has("1x1")) {
				RoomSprite ssmall = new RoomSprite1x1(sData, "1x1") {
					@Override
					public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
						return floor.getData(tx, ty, rx, ry, item, itemRan);
					}
					
					@Override
					public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
							boolean isCandle) {
						floor.render(r, s, getData2(it), it, degrade, false);
						return false;
					}
					
					@Override
					public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it,
							double degrade) {
						animate(1.0 - SETT.ROOMS().map.get(it.tile()).getDegrade(it.tx(), it.ty()));
						super.render(r, s, data, it, degrade, false);
					}
					
				};
				FurnisherItemTile ss = new FurnisherItemTile(
						this,
						false,
						ssmall,
						blue.avail,
						false
						);
				new FurnisherItem(new FurnisherItemTile[][] {
					{ss},
				}, 1,1);
			}
			
			if (sData.has("2x2")) {
				
				RoomSprite sp = new RoomSpriteXxX(sData, "2x2", 2){
					@Override
					public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
						return floor.getData(tx, ty, rx, ry, item, itemRan);
					}
					
					@Override
					public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
							boolean isCandle) {
						floor.render(r, s, getData2(it), it, degrade, false);
						return false;
					}
					
					@Override
					public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it,
							double degrade) {
						animate(1.0 - SETT.ROOMS().map.get(it.tile()).getDegrade(it.tx(), it.ty()));
						super.render(r, s, data, it, degrade, false);
					}
				};
				FurnisherItemTile tt = new FurnisherItemTile(
						this,
						false,
						sp,
						blue.avail,
						false
						);
				new FurnisherItem(new FurnisherItemTile[][] {
					{tt,tt},
					{tt,tt},
				}, 6,2);
			}
			
			if (sData.has("3x3")) {
				
				RoomSprite sp = new RoomSpriteXxX(sData, "3x3", 3) {
					@Override
					public byte getData2(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
						return floor.getData(tx, ty, rx, ry, item, itemRan);
					}
					
					@Override
					public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
							boolean isCandle) {
						floor.render(r, s, getData2(it), it, degrade, false);
						return false;
					}
					
					@Override
					public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it,
							double degrade) {
						animate(1.0 - SETT.ROOMS().map.get(it.tile()).getDegrade(it.tx(), it.ty()));
						super.render(r, s, data, it, degrade, false);
					}
				};
				FurnisherItemTile tt = new FurnisherItemTile(
						this,
						false,
						sp,
						blue.avail,
						false
						);
				new FurnisherItem(new FurnisherItemTile[][] {
					{tt,tt,tt},
					{tt,tt,tt},
					{tt,tt,tt},
				}, 12,4);
			}
			
			flush(3);
			
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
			return blue.instance.place(area);
		}

		@Override
		public RoomBlueprintImp blue() {
			return blue;
		}
		
		@Override
		public void renderExtra(SPRITE_RENDERER r, int x, int y, int tx, int ty, int rx, int ry, FurnisherItem item) {
			
			
			if (rx == 0 && ry == 0 && env != null) {
				env.addExtraView(value(item), radius(item), tx, ty, item.width(), item.width());	
			}
		}
		
		@Override
		public void renderExtra() {
			if (env != null)
				SETT.OVERLAY().envThing(env).add();
		}
		
		@Override
		public boolean envValue(SettEnv e, SettEnvValue v, int tx, int ty) {
			
			if (blue().is(tx, ty) && e == env) {
				super.envValue(e, v, tx, ty);
				FurnisherItem it = ROOMS().fData.item.get(tx, ty);
				v.value = v.value*value(it);
				v.radius = v.radius*radius(it);
				return true;
			}
			return false;
		}
		
		@Override
		public boolean envValue(SettEnv e) {
			return e == env;
		}
		
		@Override
		public void putFloor(int tx, int ty, int upgrade, AREA area) {

			super.putFloor(tx, ty, upgrade, area);
//			if (tree) {
//				SETT.FERTILITY().currentSetAbs(tx, ty, 0.7);
//			}
		}
		
		private double value(FurnisherItem it) {
			return vv;
		}
		
		public double radius(FurnisherItem it) {
			if (it.width() == 3)
				return 1.0;
			if (it.width() == 2)
				return 0.75;
			if (it.width() == 1)
				return 0.5;
			return 0;
		}
		
	}

	
}
