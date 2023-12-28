package settlement.room.infra.embassy;

import java.io.IOException;

import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_EMBASSY extends RoomBlueprintIns<EmbassyInstance> {
	
	final Job job;
	final Constructor constructor;

	public ROOM_EMBASSY(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_EMBASSY", block);
		job = new Job(this);
		
		constructor = new Constructor(this, init);
	}
	
	@Override
	protected void update(float ds) {
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
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
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}
	
}
