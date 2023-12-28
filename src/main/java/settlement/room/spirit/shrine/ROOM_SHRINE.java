package settlement.room.spirit.shrine;

import java.io.IOException;

import init.need.NEEDS;
import init.race.RACES;
import init.religion.Religion;
import init.religion.Religions;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomService;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ROOM_SHRINE extends RoomBlueprintIns<ShrineInstance> implements ROOM_SERVICE_HASER {

	public final Religion religion;
	final RoomService data; 
	
	final Constructor constructor;
	final Service bed;
	
	public ROOM_SHRINE(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		
		religion = Religions.MAP().get(init.data());
		bed = new Service(this);
		data = new RoomService(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return bed.get(tx, ty);
			}

			@Override
			public double totalMultiplier() {
				return 1.0/(NEEDS.TYPES().SHRINE.rate.get(RACES.clP(null, null)));
			}
		};
		constructor = new Constructor(this, init);
	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}
	
	public Service bed(int tx, int ty) {
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
	public RoomService service() {
		return data;
	}

}
