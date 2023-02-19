package settlement.room.military.barracks;

import java.io.IOException;

import init.RES;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import settlement.army.Div;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.industry.module.IndustryRate;
import settlement.room.main.*;
import settlement.room.main.RoomEmploymentSimple.EmployerSimple;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_BARRACKS extends RoomBlueprintIns<BarracksInstance> {
	
	final Constructor constructor;
	
	final BarracksThing thing = new BarracksThing(this);
	
	int trainingLimit = 10000;
	public static final double DEGRADE_RATE16 = 1.0/(4*16*16*16)*RES.config().BATTLE.TRAINING_DEGRADE;
	public final double DAY_RATE;
	public final double RATEI;
	public final EmployerSimple emp = new EmployerSimple(employment());
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
	
	public ROOM_BARRACKS(RoomInitData data, RoomCategorySub cat) throws IOException {
		super(0, data, "_BARRACKS", cat);
		
		constructor = new Constructor(data) {

			@Override
			public Room create(TmpArea area, RoomInit init) {
				return new BarracksInstance(ROOM_BARRACKS.this, area, init);
			}
			
		};
		
		double days = data.data().d("FULL_TRAINING_IN_DAYS", 1, 10000);
		DAY_RATE = 1.0/days + DEGRADE_RATE16;
		RATEI = DAY_RATE/16.0;
	}

	@Override
	protected void saveP(FilePutter f) {
		
		f.i(trainingLimit);
	}

	@Override
	protected void loadP(FileGetter f) throws IOException {
		
		trainingLimit = f.i();
		
	}

	@Override
	protected void clearP() {
		
		trainingLimit = 10000;
	}
	
	public int deactivated() {
		return 0;
	}

	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}
	
	public BarracksInstance barracks(Div div) {
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
	
	public COORDINATE faceCoo(int tx, int ty) {
		return thing.init(tx, ty).cooMan;
	}

}
