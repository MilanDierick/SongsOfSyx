package settlement.room.infra.transport;

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

public final class ROOM_TRANSPORT extends RoomBlueprintIns<TransportInstance> implements ROOM_EMPLOY_AUTO, ROOM_RADIUSE{

	final Constructor constructor;
	final Cart cart = new Cart(this);
	
	public ROOM_TRANSPORT(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_TRANSPORT", cat);
		
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
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((TransportInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((TransportInstance)r).auto = b; 
	}

	@Override
	public byte radiusRaw(Room t) {
		return ((TransportInstance) t).radius;
	}

	@Override
	public void radiusRawSet(Room t, byte r) {
		((TransportInstance) t).radius = r;
	}
	
	
}
