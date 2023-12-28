package settlement.room.service.hygine.bath;

import static settlement.main.SETT.*;

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
import settlement.room.service.module.RoomServiceNeed;
import settlement.room.service.module.RoomServiceNeed.ROOM_SERVICE_NEED_HASER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_BATH extends RoomBlueprintIns<BathInstance> implements ROOM_SERVICE_NEED_HASER, INDUSTRY_HASER, ROOM_EMPLOY_AUTO{

	final RoomServiceNeed data;
	final Constructor constructor;
	final CharSequence sHeating;
	final CharSequence sHeatingDesc;
	final CharSequence sHeatingProblem;
	final CharSequence sWaterProblem;
	final Industry consumtion;
	final LIST<Industry> indus;

	public ROOM_BATH(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		
		data = new RoomServiceNeed(this, init) {
			@Override
			public FSERVICE service(int tx, int ty) {
				return Bath.init(tx, ty, ROOM_BATH.this);
			}

		};
		
		constructor = new Constructor(this,init);
		sHeating = init.text().text("HEATING");
		sHeatingDesc = init.text().text("HEATING_DESC");
		sHeatingProblem = init.text().text("HEATING_PROBLEM");
		sWaterProblem = init.text().text("WATER_PROBLEM");
		consumtion = new Industry(this, init.data(), new RoomBoost[0], null);
		

		indus = new ArrayList<>(consumtion);
	
	}


	public Bath bath(int tx, int ty) {
		return Bath.init(tx, ty, this);
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
		return data.finder;
	}
	
	@Override
	public RoomServiceNeed service() {
		return data;
	}
	
	public static boolean isPool(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r instanceof BathInstance) {
			int d = ROOMS().data.get(tx, ty);
			return (d & Bits.BITS) == Bits.POOL && (d & 1) == 1;
		}
		return false;
	}
	
	public boolean isBench(int tx, int ty) {
		if (is(tx, ty)) {
			int d = ROOMS().data.get(tx, ty);
			return (d & Bits.BITS) == Bits.BENCH;
		}
		return false;
	}
	
	public DIR getBenchDir(int tx, int ty) {
		if (!isBench(tx, ty))
			throw new RuntimeException();
		for (DIR d : DIR.ORTHO) {
			int da = ROOMS().data.get(tx, ty, d);
			if (da == Bits.BENCH_TAIL)
				return d;
		}
		throw new RuntimeException();
	}

	@Override
	protected void saveP(FilePutter file){
		data.saver.save(file);
		consumtion.save(file);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		data.saver.load(saveFile);
		consumtion.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		this.data.saver.clear();
		consumtion.clear();
	}
	
	public SFinderRoomService finder() {
		return data.finder;
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
		return ((BathInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((BathInstance)r).auto = b;
	}

}
