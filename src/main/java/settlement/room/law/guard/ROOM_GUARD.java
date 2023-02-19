package settlement.room.law.guard;

import java.io.IOException;

import settlement.entity.humanoid.Humanoid;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.path.finder.SPath;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_GUARD extends RoomBlueprintIns<GuardInstance>{

	public final static int maxRadius = 90;

	final SFinderRoomService finder;
	
	final Constructor constructor;
	final Service service = new Service(this);
	
	public ROOM_GUARD(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_GUARD", block);
		finder = new SFinderRoomService("Guards") {
			
			@Override
			public FSERVICE get(int tx, int ty) {
				GuardInstance ins = getter.get(tx, ty);
				if (ins != null && ins.body().cX() == tx && ins.body().cY() == ty)
					return service.get(ins);
				return null;
			}
		};
		constructor = new Constructor(this, init);

	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	public void reportCriminal(Humanoid a, boolean force) {
		
		COORDINATE c = finder.reserve(a.tc(), maxRadius);
		
		
		if (c != null) {
			GuardInstance ins = getter.get(c);
			if (force) {
				ins.reportCriminal(a);
				return;
			}
			
			double d = SPath.LAST_DISTANCE();
			if (RND.rFloat()*ins.radius() < d)
				return;
			ins.reportCriminal(a);
		}
	}


	@Override
	protected void saveP(FilePutter saveFile){
		
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		
	}
	
	@Override
	protected void clearP() {
		
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

}
