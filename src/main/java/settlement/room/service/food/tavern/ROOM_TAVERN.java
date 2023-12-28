package settlement.room.service.food.tavern;

import java.io.IOException;

import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceNeed;
import settlement.room.service.module.RoomServiceNeed.ROOM_SERVICE_NEED_HASER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ROOM_TAVERN extends RoomBlueprintIns<TavernInstance> implements ROOM_SERVICE_NEED_HASER, ROOM_EMPLOY_AUTO{
	
	public final static int MAX_FOOD_LEVEL = 1;
	public final static int MAX_DRINK_LEVEL = 4;
	
	public final RoomServiceNeed serviceData;
	final Table table = new Table(this);
	
	final Constructor constructor;
	
	public ROOM_TAVERN(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		constructor = new Constructor(this, data);
		serviceData = new RoomServiceNeed(this, data) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return table.get(tx, ty);
			}
			
		};
		
	}

	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	public Table getTable(int tx, int ty) {
		return table.get(tx, ty);
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return serviceData.finder;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		serviceData.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		serviceData.saver.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		serviceData.saver.clear();
	}
	
	@Override
	public RoomServiceNeed service() {
		return serviceData;
	}
	
	@Override
	public boolean autoEmploy(Room r) {
		return ((TavernInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((TavernInstance)r).auto = b;
	}
	
}
