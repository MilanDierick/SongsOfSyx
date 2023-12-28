package settlement.room.water;

import java.io.IOException;

import init.D;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.environment.SettEnvMap.SettEnvValue;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.*;
import settlement.room.sprite.RoomSprite;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import view.sett.ui.room.UIRoomModule;

final class Canal extends RoomBlueprintImp {

	public final CanalConstructor constructor;
	public final CanalInstance instance;
	
	private static CharSequence ¤¤problem = "Currently not operational. Make sure it's connected to a water pump's outlet, and that the connected pumps produce enough flow to reach it.";
	private static CharSequence ¤¤ok = "Operational";
	
	static {
		D.ts(Canal.class);
	}
	
	public Canal(RoomInitData init,RoomCategorySub cat) throws IOException {
		super(init, 0, "_WATERCANAL", cat);
		this.instance = new CanalInstance(init.m, this);
		constructor = new CanalConstructor(init);
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new UIRoomModule() {
		
			@Override
			public void hover(GBox box, Room i, int rx, int ry) {
				Canal.hover(box, SETT.ROOMS().data.get(rx, ry) != 0);
			}
		});
	}
	
	@Override
	public SFinderFindable service(int tx, int ty) {
		return null;
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
	
	public static void hover(GUI_BOX box, boolean flow) {
		GBox b = (GBox) box;
		b.NL();
		if (!flow) {
			b.add(b.text().warnify().add(¤¤problem));
		}else
			b.add(b.text().normalify2().add(¤¤ok));
	}

	private final class CanalConstructor extends Furnisher{

		private final RoomSprite sp;
		protected CanalConstructor(RoomInitData init)
				throws IOException {
			super(init, 1, 0);
			
			sp = new WSprite.RSprite(Canal.this, instance);
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{new FurnisherItemTile(this, false, sp, AVAILABILITY.AVOID_PASS, false)},
			}, 1);
			
			flush(1, 0);
		}

		
		@Override
		public boolean joinsWithFloor() {
			return true;
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
			int tx = area.mX();
			int ty = area.my();
			instance.place(area);
			SETT.ROOMS().fData.spriteData2.set(tx, ty, 1);
			for (DIR d : DIR.ORTHO) {
				if (Canal.this.is(tx, ty, d)) {
					SETT.ROOMS().fData.spriteData2.set(tx, ty, d, 1);
				}
			}
			
			return SETT.ROOMS().map.get(tx, ty);
		}

		@Override
		public RoomBlueprintImp blue() {
			return Canal.this;
		}
		
		@Override
		public boolean envValue(SettEnv e, SettEnvValue v, int tx, int ty) {
			
			if (blue().is(tx, ty) && SETT.ROOMS().data.get(tx, ty) != 0 && e == SETT.ENV().environment.WATER_SWEET) {
				v.value = 1;
				v.radius = 1;
				return true;
			}
			return false;
		}
		
		@Override
		public boolean envValue(SettEnv e) {
			return e == SETT.ENV().environment.WATER_SWEET;
		}
		
		@Override
		public boolean removeFertility() {
			return false;
		}
		
		@Override
		public boolean isAreaPlacable() {
			return true;
		}

	}
	
	private static class CanalInstance extends RoomSingleton implements Pumpable{

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final static transient RoomAreaWrapper wrap = new RoomAreaWrapper();
		
		CanalInstance(ROOMS m, RoomBlueprint p){
			super(m, p);
			
		}
		
		protected Object readResolve() {
			  return blueprintI().instance;
		}

		@Override
		public Canal blueprintI() {
			return (Canal) blueprint();
		}
		
		@Override
		public ROOM_DEGRADER degrader(int tx, int ty) {
			return null;
		}
		
		@Override
		public void updateTileDay(int tx, int ty) {
			
		}
		
		@Override
		protected void removeAction(ROOMA ins) {
			super.removeAction(ins);
			reportChange(ins.mX(), ins.mY(), 0);
		}

		@Override
		protected void addAction(ROOMA ins) {
			super.removeAction(ins);
			reportChange(ins.mX(), ins.mY(), 0);
		}


		@Override
		public void drain(int tx, int ty) {
			wrap.init(this, tx, ty);
			SETT.ROOMS().data.set(wrap.area(), tx, ty, 0);
			wrap.done();
		}

		@Override
		public void pump(int tx, int ty, DIR d) {
			wrap.init(this, tx, ty);
			int da = SETT.ROOMS().data.get(tx, ty);
			da |= d.mask();
			SETT.ROOMS().data.set(wrap.area(), tx, ty, da);
			wrap.done();
		}

		@Override
		public int dirmask(int tx, int ty) {
			return SETT.ROOMS().data.get(tx, ty) & 0x0F;
		}

		@Override
		public int radius() {
			return 0;
		}
		
		
		
	}
}