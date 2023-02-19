package settlement.room.service.hygine.well;

import java.io.IOException;

import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ROOM_WELL extends RoomBlueprintIns<WellInstance> implements ROOM_SERVICE_ACCESS_HASER {

	final RoomServiceDataAccess data; 
	
	final Constructor constructor;
	final Wash bed;
	
	public ROOM_WELL(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		bed = new Wash(this);
		data = new RoomServiceDataAccess(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return bed.get(tx, ty);
			}
			
			@Override
			public double totalMultiplier() {
				return 1.0/STATS.NEEDS().DIRTINESS.rate.get(null, null);
			}
		};
		constructor = new Constructor(this, init);
	}
	
	@Override
	protected void update(float ds) {
		
	}
	
	public Wash bed(int tx, int ty) {
		return bed.get(tx, ty);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
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
	
	@Override
	public RoomServiceDataAccess service() {
		return data;
	}

}
