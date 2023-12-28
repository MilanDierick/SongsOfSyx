package settlement.room.industry.workshop;

import java.io.IOException;

import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.BonusExp.RoomExperienceBonus;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public class ROOM_WORKSHOP extends RoomBlueprintIns<WorkshopInstance> implements INDUSTRY_HASER, ROOM_EMPLOY_AUTO{

	public final static String type = "WORKSHOP";
	final Job job;
	final Constructor constructor;
	final LIST<Industry> indus;
	
	public ROOM_WORKSHOP(int index, RoomInitData init, String key, RoomCategorySub cat) throws IOException {
		super(index, init, key, cat);
		
		constructor = new Constructor(this, init);
		pushBo(init.data(), type, true);

		job = new Job(this);

		indus = INDUSTRY_HASER.createIndustries(this, init, new RoomBoost[] {constructor.efficiency}, bonus());
		new RoomExperienceBonus(this, init.data(), bonus());
	}
	
	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		for (Industry i : indus)
			i.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		for (Industry i : indus)
			i.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		for (Industry i : indus)
			i.clear();
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((WorkshopInstance) r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((WorkshopInstance) r).auto = b;
		
	}

}
