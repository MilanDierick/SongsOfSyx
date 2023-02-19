package settlement.room.military.archery;

import java.io.IOException;

import init.C;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.industry.module.IndustryRate;
import settlement.room.main.*;
import settlement.room.main.RoomEmploymentSimple.EmployerSimple;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.military.barracks.ROOM_BARRACKS;
import settlement.stats.STATS;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_ARCHERY extends RoomBlueprintIns<ArcheryInstance> {
	
	final Constructor constructor;
	final ArcheryThing thing = new ArcheryThing(this);
	private final Trajectory[] trajs = new Trajectory[4];
	
	public final EmployerSimple emp = new EmployerSimple(employment());
	public final double DAY_RATE;
	final double RATEI;
	final IndustryRate rate = new IndustryRate() {
		final LIST<RoomBoost> all = new ArrayList<Industry.RoomBoost>(0);
		@Override
		public LIST<RoomBoost> boosts() {
			return all;
		}
		
		@Override
		public BOOSTABLE bonus() {
			return BOOSTABLES.RATES().TRAINING;
		}
	};
	
	public ROOM_ARCHERY(RoomInitData data, RoomCategorySub cat) throws IOException {
		super(0, data, "_BARRACKS_ARCHERY", cat);
		
		constructor = new Constructor(this, data) {

			@Override
			public Room create(TmpArea area, RoomInit init) {
				return new ArcheryInstance(ROOM_ARCHERY.this, area, init);
			}
			
		};
	
		
		
		{
			double dist = constructor.item(1).height()-1;
			dist *= C.TILE_SIZE;
			int i = 0;
			for (DIR d : DIR.ORTHO) {
				Trajectory t = new Trajectory();
				t.calcLow(0, 0, 0, (int)(d.x()*dist), (int)(d.y()*dist), 40*C.TILE_SIZE);
				trajs[i++] = t;
			}
		}
		
		double days = data.data().d("FULL_TRAINING_IN_DAYS", 1, 10000);
		DAY_RATE = 1.0/days + ROOM_BARRACKS.DEGRADE_RATE16;
		RATEI = DAY_RATE/16.0;
	}

	@Override
	protected void saveP(FilePutter f) {
		
	}

	@Override
	protected void loadP(FileGetter f) throws IOException {
		
	}

	@Override
	protected void clearP() {
		
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
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	public DIR faceCoo(int tx, int ty) {
		FurnisherItem t = SETT.ROOMS().fData.item.get(tx, ty);
		if (t != null)
			return DIR.ORTHO.get(SETT.ROOMS().fData.item.get(tx, ty).rotation);
		return DIR.C;
	}
	
	public void fireArrow(int tx, int ty, int x, int y) {
		if (is(tx, ty)) {
			FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
			if (it != null) {
				Trajectory t = trajs[it.rotation];
				SETT.PROJS().launchDummy(x, y, 0, t, STATS.EQUIP().ammo().get(0).projectile,0);
			}
		}
	}

}
