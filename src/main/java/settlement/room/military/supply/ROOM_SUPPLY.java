package settlement.room.military.supply;

import java.io.IOException;

import init.resources.RESOURCE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.thing.halfEntity.caravan.CaravanPickup;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;
import world.army.ADSupply;

public final class ROOM_SUPPLY extends RoomBlueprintIns<SupplyInstance> implements ROOM_EMPLOY_AUTO{

	final Constructor constructor;
	final Crate crate = new Crate(this);
	final SupplyTally tally = new SupplyTally(this);
	
	
	
	public ROOM_SUPPLY(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_MILITARY_SUPPLY", cat);
		constructor = new Constructor(init);
	}

	@Override
	protected void update(float ds) {
		tally.update(ds);
		
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
		tally.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		tally.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		tally.clear();
	}
	
	@Override
	public boolean degrades() {
		return false;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((SupplyInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((SupplyInstance)r).auto = b;
	}
	
	public int amount(RESOURCE res) {
		return tally.amount(res);
	}
	
	public int reserved(RESOURCE res) {
		return tally.reserved(res);
	}
	
	public CaravanPickup reserved(int tx, int ty, RESOURCE res) {
		return tally.reserved(tx, ty, res);	
	}
	
	public CaravanPickup reservable(int tx, int ty, RESOURCE res) {
		return tally.reservable(tx, ty, res);
	}
	
	public int reservable(ADSupply s) {
		return tally.amount(s.res)-tally.reserved(s.res);
	}
	
	public void withdraw(RESOURCE res, int amount) {
		tally.withdraw(res, amount);
	}


}
