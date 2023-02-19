package settlement.room.home.house;

import java.io.IOException;

import settlement.main.SETT;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.thing.pointlight.LOS;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public class ROOM_HOME extends RoomBlueprintImp{

	final InstanceHome instance;
	final ContructorHome constructor;
	public final OddHome odd = new OddHome();
	final Houses houses = new Houses();
	
	public ROOM_HOME(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(init, 0, "_HOME", cat);
		
		constructor = new ContructorHome(init, this);
		instance = new InstanceHome(init.m, this);
	}

	@Override
	protected void update(float ds) {
		houses.update();
	}

	@Override
	protected void save(FilePutter file) {
		odd.saver.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		odd.saver.load(file);
		houses.clear();
	}

	@Override
	protected void clear() {
		odd.saver.clear();
		houses.clear();
	}

	@Override
	public SFinderFindable service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public COLOR miniC(int tx, int ty) {
		return constructor.miniColor;
	}

	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	private final LOS los = new LOS() {
		
		@Override
		public boolean passesToOtherFromThis(int fx, int fy, int tx, int ty) {	
			if (SETT.ROOMS().fData.tile.get(fx, fy) == constructor.tOpening)
				return true;
			get(fx, fy);
			return instance.isSame(fx, fy, tx, ty);
		}
		
		@Override
		public boolean passesFromOtherToThis(int fx, int fy, int tx, int ty) {
			if (SETT.ROOMS().fData.tile.get(tx, ty) == constructor.tOpening)
				return true;
			get(tx, ty);
			return instance.isSame(tx, ty, fx, fy);
		}
		
		@Override
		public boolean blocksEnv(int tx, int ty) {
			return false;
		}

		@Override
		public boolean isLightBlocker(int tx, int ty) {
			return false;
		}
	};
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new HomeHoverer());
		super.appendView(mm);
	}
	
	@Override
	public LOS LOS(int tx, int ty) {
		return los;
	}
	
	public HomeHouse house(int tx, int ty, Object user) {
		if (is(tx, ty)) {
			return houses.get(tx, ty, user);
		}
		return null;
	}

	
	

}
