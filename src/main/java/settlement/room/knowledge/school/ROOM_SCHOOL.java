package settlement.room.knowledge.school;

import java.io.IOException;

import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_SCHOOL extends RoomBlueprintIns<SchoolInstance> implements INDUSTRY_HASER, ROOM_SERVICE_ACCESS_HASER{

	final Industry industry;
	final SchoolConstructor constructor;
	final RoomServiceAccess service;
	final SchoolStation station = new SchoolStation(this);
	public final double learningSpeed;
	
	final LIST<Industry> indus;

	public ROOM_SCHOOL(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		service = new RoomServiceAccess(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return station.service(tx, ty);
			}

			@Override
			public double totalMultiplier() {
				return 1;
			}
		};

		constructor = new SchoolConstructor(this, init);
		

		industry = new Industry(this, init.data(), new RoomBoost[0], null);
		learningSpeed = init.data().d("LEARNING_SPEED", 0, 1);

		indus = new ArrayList<>(industry);
	}
	
	@Override
	protected void update(float ds) {

	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return service.finder;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		service.saver.save(saveFile);
		industry.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		service.saver.load(saveFile);
		industry.load(saveFile);
		
	}
	
	@Override
	protected void clearP() {
		service.saver.clear();
		industry.clear();
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
	public LIST<Industry> industries() {
		return indus;
	}

	@Override
	public RoomServiceAccess service() {
		return service;
	}
	
	public DIR childDir(int sx, int sy) {
		return station.serviceDir(sx, sy);
	}
	
	public double learningSpeed(int tx, int ty) {
		SchoolInstance ins = get(tx, ty);
		if (ins == null)
			return 0;
		return learningSpeed*(1.0-ins.getDegrade())*constructor.quality.get(ins);
	}
	
}
