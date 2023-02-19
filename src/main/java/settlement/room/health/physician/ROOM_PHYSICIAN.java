package settlement.room.health.physician;

import java.io.IOException;

import init.boostable.BOOSTABLES;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_PHYSICIAN extends RoomBlueprintIns<Instance> implements ROOM_SERVICE_ACCESS_HASER{

	final Constructor constructor;
	final RoomServiceDataAccess data;
	
	public ROOM_PHYSICIAN(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_PHYSICIAN", block);
		data = new RoomServiceDataAccess(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return Service.getS(tx, ty);
			}
			
			@Override
			public double totalMultiplier() {
				return 2.0/BOOSTABLES.RATES().DOCTOR.get(null, null);
			}
		};
		
		constructor = new Constructor(this, init);
	}
	
	@Override
	protected void update(float ds) {
	
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return data.finder;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		data.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		data.saver.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		data.saver.clear();
	}
	
	public SFinderRoomService finder() {
		return data.finder;
	}

	public DIR getLayDir(int sx, int sy) {
		return DIR.ORTHO.getC(SETT.ROOMS().fData.spriteData.get(sx, sy)&0b11);
	}
	
	@Override
	public RoomServiceDataAccess service() {
		return data;
	}
	

}
