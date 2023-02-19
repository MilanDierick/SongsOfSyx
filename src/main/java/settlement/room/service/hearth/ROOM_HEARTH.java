package settlement.room.service.hearth;

import java.io.IOException;

import init.resources.RESOURCES;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceDataAccess;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class ROOM_HEARTH extends RoomBlueprintIns<HearthInstance> implements ROOM_SERVICE_ACCESS_HASER, INDUSTRY_HASER{

	final RoomServiceDataAccess data; 
	
	final Constructor constructor;
	final Hearth bed;
	private final Industry industry;
	final LIST<Industry> indus;
	
	public ROOM_HEARTH(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		bed = new Hearth(this);
		data = new RoomServiceDataAccess(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return bed.get(tx, ty);
			}

			@Override
			public double totalMultiplier() {
				return 8;
			}
		};
		data.usesAccess = false;
		constructor = new Constructor(this, init);
		industry = new Industry(this, RESOURCES.WOOD(), 1.0, null, 0, null, null);

		indus = new ArrayList<>(industry);
	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	public Hearth bed(int tx, int ty) {
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

	@Override
	public LIST<Industry> industries() {
		return indus;
	}

}
