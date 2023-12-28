package settlement.room.water;

import java.io.IOException;

import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.path.AVAILABILITY;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.*;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.*;
import settlement.room.sprite.RoomSprite;
import settlement.room.sprite.RoomSprite1x1;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

final class Drain extends RoomBlueprintImp {

	public final Constructor constructor;
	public final DrainInstance instance;
	
	public Drain(RoomInitData init,RoomCategorySub cat) throws IOException {
		super(init, 0, "_WATERDRAIN", cat);
		this.instance = new DrainInstance(init.m, this);
		constructor = new Constructor(init);
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
	
	private final class Constructor extends Furnisher{


		protected Constructor(RoomInitData init)
				throws IOException {
			super(init, 1, 0);


			Json jj = init.data().json("SPRITES");
			final RoomSprite dd = new RoomSprite1x1(jj, "DRAIN_1X1");
			
			RoomSprite sp = new WSprite.RSprite(Drain.this, instance) {

				@Override
				public void renderBelow(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade) {
					dd.render(r, s, data, it, degrade, false);
					super.renderBelow(r, s, data, it, degrade);
				}
				
			};
			
			

		
			
			new FurnisherItem(new FurnisherItemTile[][] {
				{new FurnisherItemTile(this, false, sp, AVAILABILITY.AVOID_PASS, false)},
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
			int tx = area.mX();
			int ty = area.my();
			instance.place(area);
			SETT.ROOMS().fData.spriteData2.set(tx, ty, 1);
			for (DIR d : DIR.ORTHO) {
				if (Drain.this.is(tx, ty, d)) {
					SETT.ROOMS().fData.spriteData2.set(tx, ty, d, 1);
				}
			}
			
			return SETT.ROOMS().map.get(tx, ty);
		}

		@Override
		public RoomBlueprintImp blue() {
			return Drain.this;
		}

	}
	
	private static final class DrainInstance extends RoomSingleton implements Pumpable{

		private static int radius = 10;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final static transient RoomAreaWrapper wrap = new RoomAreaWrapper();
		
		DrainInstance(ROOMS m, RoomBlueprint p){
			super(m, p);
			
		}
		
		protected Object readResolve() {
			  return blueprintI().instance;
		}

		@Override
		public Drain blueprintI() {
			return (Drain) blueprint();
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
			reportChange(ins.mX(), ins.mY(), radius);
		}

		@Override
		protected void addAction(ROOMA ins) {
			super.removeAction(ins);
			reportChange(ins.mX(), ins.mY(), radius);
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
			return radius;
		}
		
	}

}