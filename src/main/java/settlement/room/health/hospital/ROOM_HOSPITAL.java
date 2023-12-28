package settlement.room.health.hospital;

import java.io.IOException;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomService;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_HOSPITAL extends RoomBlueprintIns<HospitalInstance> implements ROOM_SERVICE_HASER, INDUSTRY_HASER, ROOM_EMPLOY_AUTO{

	final RoomService service;
	final Constructor constructor;
	final Industry consumtion;
	final LIST<Industry> indus;

	public ROOM_HOSPITAL(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_HOSPITAL", block);
		
		service = new RoomService(this, init) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return Bed.service(tx, ty);
			}
			
			@Override
			public double totalMultiplier() {
				return 1;
			}
		};
		
		
		constructor = new Constructor(this,init);
		consumtion = new Industry(this, init.data(), new RoomBoost[0], null);
		indus = new ArrayList<>(consumtion);
	
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
		return service.finder;
	}
	
	@Override
	public RoomService service() {
		return service;
	}

	@Override
	protected void saveP(FilePutter file){
		consumtion.save(file);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		consumtion.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		consumtion.clear();
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
	public boolean autoEmploy(Room r) {
		return ((HospitalInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((HospitalInstance)r).auto = b;
	}
	
	public DIR layCoo(int tx, int ty) {
		return DIR.ORTHO.get(SETT.ROOMS().fData.spriteData.get(tx, ty)&0b011);
	}
	
	public double recoverRate(int tx, int ty) {
		if (is(tx, ty)) {
			return 0.75*get(tx, ty).quality();
		}
		return 0;
	}

}
