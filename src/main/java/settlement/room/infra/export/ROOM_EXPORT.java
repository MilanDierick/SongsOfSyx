package settlement.room.infra.export;

import java.io.IOException;

import settlement.path.finder.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_EXPORT extends RoomBlueprintIns<ExportInstance> implements ROOM_RADIUSE, ROOM_EMPLOY_AUTO{

	public final ExportTally tally = new ExportTally();
	
	final Constructor constructor;
	
	private final Crate crate = new Crate(this);
	
	public ROOM_EXPORT(RoomInitData  data, RoomCategorySub cat) throws IOException {
		super(0, data, "_EXPORT", cat);
		
		constructor = new Constructor(this, data);
	}

	@Override
	protected void update(float ds) {
		if (ds > 0)
			FETCHER.update(ds);
//		up -= ds;
//		if (up <= 0) {
//			for (RESOURCE r : RESOURCE.ALL()) {
//				if (tally.toBeFetched.get(r) > 0) {
//					fetch(r);
//				}
//			}
//		}
//		
		
	}
	
	
	public Crate crate(int tx, int ty) {
		return crate.get(tx, ty);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}
	
	@Override
	protected void saveP(FilePutter saveFile){
		tally.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		tally.saver.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		tally.saver.clear();
	}
	
	
	final ExportWork Work = new ExportWork(this);
	
	public final ExportFetcher FETCHER = new ExportFetcher(this, tally);
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((ExportInstance) r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		if (r instanceof ExportInstance) {
			((ExportInstance) r).auto = b;
		}
	}

	@Override
	public byte radiusRaw(Room t) {
		return ((ExportInstance) t).radius;
	}

	@Override
	public void radiusRawSet(Room t, byte r) {
		((ExportInstance) t).radius = r;
	}

}
